#!/bin/bash

# Personal Blog API 测试脚本

if [ -f .env.local ]; then
    set -a
    . ./.env.local
    set +a
elif [ -f .env ]; then
    set -a
    . ./.env
    set +a
fi

BASE_URL="${API_BASE_URL:-http://localhost:${BACKEND_PORT:-9191}}"
TOKEN=""
USER_TOKEN=""
CREATED_POST_ID=""
CREATED_USER_ID=""
CREATED_COMMENT_ID=""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✓${NC} $1"; }
print_error()   { echo -e "${RED}✗${NC} $1"; }
print_info()    { echo -e "${YELLOW}ℹ${NC} $1"; }
print_skip()    { echo -e "${CYAN}⊘${NC} $1 (跳过)"; }

print_header() {
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  $1"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

check_status() {
    local response="$1"
    local expected_field="$2"
    echo "$response" | grep -q "\"$expected_field\""
}

# ─── 1. 连接检查 ────────────────────────────────────────────────────────────────
test_connection() {
    print_header "1. API 连接检查"
    if curl -sf "$BASE_URL/api/v1/posts" > /dev/null 2>&1; then
        print_success "API 服务连接正常 ($BASE_URL)"
        return 0
    else
        print_error "API 服务连接失败，请确保后端已启动"
        print_info "启动命令: ./gradlew :backend:run"
        exit 1
    fi
}

# ─── 2. 认证：注册 ──────────────────────────────────────────────────────────────
test_register() {
    print_header "2. POST /api/v1/auth/register — 用户注册"

    RANDOM_EMAIL="testuser_$(date +%s)@example.com"
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"testuser\",\"email\":\"$RANDOM_EMAIL\",\"password\":\"test123\"}")

    if check_status "$RESPONSE" "token"; then
        USER_TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        CREATED_USER_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        print_success "注册成功 (email: $RANDOM_EMAIL, userId: $CREATED_USER_ID)"
        return 0
    else
        print_error "注册失败: $RESPONSE"
        return 1
    fi
}

# 重复注册应返回 409
test_register_duplicate() {
    print_header "2b. POST /api/v1/auth/register — 重复邮箱应返回 409"

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","email":"admin@blog.com","password":"admin123"}')

    if [ "$HTTP_CODE" = "409" ]; then
        print_success "重复注册正确返回 409 Conflict"
        return 0
    else
        print_error "期望 409，实际返回 $HTTP_CODE"
        return 1
    fi
}

# ─── 3. 认证：登录 ──────────────────────────────────────────────────────────────
test_login() {
    print_header "3. POST /api/v1/auth/login — 管理员登录"

    RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"email":"admin@blog.com","password":"admin123"}')

    TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

    if [ -n "$TOKEN" ]; then
        print_success "登录成功，Token: ${TOKEN:0:40}..."
        return 0
    else
        print_error "登录失败: $RESPONSE"
        return 1
    fi
}

test_login_invalid() {
    print_header "3b. POST /api/v1/auth/login — 错误密码应返回 401"

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"email":"admin@blog.com","password":"wrongpassword"}')

    if [ "$HTTP_CODE" = "401" ]; then
        print_success "错误密码正确返回 401 Unauthorized"
        return 0
    else
        print_error "期望 401，实际返回 $HTTP_CODE"
        return 1
    fi
}

# ─── 4. 文章：公开接口 ──────────────────────────────────────────────────────────
test_get_posts() {
    print_header "4. GET /api/v1/posts — 获取文章列表"

    RESPONSE=$(curl -s "$BASE_URL/api/v1/posts?page=1&size=5")

    if check_status "$RESPONSE" "total"; then
        TOTAL=$(echo "$RESPONSE" | grep -o '"total":[0-9]*' | cut -d':' -f2)
        print_success "获取成功，文章总数: $TOTAL"
        return 0
    else
        print_error "获取失败: $RESPONSE"
        return 1
    fi
}

test_search_posts() {
    print_header "5. GET /api/v1/posts/search — 搜索文章"

    RESPONSE=$(curl -s "$BASE_URL/api/v1/posts/search?q=Kotlin&page=1&size=5")

    if check_status "$RESPONSE" "items"; then
        print_success "搜索接口正常"
        return 0
    else
        print_error "搜索失败: $RESPONSE"
        return 1
    fi
}

test_search_missing_q() {
    print_header "5b. GET /api/v1/posts/search — 缺少 q 参数应返回 400"

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/posts/search")

    if [ "$HTTP_CODE" = "400" ]; then
        print_success "缺少参数正确返回 400 Bad Request"
        return 0
    else
        print_error "期望 400，实际返回 $HTTP_CODE"
        return 1
    fi
}

test_get_post_not_found() {
    print_header "6. GET /api/v1/posts/{id} — 不存在的文章应返回 404"

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/posts/999999")

    if [ "$HTTP_CODE" = "404" ]; then
        print_success "不存在文章正确返回 404 Not Found"
        return 0
    else
        print_error "期望 404，实际返回 $HTTP_CODE"
        return 1
    fi
}

main() {
    test_connection
    test_register
    test_register_duplicate
    test_login
    test_login_invalid
    test_get_posts
    test_search_posts
    test_search_missing_q
    test_get_post_not_found
}

main "$@"
