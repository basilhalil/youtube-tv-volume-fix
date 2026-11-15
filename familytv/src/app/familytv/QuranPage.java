package app.familytv;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Holy Quran: live Quran radios, or browse reciters → recitation → surahs. Audio from mp3quran.net. */
final class QuranPage extends Page {
    private static final String API = "https://www.mp3quran.net/api/v3/";
    private static final long WEEK = 7L * 24 * 3600 * 1000;

    private FrameLayout body;
    private TextView tabRadios, tabReciters;
    private boolean showingRadios = true;
    /** 0 = list, 1 = a reciter's recitations, 2 = surahs */
    private int level = 0;
    private JSONObject reciter, moshaf;
    private final Map<Integer, String> suwar = new HashMap<>();
    private View first;

    QuranPage(MainActivity a) { super(a); }

    @Override
    View view() {
        LinearLayout col = Ui.vertical(a);
        LinearLayout tabs = Ui.horizontal(a);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setPadding(Ui.dp(a, 6), Ui.dp(a, 12), Ui.dp(a, 6), Ui.dp(a, 6));
        tabRadios = a.pill("📻  " + a.getString(R.string.radios), () -> { showingRadios = true; level = 0; load(true); });
        tabReciters = a.pill("🎙  " + a.getString(R.string.reciters), () -> { showingRadios = false; level = 0; load(true); });
        tabs.addView(tabRadios);
        LinearLayout.LayoutParams tp = Ui.lp(Ui.WRAP, Ui.WRAP);
        tp.setMarginStart(Ui.dp(a, 12));
        tabs.addView(tabReciters, tp);
        View spacer = new View(a);
        tabs.addView(spacer, Ui.lp(0, 1, 1));
        tabs.addView(Ui.text(a, a.getString(R.string.mp3quran_credit), 12, Ui.TEXT_DIM, false));
        col.addView(tabs, Ui.lp(Ui.MATCH, Ui.WRAP));

        body = new FrameLayout(a);
        col.addView(body, Ui.lp(Ui.MATCH, 0, 1));
        first = tabRadios;
        load(false);
        return col;
    }

    @Override
    View firstFocus() { return first; }

    @Override
    boolean back() {
        if (level == 2 && reciter != null && reciter.optJSONArray("moshaf").length() > 1) { level = 1; load(true); return true; }
        if (level > 0) { level = 0; load(true); return true; }
        return false;
    }

    private void markTabs() {
        tabRadios.setAlpha(showingRadios ? 1f : 0.6f);
        tabReciters.setAlpha(showingRadios ? 0.6f : 1f);
    }

    /** Before replacing the body, park focus on the active tab so it never falls back to the menu. */
    private void clearBody() {
        if (body.hasFocus()) (showingRadios ? tabRadios : tabReciters).requestFocus();
        body.removeAllViews();
    }

    private void message(String text, Runnable retry) {
        clearBody();
        LinearLayout box = Ui.vertical(a);
        box.setGravity(Gravity.CENTER);
        TextView t = Ui.text(a, text, 18, Ui.TEXT_DIM, false);
        Ui.center(t);
        box.addView(t);
        if (retry != null) {
            TextView r = a.pill("↻", retry);
            LinearLayout.LayoutParams p = Ui.lp(Ui.WRAP, Ui.WRAP);
            p.topMargin = Ui.dp(a, 14);
            box.addView(r, p);
        }
        body.addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private void load(final boolean takeFocus) {
        markTabs();
        message(a.getString(R.string.loading), null);
        if (level == 0 && showingRadios) {
            Net.json(a, API + "radios?language=" + Data.quranLang(), "radios-" + Data.quranLang(), WEEK, j -> {
                if (j == null) { message(a.getString(R.string.load_failed), () -> load(true)); return; }
                showRadios(j.optJSONArray("radios"), takeFocus);
            });
        } else if (level == 0) {
            Net.json(a, API + "reciters?language=" + Data.quranLang(), "reciters-" + Data.quranLang(), WEEK, j -> {
                if (j == null) { message(a.getString(R.string.load_failed), () -> load(true)); return; }
                showReciters(j.optJSONArray("reciters"), takeFocus);
            });
        } else if (level == 1) {
            showMoshafs(takeFocus);
        } else {
            Net.json(a, API + "suwar?language=" + Data.quranLang(), "suwar-" + Data.quranLang(), 4 * WEEK, j -> {
                if (j == null) { message(a.getString(R.string.load_failed), () -> load(true)); return; }
                JSONArray s = j.optJSONArray("suwar");
                for (int i = 0; s != null && i < s.length(); i++) {
                    JSONObject o = s.optJSONObject(i);
                    suwar.put(o.optInt("id"), o.optString("name").trim());
                }
                showSurahs(takeFocus);
            });
        }
    }

    private LinearLayout scrollColumn(String header, boolean withBack) {
        clearBody();
        ScrollView sv = new ScrollView(a);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipToPadding(false);
        LinearLayout col = Ui.vertical(a);
        col.setPadding(Ui.dp(a, 4), 0, Ui.dp(a, 4), Ui.dp(a, 24));
        sv.addView(col);
        body.addView(sv, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        if (header != null) {
            LinearLayout h = Ui.horizontal(a);
            h.setGravity(Gravity.CENTER_VERTICAL);
            if (withBack) {
                TextView b = a.pill("‹ " + a.getString(R.string.back), this::back);
                h.addView(b);
            }
            TextView t = Ui.text(a, header, 22, Ui.TEXT, true);
            t.setPadding(Ui.dp(a, 16), 0, Ui.dp(a, 16), 0);
            h.addView(t, Ui.lp(0, Ui.WRAP, 1));
            h.setPadding(0, Ui.dp(a, 10), 0, Ui.dp(a, 6));
            col.addView(h, Ui.lp(Ui.MATCH, Ui.WRAP));
        }
        return col;
    }

    private void showRadios(JSONArray radios, boolean takeFocus) {
        LinearLayout col = scrollColumn(null, false);
        List<View> cards = new ArrayList<>();
        final String subtitle = a.getString(R.string.radios);
        for (int i = 0; radios != null && i < radios.length(); i++) {
            JSONObject r = radios.optJSONObject(i);
            final String name = r.optString("name").trim(), url = r.optString("url");
            int color = PALETTE[i % PALETTE.length];
            cards.add(new Card(a, "📻", name, null, color, false)
                    .onClick(() -> Player.play(a, new Player.Track(url, name, subtitle))));
        }
        Ui.grid(a, col, cards, 4, 104);
        finish(cards, takeFocus);
    }

    private void showReciters(JSONArray list, boolean takeFocus) {
        LinearLayout col = scrollColumn(null, false);
        List<JSONObject> rs = new ArrayList<>();
        for (int i = 0; list != null && i < list.length(); i++) rs.add(list.optJSONObject(i));
        Collections.sort(rs, (x, y) -> x.optString("name").compareTo(y.optString("name")));
        List<View> cards = new ArrayList<>();
        for (int i = 0; i < rs.size(); i++) {
            final JSONObject r = rs.get(i);
            JSONArray m = r.optJSONArray("moshaf");
            String sub = m != null && m.length() > 0 ? m.optJSONObject(0).optString("name") : null;
            cards.add(new Card(a, "🎙", r.optString("name").trim(), sub, PALETTE[i % PALETTE.length], false)
                    .onClick(() -> {
                        reciter = r;
                        JSONArray ms = r.optJSONArray("moshaf");
                        if (ms == null || ms.length() == 0) return;
                        if (ms.length() == 1) { moshaf = ms.optJSONObject(0); level = 2; } else level = 1;
                        load(true);
                    }));
        }
        Ui.grid(a, col, cards, 4, 112);
        finish(cards, takeFocus);
    }

    private void showMoshafs(boolean takeFocus) {
        LinearLayout col = scrollColumn(reciter.optString("name").trim(), true);
        JSONArray ms = reciter.optJSONArray("moshaf");
        List<View> cards = new ArrayList<>();
        for (int i = 0; i < ms.length(); i++) {
            final JSONObject m = ms.optJSONObject(i);
            cards.add(new Card(a, "📖", m.optString("name"), m.optInt("surah_total") + " · " + a.getString(R.string.surahs),
                    PALETTE[(i + 3) % PALETTE.length], false)
                    .onClick(() -> { moshaf = m; level = 2; load(true); }));
        }
        Ui.grid(a, col, cards, 3, 130);
        finish(cards, takeFocus);
    }

    private void showSurahs(boolean takeFocus) {
        final String rname = reciter.optString("name").trim();
        LinearLayout col = scrollColumn(rname + "  ·  " + moshaf.optString("name"), true);
        String server = moshaf.optString("server");
        String[] ids = moshaf.optString("surah_list").split(",");
        final List<Player.Track> playlist = new ArrayList<>();
        List<View> cards = new ArrayList<>();
        for (String s : ids) {
            int n;
            try { n = Integer.parseInt(s.trim()); } catch (NumberFormatException e) { continue; }
            String name = suwar.containsKey(n) ? suwar.get(n) : String.valueOf(n);
            String url = server + String.format(Locale.US, "%03d.mp3", n);
            playlist.add(new Player.Track(url, a.getString(R.string.surah_n, name), rname));
            final int index = playlist.size() - 1;
            cards.add(new Card(a, String.format(Locale.getDefault(), "%d", n), name, null,
                    PALETTE[n % PALETTE.length], false)
                    .onClick(() -> Player.play(a, playlist, index)));
        }
        Ui.grid(a, col, cards, 6, 104);
        finish(cards, takeFocus);
    }

    private void finish(List<View> cards, boolean takeFocus) {
        if (!cards.isEmpty()) {
            first = cards.get(0);
            if (takeFocus) first.requestFocus();
        }
    }

    private static final int[] PALETTE = {0xFF0E6B4F, 0xFF1B5E7A, 0xFF5D4037, 0xFF4E342E, 0xFF00695C,
            0xFF37474F, 0xFF33691E, 0xFF283593, 0xFF6A1B9A, 0xFF00838F};
}
