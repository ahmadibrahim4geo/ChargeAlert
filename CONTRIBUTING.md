# المساهمة في Charge Alert · Contributing to Charge Alert

## العربية

أبلغ عن المشاكل واقترح التغييرات عبر [GitHub Issues](https://github.com/ahmadibrahim4geo/ChargeAlert/issues). اذكر طراز الجهاز وإصدار أندرويد وإصدار التطبيق وخطوات إعادة المشكلة والسلوك المتوقع.

استخدم JDK 17 وAndroid SDK 36. ضع مسار SDK الخاص بجهازك في ملف `local.properties` المستثنى من Git، ولا تضف مفاتيح التوقيع أو بيانات الدخول.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

عند تغيير الواجهات، سجّل لقطات الشاشة وافحصها بصريًا:

```bash
./gradlew testDebugUnitTest -Proborazzi.test.record=true
```

على Windows استخدم `.\gradlew.bat` بدلًا من `./gradlew`.

حافظ على سهولة استخدام العربية من اليمين لليسار والإنجليزية من اليسار لليمين على الشاشات الصغيرة. يجب أن تأتي القراءات من أندرويد وألا تختلق الحركة تقدمًا في نسبة الشحن. عند تعديل المراقبة، اختبر الإغلاق وفصل الشاحن وإعادة توصيله والاستعادة من الخلفية.

افتح طلب دمج إلى `main` مع وصف التغيير والتحقق المنفذ. اكتب الشروحات الجديدة بالعربية والإنجليزية. استبعد مجلدات البناء الناتجة وإعدادات SDK الشخصية من Git. ملف `ChargeAlert.apk` في جذر المستودع هو نسخة التجربة الحالية الوحيدة؛ لا تُحدّثه إلا بعد نجاح الفحوصات.

## English

Report bugs and propose changes through [GitHub Issues](https://github.com/ahmadibrahim4geo/ChargeAlert/issues). Include the device model, Android version, app version, steps to reproduce, and expected behavior.

Use JDK 17 and Android SDK 36. Set your own SDK location in an ignored `local.properties` file; never add signing keys or credentials.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

For visual changes, record screenshots and inspect them:

```bash
./gradlew testDebugUnitTest -Proborazzi.test.record=true
```

On Windows, use `.\gradlew.bat` instead of `./gradlew`.

Keep Arabic RTL and English LTR usable on small screens. Readings must come from Android; animation must not invent battery progress. Test dismissal, reconnecting, and background recovery when changing monitoring behavior.

Open a pull request against `main` describing the change and validation. Write new documentation in both Arabic and English. Keep generated build directories and personal SDK settings out of Git. The root `ChargeAlert.apk` is the single current test build; update it only after the checks pass.

## التواصل · Contact

[أحمد إبراهيم · Ahmad Ibrahim](mailto:ahmadibrahim.geo@gmail.com)
