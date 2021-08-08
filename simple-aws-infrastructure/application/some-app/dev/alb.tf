module "some-alb-sg" {
  source = "terraform-aws-modules/security-group/aws"
  version = "2.17.0"

  name = "some-alb-sg"
  description = "Security group for Some LB"
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
      source_security_group_id = "${module.some-instances-sg.this_security_group_id}"
    },
    {
      from_port = 8180
      to_port = 8180
      protocol = 6
      source_security_group_id = "${module.some-instances-sg.this_security_group_id}"
    }
  ]
  number_of_computed_egress_with_source_security_group_id = 2

}

module "some-alb" {
  source = "terraform-aws-modules/alb/aws"
  version = "3.5.0"

  load_balancer_name = "some-alb"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"
  subnets = [
    "${data.terraform_remote_state.vpc.public_subnets}"
  ]
  security_groups = [
    "${module.some-alb-sg.this_security_group_id}"
  ]

  enable_cross_zone_load_balancing = true
  enable_deletion_protection = false
  logging_enabled = false

  http_tcp_listeners_count = "0"
  https_listeners_count = "0"

  // manually edited targets for /service1 and /service2 endpoints
  target_groups = [
    {
      name = "some-service1-target"
      backend_protocol = "HTTP"
      backend_port = 8080
      health_check_path = "/service1/actuator/health"
      health_check_port = 8080
    },
    {
      name = "some-service2-target"
      backend_protocol = "HTTP"
      backend_port = 8180
      health_check_path = "/service2/actuator/health"
      health_check_port = 8180
    }
  ]
  target_groups_count = "2"

  target_groups_defaults = {
    health_check_healthy_threshold = 2
    health_check_unhealthy_threshold = 3
    health_check_interval = 20
    health_check_timeout = 5
    health_check_matcher = "200-299"
    stickiness_enabled = false
    deregistration_delay = 180
    target_type = "instance"
    cookie_duration = 86400
  }

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
    Name = "SomeAppLoadBalancer"
  }
}

resource "aws_lb_target_group_attachment" "some-service1-target" {
  target_group_arn = "${module.some-alb.target_group_arns[0]}"
  target_id = "${module.some-instance-service1.id[0]}"
  port = 8080
}

resource "aws_lb_target_group_attachment" "some-service2-target" {
  target_group_arn = "${module.some-alb.target_group_arns[1]}"
  target_id = "${module.some-instance-service2.id[0]}"
  port = 8180
}

resource "aws_lb_listener" "http-listener" {
  load_balancer_arn = "${module.some-alb.load_balancer_id}"
  port = "80"
  protocol = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port = "443"
      protocol = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "https-listener" {
  load_balancer_arn = "${module.some-alb.load_balancer_id}"
  port = "443"
  protocol = "HTTPS"
  ssl_policy = "ELBSecurityPolicy-2016-08"

  // created after validating certificate in test account
  certificate_arn = "${module.some-domain-cert.this_acm_certificate_arn}"

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "Valid path targets: /service2/* OR /service1/*"
      status_code = "503"
    }
  }
}

resource "aws_lb_listener_rule" "forward-to-service1" {
  listener_arn = "${aws_lb_listener.https-listener.arn}"
  priority = 100

  action {
    type = "forward"
    target_group_arn = "${module.some-alb.target_group_arns[0]}"
  }

  condition {
    field = "path-pattern"
    values = [
      "/service1/*"
    ]
  }
}

resource "aws_lb_listener_rule" "forward-to-service2" {
  listener_arn = "${aws_lb_listener.https-listener.arn}"
  priority = 200

  action {
    type = "forward"
    target_group_arn = "${module.some-alb.target_group_arns[1]}"
  }

  condition {
    field = "path-pattern"
    values = [
      "/service2/*"
    ]
  }
}

module "some-domain-cert" {
  source = "terraform-aws-modules/acm/aws"
  version = "1.2.0"

  domain_name = "some.app.com"
  subject_alternative_names = [
    "dev.some.app.com"
    // wildcard with same domain name breaks module
    // "*.some.app.com"
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
