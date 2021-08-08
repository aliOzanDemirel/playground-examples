resource "aws_key_pair" "ec2_ssh_key" {
  key_name = "launch_key_${var.environment}"
  public_key = "${file("launch_key.pub")}"
}
