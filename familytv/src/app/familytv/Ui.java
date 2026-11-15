package app.familytv;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared look: palette, sizes and small view factories. Everything is built in code (no layouts). */
final class Ui {
    static final int BG_TOP = 0xFF06231B;
    static final int BG_BOTTOM = 0xFF020B09;
    static final int EMERALD = 0xFF0B3D2E;
    static final int EMERALD_LIGHT = 0xFF145A44;
    static final int GOLD = 0xFFE8C26A;
    static final int GOLD_DIM = 0x99E8C26A;
    static final int TEXT = 0xFFF5F1E6;
    static final int TEXT_DIM = 0xB3F5F1E6;
    static final int PANEL = 0x33000000;
    static final int PANEL_LIGHT = 0x1AFFFFFF;
    static final int LIVE_RED = 0xFFE53935;

    private Ui() {}

    static int dp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }

    static TextView text(Context c, CharSequence s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        t.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        t.setIncludeFontPadding(true);
        return t;
    }

    static GradientDrawable round(int color, float radiusPx) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return g;
    }

    static GradientDrawable gradient(int from, int to, float radiusPx) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{from, to});
        g.setCornerRadius(radiusPx);
        return g;
    }

    static int darker(int color, float f) {
        return Color.argb(Color.alpha(color), (int) (Color.red(color) * f), (int) (Color.green(color) * f),
                (int) (Color.blue(color) * f));
    }

    static int parse(String hex, int fallback) {
        try { return Color.parseColor(hex); } catch (Exception e) { return fallback; }
    }

    static LinearLayout vertical(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    static LinearLayout horizontal(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    static LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }

    static LinearLayout.LayoutParams lp(int w, int h, float weight) { return new LinearLayout.LayoutParams(w, h, weight); }

    static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    /** A section heading with a short gold underline. */
    static View heading(Context c, CharSequence title) {
        LinearLayout box = vertical(c);
        TextView t = text(c, title, 22, TEXT, true);
        box.addView(t);
        View line = new View(c);
        line.setBackground(round(GOLD, dp(c, 2)));
        LinearLayout.LayoutParams p = lp(dp(c, 48), dp(c, 3));
        p.topMargin = dp(c, 4);
        box.addView(line, p);
        box.setPadding(0, dp(c, 18), 0, dp(c, 12));
        return box;
    }

    /** Lays out views in rows of {@code perRow}; rows follow the layout direction (right-to-left in Arabic). */
    static void grid(Context c, LinearLayout parent, java.util.List<View> items, int perRow, int heightDp) {
        LinearLayout row = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % perRow == 0) {
                row = horizontal(c);
                parent.addView(row, lp(MATCH, WRAP));
            }
            LinearLayout.LayoutParams p = lp(0, dp(c, heightDp), 1);
            int m = dp(c, 8);
            p.setMargins(m, m, m, m);
            row.addView(items.get(i), p);
        }
        // Keep the last row's cards the same width as the others.
        if (row != null) {
            int missing = (perRow - items.size() % perRow) % perRow;
            for (int i = 0; i < missing; i++) {
                View spacer = new View(c);
                LinearLayout.LayoutParams p = lp(0, dp(c, heightDp), 1);
                int m = dp(c, 8);
                p.setMargins(m, m, m, m);
                row.addView(spacer, p);
            }
        }
    }

    static void center(TextView t) {
        t.setGravity(Gravity.CENTER);
        t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }
}
