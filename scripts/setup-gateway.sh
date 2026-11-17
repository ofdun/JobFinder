#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
umask 077
command -v openssl >/dev/null 2>&1 || { echo "Требуется OpenSSL." >&2; exit 1; }
if [ ! -e JobFinder/.env ]; then
    printf 'JWT_SECRET=%s\n' "$(openssl rand -hex 32)" > JobFinder/.env
fi
if ! grep -q '^AUTH_ENCRYPTION_KEY=.' JobFinder/.env; then
    printf '\nAUTH_ENCRYPTION_KEY=%s\n' "$(openssl rand -base64 32)" >> JobFinder/.env
fi
mkdir -p .local/gateway
if [ -s .local/gateway/admin.htpasswd ]; then
    echo "Существующие учётные данные сохранены."
    exit 0
fi
openssl rand -hex 24 > .local/gateway/admin.password
password_hash=$(openssl passwd -apr1 -stdin < .local/gateway/admin.password)
printf 'admin:%s\n' "$password_hash" > .local/gateway/admin.htpasswd
chmod 644 .local/gateway/admin.htpasswd
echo "Пользователь: admin. Пароль сохранён в .local/gateway/admin.password"
