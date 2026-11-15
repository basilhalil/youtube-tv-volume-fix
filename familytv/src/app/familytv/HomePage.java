package app.familytv;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/** Home: next prayer with a live countdown, today's times, the verse of the day and quick tiles. */
final class HomePage extends Page {
    private TextView nextName, nextTime, countdown;
    private final TextView[] mini = new TextView[5];
    private final LinearLayout[] miniBox = new LinearLayout[5];
    private Card firstCard;

    HomePage(MainActivity a) { super(a); }

    @Override
    View view() {
        ScrollView scroll = new ScrollView(a);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout col = Ui.vertical(a);
        col.setPadding(0, Ui.dp(a, 10), 0, Ui.dp(a, 20));
        scroll.addView(col);

        // Row 1: next prayer (start) + verse of the day (end)
        LinearLayout top = Ui.horizontal(a);
        col.addView(top, Ui.lp(Ui.MATCH, Ui.WRAP));
        top.addView(nextPrayerPanel(), weighted(1.1f));
        top.addView(versePanel(), weighted(1f));

        // Row 2: today's five prayers
        LinearLayout row = Ui.horizontal(a);
        LinearLayout.LayoutParams rp = Ui.lp(Ui.MATCH, Ui.WRAP);
        rp.topMargin = Ui.dp(a, 14);
        col.addView(row, rp);
        for (int i = 0; i < 5; i++) {
            LinearLayout box = Ui.vertical(a);
            box.setGravity(Gravity.CENTER);
            box.setPadding(0, Ui.dp(a, 10), 0, Ui.dp(a, 10));
            box.setBackground(Ui.round(Ui.PANEL, Ui.dp(a, 14)));
            TextView name = Ui.text(a, Data.prayerName(a, Clock.FIVE[i]), 15, Ui.TEXT_DIM, false);
            Ui.center(name);
            mini[i] = Ui.text(a, "--:--", 22, Ui.TEXT, true);
            Ui.center(mini[i]);
            box.addView(name);
            box.addView(mini[i]);
            miniBox[i] = box;
            LinearLayout.LayoutParams p = Ui.lp(0, Ui.WRAP, 1);
            p.setMargins(Ui.dp(a, 6), 0, Ui.dp(a, 6), 0);
            row.addView(box, p);
        }

        // Row 3: quick access
        col.addView(Ui.heading(a, a.getString(R.string.quick)));
        List<View> tiles = new ArrayList<>();
        firstCard = channelTile("islamic", 0, 0, tiles);
        channelTile("islamic", 0, 1, tiles);
        final String alafasy = "ar".equals(Data.lang()) ? "مشاري العفاسي" : "Mishary Alafasy";
        tiles.add(new Card(a, "🎧", alafasy, a.getString(R.string.radios), 0xFF6D4C41, false)
                .onClick(() -> Player.play(a, new Player.Track("https://backup.qurango.net/radio/mishary_alafasi",
                        alafasy, a.getString(R.string.radios)))));
        tiles.add(new Card(a, "📖", a.getString(R.string.nav_quran), a.getString(R.string.reciters), 0xFF00695C, false)
                .onClick(() -> a.goTo("quran")));
        channelTile("islamic", 1, 0, tiles);
        channelTile("kids", 0, 0, tiles);
        channelTile("turkish", 0, 0, tiles);
        tiles.add(new Card(a, "🕰", a.getString(R.string.nav_prayer), Data.cityName(Data.city(a)), 0xFF37474F, false)
                .onClick(() -> a.goTo("prayer")));
        Ui.grid(a, col, tiles, 4, 150);
        return scroll;
    }

    private LinearLayout.LayoutParams weighted(float w) {
        LinearLayout.LayoutParams p = Ui.lp(0, Ui.MATCH, w);
        p.setMargins(Ui.dp(a, 6), 0, Ui.dp(a, 6), 0);
        return p;
    }

    private View nextPrayerPanel() {
        LinearLayout p = Ui.vertical(a);
        p.setBackground(Ui.gradient(0xFF145A44, 0xFF082A20, Ui.dp(a, 22)));
        p.setPadding(Ui.dp(a, 26), Ui.dp(a, 20), Ui.dp(a, 26), Ui.dp(a, 20));
        p.addView(Ui.text(a, a.getString(R.string.next_prayer) + " · " + Data.cityName(Data.city(a)), 15, Ui.GOLD_DIM, false));
        LinearLayout line = Ui.horizontal(a);
        line.setGravity(Gravity.BOTTOM);
        nextName = Ui.text(a, "", 40, Ui.TEXT, true);
        nextTime = Ui.text(a, "", 26, Ui.GOLD, true);
        nextTime.setPadding(Ui.dp(a, 16), 0, Ui.dp(a, 16), Ui.dp(a, 6));
        line.addView(nextName);
        line.addView(nextTime);
        p.addView(line);
        countdown = Ui.text(a, "", 52, Ui.GOLD, true);
        countdown.setTextDirection(View.TEXT_DIRECTION_LTR);
        countdown.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        p.addView(countdown);
        return p;
    }

    private View versePanel() {
        JSONArray verses = Data.verses(a);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        JSONObject v = verses.optJSONObject(verses.length() == 0 ? 0 : day % verses.length());
        LinearLayout p = Ui.vertical(a);
        p.setGravity(Gravity.CENTER);
        p.setBackground(Ui.round(Ui.PANEL, Ui.dp(a, 22)));
        p.setPadding(Ui.dp(a, 24), Ui.dp(a, 18), Ui.dp(a, 24), Ui.dp(a, 18));
        TextView label = Ui.text(a, "✦ " + a.getString(R.string.ayah_of_day) + " ✦", 14, Ui.GOLD_DIM, false);
        Ui.center(label);
        p.addView(label, Ui.lp(Ui.MATCH, Ui.WRAP));
        if (v != null) {
            TextView t = Ui.text(a, v.optString("t"), 28, Ui.TEXT, true);
            t.setTextDirection(View.TEXT_DIRECTION_RTL);
            Ui.center(t);
            t.setLineSpacing(0, 1.25f);
            LinearLayout.LayoutParams tp = Ui.lp(Ui.MATCH, Ui.WRAP);
            tp.topMargin = Ui.dp(a, 10);
            p.addView(t, tp);
            TextView r = Ui.text(a, v.optString("r"), 15, Ui.GOLD, false);
            Ui.center(r);
            p.addView(r, Ui.lp(Ui.MATCH, Ui.WRAP));
        }
        return p;
    }

    /** Adds a tile for an item of channels.json and returns it. */
    private Card channelTile(String pageId, int section, int item, List<View> into) {
        JSONObject sec = Data.page(a, pageId).optJSONObject(section);
        JSONObject it = sec == null ? null : sec.optJSONArray("items").optJSONObject(item);
        if (it == null) return null;
        Card c = ChannelsPage.card(a, it);
        into.add(c);
        return c;
    }

    @Override
    void tick(long now) {
        long[] n = Clock.next(a, now);
        TimeZone z = Data.zone(Data.city(a));
        nextName.setText(Data.prayerName(a, (int) n[0]));
        nextTime.setText(Clock.time(new java.util.Date(n[1]), z));
        countdown.setText(Clock.countdown(n[1] - now));

        // Show the day of the next prayer: after Isha that is tomorrow.
        Calendar cal = Calendar.getInstance(z);
        cal.setTimeInMillis(Clock.next(a, now)[1]);
        int[] t = Clock.today(a, cal);
        for (int i = 0; i < 5; i++) {
            int p = Clock.FIVE[i];
            mini[i].setText(t[p] < 0 ? "--:--" : Clock.hm(t[p]));
            boolean isNext = p == n[0];
            miniBox[i].setBackground(isNext ? Ui.gradient(0x55E8C26A, 0x22E8C26A, Ui.dp(a, 14)) : Ui.round(Ui.PANEL, Ui.dp(a, 14)));
            mini[i].setTextColor(isNext ? Ui.GOLD : Ui.TEXT);
        }
    }

    @Override
    View firstFocus() { return firstCard; }
}
