provider "aws" {
  region = "ap-south-1"
}

resource "null_resource" "packer_build" {
  provisioner "local-exec" {
    command = "cd ../sonarqube-jenkins-image-builder && packer init . && packer build -machine-readable manual-sonarqube-jenkins.pkr.hcl | tee ../packer_output.log"
  }

  triggers = {
    always_run = "${md5(file("../sonarqube-jenkins-image-builder/manual-sonarqube-jenkins.pkr.hcl"))}"
  }
}

data "external" "packer_ami_id" {
  program = ["bash", "-c", "grep -Eo 'ami-[a-z0-9]+' ../packer_output.log | tail -1 | jq -R '{ ami_id: . }'"]

  depends_on = [null_resource.packer_build]
}

resource "aws_security_group" "jenkins_sonarqube_sg" {
  name        = "jenkins-sonarqube-sg"
  description = "Allow traffic for Jenkins and SonarQube"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  
  }

  ingress {
    from_port   = 9000
    to_port     = 9000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "jenkins-sonarqube-sg"
  }
}

resource "aws_instance" "jenkins_sonarqube_instance" {
  ami                          = data.external.packer_ami_id.result["ami_id"]   
  instance_type               = "t2.large"
  key_name                    = "key1"
  vpc_security_group_ids      = [aws_security_group.jenkins_sonarqube_sg.id]
  associate_public_ip_address = true  

  tags = {
    Name = "Jenkins-SonarQube-Instance"
  }

  provisioner "remote-exec" {
    inline = [
      "sudo systemctl start jenkins",
      "sudo systemctl start sonarqube",
    ]

    connection {
      type        = "ssh"
      user        = "ubuntu"
      private_key = file("/Users/aakashs/Downloads/key1.pem")
      host        = self.public_ip
    }
  }

  depends_on = [null_resource.packer_build]
}

output "instance_public_ip" {
  value = aws_instance.jenkins_sonarqube_instance.public_ip
}

resource "null_resource" "ansible_run" {
  provisioner "local-exec" {
    command = <<EOT
      # Update hosts.ini with the new instance public IP
      echo "[jenkins]" > hosts.ini
      echo "${aws_instance.jenkins_sonarqube_instance.public_ip} ansible_ssh_user=ubuntu ansible_ssh_private_key_file=/Users/aakashs/Downloads/key1.pem ansible_ssh_common_args='-o StrictHostKeyChecking=no'" >> hosts.ini
      
      echo "[sonarqube]" >> hosts.ini
      echo "${aws_instance.jenkins_sonarqube_instance.public_ip} ansible_ssh_user=ubuntu ansible_ssh_private_key_file=/Users/aakashs/Downloads/key1.pem ansible_ssh_common_args='-o StrictHostKeyChecking=no'" >> hosts.ini
      
      # Run all Ansible Playbooks
      ansible-playbook -i hosts.ini ../ansible/user_creation_plugins_install.yml
      ansible-playbook -i hosts.ini ../ansible/api_creation.yml
      ansible-playbook -i hosts.ini ../ansible/sonarqube_configuration.yml
      ansible-playbook -i hosts.ini ../ansible/token_configuration.yml
      ansible-playbook -i hosts.ini ../ansible/job_creation.yml
    EOT
  }

  depends_on = [aws_instance.jenkins_sonarqube_instance]
}
