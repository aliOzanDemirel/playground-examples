variable "region" {
  description = "Region for the VPC"
  default = "eu-west-1"
}

variable "environment" {
  description = "Environment of VPC"
  default = "stg"
}

variable "used_availability_zones" {
  description = "Availability zones that are used in infrastructre of budget-department"
  type = "list"
  default = [
    "eu-west-1a",
    "eu-west-1b"
  ]
}

variable "cidr_vpc" {
  description = "IP range of budget-department VPC"
  default = "10.100.0.0/16"
}

variable "cidr_public_subnets" {
  type = "list"
  default = [
    "10.100.0.0/19",
    "10.100.32.0/19"
  ]
}

variable "cidr_private_subnets" {
  type = "list"
  default = [
    "10.100.64.0/18",
    "10.100.128.0/18"
  ]
}

variable "cidr_db_subnets" {
  type = "list"
  default = [
    "10.100.192.0/19",
    "10.100.224.0/19"
  ]
}

variable "budget-department-private-domain" {
  type = "string"
  default = "stg.budget.local"
}

locals {

  // icmp_type and icmp_code are not implemented in module
  allow_icmp = {
    rule_number = 9990
    rule_action = "allow"
    protocol = "icmp"
    from_port = 8
    to_port = 0
    icmp_type = -1
    icmp_code = -1
    cidr_block = "${var.cidr_vpc}"
  },
  allow_all_traffic = {
    rule_number = 10000
    rule_action = "allow"
    protocol = -1
    from_port = 0
    to_port = 0
    cidr_block = "0.0.0.0/0"
  },

  public_acl_inbound = [
    "${local.allow_all_traffic}"
  ],
  public_acl_outbound = [
    "${local.allow_all_traffic}"
  ],

  private_acl_inbound = [
    {
      rule_number = 10
      rule_action = "allow"
      protocol = "tcp"
      from_port = 22
      to_port = 22
      cidr_block = "${var.cidr_vpc}"
    },
    {
      rule_number = 20
      rule_action = "allow"
      protocol = "tcp"
      from_port = 8080
      to_port = 8080
      cidr_block = "${var.cidr_vpc}"
    },
    {
      rule_number = 30
      rule_action = "allow"
      protocol = "tcp"
      from_port = 1024
      to_port = 65535
      cidr_block = "0.0.0.0/0"
    },
    "${local.allow_all_traffic}"
  ],

  private_acl_outbound = [
    {
      rule_number = 10
      rule_action = "allow"
      protocol = "tcp"
      from_port = 80
      to_port = 80
      cidr_block = "0.0.0.0/0"
    },
    {
      rule_number = 20
      rule_action = "allow"
      protocol = "tcp"
      from_port = 443
      to_port = 443
      cidr_block = "0.0.0.0/0"
    },
    {
      rule_number = 30
      rule_action = "allow"
      protocol = "tcp"
      from_port = 1024
      to_port = 65535
      cidr_block = "${var.cidr_vpc}"
      // cidr_block = "${var.cidr_public_subnets[0]}"
    },
    "${local.allow_all_traffic}"
  ],


  db_acl_inbound = [
    {
      rule_number = 10
      rule_action = "allow"
      protocol = "tcp"
      from_port = 3306
      to_port = 3306
      cidr_block = "${var.cidr_vpc}"
    }
  ],
  db_acl_outbound = [
    {
      rule_number = 10
      rule_action = "allow"
      protocol = "tcp"
      // from_port = 1024
      from_port = 32768
      to_port = 65535
      cidr_block = "${var.cidr_vpc}"
    }
  ]
}
