package com.personalblog.app.data.remote

expect val defaultApiBaseUrl: String

object ApiConfig {
    // 统一的 API 地址配置，可在编译时或运行时修改
    // 开发环境：http://localhost:9191 或 http://10.0.2.2:9191 (Android 模拟器)
    // 生产环境：https://your-domain.com
    var apiBaseUrl: String = defaultApiBaseUrl
}
