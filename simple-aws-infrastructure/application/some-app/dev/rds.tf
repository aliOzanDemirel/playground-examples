module "some-database-sg" {
  source = "terraform-aws-modules/security-group/aws"
  version = "2.17.0"

  name = "some-rds-sg"
  description = "Security group for Some database"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  computed_ingress_with_source_security_group_id = [
    {
      from_port = "3306"
      to_port = "3306"
      protocol = "tcp"
      source_security_group_id = "${module.some-instances-sg.this_security_group_id}"
    }
  ]
  number_of_computed_ingress_with_source_security_group_id = 1
}

module "some-database" {
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

  identifier = "some-db"
  username = "some"
  password = "SomeRandomPass"
  port = "3306"

  // database for service2 is created manually in this RDS
  name = "service1"

  vpc_security_group_ids = [
    "${module.some-database-sg.this_security_group_id}"
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
  backup_retention_period = "1"

  monitoring_interval = "0"
  monitoring_role_name = "RDSMonitoringRole"
  create_monitoring_role = false

  # create a final snapshot just before DB deletion
  skip_final_snapshot = false
  final_snapshot_identifier = "some-final"
  copy_tags_to_snapshot = true
  deletion_protection = true

  # default DB option group version
  create_db_option_group = true
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

  # default DB parameter group version
  create_db_parameter_group = true
  family = "mariadb10.3"
  parameters = [
    {
      name = "log_bin_trust_function_creators"
      value = "1"
    },
    {
      name = "character_set_connection"
      value = "utf8mb4"
    },
    {
      name = "character_set_server"
      value = "utf8mb4"
    },
    {
      name = "character_set_database"
      value = "utf8mb4"
    },
    {
      name = "collation_connection"
      value = "utf8mb4_unicode_ci"
    },
    {
      name = "collation_server"
      value = "utf8mb4_unicode_ci"
    }
  ]

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
    Name = "SomeAppDatabaseServer"
  }
}

resource "aws_route53_record" "some_db_local_dns" {
  zone_id = "${data.terraform_remote_state.vpc.private_zone_id}"
  name = "some.db.${data.terraform_remote_state.vpc.private_zone_domain}"
  type = "CNAME"
  ttl = "600"
  records = [
    "${module.some-database.this_db_instance_address}"
  ]
}
