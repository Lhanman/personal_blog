#!/bin/bash

# Personal Blog 快速部署脚本

set -e  # 遇到错误立即退出

echo "╔════════════════════════════════════════════════════════════╗"
echo "║                                                            ║"
echo "║          Personal Blog 快速部署脚本                        ║"
echo "║                                                            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${BLUE}▶${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# 检查必要的工具
check_requirements() {
    print_step "检查系统要求..."

    local missing_tools=()

    # 检查 Java
    if ! command -v java &> /dev/null; then
        missing_tools+=("Java (JDK 17+)")
    fi

    # 检查 Gradle
    if [ ! -f "./gradlew" ]; then
        missing_tools+=("Gradle wrapper")
    fi

    # 检查 PostgreSQL
    if ! command -v psql &> /dev/null && ! command -v /opt/homebrew/opt/postgresql@15/bin/psql &> /dev/null; then
        missing_tools+=("PostgreSQL")
    fi

    if [ ${#missing_tools[@]} -eq 0 ]; then
        print_success "所有必要工具已安装"
        return 0
    else
        print_error "缺少以下工具："
        for tool in "${missing_tools[@]}"; do
            echo "  - $tool"
        done
        return 1
    fi
}

# 配置环境变量
setup_env() {
    print_step "配置环境变量..."

    if [ ! -f ".env" ]; then
        if [ -f ".env.example" ]; then
            cp .env.example .env
            print_success "已创建 .env 文件"
            print_warning "请编辑 .env 文件配置数据库密码和 JWT Secret"
            echo ""
            echo -n "是否现在编辑 .env 文件？(y/N): "
            read -r response
            if [[ "$response" =~ ^[Yy]$ ]]; then
                ${EDITOR:-nano} .env
            fi
        else
            print_error ".env.example 文件不存在"
            return 1
        fi
    else
        print_success ".env 文件已存在"
    fi
}

# 启动 PostgreSQL
start_postgresql() {
    print_step "启动 PostgreSQL..."

    if lsof -i :5432 > /dev/null 2>&1; then
        print_success "PostgreSQL 已在运行"
        return 0
    fi

    if command -v brew &> /dev/null; then
        brew services start postgresql@15
        sleep 3
        print_success "PostgreSQL 已启动"
    else
        print_error "无法启动 PostgreSQL，请手动启动"
        return 1
    fi
}

# 初始化数据库
init_database() {
    print_step "初始化数据库..."

    PSQL="/opt/homebrew/opt/postgresql@15/bin/psql"

    # 创建数据库
    if $PSQL postgres -lqt | cut -d \| -f 1 | grep -qw personalblog; then
        print_success "数据库 personalblog 已存在"
    else
        $PSQL postgres -c "CREATE DATABASE personalblog;" 2>/dev/null
        print_success "数据库 personalblog 已创建"
    fi

    # 创建用户
    if $PSQL postgres -t -c "SELECT 1 FROM pg_roles WHERE rolname='postgres'" | grep -q 1; then
        print_success "用户 postgres 已存在"
    else
        $PSQL postgres -c "CREATE USER postgres WITH SUPERUSER PASSWORD 'postgres';" 2>/dev/null
        print_success "用户 postgres 已创建"
    fi
}

# 构建后端
build_backend() {
    print_step "构建后端服务..."

    ./gradlew :backend:build -x test
    print_success "后端构建完成"
}

# 构建前端
build_frontend() {
    print_step "构建前端应用..."

    echo ""
    echo "请选择要构建的平台："
    echo "  1) Android"
    echo "  2) Web (WASM)"
    echo "  3) 全部"
    echo "  4) 跳过"
    echo ""
    echo -n "请选择 [1-4]: "
    read -r choice

    case $choice in
        1)
            print_step "构建 Android 应用..."
            ./gradlew :composeApp:assembleDebug
            print_success "Android 应用构建完成"
            ;;
        2)
            print_step "构建 Web 应用..."
            ./gradlew :composeApp:wasmJsBrowserDistribution
            print_success "Web 应用构建完成"
            ;;
        3)
            print_step "构建所有平台..."
            ./gradlew :composeApp:assembleDebug
            ./gradlew :composeApp:wasmJsBrowserDistribution
            print_success "所有平台构建完成"
            ;;
        4)
            print_warning "跳过前端构建"
            ;;
        *)
            print_error "无效选择"
            return 1
            ;;
    esac
}

# 启动后端服务
start_backend() {
    print_step "启动后端服务..."

    if lsof -i :8080 > /dev/null 2>&1; then
        print_warning "端口 8080 已被占用"
        echo -n "是否停止现有服务并重启？(y/N): "
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            pkill -f "backend:run"
            sleep 2
        else
            return 0
        fi
    fi

    ./gradlew :backend:run > /tmp/backend.log 2>&1 &

    # 等待服务启动
    echo -n "等待后端服务启动"
    for i in {1..30}; do
        if curl -s http://localhost:8080/api/v1/posts > /dev/null 2>&1; then
            echo ""
            print_success "后端服务已启动"
            return 0
        fi
        echo -n "."
        sleep 1
    done

    echo ""
    print_error "后端服务启动超时"
    print_warning "查看日志: tail -f /tmp/backend.log"
    return 1
}

# 创建测试数据
create_test_data() {
    print_step "创建测试数据..."

    echo -n "是否创建测试数据？(y/N): "
    read -r response

    if [[ "$response" =~ ^[Yy]$ ]]; then
        # 登录获取 token
        TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
            -H "Content-Type: application/json" \
            -d '{"email":"admin@blog.com","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

        if [ -z "$TOKEN" ]; then
            print_error "登录失败，无法创建测试数据"
            return 1
        fi

        # 创建 3 篇测试文章
        for i in {1..3}; do
            curl -s -X POST http://localhost:8080/api/v1/admin/posts \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $TOKEN" \
                -d "{
                    \"title\": \"测试文章 $i\",
                    \"slug\": \"test-post-$i\",
                    \"summary\": \"这是测试文章 $i 的摘要\",
                    \"content\": \"# 测试文章 $i\n\n这是测试文章的内容。\n\n## 章节 1\n\n一些内容...\",
                    \"coverImageUrl\": \"https://picsum.photos/800/400?random=$i\",
                    \"published\": true
                }" > /dev/null
        done

        print_success "测试数据创建完成"
    else
        print_warning "跳过测试数据创建"
    fi
}

# 显示部署信息
show_deployment_info() {
    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                                                            ║"
    echo "║                  🎉 部署完成！                             ║"
    echo "║                                                            ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    echo "📊 服务状态"
    echo "────────────────────────────────────────────────────────────"
    lsof -i :5432 > /dev/null 2>&1 && echo "  ✓ PostgreSQL: 运行中 (端口 5432)" || echo "  ✗ PostgreSQL: 未运行"
    lsof -i :8080 > /dev/null 2>&1 && echo "  ✓ 后端服务: 运行中 (端口 8080)" || echo "  ✗ 后端服务: 未运行"
    echo ""
    echo "🌐 访问地址"
    echo "────────────────────────────────────────────────────────────"
    echo "  后端 API:        http://localhost:8080/api/v1"
    echo "  Android 模拟器:  http://10.0.2.2:8080"
    echo ""
    echo "🔐 测试账号"
    echo "────────────────────────────────────────────────────────────"
    echo "  邮箱: admin@blog.com"
    echo "  密码: admin123"
    echo "  角色: ADMIN"
    echo ""
    echo "🚀 下一步"
    echo "────────────────────────────────────────────────────────────"
    echo "  1. 安装 Android 应用:"
    echo "     ./gradlew :composeApp:installDebug"
    echo ""
    echo "  2. 运行 Web 开发模式:"
    echo "     ./gradlew :composeApp:wasmJsBrowserDevelopmentRun"
    echo ""
    echo "  3. 查看系统状态:"
    echo "     ./status.sh"
    echo ""
    echo "  4. 运行 API 测试:"
    echo "     ./test-api.sh"
    echo ""
    echo "  5. 管理数据库:"
    echo "     ./db-manager.sh"
    echo ""
    echo "  6. 查看后端日志:"
    echo "     tail -f /tmp/backend.log"
    echo ""
    echo "📚 文档"
    echo "────────────────────────────────────────────────────────────"
    echo "  README.md      - 项目主文档"
    echo "  DEV_GUIDE.md   - 开发指南"
    echo "  SUMMARY.md     - 项目总结"
    echo ""
    echo "🌟 祝你使用愉快！"
    echo ""
}

# 主函数
main() {
    # 检查系统要求
    if ! check_requirements; then
        print_error "系统要求检查失败，请安装缺少的工具"
        exit 1
    fi

    echo ""

    # 配置环境变量
    setup_env || exit 1

    echo ""

    # 启动 PostgreSQL
    start_postgresql || exit 1

    echo ""

    # 初始化数据库
    init_database || exit 1

    echo ""

    # 构建后端
    build_backend || exit 1

    echo ""

    # 构建前端
    build_frontend

    echo ""

    # 启动后端服务
    start_backend || exit 1

    echo ""

    # 创建测试数据
    create_test_data

    # 显示部署信息
    show_deployment_info
}

# 运行主函数
main
