module "other-database-sg" {
  source = "terraform-aws-modules/security-group/aws"
  version = "2.17.0"

  name = "other-rds-sg"
  description = "Security group for OtherApp database"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  computed_ingress_with_source_security_group_id = [
    {
      from_port = "3306"
      to_port = "3306"
      protocol = "tcp"
      source_security_group_id = "${module.other-instance-sg.this_security_group_id}"
    }
  ]
  number_of_computed_ingress_with_source_security_group_id = 1
}

module "other-database" {
  source = "terraform-aws-modules/rds/aws"
  version = "1.28.0"

  engine = "mariadb"
  engine_version = "10.3.13"
  instance_class = "${var.db_instance_type}"

  allocated_storage = "${var.db_size}"
  storage_type = "gp2"
  storage_encrypted = false

  apply_immediately = true
  allow_major_version_upgrade = false
  auto_minor_version_upgrade = false
  publicly_accessible = false

  identifier = "other-db"
  name = "other"
  username = "other"
  password = "SomeOtherPassword"
  port = "3306"

  vpc_security_group_ids = [
    "${module.other-database-sg.this_security_group_id}"
  ]

  create_db_subnet_group = false
  db_subnet_group_name = "${data.terraform_remote_state.vpc.db_subnet_group}"
  // subnet_ids = "${data.terraform_remote_state.vpc.db_subnets}"

  enabled_cloudwatch_logs_exports = [
    "audit",
    "general",
    "slowquery"
  ]

  iam_database_authentication_enabled = false
  maintenance_window = "Mon:00:00-Mon:03:00"
  backup_window = "03:00-06:00"
  backup_retention_period = "2"

  monitoring_interval = "0"
  monitoring_role_name = "RDSMonitoringRole"
  create_monitoring_role = false

  # create a final snapshot just before DB deletion
  skip_final_snapshot = false
  final_snapshot_identifier = "other-final"
  copy_tags_to_snapshot = true
  deletion_protection = true

  create_db_option_group = true
  //  option_group_name = "other-mariadb-option-group"
  # default DB option group version
  major_engine_version = "10.3"
  options = [
    {
      option_name = "MARIADB_AUDIT_PLUGIN"
      option_settings = [
        {
          name = "SERVER_AUDIT_EVENTS"
          value = "CONNECT"
        },
        {
          name = "SERVER_AUDIT_FILE_ROTATIONS"
          value = "5"
        }
      ]
    }
  ]

  create_db_parameter_group = true
  //  parameter_group_name = "other-mariadb-parameter-group"
  # default DB parameter group version
  family = "mariadb10.3"
  parameters = [
    {
      name = "log_bin_trust_function_creators"
      value = "1"
    },
    {
      name = "character_set_database"
      value = "utf8mb4"
    },
    {
      name = "character_set_server"
      value = "utf8mb4"
    },
    {
      name = "collation_server"
      value = "utf8mb4_unicode_ci"
    }
  ]

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
    Name = "OtherAppDatabase"
  }
}

resource "aws_route53_record" "other_db_local_dns" {
  zone_id = "${data.terraform_remote_state.vpc.private_zone_id}"
  name = "other.db.${data.terraform_remote_state.vpc.private_zone_domain}"
  type = "CNAME"
  ttl = "600"
  records = [
    "${module.other-database.this_db_instance_address}"
  ]
}
