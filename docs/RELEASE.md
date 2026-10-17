# التحقق من الإصدار 1.1.0 · Release 1.1.0 verification

17 أكتوبر 2026 · 17 October 2026

## ملف التطبيق · App artifact

ملف التطبيق الحالي الوحيد في المستودع · The repository's single current APK: [ChargeAlert.apk](../ChargeAlert.apk).

- versionName: 1.1.0
- versionCode: 2
- نوع البناء · Build type: Debug للتجربة، موقّع بمفتاح التطوير المحلي · Debug test build, signed with the local development key.
- applicationId: `com.aistudio.chargealert.btrchk` — محفوظ لتوافق هوية التطبيق · Preserved to retain app identity.
- SHA-256: `84cd42e92991dd4b3cd2fb994dcf3513c321e2ba135a9c9fd3b0ed7f777df588`

عنوان ووصف الإصدار باللغتين جاهزان للنسخ في [بيانات GitHub Release](RELEASE_NOTES.md).

A bilingual title and description are ready to copy from [GitHub Release details](RELEASE_NOTES.md).

## العربية

### ما اكتمل

1. ضبط المراقبة والإشعارات والإيقاف والغفوة، واعتماد قراءة أندرويد.
2. تطوير عشرة أنماط وإضافة عشرة أخرى، مع معرض ومعاينة كاملة.
3. تبسيط هالة النيون: قوس مضيء للحالي وخط أبيض للهدف؛ النبضة لا تحرك موضع الشحن.
4. توضيح سبب توقف الحركة في توفير الطاقة أو إعدادات المستخدم أو توقف الشحن.
5. توحيد الشعار والأيقونة، وإضافة نسخة أحادية لأندرويد.
6. README بالشعار وبيانات المطور، دون إضافة صفحة index.
7. إزالة ربط Firebase وAI غير المستخدم والمسار المحلي الثابت لـJava، وتقليص قائمة المكتبات إلى المستخدمة.
8. ملف APK موحد واستبعاد النسخ القديمة والكاش وبيانات الجهاز من Git.
9. GitHub Actions للاختبارات وLint والبناء.

### نتائج التحقق

نجح الأمر: `testDebugUnitTest lintDebug assembleDebug` محليًا، ونجح سير العمل على [GitHub Actions](https://github.com/ahmadibrahim4geo/ChargeAlert/actions/runs/39014827413).

- 36 اختبارًا ناجحًا.
- Android Lint: صفر أخطاء و71 تحذيرًا، تتضمن اقتراحات أسلوبية وتحديثات مكتبات وإعدادات قائمة.
- مراجعة صور الأنماط العشرين والمؤشر والأيقونات.
- مقارنة لقطتين للنبضة بفاصل 500ms: تغيرت 12,854 بكسل داخل منطقة نهاية القوس فقط؛ ظلت النسبة 76% والهدف 80%.
- لم يُقَس استهلاك الطاقة أو تُختبر دورة شحن على هاتف فعلي ضمن هذه الجلسة.

## English

### Completed work

1. Refined monitoring, notifications, dismissal, and snooze behavior using Android battery readings.
2. Redesigned ten themes and added ten more, with a gallery and full-screen preview.
3. Simplified Neon Halo: a filled arc for current charge and a white target tick; the pulse does not advance the charge position.
4. Explained paused motion caused by Battery Saver, user settings, or paused charging.
5. Unified the logo and launcher icon, including an Android monochrome version.
6. Added a logo-led README and developer contact details without creating an index page.
7. Removed unused Firebase and AI configuration and a machine-specific Java path, and trimmed the dependency catalog to used libraries.
8. Kept one current APK and excluded old builds, caches, and machine-specific data from Git.
9. Added GitHub Actions for tests, Lint, and builds.

### Validation results

`testDebugUnitTest lintDebug assembleDebug` passed locally, and the workflow succeeded on [GitHub Actions](https://github.com/ahmadibrahim4geo/ChargeAlert/actions/runs/39014827413).

- 36 passing tests.
- Android Lint: zero errors and 71 warnings, including style suggestions, dependency updates, and existing configuration warnings.
- Visually reviewed all twenty designs, the charge indicator, and the icons.
- Compared two pulse screenshots 500ms apart: 12,854 pixels changed only around the arc endpoint; charge stayed at 76% and the target at 80%.
- Actual power consumption and a full charging cycle were not tested on a physical phone in this session.

## بيانات المطور · Developer details

اسم المطور وبريده من بطاقة المطور في ملف `Create FlowArrow Tool/index.html`.

The developer name and email were taken from the developer card in `Create FlowArrow Tool/index.html`.

**أحمد إبراهيم · Ahmad Ibrahim** — [ahmadibrahim.geo@gmail.com](mailto:ahmadibrahim.geo@gmail.com).
