variable "region" {
  description = "Region for the VPC"
  default = "eu-west-1"
}

variable "environment" {
  description = "Environment of VPC"
  default = "stg"
}

provider "aws" {
  region = "${var.region}"
  version = "~> 2.7"
}

module "create-state-bucket" {
  source = "../../../modules/create-state-bucket"

  environment = "${var.environment}"
}
