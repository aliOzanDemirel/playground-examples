provider "aws" {
  region = "${var.region}"
  version = "~> 2.7"
  shared_credentials_file = "%USERS%/.aws/credentials"
  profile = "aws-profile-dev-account"
}

terraform {
  backend "s3" {
    bucket = "terraform-state-dev"
    key = "dev/vpc/terraform.tfstate"
    encrypt = true
    dynamodb_table = "terraform-state-lock-table-dev"
    region = "eu-west-1"
  }
}

module "budget-department-vpc" {
  source = "terraform-aws-modules/vpc/aws"
  version = "1.64.0"

  tags = {
    Terraform = "true"
    Environment = "${var.environment}"
  }

  create_vpc = true

  name = "budget-department-${var.environment}"
  cidr = "${var.cidr_vpc}"

  azs = "${var.used_availability_zones}"
  public_subnets = "${var.cidr_public_subnets}"
  private_subnets = "${var.cidr_private_subnets}"
  database_subnets = "${var.cidr_db_subnets}"

  create_redshift_subnet_group = false
  create_elasticache_subnet_group = false
  #elasticache_subnets = ["10.10.31.0/24", "10.10.32.0/24", "10.10.33.0/24"]
  #redshift_subnets    = ["10.10.41.0/24", "10.10.42.0/24", "10.10.43.0/24"]
  #intra_subnets       = ["10.10.51.0/24", "10.10.52.0/24", "10.10.53.0/24"]

  # create only one NAT GW for public subnets in AZ
  enable_nat_gateway = true
  single_nat_gateway = false
  one_nat_gateway_per_az = true
  reuse_nat_ips = false

  # default ACL will not be used by any of the subnets
  manage_default_network_acl = false
  public_dedicated_network_acl = true
  private_dedicated_network_acl = true
  database_dedicated_network_acl = true

  public_outbound_acl_rules = "${local.public_acl_outbound}"
  public_inbound_acl_rules = "${local.public_acl_inbound}"

  private_outbound_acl_rules = "${local.private_acl_outbound}"
  private_inbound_acl_rules = "${local.private_acl_inbound}"
  # private_outbound_acl_rules = "${concat(local.private_acl_outbound, var.db_acl_outbound)}"

  create_database_subnet_group = true
  # disallow internet access for databases
  create_database_subnet_route_table = false
  create_database_internet_gateway_route = false
  database_outbound_acl_rules = "${local.db_acl_outbound}"
  database_inbound_acl_rules = "${local.db_acl_inbound}"

  enable_dns_hostnames = true
  enable_dns_support = true

  # enable_dhcp_options = true
  # enable_vpn_gateway = true

  # enable_s3_endpoint = true
  # enable_dynamodb_endpoint = true

  # VPC endpoint for SSM
  # enable_ssm_endpoint              = true
  # ssm_endpoint_private_dns_enabled = true
  # ssm_endpoint_security_group_ids  = ["${data.aws_security_group.default.id}"] # ssm_endpoint_subnet_ids = ["..."]
  # VPC Endpoint for EC2
  # enable_ec2_endpoint              = true
  # ec2_endpoint_private_dns_enabled = true
  # ec2_endpoint_security_group_ids  = ["${data.aws_security_group.default.id}"]
  # VPC Endpoint for ECR API
  # enable_ecr_api_endpoint              = true
  # ecr_api_endpoint_private_dns_enabled = true
  # ecr_api_endpoint_security_group_ids  = ["${data.aws_security_group.default.id}"]

}

module "common-ssh-sg" {
  source = "terraform-aws-modules/security-group/aws"
  version = "2.17.0"

  name = "common-ssh-sg"
  description = "Security group for SSH connection"
  vpc_id = "${module.budget-department-vpc.vpc_id}"

  // some bastion server or proxy or anything other than this
  ingress_cidr_blocks = [
    "0.0.0.0/0"
  ]
  ingress_rules = [
    "ssh-22-tcp"
  ]

}
