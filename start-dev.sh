#!/bin/bash

# Personal Blog 开发环境启动脚本

set -u

load_env_file() {
    local env_file="$1"
    set -a
    . "$env_file"
    set +a
}

echo "🚀 启动 Personal Blog 开发环境..."
echo ""

# 加载本地环境变量
if [ -f .env.local ]; then
    echo "📋 加载 .env.local 环境变量..."
    load_env_file .env.local
elif [ -f .env ]; then
    echo "📋 加载 .env 环境变量..."
    load_env_file .env
fi

BACKEND_PORT="${BACKEND_PORT:-9191}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-personalblog}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-}"
PSQL_BIN="/opt/homebrew/opt/postgresql@15/bin/psql"
BACKEND_LOG="/tmp/backend.log"

# 检查 PostgreSQL 是否运行
if [[ "$DB_HOST" == "localhost" || "$DB_HOST" == "127.0.0.1" ]]; then
    if ! lsof -i :"$DB_PORT" > /dev/null 2>&1; then
        echo "📦 启动 PostgreSQL..."
        brew services start postgresql@15
        sleep 2
    else
        echo "✓ PostgreSQL 已运行"
    fi
else
    echo "ℹ️  使用远程数据库: $DB_HOST:$DB_PORT"
fi

# 预检数据库连接
if [ -x "$PSQL_BIN" ]; then
    if ! PGPASSWORD="$DB_PASSWORD" "$PSQL_BIN" -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" > /dev/null 2>/tmp/personal_blog_db_check.log; then
        echo "❌ 数据库连接失败，请检查以下配置："
        echo "   - DB_HOST=$DB_HOST"
        echo "   - DB_PORT=$DB_PORT"
        echo "   - DB_NAME=$DB_NAME"
        echo "   - DB_USER=$DB_USER"
        echo ""
        echo "   数据库错误信息："
        tail -n 20 /tmp/personal_blog_db_check.log
        exit 1
    fi
else
    echo "⚠️  未找到 psql，跳过数据库连通性预检"
fi

# 检查后端服务是否运行
if ! lsof -i :"$BACKEND_PORT" > /dev/null 2>&1; then
    echo "🔧 启动后端服务..."
    ./gradlew :backend:run > "$BACKEND_LOG" 2>&1 &
    BACKEND_PID=$!
    echo "   后端日志: $BACKEND_LOG"

    STARTED=false
    for _ in {1..20}; do
        if lsof -i :"$BACKEND_PORT" > /dev/null 2>&1; then
            STARTED=true
            break
        fi
        if ! kill -0 "$BACKEND_PID" > /dev/null 2>&1; then
            break
        fi
        sleep 1
    done

    if [ "$STARTED" != "true" ]; then
        echo "❌ 后端启动失败，请检查日志: $BACKEND_LOG"
        tail -n 40 "$BACKEND_LOG"
        exit 1
    fi
else
    echo "✓ 后端服务已运行"
fi

echo ""
echo "✅ 开发环境已就绪！"
echo ""
echo "📊 服务状态："
echo "   - PostgreSQL: $DB_HOST:$DB_PORT"
echo "   - 数据库名:   $DB_NAME"
echo "   - 数据库用户: $DB_USER"
echo "   - 后端 API:   http://localhost:$BACKEND_PORT"
echo "   - Android:    http://10.0.2.2:$BACKEND_PORT"
echo ""
echo "👤 测试账号："
echo "   - 邮箱: admin@blog.com"
echo "   - 密码: admin123"
echo ""
echo "🛠️  常用命令："
echo "   - 查看后端日志: tail -f $BACKEND_LOG"
echo "   - 停止后端:     pkill -f 'backend:run'"
echo "   - 停止数据库:   brew services stop postgresql@15"
echo "   - 运行 Android: ./gradlew :composeApp:installDebug"
echo ""
