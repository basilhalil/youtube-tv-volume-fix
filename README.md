# YouTube Volume Fix · إصلاح صوت YouTube · YouTube Ses Düzeltme

> 🤲 **صدقة جارية عن روح المرحوم أحمد خليل.** نرجو أن تقرأ الفاتحة على روحه.
> *Sadaqah jariyah for the soul of the late Ahmed Khalil. Please recite Al-Fatiha for him.*
> *Merhum Ahmed Halil'in ruhu için sadaka-i câriye. Lütfen ruhuna bir Fatiha okuyun.*

[العربية](#العربية) · [English](#english) · [Türkçe](#türkçe)

### 📺 Downloader code · رقم Downloader · Downloader kodu: **`7752330`**

**Download:** [Latest release](https://github.com/basilhalil/youtube-tv-volume-fix/releases/latest) · [`yt-volume-fix.apk`](https://github.com/basilhalil/youtube-tv-volume-fix/releases/latest/download/yt-volume-fix.apk) (29 KB)
SHA-256 (v1.2): `dbc22d0d58fdaf27ec00fe372f6037c73e8a7a64b4a415ff825f423adbe1a6ec`

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
1. **الطريقة الأسهل:** افتح تطبيق **Downloader** على التلفزيون، واكتب الرقم **`7752330`** ثم **Go**، ثم **تثبيت**.
   أو نزّل ملف `yt-volume-fix.apk` وثبّته بفلاشة USB أو ADB:
   ```
   adb install yt-volume-fix.apk
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
- **آمن على كل الأجهزة (منذ v1.2):** يتعرّف التطبيق بنفسه على جهازك. عند أول ضغطة صوت داخل YouTube يترك الضغطة تمر، ثم يتحقق هل تغيّر الصوت. إذا كانت الأزرار تعمل أصلاً، أو كان الصوت يُدار عبر HDMI-CEC، **لا يتدخل أبداً**، فلا يُفسد الصوت على أي جهاز. ويُظهر لك في شاشته هل يحتاجه تلفزيونك.
- إذا أوقفت التطبيق إجبارياً (Force stop) من الإعدادات، يُطفئ Android خدمته تلقائياً، فأعد تفعيلها.

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
1. **Easiest:** open the **Downloader** app on the TV, enter **`7752330`**, press **Go**, then **Install**.
   Or download `yt-volume-fix.apk` and install it with a USB stick or ADB:
   ```
   adb install yt-volume-fix.apk
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
- **Safe on any device (since v1.2):** the app learns how your device behaves. On the first volume press inside YouTube it lets the press through and checks whether the volume changed. If the buttons already work, or the volume is controlled over HDMI-CEC, it **never steps in**, so it cannot break volume on any device. Its screen tells you whether your TV needs it.
- If you **Force stop** the app in Settings, Android turns its service off. Just enable it again.

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
1. **En kolayı:** TV'de **Downloader** uygulamasını açın, **`7752330`** yazın, **Go**'ya ve ardından **Yükle**'ye basın.
   Ya da `yt-volume-fix.apk` dosyasını indirip USB bellek veya ADB ile kurun:
   ```
   adb install yt-volume-fix.apk
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
- **Her cihazda güvenli (v1.2'den beri):** uygulama cihazınızı kendisi tanır. YouTube'daki ilk ses tuşu basışını geçirir ve sesin değişip değişmediğini kontrol eder. Tuşlar zaten çalışıyorsa veya ses HDMI-CEC ile kontrol ediliyorsa **hiç karışmaz**, bu yüzden hiçbir cihazda sesi bozamaz. Ekranında TV'nizin ona ihtiyacı olup olmadığını gösterir.
- Uygulamayı Ayarlar'dan **Zorla durdurursanız**, Android hizmetini kapatır. Tekrar etkinleştirmeniz yeterli.

---

## Security & privacy · الأمان والخصوصية · Güvenlik ve gizlilik

- **No permissions at all.** Not even internet: the app cannot send anything anywhere.
- **Least-privilege Accessibility service (since v1.1):** it can only filter key presses and see *which app* is in front. It **cannot read screen content** (`canRetrieveWindowContent="false"`).
- It reacts only to Volume Up / Down / Mute, and only while a YouTube app is in front. Every other key passes through untouched.
- No data collection, no analytics, no ads, no third-party libraries. About 240 lines of code you can read in [`src/`](src/).
- Every release lists its SHA-256 so you can verify the file you downloaded.

- **لا يطلب أي صلاحية،** ولا حتى الإنترنت، فلا يستطيع إرسال أي شيء إلى أي مكان.
- **منذ v1.1 لا يستطيع قراءة محتوى الشاشة إطلاقاً.** يعرف فقط اسم التطبيق المفتوح، ويلتقط أزرار الصوت.
- لا جمع بيانات، ولا إعلانات، ولا مكتبات خارجية. الكود كله حوالي 240 سطراً.

- **Hiçbir izin istemez,** internet bile: hiçbir yere veri gönderemez.
- **v1.1'den beri ekran içeriğini okuyamaz.** Yalnızca öndeki uygulamanın adını bilir ve ses tuşlarını yakalar.
- Veri toplama, reklam veya üçüncü taraf kütüphane yok. Tüm kod yaklaşık 240 satır.

## Build from source

No Gradle needed. Requires JDK 8+, Android SDK Build-Tools 30.0.3 and the Android 8.0 (API 26) platform:
```
YTVF_KS_PASS=<your keystore password> bash build.sh <build-tools dir> <android.jar> <jdk bin dir>
```
The signing key (`*.keystore`) and its password are not in the repository. To build your own copy, create your own key with `keytool`. Official releases are signed only by the maintainer, with certificate SHA-256 `89:29:01:D2:BE:05:5C:F3:A8:17:C9:8A:E3:E8:B5:25:FD:4E:C9:CF:DD:65:BA:EA:84:25:29:E3:35:CA:99:A5`.

## License · الرخصة · Lisans

[GNU GPL v3](LICENSE), with one additional term (section 7b): any copy or modified version must keep the dedication to the late Ahmed Khalil visible in the app, and credit this project with a link. See [NOTICE](NOTICE).

**Request:** If you build on this code, please also keep the message asking users to recite Al-Fatiha for his soul.

**الرخصة:** [GNU GPL v3](LICENSE): يحق لك استخدام الكود وتعديله ونشره، بشرط أن تنشر كودك أنت أيضاً بالرخصة نفسها، وأن **تُبقي الإهداء إلى روح المرحوم أحمد خليل ظاهراً في التطبيق** مع رابط هذا المشروع.
**ونرجو** ممن يستخدم الكود أن يُبقي أيضاً رسالة قراءة الفاتحة على روحه. جزاكم الله خيراً.

**Lisans:** [GNU GPL v3](LICENSE): kodu kullanabilir, değiştirebilir ve yayabilirsiniz; ancak kendi kodunuzu da aynı lisansla yayınlamalı ve **merhum Ahmed Halil'e yapılan ithafı** bu projenin bağlantısıyla birlikte uygulamada görünür tutmalısınız.
**Ricamız:** Fatiha okunmasını isteyen mesajı da korumanızdır. Allah razı olsun.
