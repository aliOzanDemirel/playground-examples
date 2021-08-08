provider "aws" {
  region = "${var.region}"
  version = "~> 2.7"
}

terraform {
  backend "s3" {
    bucket = "terraform-state-dev"
    key = "dev/app/some/terraform.tfstate"
    encrypt = true
    dynamodb_table = "terraform-state-lock-table-dev"
    region = "eu-west-1"
  }
}

data "terraform_remote_state" "vpc" {
  backend = "s3"
  config {
    bucket = "terraform-state-dev"
    key = "dev/vpc/terraform.tfstate"
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

resource "aws_ecr_repository" "some-service1-ecr-repo" {
  name = "service1"
  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
  }
}

resource "aws_ecr_repository" "some-service2-ecr-repo" {
  name = "service2"
  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
  }
}

module "some-instances-sg" {
  source = "terraform-aws-modules/security-group/aws"
  version = "2.17.0"

  name = "some-ec2-sg"
  description = "Security group for Some instances"
  vpc_id = "${data.terraform_remote_state.vpc.vpc_id}"

  computed_ingress_with_source_security_group_id = [
    {
      from_port = 8080
      to_port = 8080
      protocol = 6
      source_security_group_id = "${module.some-alb-sg.this_security_group_id}"
    },
    {
      from_port = 8180
      to_port = 8180
      protocol = 6
      source_security_group_id = "${module.some-alb-sg.this_security_group_id}"
    }
  ]
  number_of_computed_ingress_with_source_security_group_id = 2

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
      source_security_group_id = "${module.some-database-sg.this_security_group_id}"
    }
  ]
  number_of_computed_egress_with_source_security_group_id = 1

}

module "some-instance-service1" {
  source = "terraform-aws-modules/ec2-instance/aws"
  version = "1.21.0"

  name = "service1"
  instance_count = 1

  ami = "${data.aws_ami.ami_with_docker_and_git.id}"
  instance_type = "${var.ec2_instance_type}"
  monitoring = false
  vpc_security_group_ids = [
    "${module.some-instances-sg.this_security_group_id}"
  ]
  subnet_id = "${data.terraform_remote_state.vpc.private_subnets[0]}"

  // manually created EC2AccesForSSMAndECR
  iam_instance_profile = "EC2AccesForSSMAndECR"
  key_name = "${data.terraform_remote_state.vpc.ec2_ssh_key_name}"

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
    Name = "SomeAppService1Server"
  }
}

module "some-instance-service2" {
  source = "terraform-aws-modules/ec2-instance/aws"
  version = "1.21.0"

  name = "service2"
  instance_count = 1

  ami = "${data.aws_ami.ami_with_docker_and_git.id}"
  instance_type = "${var.ec2_instance_type}"
  monitoring = false
  vpc_security_group_ids = [
    "${module.some-instances-sg.this_security_group_id}"
  ]
  subnet_id = "${data.terraform_remote_state.vpc.private_subnets[0]}"

  // manually created EC2AccesForSSMAndECR
  iam_instance_profile = "EC2AccesForSSMAndECR"
  key_name = "${data.terraform_remote_state.vpc.ec2_ssh_key_name}"

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
    Name = "SomeAppService2Server"
  }
}
