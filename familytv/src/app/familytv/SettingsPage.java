package app.familytv;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** City, calculation method and Asr school, plus the about/dedication text. */
final class SettingsPage extends Page {
    private View first;

    SettingsPage(MainActivity a) { super(a); }

    @Override
    View view() {
        ScrollView sv = new ScrollView(a);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipToPadding(false);
        LinearLayout col = Ui.vertical(a);
        col.setPadding(Ui.dp(a, 4), 0, Ui.dp(a, 4), Ui.dp(a, 30));
        sv.addView(col);

        String selectedCity = Data.city(a).optString("id");
        col.addView(Ui.heading(a, "📍 " + a.getString(R.string.city)));
        JSONArray cities = Data.cities(a);
        List<View> cityCards = new ArrayList<>();
        for (int i = 0; i < cities.length(); i++) {
            final JSONObject c = cities.optJSONObject(i);
            boolean sel = c.optString("id").equals(selectedCity);
            Card card = new Card(a, null, (sel ? "✓  " : "") + Data.cityName(c), null,
                    sel ? 0xFF8D6E2A : 0xFF1E3A31, false);
            card.onClick(() -> { Data.setCity(a, c); a.refresh(); });
            if (sel) first = card;
            cityCards.add(card);
        }
        Ui.grid(a, col, cityCards, 6, 78);

        col.addView(Ui.heading(a, "🧭 " + a.getString(R.string.method)));
        PrayerTimes.Method current = Data.method(a);
        List<View> methods = new ArrayList<>();
        for (final PrayerTimes.Method m : PrayerTimes.ALL) {
            boolean sel = m == current;
            methods.add(new Card(a, null, (sel ? "✓  " : "") + Data.methodName(a, m), null, sel ? 0xFF8D6E2A : 0xFF1E3A31, false)
                    .onClick(() -> { Data.setMethod(a, m); a.refresh(); }));
        }
        Ui.grid(a, col, methods, 3, 84);

        col.addView(Ui.heading(a, "🌤 " + a.getString(R.string.asr_method)));
        boolean hanafi = Data.hanafi(a);
        List<View> asr = new ArrayList<>();
        asr.add(new Card(a, null, (!hanafi ? "✓  " : "") + a.getString(R.string.asr_standard), null,
                !hanafi ? 0xFF8D6E2A : 0xFF1E3A31, false).onClick(() -> { Data.setHanafi(a, false); a.refresh(); }));
        asr.add(new Card(a, null, (hanafi ? "✓  " : "") + a.getString(R.string.asr_hanafi), null,
                hanafi ? 0xFF8D6E2A : 0xFF1E3A31, false).onClick(() -> { Data.setHanafi(a, true); a.refresh(); }));
        Ui.grid(a, col, asr, 2, 84);

        LinearLayout about = Ui.vertical(a);
        about.setBackground(Ui.round(Ui.PANEL, Ui.dp(a, 18)));
        about.setPadding(Ui.dp(a, 22), Ui.dp(a, 18), Ui.dp(a, 22), Ui.dp(a, 18));
        TextView ab = Ui.text(a, a.getString(R.string.about), 15, Ui.TEXT, false);
        TextView ded = Ui.text(a, "🤲  " + a.getString(R.string.dedication), 15, Ui.GOLD, false);
        ded.setPadding(0, Ui.dp(a, 10), 0, 0);
        TextView ver = Ui.text(a, a.getString(R.string.app_name) + " 1.0 · " + a.getString(R.string.mp3quran_credit), 12, Ui.TEXT_DIM, false);
        ver.setPadding(0, Ui.dp(a, 10), 0, 0);
        about.addView(ab);
        about.addView(ded);
        about.addView(ver);
        LinearLayout.LayoutParams ap = Ui.lp(Ui.MATCH, Ui.WRAP);
        ap.topMargin = Ui.dp(a, 22);
        col.addView(about, ap);
        return sv;
    }

    @Override
    View firstFocus() { return first; }
}
