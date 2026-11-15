package app.familytv;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** A focusable tile: glyph, title, optional subtitle and LIVE badge. Grows and gets a gold frame when focused. */
final class Card extends FrameLayout {
    private final GradientDrawable frame;
    private Runnable onClick;

    Card(Context c, String glyph, CharSequence title, CharSequence subtitle, int color, boolean live) {
        super(c);
        setFocusable(true);
        setClickable(true);
        setClipToOutline(false);
        float r = Ui.dp(c, 18);

        GradientDrawable body = Ui.gradient(color, Ui.darker(color, 0.45f), r);
        frame = new GradientDrawable();
        frame.setCornerRadius(r);
        frame.setColor(0x00000000);
        frame.setStroke(Ui.dp(c, 3), 0x00000000);
        setBackground(new LayerDrawable(new android.graphics.drawable.Drawable[]{body, frame}));

        LinearLayout col = Ui.vertical(c);
        col.setGravity(Gravity.CENTER);
        int pad = Ui.dp(c, 12);
        col.setPadding(pad, pad, pad, pad);

        if (glyph != null && !glyph.isEmpty()) {
            TextView g = Ui.text(c, glyph, glyph.length() <= 2 ? 34 : 24, 0xEEFFFFFF, true);
            Ui.center(g);
            col.addView(g, Ui.lp(Ui.WRAP, Ui.WRAP));
        }
        TextView t = Ui.text(c, title, 18, Ui.TEXT, true);
        Ui.center(t);
        t.setMaxLines(2);
        col.addView(t, Ui.lp(Ui.WRAP, Ui.WRAP));
        if (subtitle != null && subtitle.length() > 0) {
            TextView s = Ui.text(c, subtitle, 13, Ui.TEXT_DIM, false);
            Ui.center(s);
            s.setMaxLines(2);
            col.addView(s, Ui.lp(Ui.WRAP, Ui.WRAP));
        }
        addView(col, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (live) {
            TextView badge = Ui.text(c, "● " + c.getString(R.string.live), 11, 0xFFFFFFFF, true);
            badge.setBackground(Ui.round(Ui.LIVE_RED, Ui.dp(c, 8)));
            badge.setPadding(Ui.dp(c, 8), Ui.dp(c, 2), Ui.dp(c, 8), Ui.dp(c, 2));
            LayoutParams bp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.START);
            bp.setMargins(Ui.dp(c, 10), Ui.dp(c, 10), Ui.dp(c, 10), 0);
            addView(badge, bp);
        }

        setOnFocusChangeListener((v, has) -> {
            frame.setStroke(Ui.dp(c, 3), has ? Ui.GOLD : 0x00000000);
            animate().scaleX(has ? 1.07f : 1f).scaleY(has ? 1.07f : 1f).translationZ(has ? Ui.dp(c, 12) : 0)
                    .setDuration(140).start();
        });
        setOnClickListener(v -> { if (onClick != null) onClick.run(); });
    }

    Card onClick(Runnable r) { this.onClick = r; return this; }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            animate().scaleX(0.97f).scaleY(0.97f).setDuration(60).withEndAction(() ->
                    animate().scaleX(1.07f).scaleY(1.07f).setDuration(90).start()).start();
        }
        return super.onKeyDown(keyCode, event);
    }
}
