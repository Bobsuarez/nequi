#!/bin/bash
set -e
exec > /var/log/user-data.log 2>&1

# ── 1. Instalar Docker ────────────────────────────────────
dnf update -y
dnf install -y docker git
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user

# ── 2. Instalar Docker Compose v2 ─────────────────────────
mkdir -p /usr/local/lib/docker/cli-plugins
curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# ── 3. Clonar el repositorio ──────────────────────────────
REPO_URL="${github_repo}"

# Si hay token (repo privado), inyectarlo en la URL
%{ if github_token != "" }
REPO_URL=$(echo "$REPO_URL" | sed "s|https://|https://${github_token}@|")
%{ endif }

git clone --branch "${github_branch}" "$REPO_URL" /app
cd /app

# ── 4. Levantar el stack ──────────────────────────────────
docker compose up -d --build

# ── 5. Verificar que levantó ──────────────────────────────
sleep 10
docker compose ps
# Auto-shutdown en 3 horas por seguridad
shutdown -h +120