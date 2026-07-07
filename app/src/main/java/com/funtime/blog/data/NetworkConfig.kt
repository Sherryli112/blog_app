package com.funtime.blog.data

object NetworkConfig {
    // 官網 Next.js 內建的公開代理路由，Server 端已內建 Strapi Token，App 不需要帶任何 Token
    const val BASE_URL = "https://www.funtime.com.tw/api/proxy"
    // CMS 上傳檔案的公開對外入口，圖片走這個網址（/api/proxy 對所有路徑會補 /api 前綴，圖片路徑不能套用）
    const val IMAGE_BASE_URL = "https://upd-api.funtime.com.tw"
}
