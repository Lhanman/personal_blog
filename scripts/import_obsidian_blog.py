#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import sys
from dataclasses import dataclass
from pathlib import Path
from urllib import error, request


DEFAULT_SOURCE_DIR = "/Users/lhanboyy/workbench/lhan_doc/个人项目/博客"
DEFAULT_BASE_URL = "http://localhost:9191"
DEFAULT_ADMIN_EMAIL = "admin@blog.com"
DEFAULT_ADMIN_PASSWORD = "admin123"

SLUG_OVERRIDES = {
    "00 - 项目概览": "personal-blog-project-overview",
    "01 - 后端架构": "backend-architecture",
    "02 - 前端架构": "frontend-architecture",
    "03 - 数据库设计": "database-design",
    "04 - API 文档": "api-documentation",
    "05 - 使用手册": "user-manual",
    "06 - 开发指南": "development-guide",
    "07 - 部署指南": "deployment-guide",
    "08 - 故障排除": "troubleshooting-guide",
}

SUMMARY_OVERRIDES = {
    "00 - 项目概览": "Kotlin Multiplatform 个人博客项目的整体介绍、技术栈与模块结构。",
    "01 - 后端架构": "后端服务的技术栈、启动流程、目录结构与安全机制说明。",
    "02 - 前端架构": "前端跨平台架构、模块划分、状态管理与页面组织说明。",
    "03 - 数据库设计": "博客系统的表结构、索引设计与 Flyway 迁移约定。",
    "04 - API 文档": "公开接口、认证接口与管理员接口的请求说明与示例。",
    "05 - 使用手册": "本地使用、登录管理、发布文章与常见操作说明。",
    "06 - 开发指南": "项目构建、调试、测试与协作开发规范。",
    "07 - 部署指南": "本地开发、Docker 部署、环境变量与生产环境注意事项。",
    "08 - 故障排除": "后端、数据库、客户端与构建问题的排查方法汇总。",
}


@dataclass
class NotePost:
    source_name: str
    title: str
    slug: str
    summary: str
    content: str


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Import Obsidian markdown notes into personal blog posts.")
    parser.add_argument("--source-dir", default=DEFAULT_SOURCE_DIR, help="Directory containing markdown notes")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL, help="Backend base URL")
    parser.add_argument("--email", default=DEFAULT_ADMIN_EMAIL, help="Admin email")
    parser.add_argument("--password", default=DEFAULT_ADMIN_PASSWORD, help="Admin password")
    parser.add_argument("--dry-run", action="store_true", help="Only print the import plan")
    parser.add_argument("--update-existing", action="store_true", help="Update existing posts when slug already exists")
    return parser.parse_args()


def api_json(method: str, url: str, payload: dict | None = None, token: str | None = None) -> dict:
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = request.Request(url, data=data, method=method, headers=headers)
    with request.urlopen(req) as response:
        body = response.read().decode("utf-8")
        return json.loads(body) if body else {}


def login(base_url: str, email: str, password: str) -> str:
    payload = {"email": email, "password": password}
    response = api_json("POST", f"{base_url}/api/v1/auth/login", payload)
    return response["token"]


def get_existing_posts(base_url: str) -> dict[str, int]:
    page = api_json("GET", f"{base_url}/api/v1/posts?page=1&size=100")
    return {item["slug"]: item["id"] for item in page.get("items", [])}


def extract_title(markdown: str, fallback: str) -> str:
    for line in markdown.splitlines():
        stripped = line.strip()
        if stripped.startswith("# "):
            return stripped[2:].strip()
    return fallback


def extract_summary(markdown: str, fallback: str) -> str:
    lines = [line.strip() for line in markdown.splitlines()]
    for line in lines:
        if not line or line.startswith("#") or line.startswith("```"):
            continue
        if line.startswith("|") or line.startswith("-") or line.startswith("`"):
            continue
        return line[:140]
    return fallback


def build_posts(source_dir: Path) -> list[NotePost]:
    posts: list[NotePost] = []
    for path in sorted(source_dir.glob("*.md")):
        stem = path.stem
        if not stem or stem == ".md":
            continue
        markdown = path.read_text(encoding="utf-8").strip()
        title = extract_title(markdown, stem)
        slug = SLUG_OVERRIDES.get(stem)
        if not slug:
            raise ValueError(f"Missing slug override for {stem}")
        summary = SUMMARY_OVERRIDES.get(stem) or extract_summary(markdown, title)
        content = f"> 来源：Obsidian / 个人项目 / 博客 / {path.name}\n\n{markdown}\n"
        posts.append(NotePost(stem, title, slug, summary, content))
    return posts


def post_payload(post: NotePost) -> dict:
    return {
        "title": post.title,
        "slug": post.slug,
        "summary": post.summary,
        "content": post.content,
        "coverImageUrl": None,
        "tagIds": [],
        "published": True,
    }


def create_post(base_url: str, token: str, post: NotePost) -> dict:
    return api_json("POST", f"{base_url}/api/v1/admin/posts", post_payload(post), token)


def update_post(base_url: str, token: str, post_id: int, post: NotePost) -> dict:
    return api_json("PUT", f"{base_url}/api/v1/admin/posts/{post_id}", post_payload(post), token)


def main() -> int:
    args = parse_args()
    source_dir = Path(args.source_dir)
    if not source_dir.exists():
        print(f"Source directory not found: {source_dir}", file=sys.stderr)
        return 1

    posts = build_posts(source_dir)
    print(f"Discovered {len(posts)} markdown notes in {source_dir}")
    for post in posts:
        print(f"- {post.slug} <- {post.source_name}")

    if args.dry_run:
        return 0

    try:
        token = login(args.base_url, args.email, args.password)
        existing_posts = get_existing_posts(args.base_url)
    except error.HTTPError as exc:
        details = exc.read().decode("utf-8", errors="ignore")
        print(f"Failed to connect to backend: HTTP {exc.code} {details}", file=sys.stderr)
        return 1
    except Exception as exc:
        print(f"Failed to connect to backend: {exc}", file=sys.stderr)
        return 1

    created = 0
    updated = 0
    skipped = 0
    for post in posts:
        post_id = existing_posts.get(post.slug)
        if post_id is not None and not args.update_existing:
            skipped += 1
            print(f"SKIP   {post.slug} (already exists)")
            continue
        try:
            if post_id is not None:
                response = update_post(args.base_url, token, post_id, post)
                updated += 1
                print(f"UPDATE {post.slug} -> id={response.get('id')}")
            else:
                response = create_post(args.base_url, token, post)
                created += 1
                print(f"CREATE {post.slug} -> id={response.get('id')}")
        except error.HTTPError as exc:
            details = exc.read().decode("utf-8", errors="ignore")
            print(f"ERROR {post.slug} -> HTTP {exc.code} {details}", file=sys.stderr)
        except Exception as exc:
            print(f"ERROR {post.slug} -> {exc}", file=sys.stderr)

    print(f"Done. created={created}, updated={updated}, skipped={skipped}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
