output "vpc_id" {
  value = "${module.budget-department-vpc.vpc_id}"
}

output "vpc_arn" {
  value = "${module.budget-department-vpc.vpc_arn}"
}

output "vpc_cidr" {
  value = "${module.budget-department-vpc.vpc_cidr_block}"
}

output "public_subnets" {
  value = "${module.budget-department-vpc.public_subnets}"
}

output "public_subnets_cidr" {
  value = "${module.budget-department-vpc.public_subnets_cidr_blocks}"
}

output "private_subnets" {
  value = "${module.budget-department-vpc.private_subnets}"
}

output "private_subnets_cidr" {
  value = "${module.budget-department-vpc.private_subnets_cidr_blocks}"
}

output "db_subnets" {
  value = "${module.budget-department-vpc.database_subnets}"
}

output "db_subnets_cidr" {
  value = "${module.budget-department-vpc.database_subnets_cidr_blocks}"
}

output "db_subnet_group" {
  value = "${module.budget-department-vpc.database_subnet_group}"
}

output "ec2_ssh_key_name" {
  value = "${aws_key_pair.ec2_ssh_key.key_name}"
}

output "private_zone_id" {
  value = "${aws_route53_zone.budget-department-private-zone.zone_id}"
}

output "private_zone_domain" {
  value = "${aws_route53_zone.budget-department-private-zone.name}"
}
