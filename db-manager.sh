#!/bin/bash

# Personal Blog 数据库管理脚本

DB_NAME="personalblog"
PSQL="/opt/homebrew/opt/postgresql@15/bin/psql"

show_menu() {
    echo ""
    echo "=== Personal Blog 数据库管理 ==="
    echo ""
    echo "1. 查看数据库状态"
    echo "2. 查看所有文章"
    echo "3. 查看所有用户"
    echo "4. 查看所有标签"
    echo "5. 创建测试数据"
    echo "6. 清空所有数据"
    echo "7. 备份数据库"
    echo "8. 恢复数据库"
    echo "9. 连接数据库 (psql)"
    echo "0. 退出"
    echo ""
    echo -n "请选择操作 [0-9]: "
}

view_status() {
    echo ""
    echo "📊 数据库状态"
    echo "================================"
    $PSQL $DB_NAME -c "
        SELECT
            '文章总数' as 项目, COUNT(*)::text as 数量 FROM posts
        UNION ALL
        SELECT '已发布文章', COUNT(*)::text FROM posts WHERE published = true
        UNION ALL
        SELECT '用户总数', COUNT(*)::text FROM users
        UNION ALL
        SELECT '标签总数', COUNT(*)::text FROM tags
        UNION ALL
        SELECT '评论总数', COUNT(*)::text FROM comments;
    "
}

view_posts() {
    echo ""
    echo "📝 文章列表"
    echo "================================"
    $PSQL $DB_NAME -c "
        SELECT
            id,
            LEFT(title, 40) as 标题,
            slug,
            CASE WHEN published THEN '✓' ELSE '✗' END as 已发布,
            TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI') as 创建时间
        FROM posts
        ORDER BY created_at DESC;
    "
}

view_users() {
    echo ""
    echo "👤 用户列表"
    echo "================================"
    $PSQL $DB_NAME -c "
        SELECT
            id,
            username as 用户名,
            email as 邮箱,
            role as 角色,
            TO_CHAR(created_at, 'YYYY-MM-DD') as 注册日期
        FROM users
        ORDER BY created_at DESC;
    "
}

view_tags() {
    echo ""
    echo "🏷️  标签列表"
    echo "================================"
    $PSQL $DB_NAME -c "
        SELECT
            t.id,
            t.name as 标签名,
            t.slug,
            COUNT(pt.post_id) as 文章数
        FROM tags t
        LEFT JOIN post_tags pt ON t.id = pt.tag_id
        GROUP BY t.id, t.name, t.slug
        ORDER BY COUNT(pt.post_id) DESC;
    "
}

create_test_data() {
    echo ""
    echo "🔧 创建测试数据..."

    # 获取管理员 token
    TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
        -H "Content-Type: application/json" \
        -d '{"email":"admin@blog.com","password":"admin123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

    if [ -z "$TOKEN" ]; then
        echo "❌ 登录失败，请确保后端服务正在运行"
        return
    fi

    echo "✓ 登录成功"

    # 创建文章
    for i in {1..5}; do
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
            }" > /dev/null && echo "✓ 创建文章 $i"
    done

    echo ""
    echo "✅ 测试数据创建完成"
}

clear_all_data() {
    echo ""
    echo "⚠️  警告：此操作将清空所有数据！"
    echo -n "确认清空？(yes/no): "
    read confirm

    if [ "$confirm" = "yes" ]; then
        echo "清空数据中..."
        $PSQL $DB_NAME -c "TRUNCATE posts, post_tags, tags, comments, users CASCADE;"
        echo "✅ 数据已清空"
    else
        echo "操作已取消"
    fi
}

backup_database() {
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    echo ""
    echo "📦 备份数据库到: $BACKUP_FILE"
    /opt/homebrew/opt/postgresql@15/bin/pg_dump $DB_NAME > "$BACKUP_FILE"
    echo "✅ 备份完成"
}

restore_database() {
    echo ""
    echo "📂 可用的备份文件："
    ls -1 backup_*.sql 2>/dev/null || echo "没有找到备份文件"
    echo ""
    echo -n "请输入备份文件名: "
    read backup_file

    if [ -f "$backup_file" ]; then
        echo "恢复数据库中..."
        $PSQL $DB_NAME < "$backup_file"
        echo "✅ 恢复完成"
    else
        echo "❌ 文件不存在"
    fi
}

connect_database() {
    echo ""
    echo "连接到数据库..."
    echo "提示：输入 \\q 退出"
    echo ""
    $PSQL $DB_NAME
}

# 主循环
while true; do
    show_menu
    read choice

    case $choice in
        1) view_status ;;
        2) view_posts ;;
        3) view_users ;;
        4) view_tags ;;
        5) create_test_data ;;
        6) clear_all_data ;;
        7) backup_database ;;
        8) restore_database ;;
        9) connect_database ;;
        0) echo "再见！"; exit 0 ;;
        *) echo "无效选择，请重试" ;;
    esac

    echo ""
    echo -n "按 Enter 继续..."
    read
done
