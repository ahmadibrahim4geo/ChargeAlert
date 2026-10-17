# توقيع الإصدار · Release signing

## العربية

يستخدم الإصدار 1.1.0 مفتاح Release مخصصًا، محفوظًا محليًا في `signing/chargealert-release.keystore`. إعداداته وكلمة مروره محفوظة في `.env.signing`. الملفان مستثنيان من Git ولا يدخلان في مرفقات الإصدار.

احتفظ بنسخة احتياطية آمنة من **الملفين معًا** خارج جهاز التطوير. تحتاج تحديثات APK القادمة إلى المفتاح نفسه وكلمة مروره. لا تنشئ مفتاحًا بديلًا لكل تحديث.

مفتاح Release مختلف عن مفتاح Debug السابق؛ لا يمكن تثبيت هذا الإصدار كتحديث فوق نسخة Debug. احفظ إعداداتك قبل إزالة النسخة السابقة إذا ظهر تعارض توقيع.

لتشغيل الاختبارات وفحص Release وبنائه والتحقق من توقيعه وتجهيز ملفات الرفع على Windows:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/Build-Release.ps1
```

أضف `-Offline` عند توفر الاعتماديات في الكاش. يقرأ السكربت إعدادات التوقيع المحلية دون طباعتها، ثم يعيد قيم متغيرات البيئة السابقة بعد الانتهاء. ينتج `ChargeAlert.apk` في جذر المشروع، وملفات الرفع داخل `build/release-upload/`: التطبيق، وبصمة SHA-256، وعنوان الإصدار ووصفه بالعربية والإنجليزية. حدّث وصف الإصدار في `docs/RELEASE_NOTES.md` عند إصدار نسخة جديدة.

في جهاز آخر يمكن ضبط متغيرات `KEYSTORE_PATH` و`STORE_PASSWORD` و`KEY_ALIAS` و`KEY_PASSWORD` محليًا بدل الملف. يجب توفير مفتاح الإصدار نفسه؛ أسرار التوقيع لا تُرفع إلى المستودع.

## English

Version 1.1.0 uses a dedicated Release key stored locally at `signing/chargealert-release.keystore`. Its settings and password are saved in `.env.signing`. Both files are excluded from Git and from release attachments.

Keep a secure backup of **both files together** outside the development computer. Future APK updates require the same key and its password. Do not generate a replacement key for each update.

The Release key differs from the previous Debug key, so this build cannot update a Debug installation. Save your settings before removing the previous copy if Android reports a signature conflict.

To run tests, check and build Release, verify its signature, and prepare upload files on Windows:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/Build-Release.ps1
```

Add `-Offline` when dependencies are cached. The script reads local signing settings without printing them and restores previous environment-variable values when it finishes. It produces `ChargeAlert.apk` at the project root and upload files in `build/release-upload/`: the APK, its SHA-256 checksum, and the bilingual release title and description. Update `docs/RELEASE_NOTES.md` when preparing a new version.

On another machine, set `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` locally instead of using the file. Supply the same Release key; signing secrets must stay out of the repository.
