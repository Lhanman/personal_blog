#!/bin/bash
# 首次申请 SSL 证书脚本
# 使用前请先配置 .env 文件中的 DOMAIN 和 CERTBOT_EMAIL

set -e

if [ ! -f .env ]; then
    echo "错误：找不到 .env 文件，请先创建并配置 DOMAIN 和 CERTBOT_EMAIL"
    exit 1
fi

source .env

if [ -z "$DOMAIN" ] || [ -z "$CERTBOT_EMAIL" ]; then
    echo "错误：.env 中缺少 DOMAIN 或 CERTBOT_EMAIL"
    exit 1
fi

echo "为域名 $DOMAIN 申请 SSL 证书..."

# 替换 nginx 配置中的域名占位符
sed -i "s/\${DOMAIN}/$DOMAIN/g" docker/nginx.prod.conf

# 确保 certbot 目录存在
mkdir -p docker/certbot/conf docker/certbot/www

# 先用 HTTP-only 配置启动 nginx（用于 ACME challenge）
docker compose -f docker-compose.prod.yml up -d nginx

# 申请证书
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$CERTBOT_EMAIL" \
    --agree-tos \
    --no-eff-email \
    -d "$DOMAIN"

echo "证书申请成功！重启 nginx..."
docker compose -f docker-compose.prod.yml restart nginx

echo "完成！访问 https://$DOMAIN 验证"
