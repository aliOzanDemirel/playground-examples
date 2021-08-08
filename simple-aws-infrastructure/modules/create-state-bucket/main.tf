variable "environment" {
  description = "Environment of VPC"
}

data "aws_region" "current" {}

resource "aws_s3_bucket" "s3-terraform-state-storage" {
  bucket = "terraform-state-bucket-${var.environment}"

  versioning {
    enabled = true
  }

  lifecycle {
    prevent_destroy = true
  }

  tags {
    Name = "S3 Remote Terraform State Store"
    Environment = "${var.environment}"
    LockTableRegion = "${data.aws_region.current.name}"
    Terraform = true
  }
}

// one dynamodb table is enough to lock multiple terraform state files as dynamodb will create seperate rows for seperate state files
resource "aws_dynamodb_table" "dynamodb-terraform-state-lock" {
  name = "terraform-state-lock-table-${var.environment}"

  read_capacity = 1
  write_capacity = 1
  hash_key = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags {
    Name = "DynamoDB Terraform State Lock Table"
    Environment = "${var.environment}"
    Terraform = true
  }
}
