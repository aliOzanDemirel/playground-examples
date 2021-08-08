resource "aws_route53_zone" "budget-department-private-zone" {

  name = "${var.budget-department-private-domain}"
  comment = "Private zone for ${var.environment} VPC"

  vpc {
    vpc_id = "${module.budget-department-vpc.vpc_id}"
  }

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
  }
}
