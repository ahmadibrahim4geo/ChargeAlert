# تحديث مظهر الشحن · Charging appearance update

## العربية — 17 أكتوبر 2026

أُعيد تصميم العشرة أنماط السابقة مع الاحتفاظ بمعرفاتها المحفوظة، وأُضيفت عشرة أنماط جديدة. المتاح الآن 20 نمطاً.

## العشرة المطورة

| النمط | التغيير |
|---|---|
| هالة النيون | قوس واحد للشحن الحالي وعلامة بيضاء منفصلة للهدف، ونبضة أثناء الشحن |
| الزجاج المنشوري | طبقات زجاجية متداخلة وانعكاسات لؤلؤية |
| المدّ البنفسجي | مستوى سائل يرتبط بنسبة البطارية |
| نواة الطاقة | هندسة سداسية وحلقات شحن مقسمة |
| الدوائر الذكية | بديل الكود الرقمي: مسارات إلكترونية وشريحة مركزية واضحة |
| أعمدة الطاقة | أعمدة متدرجة تعكس نسبة الشحن |
| ساعة منتصف الليل | ساعة فعلية مع نسبة الشحن ومؤشر دقيق |
| مدارات البلازما | مدارات بيضاوية متقاطعة |
| ستائر الشفق | شرائط ضوء خضراء وبنفسجية |
| التاج الشمسي | أشعة دقيقة وحلقة دافئة |

## العشرة الجديدة

خسوف القمر، عمق المحيط، حرير وردي، الميناء الذهبي، استوديو أبيض، أوجه الكريستال، زهرة الضوء، خطوط التضاريس، مسار المذنب، أفق الغروب.

## تجربة الاختيار والمعاينة

- معرض بعمودين من المصغرات، مع تصنيفات: ضوء وطاقة، من الطبيعة، هادئ وأنيق.
- شارة «جديد» للأنماط المضافة وتحديد واضح للنمط المختار.
- معاينة كبيرة للنمط المختار وزر لفتح شاشة الشحن كاملة.
- خلفية الشاشة وألوان الأزرار والمؤشرات تتبع النمط.
- المعاينة الكاملة توضح أنها معاينة مظهر، وتستخدم قراءة بطارية الجهاز الفعلية. صور المعرض تستخدم بيانات توضيحية معلنة.
- المصغرات ثابتة، والحركة تعمل في النمط المعروض فقط. الأنماط الثابتة لا تنشئ ساعة رسوم متحركة.
- تقليل الحركة وتوفير الطاقة يحافظان على التصميم المختار مع إيقاف الحركة.

## التحقق

في مرحلة تحديث المظهر نجحت الاختبارات الـ32 وبناء Debug وفحص Android Lint (صفر أخطاء، 91 تحذيراً). لاحقًا اجتاز الإصدار النهائي 1.1.0 عدد 36 اختبارًا، مع صفر أخطاء و71 تحذيرًا؛ راجع [التحقق من الإصدار](RELEASE.md).
تغطي الإضافات عرض الأنماط العشرين في توفير الطاقة، اختيار النمط وتحديث المعاينة، فتح المعاينة الكاملة، والاحتفاظ بالاختيارات القديمة.
تمت مراجعة الصور المجمعة وصور الواجهة، وضبط مركز الدوران والتدرجات لتتطابق المصغرات والشاشات عالية الكثافة.

- [معاينة العشرين تصميماً](../app/src/test/screenshots/appearance/all-20-designs.png)
- [معاينة معرض الاختيار](../app/src/test/screenshots/appearance/gallery.png)
- [الزجاج المنشوري](../app/src/test/screenshots/appearance/minimal_glass.png)
- [الدوائر الذكية](../app/src/test/screenshots/appearance/matrix_pulse.png)

نسخة التجربة الحالية الموحدة: [ChargeAlert.apk](../ChargeAlert.apk)، الإصدار 1.1.0. الصور ناتجة من اختبارات Compose ببيانات توضيحية؛ لم يتم قياس أداء الرسوم أو استهلاك الطاقة على هاتف فعلي.

---

## English

17 October 2026

All ten previous designs were redesigned while retaining their saved identifiers, and ten new designs were added. There are now 20 designs.

### The ten redesigned themes

| Theme | Change |
|---|---|
| Neon Halo | One current-charge arc, a separate white target tick, and a pulse during active charging |
| Prism Glass | Layered glass panels with pearlescent reflections |
| Violet Tide | A liquid level tied to battery percentage |
| Energy Core | Hexagonal geometry and segmented charging rings |
| Circuit Studio | Replaces Digital Code with circuit paths and a clear central chip |
| Energy Columns | Graduated columns that reflect charge level |
| Midnight Clock | The actual time, battery percentage, and a precise indicator |
| Plasma Orbits | Intersecting elliptical orbits |
| Aurora Curtains | Green and violet ribbons of light |
| Solar Corona | Fine rays and a warm ring |

### The ten new themes

Lunar Eclipse, Ocean Depth, Rose Silk, Golden Dial, Mono Studio, Crystal Facets, Light Bloom, Contour Lines, Comet Trail, and Sunset Horizon.

### Selection and preview experience

- A two-column thumbnail gallery grouped into Light & Energy, Nature, and Calm & Elegant collections.
- A New badge for added designs and a clear selection indicator.
- A large preview of the selected design, with a button to open the full charging screen.
- Screen backgrounds, button colors, and indicators follow the chosen theme.
- The full-screen preview identifies itself as a design preview and uses actual device battery readings. Gallery images use explicitly labeled illustrative data.
- Thumbnails are static; only the displayed animated design runs motion. Static designs do not create an animation clock.
- Reduced motion and Battery Saver retain the selected design while stopping motion.

### Validation

At the appearance-update milestone, 32 tests, the Debug build, and Android Lint passed, with zero Lint errors and 91 warnings. The final 1.1.0 validation subsequently passed 36 tests, with zero Lint errors and 71 warnings; see [release verification](RELEASE.md).

Coverage includes all twenty designs in Battery Saver, selecting a theme and updating its preview, opening the full-screen preview, and preserving previously saved selections. The contact sheet and UI images were visually reviewed. Rotation centers and gradients were adjusted so that thumbnails match high-density screens.

- [All 20 designs](../app/src/test/screenshots/appearance/all-20-designs.png)
- [Theme gallery](../app/src/test/screenshots/appearance/gallery.png)
- [Prism Glass](../app/src/test/screenshots/appearance/minimal_glass.png)
- [Circuit Studio](../app/src/test/screenshots/appearance/matrix_pulse.png)

Single current test build: [ChargeAlert.apk](../ChargeAlert.apk), version 1.1.0. Images come from Compose tests using illustrative data. Animation performance and power consumption were not measured on a physical phone.
