output "instance_public_ip" {
  value       = aws_instance.app_server.public_ip
  description = "IP pública del servidor"
}

output "apis" {
  value = {
    franchise_service    = "http://${aws_instance.app_server.public_ip}:8080"
    branch_service       = "http://${aws_instance.app_server.public_ip}:8081"
    product_service      = "http://${aws_instance.app_server.public_ip}:8083"
    rabbitmq_management  = "http://${aws_instance.app_server.public_ip}:15672"
  }
}

output "ssh_command" {
  value = "ssh -i app-key.pem ec2-user@${aws_instance.app_server.public_ip}"
}