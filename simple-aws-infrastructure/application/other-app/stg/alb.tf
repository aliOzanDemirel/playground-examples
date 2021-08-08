module "other-alb-sg" {
  source = "terraform-aws-modules/security-group/aws"
  version = "2.17.0"

  name = "other-alb-sg"
  description = "Security group for Other LB"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  ingress_cidr_blocks = [
    "0.0.0.0/0"
  ]
  ingress_rules = [
    "http-80-tcp",
    "https-443-tcp"
  ]

  computed_egress_with_source_security_group_id = [
    {
      from_port = 8080
      to_port = 8080
      protocol = 6
      source_security_group_id = "${module.other-instance-sg.this_security_group_id}"
    }
  ]
  number_of_computed_egress_with_source_security_group_id = 1

}

module "other-alb" {
  source = "terraform-aws-modules/alb/aws"
  version = "3.5.0"

  load_balancer_name = "other-alb"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"
  subnets = [
    "${data.terraform_remote_state.vpc.public_subnets}"
  ]
  security_groups = [
    "${module.other-alb-sg.this_security_group_id}"
  ]

  enable_cross_zone_load_balancing = true
  enable_deletion_protection = false

  logging_enabled = false
  //  log_bucket_name = "logs-us-east-2-123456789012"
  //  log_location_prefix = "my-alb-logs"

  // manually created with redirect action since module has this feature as open issue
  //  https://github.com/terraform-aws-modules/terraform-aws-alb/pull/78
  //  http_tcp_listeners = [
  //    {
  //      port = 80
  //      protocol = "HTTP"
  //    }
  //  ]
  //  http_tcp_listeners_count = "1"

  // created after validating certificate
  https_listeners = [
    {
      certificate_arn = "${module.other-domain-cert.this_acm_certificate_arn}"
      port = 443
      target_group_index = 0
    }
  ]
  https_listeners_count = "1"

  target_groups = "${list(map("name", "other-instances-ssl", "backend_protocol", "HTTPS", "backend_port", "8080"))}"
  target_groups_count = "1"

  target_groups_defaults = {
    health_check_healthy_threshold = 2
    health_check_unhealthy_threshold = 3
    health_check_interval = 20
    health_check_timeout = 5
    health_check_matcher = "200-299"
    health_check_path = "/actuator/health"
    health_check_port = "8080"
    stickiness_enabled = false
    deregistration_delay = 180
    target_type = "instance"
    cookie_duration = 86400
  }

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
    Name = "OtherLoadBalancer"
  }
}

resource "aws_lb_target_group_attachment" "other_instances_attachment" {
  target_group_arn = "${module.other-alb.target_group_arns[0]}"
  target_id = "${module.other-instances.id[0]}"
  port = 8080
}

module "other-domain-cert" {
  source = "terraform-aws-modules/acm/aws"
  version = "1.2.0"

  domain_name = "other.com"
  subject_alternative_names = [
    "stg.other.com"
    // wildcard with same domain name breaks module
    // "*.other.com"
  ]
  validation_method = "DNS"

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
  }

  // module should not validate automatically if the domain zone is in another account
  // record sets should then be added manually for domain validation
  validate_certificate = false
  wait_for_validation = false

}
