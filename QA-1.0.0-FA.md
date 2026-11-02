# QA نسخه 1.0.0 اپ اولیا

- WebView: صفر مورد در سورس اجرایی.
- همه endpointهای `/api/guardian/...` استفاده‌شده در اپ با `server.mjs` سورس هاست تطبیق داده شدند.
- XMLهای Android معتبر هستند.
- Workflow GitHub Actions از نظر YAML معتبر است.
- Kotlin parser روی چهار فایل سورس بدون خطای نحوی اجرا شد.
- تست‌های HTTP موجود سورس هاست برای Guardian login/session/family با موفقیت اجرا شدند.
- Build کامل Android در این محیط اجرا نشده چون Android Gradle dependencies/SDK کامل محلی موجود نیست؛ Workflow GitHub برای Build آماده است.
