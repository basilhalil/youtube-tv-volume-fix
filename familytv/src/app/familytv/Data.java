package app.familytv;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.TimeZone;

/** Bundled data (channels, cities, verses), the UI language and saved settings. */
final class Data {
    private static JSONObject channels;
    private static JSONArray cities, verses;

    private Data() {}

    /** "ar", "tr" or "en", following the TV's language. */
    static String lang() {
        String l = Locale.getDefault().getLanguage();
        return "ar".equals(l) || "tr".equals(l) ? l : "en";
    }

    /** mp3quran.net language code for the TV's language. */
    static String quranLang() {
        String l = lang();
        return "en".equals(l) ? "eng" : l;
    }

    static String pick(JSONObject names) {
        if (names == null) return "";
        String v = names.optString(lang(), "");
        return v.isEmpty() ? names.optString("en", names.optString("ar", "")) : v;
    }

    static String readAll(InputStream in) throws java.io.IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            in.close();
        }
    }

    private static String asset(Context c, String name) {
        try { return readAll(c.getAssets().open(name)); } catch (Exception e) { return null; }
    }

    static synchronized JSONObject channels(Context c) {
        if (channels == null) {
            try { channels = new JSONObject(asset(c, "channels.json")); } catch (Exception e) { channels = new JSONObject(); }
        }
        return channels;
    }

    /** The sections of one channels page ("islamic", "arabic", "kids", "turkish"). */
    static JSONArray page(Context c, String id) {
        JSONObject pages = channels(c).optJSONObject("pages");
        JSONArray a = pages == null ? null : pages.optJSONArray(id);
        return a == null ? new JSONArray() : a;
    }

    static synchronized JSONArray cities(Context c) {
        if (cities == null) {
            try { cities = new JSONArray(asset(c, "cities.json")); } catch (Exception e) { cities = new JSONArray(); }
        }
        return cities;
    }

    static synchronized JSONArray verses(Context c) {
        if (verses == null) {
            try { verses = new JSONArray(asset(c, "verses.json")); } catch (Exception e) { verses = new JSONArray(); }
        }
        return verses;
    }

    // ---- settings ----

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    static JSONObject city(Context c) {
        String id = prefs(c).getString("city", "istanbul");
        JSONArray all = cities(c);
        for (int i = 0; i < all.length(); i++) {
            JSONObject o = all.optJSONObject(i);
            if (o != null && id.equals(o.optString("id"))) return o;
        }
        return all.optJSONObject(0);
    }

    static void setCity(Context c, JSONObject city) {
        // Choosing a city also picks the method its country normally uses; the user can still change it.
        prefs(c).edit().putString("city", city.optString("id"))
                .putString("method", defaultMethod(city.optString("cc"))).apply();
    }

    static PrayerTimes.Method method(Context c) {
        String id = prefs(c).getString("method", null);
        if (id == null) id = defaultMethod(city(c).optString("cc"));
        return PrayerTimes.byId(id);
    }

    static void setMethod(Context c, PrayerTimes.Method m) { prefs(c).edit().putString("method", m.id).apply(); }

    static boolean hanafi(Context c) { return prefs(c).getBoolean("hanafi", false); }

    static void setHanafi(Context c, boolean v) { prefs(c).edit().putBoolean("hanafi", v).apply(); }

    static String defaultMethod(String cc) {
        switch (cc) {
            case "TR": return "diyanet";
            case "SA": case "QA": case "BH": case "OM": case "YE": return "umm_al_qura";
            case "EG": case "SD": case "SY": case "LB": case "IQ": case "JO": case "PS": case "LY": return "egypt";
            default: return "mwl";
        }
    }

    static TimeZone zone(JSONObject city) {
        return TimeZone.getTimeZone(city.optString("tz", "Europe/Istanbul"));
    }

    static String cityName(JSONObject city) {
        String v = city.optString(lang(), "");
        return v.isEmpty() ? city.optString("en") : v;
    }

    static String methodName(Context c, PrayerTimes.Method m) {
        int res;
        switch (m.id) {
            case "diyanet": res = R.string.method_diyanet; break;
            case "umm_al_qura": res = R.string.method_umm_al_qura; break;
            case "egypt": res = R.string.method_egypt; break;
            case "karachi": res = R.string.method_karachi; break;
            case "isna": res = R.string.method_isna; break;
            default: res = R.string.method_mwl;
        }
        return c.getString(res);
    }

    static String prayerName(Context c, int i) {
        int[] ids = {R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.isha};
        return c.getString(ids[i]);
    }
}
