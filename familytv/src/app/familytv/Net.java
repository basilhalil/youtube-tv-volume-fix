package app.familytv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small JSON fetcher with an on-disk cache, so lists open instantly and still work when the network drops. */
final class Net {
    interface Callback { void done(JSONObject json); }

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private Net() {}

    /**
     * Calls back on the main thread: from cache when it is younger than {@code maxAgeMs},
     * otherwise from the network (falling back to a stale cache, then null).
     */
    static void json(Context ctx, String url, String cacheName, long maxAgeMs, Callback cb) {
        final File f = new File(ctx.getFilesDir(), "cache-" + cacheName + ".json");
        POOL.execute(() -> {
            JSONObject result = null;
            try {
                if (f.exists() && System.currentTimeMillis() - f.lastModified() < maxAgeMs) {
                    result = new JSONObject(Data.readAll(new FileInputStream(f)));
                }
            } catch (Exception ignored) { }
            if (result == null) {
                try {
                    String body = get(url);
                    result = new JSONObject(body);
                    File tmp = new File(f.getPath() + ".tmp");
                    FileOutputStream out = new FileOutputStream(tmp);
                    out.write(body.getBytes("UTF-8"));
                    out.close();
                    tmp.renameTo(f);
                } catch (Exception e) {
                    try { if (f.exists()) result = new JSONObject(Data.readAll(new FileInputStream(f))); }
                    catch (Exception ignored) { }
                }
            }
            final JSONObject r = result;
            MAIN.post(() -> cb.done(r));
        });
    }

    private static String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(12000);
        c.setReadTimeout(20000);
        c.setRequestProperty("User-Agent", "FamilyTV/1.0 (Android TV)");
        try {
            if (c.getResponseCode() != 200) throw new java.io.IOException("HTTP " + c.getResponseCode());
            return Data.readAll(c.getInputStream());
        } finally {
            c.disconnect();
        }
    }
}
