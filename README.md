<div align="center">
  <img src="assets/branding/charge-alert.png" width="144" height="144" alt="شعار تنبيه الشحن · Charge Alert logo" />

  <h1>Charge Alert · تنبيه الشحن</h1>

  <p>نسبة شحن واضحة. تنبيه في الوقت المناسب. عشرون مظهرًا على ذوقك.</p>
  <p>Clear battery readings, a charge-target alarm, and 20 charging designs.</p>

  <p>
    <a href="https://github.com/ahmadibrahim4geo/ChargeAlert/raw/refs/heads/main/ChargeAlert.apk"><strong>⬇ تحميل التطبيق · Download APK</strong></a>
    &nbsp;·&nbsp;
    <a href="#build">البناء · Build</a>
    &nbsp;·&nbsp;
    <a href="#developer">المطور · Developer</a>
  </p>

  <p>الإصدار · Version 1.1.0 · Android 7.0+ · Kotlin · Jetpack Compose · MIT</p>
</div>

## التطبيق · About

### العربية

يقرأ Charge Alert نسبة البطارية التي يبلغ بها أندرويد، وينبّهك عند الوصول لهدف تختاره بين **50% و100%**. يميّز مؤشر الشحن بين:

- **القوس المضيء:** شحنك الحالي، والرقم في الوسط هو نسبة البطارية الفعلية.
- **الخط الأبيض:** النسبة التي تريد الوصول إليها.
- **النبضة:** الشحن نشط؛ مكانها لا يتقدم إلا مع تغير قراءة البطارية.

عند إيقاف الحركة أو تشغيل توفير الطاقة يوضح التطبيق سبب ثبات العرض.

### English

Charge Alert reads the battery percentage reported by Android and alerts you when it reaches a target you choose between **50% and 100%**. The charging indicator separates three things:

- **Filled arc:** your current charge; the number in the center is the actual battery percentage.
- **White tick:** your selected charge target.
- **Pulse:** charging is active; its position only advances when the battery reading changes.

When motion is disabled or Battery Saver is enabled, the app explains why the display is still.

## المميزات · Features

### العربية

- 20 تصميم شحن، منها الزجاج المنشوري والدوائر الذكية ومدارات البلازما والقمر والمحيط والكريستال والغروب.
- معرض مصغّرات ومعاينة كاملة، مع دعم العربية والإنجليزية.
- إغلاق شاشة الشحن يُبقي المراقبة فعالة؛ إيقاف تنبيه الهدف يمنع تكراره في نفس الجلسة.
- تكرار اختياري كل خمس دقائق وغفوة محفوظة. يتوقف صوت التنبيه واهتزازه بعد دقيقة.
- قراءات نسبة الشحن والحرارة والجهد والتيار، دون افتراض سعة بطارية وهمية.
- خدمة مراقبة تعتمد على تحديثات أندرويد، ومهمة استعادة واحدة في الخلفية.
- الاحتفاظ بالتصميم المختار عند تقليل الحركة، وأيقونة جديدة تدعم أيقونات أندرويد الأحادية.

### English

- 20 charging designs, including Prism Glass, Circuit Studio, Plasma Orbits, Lunar Eclipse, Ocean Depth, Crystal Facets, and Sunset Horizon.
- A thumbnail gallery and full-screen preview, with Arabic and English support.
- Closing the charging screen keeps monitoring active; dismissing the target alarm prevents it from repeating in the same charging session.
- Optional repeats every five minutes and a saved snooze. Alarm sound and vibration stop after one minute.
- Battery percentage, temperature, voltage, and current readings, without assuming a made-up battery capacity.
- A monitoring service driven by Android battery updates, with one background recovery task.
- The selected design stays visible with reduced motion, and the new icon supports Android monochrome icons.

## معاينات · Screenshots

الصور التالية ناتجة من اختبارات الواجهة ببيانات توضيحية.

These screenshots come from UI tests using illustrative data.

<p align="center">
  <img src="app/src/test/screenshots/charging-ar.png" width="270" alt="مؤشر الشحن بالعربية · Arabic indicator showing current charge and target" />
  <img src="app/src/test/screenshots/appearance/minimal_glass.png" width="270" alt="الزجاج المنشوري · Prism Glass charging design" />
  <img src="app/src/test/screenshots/appearance/matrix_pulse.png" width="270" alt="الدوائر الذكية · Circuit Studio charging design" />
</p>

<details>
<summary>عرض التصميمات العشرين · View all 20 designs</summary>

![التصميمات العشرون · All 20 charging designs](app/src/test/screenshots/appearance/all-20-designs.png)

</details>

## تحميل وتثبيت · Download and installation

[**تحميل الإصدار 1.1.0 · Download version 1.1.0**](https://github.com/ahmadibrahim4geo/ChargeAlert/raw/refs/heads/main/ChargeAlert.apk)

### العربية

يوجد **ملف APK واحد فقط** في المستودع: [ChargeAlert.apk](ChargeAlert.apk). يُستبدل عند تحديث التطبيق بدل الاحتفاظ بنسخ متكررة. يتطلب أندرويد 7.0 أو أحدث.

هذا بناء **Release موقّع بمفتاح إصدار مخصص**، متاح حاليًا للتجربة قبل إتمام اختبارات الهاتف الفعلي. يستخدم مفتاحًا مختلفًا عن نسخة Debug السابقة، لذلك لا يمكن تثبيته كتحديث فوقها. احفظ إعداداتك قبل إزالة النسخة السابقة عند ظهور تعارض توقيع. ستستخدم تحديثات Release القادمة مفتاح الإصدار نفسه.

### English

The repository contains **one current APK**: [ChargeAlert.apk](ChargeAlert.apk). It is replaced when the app is updated, rather than keeping duplicate builds. Android 7.0 or later is required.

This is a **Release build signed with a dedicated release key**, currently available for testing before physical-device validation is complete. Its key differs from the previous Debug build, so it cannot update that installation. Save your settings before removing the previous copy if Android reports a signature conflict. Future Release updates must use the same release key.

<a id="build"></a>

## البناء من المصدر · Build from source

### العربية

المتطلبات: **JDK 17** و**Android SDK 36**. الحد الأدنى API 24 والهدف API 35. تعمل اختبارات Robolectric على API 34 مع Java 17.

### English

Requirements: **JDK 17** and **Android SDK 36**. The minimum API is 24 and the target is API 35. Robolectric tests run on API 34 with Java 17.

```bash
git clone https://github.com/ahmadibrahim4geo/ChargeAlert.git
cd ChargeAlert
./gradlew testDebugUnitTest lintDebug assembleDebug
```

على Windows · On Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

### إعداد البناء · Build setup

**العربية**

- حدّد SDK محليًا من Android Studio أو في `local.properties`؛ هذا الملف مستثنى من Git.
- لا يحتاج التطبيق Firebase أو ملف `google-services.json` أو مفاتيح API.
- ناتج البناء: `app/build/outputs/apk/debug/app-debug.apk`.
- لنسخة release موقعّة: اضبط `KEYSTORE_PATH` و`STORE_PASSWORD` و`KEY_ALIAS` و`KEY_PASSWORD` محليًا، ثم شغّل `assembleRelease`. بدونها يكون بناء release غير موقع.
- لا ترفع ملفات التوقيع أو كلمات المرور للمستودع.
- لبناء وفحص نسخة Release موقعّة وتجهيز ملفات رفعها على Windows، استخدم `powershell -ExecutionPolicy Bypass -File scripts/Build-Release.ps1` بعد ضبط التوقيع محليًا؛ راجع [دليل التوقيع](docs/SIGNING.md).

**English**

- Set your SDK location through Android Studio or an ignored `local.properties` file.
- Firebase, `google-services.json`, and API keys are not required.
- Build output: `app/build/outputs/apk/debug/app-debug.apk`.
- For a signed release, set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` locally, then run `assembleRelease`. Without these settings, the release build is unsigned.
- Keep signing files and passwords out of the repository.
- To check, build, sign, and prepare Release upload files on Windows, run `powershell -ExecutionPolicy Bypass -File scripts/Build-Release.ps1` after configuring signing locally; see the [signing guide](docs/SIGNING.md).

لتسجيل لقطات الواجهة · To record UI screenshots:

```powershell
.\gradlew.bat testDebugUnitTest '-Proborazzi.test.record=true'
```

[GitHub Actions](.github/workflows/android.yml) يشغّل الاختبارات وLint والبناء عند الرفع وطلبات الدمج. نواتجه بناء تطوير، ويظل `ChargeAlert.apk` ملف التنزيل الموحد.

[GitHub Actions](.github/workflows/android.yml) runs tests, Lint, and the build on pushes and pull requests. Its APK artifacts are development builds; `ChargeAlert.apk` remains the single repository download.

## حدود التشغيل والقراءات · Behavior and reading limitations

### العربية

- قيود أندرويد والشركات المصنعة قد تؤخر بدء المراقبة من الخلفية. إذا رفض النظام بدء الخدمة يظهر إشعار لبدئها بتدخل المستخدم.
- الإيقاف الإجباري (Force Stop) يوقف التشغيل التلقائي حتى إعادة فتح التطبيق. العرض الكامل يخضع لصلاحيات النظام.
- النسبة قراءة أندرويد وليست قياسًا مستقلًا للسعة الكيميائية. القدرة المحسوبة تخص صافي البطارية ولا تمثل قدرة المحول.
- الوقت المتبقي تقدير مشتق من بيانات النظام، ويُخفى إذا لم تتوفر قراءة مناسبة.
- لم يُقَس استهلاك الطاقة الفعلي على هاتف ضمن هذه المراجعة؛ اختبارات الواجهة لا تقيسه.

### English

- Android and manufacturer restrictions may delay background monitoring. If Android blocks the service from starting, a notification lets the user start it.
- Force Stop prevents automatic operation until the app is reopened. Full-screen display depends on system permissions.
- The percentage is Android's reading, not an independent measurement of chemical battery capacity. Calculated power is net battery power, not the adapter's output.
- Remaining time is an estimate derived from system data and is hidden when a suitable reading is unavailable.
- Actual power consumption has not been measured on a phone during this review; UI tests do not measure it.

## الملفات والشروحات · Project files and documentation

| المسار · Path | المحتوى · Contents |
|---|---|
| `app/src/main/` | التطبيق وموارده · Android application and resources |
| `app/src/test/` | اختبارات الوحدة والتكامل والصور · Unit, integration, and screenshot tests |
| `assets/branding/` | الشعار بصيغتي SVG وPNG · SVG and PNG logo |
| `docs/` | المراجعة والمظهر والإصدار · Review, appearance, and release documentation |
| `scripts/` | أداة تصدير الشعار · Branding export helper |
| `.github/workflows/` | فحوصات أندرويد الآلية · Automated Android checks |
| `ChargeAlert.apk` | نسخة التجربة الحالية الوحيدة · Single current test build |

- [سجل التغييرات · Changelog](CHANGELOG.md)
- [المظهر · Appearance](docs/APPEARANCE.md)
- [تقرير المراجعة · Project review](docs/PROJECT_REVIEW.md)
- [تفاصيل التحقق من الإصدار · Release verification](docs/RELEASE.md)
- [حفظ مفتاح الإصدار والبناء الموقّع · Release key and signed build guide](docs/SIGNING.md)
- [عنوان ووصف GitHub Release الجاهزان للنسخ · Copy-ready GitHub Release title and description](docs/RELEASE_NOTES.md)
- [المساهمة · Contributing](CONTRIBUTING.md)

<a id="developer"></a>

## المطور · Developer

**أحمد إبراهيم — Ahmad Ibrahim**

- البريد · Email: [ahmadibrahim.geo@gmail.com](mailto:ahmadibrahim.geo@gmail.com)
- GitHub: [@ahmadibrahim4geo](https://github.com/ahmadibrahim4geo)

بيانات التواصل من بطاقة المطور في مشروع Create FlowArrow Tool. هذا المشروع تطبيق أندرويد؛ لا يحتوي على صفحة index أو موقع إضافي.

Contact details were taken from the developer card in Create FlowArrow Tool. This project is an Android app and does not include an index page or an additional website.

## الترخيص · License

المشروع متاح بموجب ترخيص MIT. · This project is available under the MIT License.

[MIT License](LICENSE) · Copyright © 2026 Ahmad Ibrahim.
