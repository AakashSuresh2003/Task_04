provider "aws" {
  region = var.aws_region
}

module "ami" {
  source = "./modules/ami"
}

module "security_group" {
  source = "./modules/security_group"
}

module "ec2_instance" {
  source            = "./modules/ec2_instance"
  ami_id           = module.ami.custom_ami_id
  security_group_id = module.security_group.security_group_id
  instance_type    = var.instance_type
  key_name         = var.key_name
}

module "ansible" {
  source             = "./modules/ansible"
  instance_public_ip = module.ec2_instance.public_ip
  key_name           = var.key_name
}
