variable "region" {
  description = "Region for the VPC"
  default = "eu-west-1"
}

variable "environment" {
  description = "Environment of VPC"
  default = "dev"
}

variable "ec2_instance_type" {
  description = "EC2 instance type"
  default = "t2.micro"
}

variable "db_instance_type" {
  description = "RDS instance class"
  default = "db.t2.micro"
}

variable "db_size" {
  description = "RDS storage size"
  default = 10
}
