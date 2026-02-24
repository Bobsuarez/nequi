

# ── Data sources ──────────────────────────────────────────
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_budgets_budget" "limite" {
  name         = "limite-prueba"
  budget_type  = "COST"
  limit_amount = "1"
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 80
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = ["eduar.suarez001@email.com"]  # <- cambia esto
  }
}

# ── Security Group ────────────────────────────────────────
resource "aws_security_group" "app_sg" {
  name        = "docker-compose-sg"
  description = "Acceso a los servicios del stack"

  # RabbitMQ Management UI
  ingress {
    from_port   = 15672
    to_port     = 15672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # APIs
  ingress {
    from_port   = 8080
    to_port     = 8083
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SSH (para debugging)
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# ── Key Pair ──────────────────────────────────────────────
resource "tls_private_key" "app_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "app_key_pair" {
  key_name   = "docker-compose-key"
  public_key = tls_private_key.app_key.public_key_openssh
}

resource "local_file" "private_key" {
  content         = tls_private_key.app_key.private_key_pem
  filename        = "${path.module}/app-key.pem"
  file_permission = "0400"
}

# ── EC2 Instance ──────────────────────────────────────────
resource "aws_instance" "app_server" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = "t2.micro"   # Free Tier
  key_name               = aws_key_pair.app_key_pair.key_name
  vpc_security_group_ids = [aws_security_group.app_sg.id]

  # Disco suficiente para las imágenes Docker
  root_block_device {
    volume_size = 20  # GB — free tier incluye 30GB
    volume_type = "gp2"
  }

  user_data = templatefile("${path.module}/user_data.sh", {
    github_repo   = var.github_repo
    github_branch = var.github_branch
    github_token  = var.github_token
  })

  tags = {
    Name = "docker-compose-server"
  }
}

# Después del resource aws_instance, agrega:
resource "aws_cloudwatch_metric_alarm" "cpu_alto" {
  alarm_name          = "cpu-alto"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "CPU mayor al 80%"
  dimensions = {
    InstanceId = aws_instance.app_server.id
  }
}