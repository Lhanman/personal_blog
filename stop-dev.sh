#!/bin/bash

# Personal Blog 开发环境停止脚本

echo "🛑 停止 Personal Blog 开发环境..."
echo ""

# 停止后端服务
if pgrep -f "backend:run" > /dev/null; then
    echo "🔧 停止后端服务..."
    pkill -f "backend:run"
    sleep 2
    echo "   ✓ 后端服务已停止"
else
    echo "   ℹ️  后端服务未运行"
fi

# 停止 PostgreSQL（可选）
read -p "是否停止 PostgreSQL 服务？(y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📦 停止 PostgreSQL..."
    brew services stop postgresql@15
    echo "   ✓ PostgreSQL 已停止"
fi

echo ""
echo "✅ 开发环境已停止"
