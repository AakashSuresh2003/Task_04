resource "null_resource" "packer_build" {
  provisioner "local-exec" {
    command = "packer build ./packer_template.pkr.hcl"
  }

  triggers = {
    always_run = "${timestamp()}"
  }
}

data "aws_ami" "custom_ami" {
  most_recent = true
  filter {
    name   = "name"
    values = ["custom-ami-with-docker-*"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
  owners = ["self"]
  depends_on = [null_resource.packer_build]
}

output "custom_ami_id" {
  value = data.aws_ami.custom_ami.id
}
