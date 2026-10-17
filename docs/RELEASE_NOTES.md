# بيانات GitHub Release · GitHub Release details

| الحقل · Field | القيمة · Value |
|---|---|
| Tag | `v1.1.0` |
| Target | `main` |
| Title | Charge Alert v1.1.0 — مظهر جديد وتنبيهات أوضح / New Designs & Clearer Alerts |
| Attachment | [ChargeAlert.apk](../ChargeAlert.apk) |
| Pre-release | نعم، لحين إتمام اختبارات الهاتف الفعلي · Yes, pending physical-device validation |

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

هذا بناء **Release موقّع بمفتاح إصدار مخصص**، ومتاح كتجربة أولية لحين إتمام اختبارات الهاتف الفعلي. مفتاحه مختلف عن نسخة Debug السابقة، لذلك لا يمكن تثبيته كتحديث فوقها؛ احفظ إعداداتك قبل إزالة النسخة السابقة إذا ظهر تعارض توقيع. لم تُختبر دورة شحن أو يُقَس استهلاك الطاقة على هاتف فعلي بعد؛ ويظل بدء المراقبة والعرض الكامل خاضعين لقيود أندرويد وصلاحياته.

**التحقق**

اجتاز المشروع **36 اختبارًا** محليًا، ونجح فحص Android Lint الخاص بـRelease والبناء. نتيجة Lint لـRelease: صفر أخطاء و54 تحذيرًا. يجري التحقق من توقيع ملف APK قبل تجهيزه للرفع. كما نجحت فحوصات نسخة Debug السابقة على [GitHub Actions](https://github.com/ahmadibrahim4geo/ChargeAlert/actions/runs/39014827413).

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

This is a **Release build signed with a dedicated release key**, offered as a pre-release pending physical-device validation. Its key differs from the previous Debug build, so it cannot update that installation; save your settings before removing the previous copy if Android reports a signature conflict. A full charging cycle and actual power consumption have not yet been tested on a physical phone; background monitoring and full-screen display remain subject to Android restrictions and permissions.

**Validation**

The project passed **36 tests** locally, along with Release Android Lint and the build. Release Lint reported zero errors and 54 warnings. The APK signature is verified before preparing the upload files. The previous Debug checks also succeeded on [GitHub Actions](https://github.com/ahmadibrahim4geo/ChargeAlert/actions/runs/39014827413).

---

- **المطور · Developer:** أحمد إبراهيم · Ahmad Ibrahim
- **البريد · Email:** [ahmadibrahim.geo@gmail.com](mailto:ahmadibrahim.geo@gmail.com)
- **GitHub:** [@ahmadibrahim4geo](https://github.com/ahmadibrahim4geo)

<!-- RELEASE_BODY_END -->
