# YouTube Volume Fix · إصلاح صوت YouTube · YouTube Ses Düzeltme

> 🤲 **صدقة جارية عن روح المرحوم أحمد خليل.** نرجو أن تقرأ الفاتحة على روحه.
> *Sadaqah jariyah for the soul of the late Ahmed Khalil. Please recite Al-Fatiha for him.*
> *Merhum Ahmed Halil'in ruhu için sadaka-i câriye. Lütfen ruhuna bir Fatiha okuyun.*

[العربية](#العربية) · [English](#english) · [Türkçe](#türkçe)

**Download:** [`releases/yt-volume-fix-v1.0.apk`](releases/yt-volume-fix-v1.0.apk) (25 KB)
SHA-256: `ad5106003c5b6b4493916aad64943c899307ad8b37807543323220924e5ec987`

---

## العربية

### ما المشكلة؟
في بعض شاشات Android TV (خصوصاً الشاشات العامة بلوحات MStar وCultraview)، **لا تعمل أزرار الصوت في الريموت داخل تطبيق YouTube**، مع أنها تعمل في كل مكان آخر.

السبب أن نظام التلفزيون يمرّر أزرار الصوت إلى التطبيق المفتوح بدل أن يعالجها بنفسه، وتطبيق YouTube الحديث يستقبلها ولا يفعل بها شيئاً.

### ماذا يفعل التطبيق؟
عندما يكون YouTube مفتوحاً، يلتقط التطبيق أزرار **رفع الصوت وخفضه وكتمه** ويغيّر صوت التلفزيون بنفسه.
- يعمل مع YouTube وYouTube Kids وYouTube Music للتلفزيون.
- **لا يلمس أي زر آخر، ولا أي تطبيق آخر.**
- لا يقرأ ما على الشاشة، ولا يجمع أي بيانات، ولا يحتاج الإنترنت.
- مجاني تماماً، بدون إعلانات، ومفتوح المصدر.

### التثبيت
1. نزّل ملف `yt-volume-fix-v1.0.apk` وثبّته على التلفزيون، عن طريق تطبيق **Downloader** أو فلاشة USB أو ADB:
   ```
   adb install yt-volume-fix-v1.0.apk
   ```
2. افتح التطبيق **"إصلاح صوت YouTube"**.
3. اضغط **"التفعيل من إعدادات إمكانية الوصول"** ثم **متابعة**.
4. اختر **"أزرار الصوت في YouTube"**، ثم **تمكين**، ثم **موافق**.
5. ارجع إلى التطبيق، يجب أن ترى: ✅ **مفعّل**.

> ⚠️ **عن رسالة التحذير:** عند التفعيل يعرض Android رسالة عامة تقول إن الخدمة "يمكنها جمع كل النص الذي تكتبه". **هذه الرسالة تظهر لأي تطبيق من هذا النوع**، وهي ليست خاصة بنا. تطبيقنا لا يقرأ أي نص، والكود كله منشور هنا لمن أراد التأكد.

### إذا لم تجد إعدادات إمكانية الوصول
بعض التلفزيونات تخفيها. يمكنك التفعيل مرة واحدة من الكمبيوتر:
```
adb shell settings put secure enabled_accessibility_services app.ytvolumefix/app.ytvolumefix.VolumeKeyService
adb shell settings put secure accessibility_enabled 1
```
(إذا كانت لديك خدمات إمكانية وصول أخرى مفعّلة، فأضف خدمتنا إلى القائمة بفاصل `:` بدل استبدالها.)

### ملاحظات
- **جُرّب على:** شاشة بلوحة MStar/Cultraview بنظام Android 8.0.
- **قد لا يعمل** على تلفزيونات تتحكم بالصوت بطريقة خاصة بالشركة المصنّعة (عبر HDMI-CEC أو مكبّر صوت خارجي). إن جرّبته، أخبرنا بالنتيجة في [Issues](../../issues).

---

## English

### The problem
On some Android TVs (especially generic TVs built on MStar / Cultraview boards), **the remote's volume buttons do nothing inside the YouTube app**, while they work everywhere else.

The TV's system passes the volume keys to the open app instead of handling them itself, and the current YouTube app receives them and ignores them.

### What this app does
While YouTube is open, it catches the **volume up, volume down and mute** buttons and changes the TV volume itself.
- Works with YouTube, YouTube Kids and YouTube Music for TV.
- **Leaves every other button and every other app alone.**
- Doesn't read the screen, collect any data, or use the internet.
- Free, no ads, open source.

### Install
1. Download `yt-volume-fix-v1.0.apk` and install it on the TV (with the **Downloader** app, a USB stick, or ADB):
   ```
   adb install yt-volume-fix-v1.0.apk
   ```
2. Open **"YouTube Volume Fix"**.
3. Select **"Enable in Accessibility settings"**, then **Continue**.
4. Choose **"Volume keys in YouTube"**, then **Enable**, then **OK**.
5. Go back to the app. You should see ✅ **Enabled**.

> ⚠️ **About the warning:** When you enable it, Android shows a generic message saying the service "can collect all text you type". **Android shows this for every accessibility app.** This app doesn't read any text, and all of its code is published here.

### If your TV has no Accessibility settings
Some TVs hide them. Enable it once from a computer:
```
adb shell settings put secure enabled_accessibility_services app.ytvolumefix/app.ytvolumefix.VolumeKeyService
adb shell settings put secure accessibility_enabled 1
```
(If other accessibility services are already enabled, append ours to the list with `:` instead of replacing it.)

### Notes
- **Tested on:** an MStar / Cultraview TV running Android 8.0.
- **May not work** on TVs whose maker controls volume in its own way (HDMI-CEC, external sound systems). If you try it, please report the result in [Issues](../../issues).

---

## Türkçe

### Sorun
Bazı Android TV'lerde (özellikle MStar / Cultraview kartlı TV'lerde) **kumandanın ses tuşları YouTube uygulamasında çalışmaz**, ama her yerde çalışır.

TV sistemi ses tuşlarını kendisi işlemek yerine açık uygulamaya iletir. Güncel YouTube uygulaması bu tuşları alır ve yok sayar.

### Uygulama ne yapar?
YouTube açıkken **ses açma, ses kısma ve sessiz** tuşlarını yakalar ve TV sesini kendisi değiştirir.
- YouTube, YouTube Kids ve YouTube Music (TV) ile çalışır.
- **Diğer tuşlara ve uygulamalara dokunmaz.**
- Ekranı okumaz, veri toplamaz, internet kullanmaz.
- Ücretsiz, reklamsız, açık kaynak.

### Kurulum
1. `yt-volume-fix-v1.0.apk` dosyasını indirip TV'ye kurun (**Downloader** uygulaması, USB bellek veya ADB ile):
   ```
   adb install yt-volume-fix-v1.0.apk
   ```
2. **"YouTube Ses Düzeltme"** uygulamasını açın.
3. **"Erişilebilirlik ayarlarından etkinleştir"** ve ardından **Devam**'ı seçin.
4. **"YouTube'da ses tuşları"** → **Etkinleştir** → **Tamam**.
5. Uygulamaya dönün: ✅ **Etkin** yazısını görmelisiniz.

> ⚠️ **Uyarı mesajı hakkında:** Etkinleştirirken Android, hizmetin "yazdığınız tüm metinleri toplayabileceğini" söyleyen genel bir mesaj gösterir. **Bu mesaj tüm erişilebilirlik uygulamalarında çıkar.** Bu uygulama hiçbir metni okumaz ve tüm kodu burada yayınlanmıştır.

### TV'nizde Erişilebilirlik ayarları yoksa
Bazı TV'ler bu ayarları gizler. Bir bilgisayardan bir kez etkinleştirin:
```
adb shell settings put secure enabled_accessibility_services app.ytvolumefix/app.ytvolumefix.VolumeKeyService
adb shell settings put secure accessibility_enabled 1
```
(Başka erişilebilirlik hizmetleri açıksa, listeyi değiştirmek yerine bizimkini `:` ile ekleyin.)

### Notlar
- **Test edildi:** Android 8.0 çalışan MStar / Cultraview TV.
- Sesi üreticiye özel yöntemle kontrol eden TV'lerde (HDMI-CEC, harici ses sistemi) **çalışmayabilir**. Denerseniz sonucu [Issues](../../issues) bölümüne yazın.

---

## Build from source

No Gradle needed. Requires JDK 8+, Android SDK Build-Tools 30.0.3 and the Android 8.0 (API 26) platform:
```
bash build.sh <build-tools dir> <android.jar> <jdk bin dir>
```
The signing key (`*.keystore`) is not in the repository. Keep your own key safe: updates must be signed with the same key.

## License · الرخصة · Lisans

[GNU GPL v3](LICENSE), with one additional term (section 7b): any copy or modified version must keep the dedication to the late Ahmed Khalil visible in the app, and credit this project with a link. See [NOTICE](NOTICE).

**Request:** If you build on this code, please also keep the message asking users to recite Al-Fatiha for his soul.

**الرخصة:** [GNU GPL v3](LICENSE): يحق لك استخدام الكود وتعديله ونشره، بشرط أن تنشر كودك أنت أيضاً بالرخصة نفسها، وأن **تُبقي الإهداء إلى روح المرحوم أحمد خليل ظاهراً في التطبيق** مع رابط هذا المشروع.
**ونرجو** ممن يستخدم الكود أن يُبقي أيضاً رسالة قراءة الفاتحة على روحه. جزاكم الله خيراً.

**Lisans:** [GNU GPL v3](LICENSE): kodu kullanabilir, değiştirebilir ve yayabilirsiniz; ancak kendi kodunuzu da aynı lisansla yayınlamalı ve **merhum Ahmed Halil'e yapılan ithafı** bu projenin bağlantısıyla birlikte uygulamada görünür tutmalısınız.
**Ricamız:** Fatiha okunmasını isteyen mesajı da korumanızdır. Allah razı olsun.
