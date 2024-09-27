variable "ami_id" {
  description = "AMI ID to launch the instance"
  type        = string
}

variable "security_group_id" {
  description = "Security Group ID to associate with the instance"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
}

variable "key_name" {
  description = "Name of the SSH key pair"
  type        = string
}
