# بيانات GitHub Release · GitHub Release details

| الحقل · Field | القيمة · Value |
|---|---|
| Tag | `v1.1.0` |
| Target | `main` |
| Title | Charge Alert v1.1.0 — مظهر جديد وتنبيهات أوضح / New Designs & Clearer Alerts |
| Attachment | [ChargeAlert.apk](../ChargeAlert.apk) |
| Pre-release | نعم، لأن المرفق بناء Debug للتجربة · Yes, because the attachment is a Debug test build |

انسخ الوصف بدءًا من عنوان «⚡ Charge Alert v1.1.0 · تنبيه الشحن» وحتى بيانات المطور في خانة وصف الإصدار على GitHub، وأرفق ملف التطبيق أعلاه.

Copy the description from the “⚡ Charge Alert v1.1.0 · تنبيه الشحن” heading through the developer details into the GitHub Release description, and attach the APK linked above.

<!-- RELEASE_BODY_START -->

## ⚡ Charge Alert v1.1.0 · تنبيه الشحن

### العربية

تحديث لتحسين تجربة الشحن، وتوضيح قراءة البطارية والهدف، وضبط سلوك التنبيهات.

**أبرز التغييرات**

- **20 مظهرًا للشحن:** تطوير الأنماط العشرة السابقة وإضافة عشرة جديدة، مع معرض مصغّرات ومعاينة كاملة.
- **مؤشر أسهل في القراءة:** القوس المضيء للشحن الحالي، والعلامة البيضاء للهدف، والنبضة لتوضيح أن الشحن نشط.
- **حركة مفهومة:** توضيح سبب ثبات المظهر عند توفير الطاقة أو تعطيل الحركة أو توقف الشحن.
- **ضبط التنبيهات:** إغلاق شاشة الشحن يُبقي المراقبة فعالة، وإيقاف تنبيه الهدف يمنع رجوعه في نفس جلسة الشحن.
- **تكرار وغفوة محفوظان:** تكرار اختياري كل خمس دقائق، مع انتهاء الصوت والاهتزاز تلقائيًا بعد دقيقة.
- **قراءات من أندرويد:** دون افتراض سعة بطارية وهمية، مع إخفاء البيانات غير المدعومة.
- **هوية محدثة:** أيقونة وشعار جديدان، مع دعم العربية والإنجليزية.

**التثبيت**

حمّل ملف **ChargeAlert.apk** من مرفقات هذا الإصدار. يتطلب **Android 7.0 أو أحدث**.

هذا إصدار تجريبي **Debug** موقّع بمفتاح تطوير محلي. التحديث فوق نسخة موجودة يتطلب تطابق مفتاح التوقيع. لم تُختبر دورة شحن أو يُقَس استهلاك الطاقة على هاتف فعلي بعد؛ ويظل بدء المراقبة والعرض الكامل خاضعين لقيود أندرويد وصلاحياته.

**التحقق**

اجتاز المشروع **36 اختبارًا**، ونجحت فحوصات الاختبارات وAndroid Lint والبناء على [GitHub Actions](https://github.com/ahmadibrahim4geo/ChargeAlert/actions/runs/39014827413). نتيجة Lint: صفر أخطاء و71 تحذيرًا.

---

### English

An update that improves the charging experience, makes battery readings and targets clearer, and refines alarm behavior.

**Highlights**

- **20 charging designs:** ten redesigned themes plus ten new ones, with a thumbnail gallery and full-screen preview.
- **A clearer indicator:** the filled arc shows current charge, the white tick marks the target, and a pulse indicates active charging.
- **Understandable motion:** the app explains when a design is still because of Battery Saver, disabled motion, or paused charging.
- **Refined alarms:** closing the charging screen keeps monitoring active; dismissing the target alarm prevents it from returning in the same charging session.
- **Saved repeat and snooze settings:** optional repeats every five minutes, with sound and vibration stopping automatically after one minute.
- **Android battery readings:** no assumed battery capacity; unsupported readings are hidden.
- **Updated identity:** a new icon and logo, with Arabic and English support.

**Installation**

Download **ChargeAlert.apk** from this release's attachments. Requires **Android 7.0 or later**.

This is a **Debug pre-release**, signed with a local development key. Updating an existing installation requires a matching signing key. A full charging cycle and actual power consumption have not yet been tested on a physical phone; background monitoring and full-screen display remain subject to Android restrictions and permissions.

**Validation**

The project passed **36 tests**, and the tests, Android Lint, and build checks succeeded on [GitHub Actions](https://github.com/ahmadibrahim4geo/ChargeAlert/actions/runs/39014827413). Lint reported zero errors and 71 warnings.

---

- **المطور · Developer:** أحمد إبراهيم · Ahmad Ibrahim
- **البريد · Email:** [ahmadibrahim.geo@gmail.com](mailto:ahmadibrahim.geo@gmail.com)
- **GitHub:** [@ahmadibrahim4geo](https://github.com/ahmadibrahim4geo)

<!-- RELEASE_BODY_END -->
