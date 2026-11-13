#!/usr/bin/env bash
# Builds yt-volume-fix.apk without Gradle: aapt2 -> javac -> d8 -> zip -> zipalign -> apksigner.
# Usage: build.sh <build-tools dir> <android.jar> <jdk bin dir>
set -euo pipefail
BT="$1"; ANDROID_JAR="$2"; JDK="$3"
cd "$(dirname "$0")"
rm -rf out && mkdir -p out/res out/classes out/dex

"$BT/aapt2.exe" compile --dir res -o out/res/compiled.zip
"$BT/aapt2.exe" link -o out/unsigned.apk -I "$ANDROID_JAR" \
  --manifest AndroidManifest.xml --java out/gen out/res/compiled.zip

"$JDK/javac.exe" -source 8 -target 8 -encoding UTF-8 -nowarn \
  -bootclasspath "$ANDROID_JAR" -d out/classes \
  $(find src out/gen -name '*.java')

"$JDK/java.exe" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --min-api 21 --lib "$ANDROID_JAR" --output out/dex $(find out/classes -name '*.class')

cp out/dex/classes.dex out/classes.dex
(cd out && "$JDK/jar.exe" uf unsigned.apk classes.dex)

"$BT/zipalign.exe" -f 4 out/unsigned.apk out/aligned.apk

if [ ! -f ytvolumefix.keystore ]; then
  "$JDK/keytool.exe" -genkeypair -keystore ytvolumefix.keystore -storepass ytvolumefix -keypass ytvolumefix \
    -alias ytvolumefix -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=YouTube Volume Fix"
fi
"$JDK/java.exe" -jar "$BT/lib/apksigner.jar" sign --ks ytvolumefix.keystore --ks-pass pass:ytvolumefix --key-pass pass:ytvolumefix \
  --out yt-volume-fix.apk out/aligned.apk
"$JDK/java.exe" -jar "$BT/lib/apksigner.jar" verify yt-volume-fix.apk && echo "BUILD OK: $(ls -la yt-volume-fix.apk)"
