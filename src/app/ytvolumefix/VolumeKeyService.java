/*
 * YouTube Volume Fix - Copyright (C) 2026 basilhalil
 * Licensed under the GNU GPL v3 with an additional term (section 7b): see LICENSE and NOTICE.
 * Dedicated to the soul of the late Ahmed Khalil (المرحوم أحمد خليل).
 */
package app.ytvolumefix;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * On some TVs the system hands the remote's volume keys to the app in front instead of handling
 * them itself, and the YouTube TV app swallows them. This service only steps in on such devices.
 *
 * It never assumes: it learns how the device behaves, only from what happens inside YouTube, and
 * stays out of the way when it is not needed. Volume presses outside YouTube are never looked at:
 * on many TVs the HDMI inputs and TV channels use the TV's own volume, which says nothing about YouTube.
 *  - Fixed-volume devices (sticks that control the TV over HDMI-CEC): never touches the keys.
 *  - Inside YouTube, the first press is let through and checked a moment later. If the volume
 *    changed, the device works on its own (NATIVE) and the app stays out of the way. If not,
 *    YouTube swallowed it (FIX): the app applies that press itself.
 *  - That first fix is then checked too: if even the app's own change did not move the volume,
 *    this TV controls volume some other way (UNSUPPORTED) and the app stays out of the way.
 */
public class VolumeKeyService extends AccessibilityService {
    static final Set<String> YOUTUBE_APPS = new HashSet<String>(Arrays.asList(
            "com.google.android.youtube.tv",
            "com.google.android.youtube.tvkids",
            "com.google.android.youtube.tvmusic",
            "com.google.android.youtube.tvunplugged"));

    static final String PREFS = "detect";
    static final String KEY_MODE = "mode";
    static final int MODE_UNKNOWN = 0;      // not learned yet
    static final int MODE_FIX = 1;          // YouTube swallows the keys here: we handle them
    static final int MODE_NATIVE = 2;       // the keys already work in YouTube: do nothing
    static final int MODE_UNSUPPORTED = 3;  // even our own change does not move the volume: do nothing

    /** Bumped when the learning rules change, so results learned by older rules are thrown away. */
    static final String KEY_LOGIC = "logic";
    static final int LOGIC_VERSION = 2;

    private static final int CHECK_DELAY_MS = 350;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile String foreground = "";
    private int mode = MODE_UNKNOWN;
    private boolean probing = false;

    /**
     * v1.2 could wrongly decide "unsupported" from volume presses on HDMI inputs or TV channels.
     * Forget anything learned by older rules. Called by the service and by the app screen.
     */
    static void migrate(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (sp.getInt(KEY_LOGIC, 1) < LOGIC_VERSION) {
            sp.edit().putInt(KEY_MODE, MODE_UNKNOWN).putInt(KEY_LOGIC, LOGIC_VERSION).commit();
        }
    }

    // Kept as a field: SharedPreferences holds listeners weakly.
    private final SharedPreferences.OnSharedPreferenceChangeListener onPrefs =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
                    if (KEY_MODE.equals(key)) mode = sp.getInt(KEY_MODE, MODE_UNKNOWN); // "re-check" button
                }
            };

    @Override
    protected void onServiceConnected() {
        migrate(this);
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        mode = sp.getInt(KEY_MODE, MODE_UNKNOWN);
        sp.registerOnSharedPreferenceChangeListener(onPrefs);
    }

    @Override
    public void onDestroy() {
        getSharedPreferences(PREFS, MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(onPrefs);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence p = e.getPackageName();
        if (p == null) return;
        String pkg = p.toString();
        // Overlays (volume panel, toasts, keyboard) do not change the app in front.
        if (pkg.equals("com.android.systemui") || pkg.equals("android")
                || pkg.equals(getPackageName()) || pkg.contains("inputmethod")) return;
        foreground = pkg;
    }

    /**
     * Only the package name of the app in front is ever looked at (from window-change events).
     * The service cannot read screen content: canRetrieveWindowContent is off in volume_service.xml.
     */
    private boolean youtubeInFront() {
        return YOUTUBE_APPS.contains(foreground);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        if (code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN
                && code != KeyEvent.KEYCODE_VOLUME_MUTE) {
            return false;
        }
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null || isFixedVolume(am)) return false;   // e.g. HDMI-CEC sticks
        if (mode == MODE_UNSUPPORTED || mode == MODE_NATIVE) return false;

        boolean firstDown = event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0;

        // Outside YouTube: never touch the keys and never learn from them.
        if (!youtubeInFront()) return false;

        if (mode == MODE_FIX) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) adjust(am, code, event.getRepeatCount());
            return true; // consume DOWN and UP so YouTube never sees them
        }

        // MODE_UNKNOWN inside YouTube: let the press through, then see whether it did anything.
        if (firstDown && !probing && code != KeyEvent.KEYCODE_VOLUME_MUTE) probeInYouTube(am, code);
        return false;
    }

    private void probeInYouTube(final AudioManager am, final int code) {
        final int before = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        final int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (atLimit(code, before, max)) return; // cannot tell anything at 0 or at max
        probing = true;
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                probing = false;
                if (mode != MODE_UNKNOWN) return;
                int after = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (after != before) {
                    setMode(MODE_NATIVE);               // works without us: stay out of the way
                } else {
                    setMode(MODE_FIX);                  // swallowed: apply this press, handle the rest
                    adjust(am, code, 0);
                    verifyOwnFix(am, before);
                }
            }
        }, CHECK_DELAY_MS);
    }

    /** If even our own change did not move the volume, this TV controls volume another way. */
    private void verifyOwnFix(final AudioManager am, final int before) {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (mode == MODE_FIX && am.getStreamVolume(AudioManager.STREAM_MUSIC) == before) {
                    setMode(MODE_UNSUPPORTED);
                }
            }
        }, CHECK_DELAY_MS);
    }

    private static boolean atLimit(int code, int vol, int max) {
        return (code == KeyEvent.KEYCODE_VOLUME_UP && vol >= max)
                || (code == KeyEvent.KEYCODE_VOLUME_DOWN && vol <= 0);
    }

    private static void adjust(AudioManager am, int code, int repeatCount) {
        int dir;
        if (code == KeyEvent.KEYCODE_VOLUME_UP) dir = AudioManager.ADJUST_RAISE;
        else if (code == KeyEvent.KEYCODE_VOLUME_DOWN) dir = AudioManager.ADJUST_LOWER;
        else if (repeatCount > 0) return; // one mute toggle per press
        else dir = Build.VERSION.SDK_INT >= 23 ? AudioManager.ADJUST_TOGGLE_MUTE : AudioManager.ADJUST_SAME;
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI);
    }

    static boolean isFixedVolume(AudioManager am) {
        return Build.VERSION.SDK_INT >= 21 && am.isVolumeFixed();
    }

    private void setMode(int m) {
        mode = m;
        SharedPreferences.Editor ed = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        ed.putInt(KEY_MODE, m).apply();
    }

    @Override
    public void onInterrupt() {}
}
