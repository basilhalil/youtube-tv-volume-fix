package app.familytv;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/** Today's six times (next one highlighted with a countdown) and the coming week. */
final class PrayerPage extends Page {
    private final LinearLayout[] boxes = new LinearLayout[6];
    private final TextView[] times = new TextView[6];
    private final TextView[] counters = new TextView[6];
    private TextView change;

    PrayerPage(MainActivity a) { super(a); }

    @Override
    View view() {
        JSONObject city = Data.city(a);
        TimeZone z = Data.zone(city);
        ScrollView sv = new ScrollView(a);
        sv.setVerticalScrollBarEnabled(false);
        LinearLayout col = Ui.vertical(a);
        col.setPadding(0, Ui.dp(a, 10), 0, Ui.dp(a, 24));
        sv.addView(col);

        LinearLayout head = Ui.horizontal(a);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView cityName = Ui.text(a, "📍 " + Data.cityName(city), 24, Ui.GOLD, true);
        head.addView(cityName, Ui.lp(0, Ui.WRAP, 1));
        change = a.pill("⚙  " + a.getString(R.string.city), () -> a.goTo("settings"));
        head.addView(change);
        col.addView(head, Ui.lp(Ui.MATCH, Ui.WRAP));

        LinearLayout row = Ui.horizontal(a);
        LinearLayout.LayoutParams rp = Ui.lp(Ui.MATCH, Ui.WRAP);
        rp.topMargin = Ui.dp(a, 16);
        col.addView(row, rp);
        String[] icons = {"🌌", "🌅", "☀", "🌤", "🌇", "🌙"};
        for (int i = 0; i < 6; i++) {
            LinearLayout b = Ui.vertical(a);
            b.setGravity(Gravity.CENTER);
            b.setPadding(0, Ui.dp(a, 18), 0, Ui.dp(a, 18));
            TextView ic = Ui.text(a, icons[i], 28, Ui.TEXT, false);
            Ui.center(ic);
            TextView name = Ui.text(a, Data.prayerName(a, i), 18, Ui.TEXT_DIM, true);
            Ui.center(name);
            times[i] = Ui.text(a, "--:--", 34, Ui.TEXT, true);
            Ui.center(times[i]);
            counters[i] = Ui.text(a, "", 14, Ui.GOLD, false);
            Ui.center(counters[i]);
            b.addView(ic);
            b.addView(name);
            b.addView(times[i]);
            b.addView(counters[i]);
            boxes[i] = b;
            LinearLayout.LayoutParams p = Ui.lp(0, Ui.WRAP, 1);
            p.setMargins(Ui.dp(a, 6), 0, Ui.dp(a, 6), 0);
            row.addView(b, p);
        }

        // The coming seven days.
        col.addView(Ui.heading(a, "🗓"));
        Calendar day = Calendar.getInstance(z);
        SimpleDateFormat df = new SimpleDateFormat("EEE d MMM", Clock.locale());
        df.setTimeZone(z);
        LinearLayout table = Ui.vertical(a);
        table.setBackground(Ui.round(Ui.PANEL, Ui.dp(a, 18)));
        table.setPadding(Ui.dp(a, 16), Ui.dp(a, 8), Ui.dp(a, 16), Ui.dp(a, 8));
        table.addView(tableRow(null, null, true));
        for (int d = 0; d < 7; d++) {
            int[] t = Clock.today(a, day);
            table.addView(tableRow(df.format(day.getTime()) + (d == 0 ? "  •" : ""), t, false));
            day.add(Calendar.DAY_OF_MONTH, 1);
        }
        col.addView(table, Ui.lp(Ui.MATCH, Ui.WRAP));

        TextView note = Ui.text(a, a.getString(R.string.prayer_note, Data.cityName(city),
                Data.methodName(a, Data.method(a))), 13, Ui.TEXT_DIM, false);
        note.setPadding(Ui.dp(a, 8), Ui.dp(a, 14), Ui.dp(a, 8), 0);
        col.addView(note);
        return sv;
    }

    private View tableRow(String label, int[] t, boolean header) {
        LinearLayout r = Ui.horizontal(a);
        r.setPadding(0, Ui.dp(a, 7), 0, Ui.dp(a, 7));
        TextView l = Ui.text(a, header ? "" : label, 16, header ? Ui.GOLD : Ui.TEXT, header);
        r.addView(l, Ui.lp(0, Ui.WRAP, 1.6f));
        for (int i = 0; i < 6; i++) {
            String s = header ? Data.prayerName(a, i) : (t[i] < 0 ? "--:--" : Clock.hm(t[i]));
            TextView c = Ui.text(a, s, 16, header ? Ui.GOLD : Ui.TEXT, header);
            Ui.center(c);
            r.addView(c, Ui.lp(0, Ui.WRAP, 1));
        }
        return r;
    }

    @Override
    void tick(long now) {
        TimeZone z = Data.zone(Data.city(a));
        // Show the day of the next prayer: after Isha that is tomorrow.
        Calendar cal = Calendar.getInstance(z);
        cal.setTimeInMillis(Clock.next(a, now)[1]);
        int[] t = Clock.today(a, cal);
        long[] next = Clock.next(a, now);
        for (int i = 0; i < 6; i++) {
            times[i].setText(t[i] < 0 ? "--:--" : Clock.hm(t[i]));
            boolean isNext = i == next[0];
            boxes[i].setBackground(isNext ? Ui.gradient(0xFF2A6B4F, 0xFF0D3326, Ui.dp(a, 20))
                    : Ui.round(Ui.PANEL, Ui.dp(a, 20)));
            times[i].setTextColor(isNext ? Ui.GOLD : Ui.TEXT);
            counters[i].setText(isNext ? a.getString(R.string.in_time, Clock.countdown(next[1] - now)) : "");
        }
    }

    @Override
    View firstFocus() { return change; }
}
