package app.familytv;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * One shared audio player for Quran radios and surahs. It keeps playing while the user browses other pages
 * of the app. A playlist (a reciter's surahs) advances automatically to the next surah.
 */
final class Player {
    interface Listener { void changed(); }

    static final class Track {
        final String url, title, subtitle;
        Track(String url, String title, String subtitle) { this.url = url; this.title = title; this.subtitle = subtitle; }
    }

    private static MediaPlayer mp;
    private static List<Track> list = new ArrayList<>();
    private static int index = -1;
    private static boolean preparing, paused;
    private static final List<Listener> listeners = new ArrayList<>();

    private Player() {}

    static void listen(Listener l) { if (!listeners.contains(l)) listeners.add(l); }

    static void unlisten(Listener l) { listeners.remove(l); }

    private static void fire() { for (Listener l : new ArrayList<>(listeners)) l.changed(); }

    static Track current() { return index >= 0 && index < list.size() ? list.get(index) : null; }

    static boolean active() { return current() != null; }

    static boolean paused() { return paused; }

    static boolean preparing() { return preparing; }

    static void play(Context c, List<Track> tracks, int start) {
        list = new ArrayList<>(tracks);
        index = start;
        startCurrent(c.getApplicationContext());
    }

    static void play(Context c, Track t) {
        List<Track> one = new ArrayList<>();
        one.add(t);
        play(c, one, 0);
    }

    private static void startCurrent(final Context c) {
        release();
        Track t = current();
        if (t == null) { fire(); return; }
        mp = new MediaPlayer();
        mp.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());
        mp.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
        preparing = true;
        paused = false;
        try {
            mp.setDataSource(t.url);
            mp.setOnPreparedListener(p -> { preparing = false; p.start(); fire(); });
            mp.setOnCompletionListener(p -> {
                if (index + 1 < list.size()) { index++; startCurrent(c); } else stop();
            });
            mp.setOnErrorListener((p, what, extra) -> { stop(); return true; });
            mp.prepareAsync();
        } catch (Exception e) {
            stop();
            return;
        }
        fire();
    }

    static void togglePause() {
        if (mp == null || preparing) return;
        if (mp.isPlaying()) { mp.pause(); paused = true; } else { mp.start(); paused = false; }
        fire();
    }

    static void stop() {
        release();
        list = new ArrayList<>();
        index = -1;
        fire();
    }

    private static void release() {
        preparing = false;
        paused = false;
        if (mp != null) {
            try { mp.reset(); mp.release(); } catch (Exception ignored) { }
            mp = null;
        }
    }
}
