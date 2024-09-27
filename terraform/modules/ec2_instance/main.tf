resource "aws_instance" "my_ec2" {
  ami             = var.ami_id
  instance_type   = var.instance_type
  key_name        = var.key_name
  security_groups = [var.security_group_id]

  tags = {
    Name = "SonarQube-Jenkins-Instance"
  }
}

output "public_ip" {
  value = aws_instance.my_ec2.public_ip
}
