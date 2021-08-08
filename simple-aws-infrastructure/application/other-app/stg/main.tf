provider "aws" {
  region = "${var.region}"
  version = "~> 2.7"
}

terraform {
  backend "s3" {
    bucket = "terraform-state-stg"
    key = "stg/app/other/terraform.tfstate"
    encrypt = true
    dynamodb_table = "terraform-state-lock-table-stg"
    region = "eu-west-1"
  }
}

data "terraform_remote_state" "vpc" {
  backend = "s3"
  config {
    bucket = "terraform-state-stg"
    key = "stg/vpc/terraform.tfstate"
    region = "eu-west-1"
  }
}

data "aws_ami" "ami_with_docker_and_git" {
  owners = [
    "ACCOUNT_ID"
  ]
  filter {
    name = "name"
    values = [
      "amazon-linux-2-docker-aws-git"
    ]
  }
  filter {
    name = "virtualization-type"
    values = [
      "hvm"
    ]
  }
}

resource "aws_ecr_repository" "other-ecr-repo" {
  name = "other"
  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
  }
}

module "other-instance-sg" {
  source = "terraform-aws-modules/security-group/aws"
  version = "2.17.0"

  name = "other-ec2-sg"
  description = "Security group for Other instances"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  computed_ingress_with_source_security_group_id = [
    {
      from_port = 8080
      to_port = 8080
      protocol = 6
      source_security_group_id = "${module.other-alb-sg.this_security_group_id}"
    }
  ]
  number_of_computed_ingress_with_source_security_group_id = 1

  egress_cidr_blocks = [
    "0.0.0.0/0"
  ]
  egress_rules = [
    "http-80-tcp",
    "https-443-tcp"
  ]

  computed_egress_with_source_security_group_id = [
    {
      from_port = "3306"
      to_port = "3306"
      protocol = "tcp"
      source_security_group_id = "${module.other-database-sg.this_security_group_id}"
    }
  ]
  number_of_computed_egress_with_source_security_group_id = 1

}

module "other-instances" {
  source = "terraform-aws-modules/ec2-instance/aws"
  version = "1.21.0"

  name = "other"
  instance_count = 1

  ami = "${data.aws_ami.ami_with_docker_and_git.id}"
  instance_type = "${var.ec2_instance_type}"
  monitoring = false
  vpc_security_group_ids = [
    "${module.other-instance-sg.this_security_group_id}"
  ]
  subnet_id = "${data.terraform_remote_state.vpc.private_subnets[0]}"

  // manually created EC2AccesForSSMAndECR
  iam_instance_profile = "EC2AccesForSSMAndECR"
  key_name = "${data.terraform_remote_state.vpc.ec2_ssh_key_name}"

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
    Name = "OtherAppServer"
  }
}
