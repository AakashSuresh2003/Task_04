resource "local_file" "ansible_inventory" {
  filename = "/Users/aakashs/Desktop/ansible/hosts.ini"  # Update path as necessary
  content  = <<-EOF
  [my_ec2]
  ${var.instance_public_ip} ansible_host=ubuntu ansible_ssh_private_key_file=/Users/aakashs/Downloads/${var.key_name}.pem
  EOF
}

resource "null_resource" "run_ansible_playbook" {
  provisioner "local-exec" {
    command = <<-EOT
      ansible-playbook -i /Users/aakashs/Desktop/ansible/hosts.ini ${path.module}/main_playbook.yml
    EOT
  }

  depends_on = [local_file.ansible_inventory]
}
