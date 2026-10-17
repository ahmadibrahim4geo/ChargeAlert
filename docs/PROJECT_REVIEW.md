# مراجعة Charge Alert · Charge Alert project review

## العربية — 17 أكتوبر 2026

## المشاكل التي ثبتت من الكود

| المشكلة | السبب السابق | التعديل |
|---|---|---|
| شاشة الشحن ترجع بعد إغلاقها | أربعة مسارات تفتح الشاشة: الخدمة، مستقبل الشاحن، Worker، JobService | الخدمة وحدها تدير العرض، مع علامة محفوظة لكل توصيلة. مهام الاستعادة لا تفتح واجهات. |
| رجوع التنبيه بعد إيقافه | repeatNotification يعيد التنبيه مع كل بث للبطارية، بلا فترة زمنية أو إقرار إيقاف مستقل | سياسة قابلة للاختبار: مرة لكل هدف في الجلسة، وتكرار اختياري كل خمس دقائق، والإيقاف يتغلب على التكرار. |
| الإشعار يقول إن المنبه يرن بعد إيقافه | استنتاج حالة المنبه من نسبة البطارية | حالة تنبيه مستقلة؛ الوصول للنسبة لا يعني وجود صوت نشط. |
| تكرار الشغل في الخلفية | JobService يعيد جدولة نفسه فور انتهاء العمل أثناء استمرار الشحن، بجانب WorkManager | حذف JobService وإلغاء مهمته القديمة، والإبقاء على مهمة استعادة دورية واحدة. |
| الغفوة تفقد حالتها | Handler تابع للواجهة يعيد فتح التنبيه بعد خمس دقائق | حفظ الهدف وموعد الغفوة؛ الخدمة تعيد تقييم القراءة الفعلية عند انتهاء المهلة. |
| تقديرات غير موثوقة | سعة مفترضة 5000mAh وتخمين وحدة التيار واستخدام القيمة المطلقة للتيار | التحويل من µA إلى mA طبقاً لـ BatteryManager، وإخفاء البيانات غير المدعومة، وتقدير الوقت من تقدير النظام فقط. |
| الشاشة تستهلك طاقة بلا نهاية | KEEP_SCREEN_ON مع WakeLock ساطع | الاعتماد على مهلة الشاشة العادية، وإيقاف تركيب الرسوم عند انتقال الشاشة للخلفية. |
| الحرارة/البطارية المنخفضة تكرر التنبيه | تنبيه جديد لكل تغير صغير في الحرارة أو النسبة | تنبيه واحد لكل تجاوز، وإعادة التسليح بعد التعافي بهامش درجتين/نقطتين. |

## السلوك المتفق عليه في التطبيق

- إغلاق شاشة الشحن قبل بلوغ الهدف يُبقي المراقبة فعالة.
- الوصول للهدف يعتمد على النسبة التي يبلغ بها أندرويد؛ هدف 100% لا ينطلق عند 99%.
- سحب تنبيه الهدف أو الضغط على إيقافه يمنع رجوعه في نفس جلسة الشحن.
- تغيير الهدف يعيد تسليح الهدف الجديد. فصل الشاحن يبدأ جلسة جديدة عند إعادة توصيله.
- اهتزاز وصوت التنبيه ينتهيان تلقائياً بعد دقيقة. التكرار الاختياري يكون كل خمس دقائق حتى الإيقاف.
- سحب إشعار المراقبة، عندما يسمح أندرويد بذلك، يمنع تحديثاته الروتينية من إظهاره مجدداً في نفس الجلسة. قد يعرض أندرويد إشعار خدمة الخلفية مجدداً إذا أعاد إنشاء العملية؛ لا يمكن ضمان إخفاء إشعار خدمة فعالة.
- زر «إيقاف هذه الجلسة» يوقف الخدمة حتى فصل الشاحن وإعادة توصيله، أو إعادة تفعيل المراقبة من التطبيق.
- صوت المنبه يحترم مستوى صوت المنبه وصلاحيات النظام، ولا يرفع صوت الجهاز تلقائياً.
- شاشة الشحن تدعم العربية والإنجليزية، التمرير، بيانات حقيقية، اختيار الثيم، وتقليل الحركة. توفير الطاقة يستخدم مؤشراً ثابتاً.
- المعاينات التي تستخدم بيانات توضيحية موضحة بذلك؛ اختبار المنبه لا يسجل الهدف الحقيقي على أنه تم إيقافه.

## بنية التنفيذ

- BatteryReader: القراءة الموحدة والتحقق من القيم.
- ChargeAlertPolicy: قرار تنبيه الهدف والتكرار والغفوة.
- ChargingSessionStore: حفظ حالة الجلسة والإيقاف والغفوة؛ مستثناة من النسخ الاحتياطي.
- BatteryMonitoringService: مالك دورة المراقبة والعرض والتنبيه.
- MonitoringController: تشغيل/إيقاف الخدمة ومراعاة قيود الخلفية.
- ChargingWorker: استعادة دورية غير فورية، دون إطلاق شاشة.
- NotificationHelper: القنوات، إجراءات الإشعارات، تشغيل الصوت والاهتزاز بمدة محدودة.

## حدود القراءة والتشغيل

النسبة قراءة نظام أندرويد وليست قياساً مستقلاً للسعة الكيميائية للبطارية. صافي قدرة البطارية المحسوب من تيارها وجهدها ليس قدرة الشاحن المكتوبة على المحول. زمن الوصول للهدف تقريب مشتق من تقدير النظام للوصول إلى 100%، وقد يتغير مع الحرارة والحمل وسرعة الشحن. لا يعرض التطبيق زمناً عندما لا يقدمه النظام.

Android 12+ قد يمنع بدء Foreground Service من الخلفية. عند حدوث ذلك يظهر إشعار لبدء المراقبة بتدخل المستخدم. WorkManager لا يضمن تشغيل المهمة لحظة توصيل الشاحن. وضع المراقبة الدائمة يساعد في الاكتشاف أثناء بقاء الخدمة فعالة، لكنه لا يتجاوز Force Stop أو قيود الشركات المصنعة. عرض ملء الشاشة يخضع لصلاحيات Android 14+؛ تمت إزالة محاولة تجاوز غياب الصلاحية بواسطة AlarmManager.

إذا ماتت العملية ثم فُصل الشاحن وأعيد توصيله بالكامل قبل تشغيلها مرة أخرى، فقد لا يصل حدث الفصل للتطبيق. لا يوفر أندرويد معرفاً مستقلاً لكل جلسة شحن يمكن الاعتماد عليه لاستنتاج هذه الحالة؛ يلزم اختبارها على الأجهزة المستهدفة.

## تحقق على هاتف قبل إصدار نسخة إنتاجية

لا يوجد هاتف أو محاكي متصل أثناء المراجعة. لم يتم قياس استهلاك الطاقة الفعلي.

1. اختر 80%: تحقق من عدم التنبيه عند 79% ومن التنبيه عند أول قراءة 80% أو أكثر.
2. أغلق شاشة الشحن عند 60%؛ انتظر تحديثات البطارية واستعادة الخلفية؛ يجب ألا تعود الشاشة، ويظل تنبيه 80% مسلحاً.
3. فعّل التكرار، أوقف تنبيه الهدف، وانتظر أكثر من خمس دقائق؛ يجب ألا يرجع. افصل الشاحن وأعد توصيله للتحقق من إعادة التسليح.
4. جرّب سحب إشعار الهدف، زر إيقاف الجلسة، الغفوة، دوران الشاشة، والعودة من الخلفية.
5. اختبر الشحن المتوقف مؤقتاً، اكتمال الشحن، وغياب بيانات التيار.
6. اختبر Android 8/12/14/15، رفض إذن الإشعارات، رفض ملء الشاشة، إعادة التشغيل، Force Stop، وأجهزة Samsung/Xiaomi/Oppo.
7. قارن استهلاك الطاقة خلال دورة شحن فعلية، مع الحركة وبدونها؛ اختبارات الكود لا تقيس توفير الطاقة.

مراجع أندرويد:
- [قيود تشغيل خدمات الخلفية](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- [BatteryManager ووحدات القياس](https://developer.android.com/reference/android/os/BatteryManager)
- [متطلبات ملء الشاشة في Android 14](https://developer.android.com/about/versions/14/behavior-changes-14)


## نتيجة التحقق المنفذة

هذه نتائج مرحلة إصلاح المراقبة الأولى. لاحقًا اجتاز الإصدار النهائي 1.1.0 عدد 36 اختبارًا، مع صفر أخطاء و71 تحذيرًا؛ راجع [التحقق من الإصدار](RELEASE.md).

- نجح الأمر: .\gradlew.bat testDebugUnitTest lintDebug assembleDebug --offline '-Proborazzi.test.record=true'
- 28 اختباراً ناجحاً: 12 لسياسة التنبيه والقراءة، 9 لتكامل الجلسة والخدمة، 4 للواجهة، و3 اختبارات المشروع الأخرى.
- Android Lint: صفر أخطاء، 91 تحذيراً. معظم التحذيرات تتعلق بإصدارات مكتبات متاحة، اقتراحات أسلوبية، موارد غير مستخدمة، وإعدادات موجودة مثل targetSdk وطلب استثناء تحسين البطارية. لم يتم تحديث كل المكتبات ضمن هذا الإصلاح.
- تمت مراجعة خمس لقطات بصرياً في app/src/test/screenshots: العربية، معاينة الإنجليزية، ثيم النيون، والشاشة الصغيرة قبل التمرير وبعده. اللقطات ناتجة من اختبارات ببيانات توضيحية، وليست من هاتف المستخدم.
- نسخة التجربة الحالية الموحدة: ChargeAlert.apk، الإصدار 1.1.0. نُقلت النسخ التجريبية السابقة إلى أرشيف محلي مستثنى من Git.
- حفظت مصادر app/src السابقة في build/review/original-src.zip، وفروق المصدر في build/review/source-changes.patch.
- أُعد مستودع Git محلي مرتبط بـ ahmadibrahim4geo/ChargeAlert، مع استبعاد بيانات الجهاز ومفاتيح التوقيع.
- لم تُختبر دورة شحن على جهاز، ولم يُقَس استهلاك البطارية الفعلي.

---

## English — 17 October 2026

### Problems confirmed in the code

| Problem | Previous cause | Change |
|---|---|---|
| The charging screen returned after dismissal | Four launch paths: service, charger receiver, Worker, and JobService | The service alone owns screen display, with a saved flag for each connection. Recovery tasks do not open screens. |
| The alarm returned after dismissal | repeatNotification retriggered on every battery broadcast without a time interval or separate dismissal state | A testable policy: once per target per session, optional repeats every five minutes, and dismissal takes precedence over repeats. |
| The notification claimed the alarm was ringing after it stopped | Alarm state was inferred from battery percentage | Alarm state is tracked separately; reaching the target does not imply active sound. |
| Duplicate background work | JobService immediately rescheduled itself while charging, alongside WorkManager | Removed JobService, canceled its old job, and retained one periodic recovery task. |
| Snooze state was lost | An activity-owned Handler reopened the alarm after five minutes | The target and snooze deadline are saved; the service checks the actual reading again after the deadline. |
| Unreliable estimates | Assumed 5000mAh capacity, guessed current units, and used absolute current | Convert µA to mA according to BatteryManager, hide unsupported data, and derive time estimates only from the system estimate. |
| The screen consumed power indefinitely | KEEP_SCREEN_ON and a bright WakeLock | Use the normal screen timeout and stop composing graphics when the screen goes into the background. |
| Temperature and low-battery alarms repeated | A new alarm for every small temperature or percentage change | One alert per threshold crossing, rearmed after recovery by two degrees or percentage points. |

### Intended app behavior

- Closing the charging screen before the target is reached keeps monitoring active.
- The target uses Android's reported percentage; a 100% target does not trigger at 99%.
- Swiping away or stopping the target alarm prevents it from returning in the same charging session.
- Changing the target arms the new target. Disconnecting and reconnecting starts a new session.
- Alarm sound and vibration stop automatically after one minute. Optional repeats occur every five minutes until dismissed.
- Where Android allows the monitoring notification to be swiped away, routine updates do not show it again during that session. Android may show a foreground-service notification again after recreating the process; hiding an active service notification cannot be guaranteed.
- Stop this session stops the service until the charger is disconnected and reconnected, or monitoring is re-enabled in the app.
- Alarm sound respects the alarm volume and system permissions; it does not automatically raise device volume.
- The charging screen supports Arabic and English, scrolling, real readings, theme selection, and reduced motion. Battery Saver uses a static indicator.
- Previews using illustrative data are labeled. Testing an alarm does not mark the real target as dismissed.

### Implementation structure

- `BatteryReader`: centralized reading and value validation.
- `ChargeAlertPolicy`: target alarm, repeat, and snooze decisions.
- `ChargingSessionStore`: persistent session, dismissal, and snooze state, excluded from backups.
- `BatteryMonitoringService`: owner of monitoring, screen display, and alarms.
- `MonitoringController`: starts and stops the service while respecting background restrictions.
- `ChargingWorker`: periodic, non-immediate recovery without launching a screen.
- `NotificationHelper`: channels, notification actions, and time-limited sound and vibration.

### Reading and operation limitations

Battery percentage comes from Android and is not an independent measurement of chemical battery capacity. Net battery power calculated from current and voltage is not the adapter's rated power. Time to the selected target is an approximation derived from the system estimate to 100%, and may change with temperature, device load, and charging speed. No time is shown when the system does not provide an estimate.

Android 12+ may prevent a foreground service from starting in the background. If that happens, a notification allows the user to start monitoring. WorkManager does not guarantee execution at the instant the charger is connected. Persistent monitoring helps detect changes while the service remains active, but does not override Force Stop or manufacturer restrictions. Full-screen display depends on Android 14+ permissions; the attempt to bypass missing permission through AlarmManager was removed.

If the process dies and the charger is fully disconnected and reconnected before the process restarts, the app may miss the disconnect event. Android does not provide a separate reliable charging-session identifier to infer that sequence. This needs testing on the target devices.

### Physical-device checks before a production release

No phone or emulator was connected during the review. Actual power consumption was not measured.

1. Select 80%: verify no alarm at 79% and an alarm at the first reading of 80% or higher.
2. Close the charging screen at 60%; wait for battery updates and background recovery. The screen should stay closed and the 80% alarm should remain armed.
3. Enable repeat, dismiss the target alarm, and wait more than five minutes; it should not return. Disconnect and reconnect to verify rearming.
4. Test swiping away the target notification, Stop this session, snooze, screen rotation, and returning from the background.
5. Test paused charging, a full battery, and unavailable current data.
6. Test Android 8/12/14/15, denied notification permission, denied full-screen permission, reboot, Force Stop, and Samsung/Xiaomi/Oppo devices.
7. Compare power use over an actual charging cycle with motion enabled and disabled; code tests do not measure energy savings.

Android references:

- [Background foreground-service start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- [BatteryManager and measurement units](https://developer.android.com/reference/android/os/BatteryManager)
- [Android 14 full-screen intent requirements](https://developer.android.com/about/versions/14/behavior-changes-14)

### Validation performed

These results describe the initial monitoring-fix milestone. Final 1.1.0 validation subsequently passed 36 tests with zero Lint errors and 71 warnings; see [release verification](RELEASE.md).

- Passed: `.\gradlew.bat testDebugUnitTest lintDebug assembleDebug --offline '-Proborazzi.test.record=true'`.
- 28 passing tests: 12 policy and reading tests, nine session and service integration tests, four UI tests, and three other project tests.
- Android Lint: zero errors and 91 warnings, mostly available dependency updates, style suggestions, unused resources, and existing settings such as targetSdk and the battery optimization exemption request. Not all dependencies were updated as part of this fix.
- Visually reviewed five screenshots in `app/src/test/screenshots`: Arabic, English preview, Neon theme, and the small screen before and after scrolling. Images came from tests using illustrative data, rather than the user's phone.
- The single current test build is [ChargeAlert.apk](../ChargeAlert.apk), version 1.1.0. Earlier test builds were moved to a local archive excluded from Git.
- Previous `app/src` sources were saved locally in `build/review/original-src.zip`, with source changes in `build/review/source-changes.patch`.
- A local Git repository was prepared and linked to `ahmadibrahim4geo/ChargeAlert`, excluding machine-specific data and signing keys.
- A physical charging cycle and actual battery consumption were not tested.
