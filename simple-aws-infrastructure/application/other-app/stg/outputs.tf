output "other_instance_ids" {
  value = "${module.other-instances.id}"
}

output "other_db_local_dns" {
  value = "${aws_route53_record.other_db_local_dns.records}"
}

output "other_db_address" {
  value = "${module.other-database.this_db_instance_address}"
}

output "other_db_rds_id" {
  value = "${module.other-database.this_db_instance_id}"
}

output "other_alb_id" {
  value = "${module.other-alb.load_balancer_id}"
}

output "other_alb_address" {
  value = "${module.other-alb.dns_name}"
}

output "other_cert_arn" {
  value = "${module.other-domain-cert.this_acm_certificate_arn}"
}

output "other_ecr_repo" {
  value = "${aws_ecr_repository.other-ecr-repo.repository_url}"
}

output "other_ecr_registry_id" {
  value = "${aws_ecr_repository.other-ecr-repo.registry_id}"
}
