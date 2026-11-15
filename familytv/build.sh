#!/usr/bin/env bash
# Builds familytv.apk without Gradle: aapt2 -> javac -> d8 -> zip -> zipalign -> apksigner.
# Usage: FTV_KS_PASS=<keystore password> build.sh <build-tools dir> <android.jar> <jdk bin dir>
#
# The signing key (familytv.keystore) and its password are never stored in this repository.
# On the first build a new key is created with that password. Keep both backed up privately:
# every update must be signed with the same key.
set -euo pipefail
BT="$1"; ANDROID_JAR="$2"; JDK="$3"
cd "$(dirname "$0")"

KS=familytv.keystore
: "${FTV_KS_PASS:?Set FTV_KS_PASS to the keystore password (it is not stored in the repo)}"

rm -rf out && mkdir -p out/res out/classes out/dex

"$BT/aapt2.exe" compile --dir res -o out/res/compiled.zip
"$BT/aapt2.exe" link -o out/unsigned.apk -I "$ANDROID_JAR" \
  --manifest AndroidManifest.xml -A assets --java out/gen out/res/compiled.zip

# --release 8 keeps the JDK's own java.lang.invoke classes visible, which lambdas need at compile time;
# d8 then desugars the lambdas for Android.
"$JDK/javac.exe" --release 8 -encoding UTF-8 -nowarn -g:none \
  -classpath "$ANDROID_JAR" -d out/classes \
  $(find src out/gen -name '*.java')

"$JDK/java.exe" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 24 \
  --lib "$ANDROID_JAR" --output out/dex $(find out/classes -name '*.class')

cp out/dex/classes.dex out/classes.dex
(cd out && "$JDK/jar.exe" uf unsigned.apk classes.dex)

"$BT/zipalign.exe" -f 4 out/unsigned.apk out/aligned.apk

if [ ! -f "$KS" ]; then
  echo "Creating a new signing key: $KS (back it up together with its password!)"
  "$JDK/keytool.exe" -genkeypair -keystore "$KS" -storepass:env FTV_KS_PASS -keypass:env FTV_KS_PASS \
    -alias familytv -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Family TV"
fi

# Password is passed through the environment, never on the command line or in this file.
"$JDK/java.exe" -jar "$BT/lib/apksigner.jar" sign --ks "$KS" \
  --ks-pass env:FTV_KS_PASS --key-pass env:FTV_KS_PASS \
  --out familytv.apk out/aligned.apk
rm -f familytv.apk.idsig
"$JDK/java.exe" -jar "$BT/lib/apksigner.jar" verify familytv.apk && echo "BUILD OK: $(ls -la familytv.apk)"
