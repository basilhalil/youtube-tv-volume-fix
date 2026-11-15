package app.familytv;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** A page of channel tiles from channels.json. Each tile opens the broadcaster's official YouTube channel. */
final class ChannelsPage extends Page {
    private final String id;
    private View first;

    ChannelsPage(MainActivity a, String id) { super(a); this.id = id; }

    static Card card(MainActivity a, JSONObject it) {
        final String yt = it.optString("yt");
        final boolean live = it.optBoolean("live");
        String sub = Data.pick(it.optJSONObject("d"));
        if (sub.isEmpty()) sub = live ? "" : a.getString(R.string.opens_youtube);
        Card c = new Card(a, it.optString("g"), Data.pick(it.optJSONObject("n")), sub,
                Ui.parse(it.optString("c"), Ui.EMERALD_LIGHT), live);
        c.onClick(() -> a.openYouTube(yt, live));
        return c;
    }

    @Override
    View view() {
        ScrollView scroll = new ScrollView(a);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        LinearLayout col = Ui.vertical(a);
        col.setPadding(Ui.dp(a, 4), 0, Ui.dp(a, 4), Ui.dp(a, 24));
        scroll.addView(col);

        JSONArray sections = Data.page(a, id);
        for (int s = 0; s < sections.length(); s++) {
            JSONObject sec = sections.optJSONObject(s);
            col.addView(Ui.heading(a, Data.pick(sec.optJSONObject("title"))));
            JSONArray items = sec.optJSONArray("items");
            List<View> big = new ArrayList<>(), small = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject it = items.optJSONObject(i);
                Card c = card(a, it);
                if (first == null) first = c;
                (it.optBoolean("big") ? big : small).add(c);
            }
            if (!big.isEmpty()) Ui.grid(a, col, big, 2, 210);
            if (!small.isEmpty()) Ui.grid(a, col, small, 4, 150);
        }

        TextView note = Ui.text(a, "▶  " + a.getString(R.string.about), 13, Ui.TEXT_DIM, false);
        note.setPadding(Ui.dp(a, 8), Ui.dp(a, 18), Ui.dp(a, 8), 0);
        col.addView(note);
        return scroll;
    }

    @Override
    View firstFocus() { return first; }
}
