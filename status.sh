#!/bin/bash

# Personal Blog 开发环境状态检查脚本

set -u

load_env_file() {
    local env_file="$1"
    set -a
    . "$env_file"
    set +a
}

if [ -f .env.local ]; then
    load_env_file .env.local
elif [ -f .env ]; then
    load_env_file .env
fi

BACKEND_PORT="${BACKEND_PORT:-9191}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-personalblog}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-}"
PSQL_BIN="/opt/homebrew/opt/postgresql@15/bin/psql"

echo "📊 Personal Blog 系统状态"
echo "================================"
echo ""

# PostgreSQL 状态
echo "1️⃣  PostgreSQL 数据库"
if lsof -i :"$DB_PORT" > /dev/null 2>&1; then
    PID=$(lsof -i :"$DB_PORT" | grep LISTEN | awk '{print $2}' | head -1)
    echo "   状态: ✅ 运行中 (PID: $PID)"
    echo "   地址: $DB_HOST:$DB_PORT"
else
    echo "   状态: ❌ 未运行"
fi
echo ""

# 后端服务状态
echo "2️⃣  后端 API 服务"
if lsof -i :"$BACKEND_PORT" > /dev/null 2>&1; then
    PID=$(lsof -i :"$BACKEND_PORT" | grep LISTEN | awk '{print $2}' | head -1)
    echo "   状态: ✅ 运行中 (PID: $PID)"
    echo "   地址: http://localhost:$BACKEND_PORT"

    if curl -s "http://localhost:$BACKEND_PORT/api/v1/posts" > /dev/null 2>&1; then
        TOTAL=$(curl -s "http://localhost:$BACKEND_PORT/api/v1/posts" | grep -o '"total":[0-9]*' | cut -d':' -f2)
        echo "   API:  ✅ 正常响应 (${TOTAL:-0} 篇文章)"
    else
        echo "   API:  ⚠️  无响应"
    fi
else
    echo "   状态: ❌ 未运行"
fi
echo ""

# 数据库内容
echo "3️⃣  数据库内容"
if [ -x "$PSQL_BIN" ] && lsof -i :"$DB_PORT" > /dev/null 2>&1; then
    POSTS=$(PGPASSWORD="$DB_PASSWORD" "$PSQL_BIN" -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM posts WHERE published = true;" 2>/dev/null | xargs)
    USERS=$(PGPASSWORD="$DB_PASSWORD" "$PSQL_BIN" -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM users;" 2>/dev/null | xargs)
    TAGS=$(PGPASSWORD="$DB_PASSWORD" "$PSQL_BIN" -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM tags;" 2>/dev/null | xargs)

    if [ -n "${POSTS:-}" ] || [ -n "${USERS:-}" ] || [ -n "${TAGS:-}" ]; then
        echo "   文章: ${POSTS:-0} 篇（已发布）"
        echo "   用户: ${USERS:-0} 个"
        echo "   标签: ${TAGS:-0} 个"
    else
        echo "   ⚠️  无法连接数据库，请检查 DB_HOST / DB_PORT / DB_NAME / DB_USER"
    fi
else
    echo "   ⚠️  数据库未运行，无法查询"
fi
echo ""

# Android 配置
echo "4️⃣  Android 应用配置"
echo "   API 地址: http://10.0.2.2:$BACKEND_PORT"
if [ -f "frontend/composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
    echo "   APK: ✅ 已构建"
else
    echo "   APK: ⚠️  未构建"
fi
echo ""

echo "================================"
echo ""
echo "💡 快速命令："
echo "   启动环境: ./start-dev.sh"
echo "   停止环境: ./stop-dev.sh"
echo "   查看日志: tail -f /tmp/backend.log"
echo "   安装应用: ./gradlew :composeApp:installDebug"
echo ""
