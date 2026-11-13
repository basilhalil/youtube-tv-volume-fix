package app.ytvolumefix;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * On some TVs the system hands the remote's volume keys to the app in front instead of handling
 * them itself, and the YouTube TV app swallows them. While a YouTube app is in front, this service
 * takes the volume keys first and changes the TV volume directly. Nothing else is touched.
 */
public class VolumeKeyService extends AccessibilityService {
    static final Set<String> YOUTUBE_APPS = new HashSet<String>(Arrays.asList(
            "com.google.android.youtube.tv",
            "com.google.android.youtube.tvkids",
            "com.google.android.youtube.tvmusic",
            "com.google.android.youtube.tvunplugged"));

    private volatile String foreground = "";

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

    private boolean youtubeInFront() {
        try {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                CharSequence p = root.getPackageName();
                root.recycle();
                if (p != null) return YOUTUBE_APPS.contains(p.toString());
            }
        } catch (Exception ignored) {
        }
        return YOUTUBE_APPS.contains(foreground);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        if (code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN
                && code != KeyEvent.KEYCODE_VOLUME_MUTE) {
            return false;
        }
        if (!youtubeInFront()) return false;

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int dir;
            if (code == KeyEvent.KEYCODE_VOLUME_UP) dir = AudioManager.ADJUST_RAISE;
            else if (code == KeyEvent.KEYCODE_VOLUME_DOWN) dir = AudioManager.ADJUST_LOWER;
            else if (event.getRepeatCount() > 0) return true; // one mute toggle per press
            else dir = Build.VERSION.SDK_INT >= 23 ? AudioManager.ADJUST_TOGGLE_MUTE : AudioManager.ADJUST_SAME;
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI);
        }
        return true; // consume DOWN and UP so YouTube never sees them
    }

    @Override
    public void onInterrupt() {}
}
