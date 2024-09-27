variable "instance_public_ip" {
  description = "Public IP of the EC2 instance"
  type        = string
}

variable "key_name" {
  description = "Name of the SSH key pair"
  type        = string
}
