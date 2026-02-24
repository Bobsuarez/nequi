variable "aws_region" {
  default = "us-east-1"
}

variable "github_repo" {
  description = "URL del repositorio GitHub"
  type        = string
  default     = "https://github.com/Bobsuarez/nequi.git"
}

variable "github_branch" {
  default = "master"
}

variable "github_token" {
  description = "Token de GitHub (solo repos privados)"
  type        = string
  default     = ""
  sensitive   = true
}