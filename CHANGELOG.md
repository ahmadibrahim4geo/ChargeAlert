# سجل التغييرات · Changelog

يُوثّق هذا الملف التغييرات البارزة في **Charge Alert (تنبيه الشحن)** بالعربية والإنجليزية. يتبع تنسيق [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) ونظام [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

All notable changes to the **Charge Alert (تنبيه الشحن)** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-10-17

### العربية

**تغييرات**

- توضيح المؤشر: قوس للشحن الحالي، وعلامة بيضاء منفصلة للهدف، ونبضة للشحن النشط، مع تفسير توقف الحركة وإزالة رسوم التقدم المكررة.
- استبدال الأيقونة برمز أبسط للبطارية والبرق، مع نسخ تقليدية وتكيفية وأحادية.
- تجهيز المشروع للبناء على GitHub، بملف APK حالي واحد وREADME بالشعار وبيانات المطور.
- إزالة إعدادات Firebase وAI غير المستخدمة ومسار Java الخاص بجهاز التطوير.
- إعادة تصميم الأنماط العشرة، ومنها الزجاج المنشوري والدوائر الذكية، مع الاحتفاظ بمعرفات الاختيارات المحفوظة.
- استبدال قائمة الأنماط النصية بمعرض مصغّرات وتصنيفات ومعاينة كاملة.
- ربط خلفية شاشة الشحن وألوانها بالنمط المختار.
- الاحتفاظ بالنمط في تقليل الحركة وتوفير الطاقة؛ الأنماط الثابتة لا تشغّل ساعة رسوم متحركة.

**إضافات**

- عشرة أنماط: خسوف القمر، عمق المحيط، حرير وردي، الميناء الذهبي، استوديو أبيض، أوجه الكريستال، زهرة الضوء، خطوط التضاريس، مسار المذنب، وأفق الغروب.
- اختبارات مرئية للأنماط العشرين واختيار المعرض والتوافق مع الاختيارات المحفوظة سابقًا.

### English

### Changed
- Clarified the charge indicator: one current-charge arc, a separate white target tick and an active-charging pulse. Explained paused motion and removed duplicate progress graphics.
- Replaced the launcher artwork with a simpler battery-and-bolt mark, including legacy, adaptive and monochrome icons.
- Prepared a portable GitHub project with one current APK, a logo-led README and developer contact details.
- Removed unused Firebase/AI generator configuration and a machine-specific Java path.
- Rebuilt all ten charging designs, including Prism Glass and Circuit Studio, while retaining saved theme IDs.
- Replaced the text-only theme picker with a thumbnail gallery, collections and a full-screen preview.
- Matched the charging screen background and accents to the selected theme.
- Preserved the chosen design in reduced-motion and Battery Saver modes. Static designs do not run an animation clock.

### Added
- Ten designs: Lunar Eclipse, Ocean Depth, Rose Silk, Golden Dial, Mono Studio, Crystal Facets, Light Bloom, Contour Lines, Comet Trail and Sunset Horizon.
- Visual coverage for all twenty designs, gallery selection and compatibility with previously saved selections.

## [1.0.0] - 2026-07-25
<!-- Original release notes below describe the previous implementation. -->

هذه ملاحظات الإصدار الأصلي وتصف التنفيذ السابق؛ راجع 1.1.0 و[تقرير المراجعة](docs/PROJECT_REVIEW.md) للسلوك الحالي.

These original release notes describe the previous implementation; see 1.1.0 and the [project review](docs/PROJECT_REVIEW.md) for current behavior.

### العربية

**إضافات الإصدار الأصلي**

- مراقبة نسبة البطارية والتنبيه عند بلوغ هدف يختاره المستخدم بين 50% و100%.
- نظام تنبيهات يتيح اختيار قناة المنبه أو الرنين أو الإشعار، مع خضوع الصوت لوضع الصامت وعدم الإزعاج وصلاحيات النظام.
- عشرة أنماط متحركة لشاشة الشحن باستخدام Canvas، مع جسيمات وإضاءة نيون وتدرجات وموجات وواجهات مستقبلية.
- إشعار ملء الشاشة لعرض واجهة التنبيه فوق شاشة القفل على Android 10+، وفق صلاحيات النظام.
- خدمة أمامية لمراقبة الشحن أثناء إغلاق واجهة التطبيق، ضمن قيود أندرويد.
- واجهة بالعربية من اليمين لليسار والإنجليزية من اليسار لليمين.
- عرض حرارة البطارية والجهد والحالة الصحية والتقنية الكيميائية.
- إدارة WakeLock لإيقاظ الشاشة عند توصيل الشاحن أو فصله في التنفيذ السابق.
- طلب استثناء تحسين البطارية لدعم تشغيل خدمة المراقبة في الخلفية.
- بطاقة حالة تعرض اتصال الخدمة والمراقبة والهدف ومصدر الشحن.

### English

### Added
- **Battery Charge Monitoring**: Monitor battery percentage in real-time and sound an alert when reaching user-selected target thresholds (50% - 100%).
- **Smart Alert System**: Selectable alarm, ringtone, or notification audio channels, subject to silent/Do-Not-Disturb settings and system permissions.
- **10 Premium Animated Lockscreen Themes**: High-performance, Canvas-driven charging screens featuring particle effects, neon glows, sweep gradients, wave ripples, and futuristic HUD displays.
- **Full-Screen Notification Intent**: Lockscreen alert activity for Android 10+ (API 29+), subject to system permissions.
- **Foreground Service Integration**: Background monitoring (`BatteryMonitoringService`) while the app interface is closed, subject to Android restrictions.
- **Bilingual Interface**: Full localization support for Arabic (RTL native support) and English (LTR).
- **Comprehensive Battery Health Metrics**: Real-time display of battery temperature (°C / °F), voltage (mV), health condition, and battery chemistry technology.
- **Smart WakeLock Management**: Automatically wakes up the screen upon charger plugin/unplug events.
- **Battery Optimization Exemption Request**: Built-in prompts to request battery optimization exclusion for uninterrupted background service execution across manufacturer OS skins.
- **Dynamic App Status Card**: Instant visual dashboard indicating service connection status, monitoring state, target percentage, and active charging source.
