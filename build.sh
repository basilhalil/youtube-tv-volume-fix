#!/usr/bin/env bash
# Builds yt-volume-fix.apk without Gradle: aapt2 -> javac -> d8 -> zip -> zipalign -> apksigner.
# Usage: YTVF_KS_PASS=<keystore password> build.sh <build-tools dir> <android.jar> <jdk bin dir>
#
# The signing key (ytvolumefix.keystore) and its password are never stored in this repository.
# Keep both backed up privately: every update must be signed with the same key.
set -euo pipefail
BT="$1"; ANDROID_JAR="$2"; JDK="$3"
cd "$(dirname "$0")"

KS=ytvolumefix.keystore
: "${YTVF_KS_PASS:?Set YTVF_KS_PASS to the keystore password (it is not stored in the repo)}"
[ -f "$KS" ] || { echo "Missing $KS (the private signing key). Restore it from your backup." >&2; exit 1; }

rm -rf out && mkdir -p out/res out/classes out/dex

"$BT/aapt2.exe" compile --dir res -o out/res/compiled.zip
"$BT/aapt2.exe" link -o out/unsigned.apk -I "$ANDROID_JAR" \
  --manifest AndroidManifest.xml --java out/gen out/res/compiled.zip

"$JDK/javac.exe" -source 8 -target 8 -encoding UTF-8 -nowarn -g:none \
  -bootclasspath "$ANDROID_JAR" -d out/classes \
  $(find src out/gen -name '*.java')

"$JDK/java.exe" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 21 \
  --lib "$ANDROID_JAR" --output out/dex $(find out/classes -name '*.class')

cp out/dex/classes.dex out/classes.dex
(cd out && "$JDK/jar.exe" uf unsigned.apk classes.dex)

"$BT/zipalign.exe" -f 4 out/unsigned.apk out/aligned.apk

# Password is passed through the environment, never on the command line or in this file.
"$JDK/java.exe" -jar "$BT/lib/apksigner.jar" sign --ks "$KS" \
  --ks-pass env:YTVF_KS_PASS --key-pass env:YTVF_KS_PASS \
  --out yt-volume-fix.apk out/aligned.apk
rm -f yt-volume-fix.apk.idsig
"$JDK/java.exe" -jar "$BT/lib/apksigner.jar" verify yt-volume-fix.apk && echo "BUILD OK: $(ls -la yt-volume-fix.apk)"
