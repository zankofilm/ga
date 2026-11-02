# مدرسه‌یار اولیا — Android Native / Direct API

نسخه 1.0.0 اولین سورس مستقل اپلیکیشن اولیا است. این پروژه **WebView ندارد** و هیچ صفحه‌ای از پنل وب را داخل Android نمایش نمی‌دهد. UI با Jetpack Compose ساخته شده و داده‌ها مستقیماً از APIهای رسمی پنل اولیا دریافت می‌شوند.

## قابلیت‌ها

- ورود جدید: کد ملی ولی + رمز عبور
- ورود حساب قدیمی: کد ملی دانش‌آموز + کد دسترسی
- تغییر اجباری رمز اولیه و تغییر رمز از تنظیمات
- قفل اختیاری اثر انگشت؛ پس از فعال‌سازی، هر بار باز شدن اپ بیومتریک درخواست می‌شود
- پشتیبانی از چند فرزند و انتخاب فرزند در بالای اپ
- خانه و مرکز اقدام خانواده
- مشخصات و پرونده فرزند با عکس واقعی
- کارنامه‌های رسمی، نمرات و تحلیل درسی
- گزارش جامع آزمون‌ها
- حضور و غیاب روزانه
- تکالیف و وضعیت تحویل
- برنامه هفتگی، رویدادها و کلاس آنلاین
- پیام‌های مستقیم مدرسه
- درخواست ملاقات، پاسخ دعوت ملاقات و درخواست زمان دیگر
- درخواست غیرحضوری آموزشی/اداری/رفاهی
- رضایت‌نامه‌های مدرسه
- اعلان‌های فرزند
- نشست‌های فعال و بستن نشست‌ها

## APIهای اصلی

- `POST /api/guardian/login`
- `POST /api/guardian/change-password`
- `POST /api/guardian/logout`
- `GET /api/guardian/dashboard`
- `GET /api/guardian/student-dossier`
- `GET /api/guardian/student-analysis`
- `GET /api/guardian/exam-report/t06`
- `GET /api/guardian/attendance-day`
- `POST /api/guardian/notifications/read`
- `POST /api/guardian/meeting-request`
- `POST /api/guardian/contact-request`
- `POST /api/guardian/meeting-invitations/respond`
- `POST /api/guardian/consents/respond`
- `GET /api/guardian/sessions`
- `POST /api/guardian/sessions/revoke`
- `POST /api/guardian/sessions/revoke-others`

## Build

ریپو را در GitHub قرار دهید و Workflow با نام **Build Madreseyar Guardian APK** را اجرا کنید. خروجی `app-debug.apk` به‌عنوان Artifact آپلود می‌شود.

پیش‌فرض API: `https://scooljavanrood.ir/app/`
