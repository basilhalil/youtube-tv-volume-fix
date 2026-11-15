package app.familytv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/** Shell: navigation rail, top bar (clock + Hijri date), page host, now-playing bar and the adhan banner. */
public final class MainActivity extends Activity implements Player.Listener {
    static final String[] NAV = {"home", "islamic", "quran", "prayer", "arabic", "kids", "turkish", "settings"};
    static final String[] ICONS = {"⌂", "🕋", "📖", "🕰", "📺", "🎈", "🇹🇷", "⚙"};
    static final int[] LABELS = {R.string.nav_home, R.string.nav_islamic, R.string.nav_quran, R.string.nav_prayer,
            R.string.nav_arabic, R.string.nav_kids, R.string.nav_turkish, R.string.nav_settings};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TextView> navItems = new ArrayList<>();
    private FrameLayout host;
    private TextView pageTitle, clock, dates, nowTitle, nowSub, pauseBtn, banner;
    private LinearLayout nowBar;
    private Page page;
    private int current = -1;
    private Runnable pendingSwitch;
    private String lastAdhanKey = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FrameLayout root = new FrameLayout(this);
        root.addView(new PatternView(this));

        LinearLayout main = Ui.horizontal(this);
        root.addView(main, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        main.addView(buildRail(), Ui.lp(Ui.dp(this, 270), Ui.MATCH));

        LinearLayout content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 28), Ui.dp(this, 20), Ui.dp(this, 36), Ui.dp(this, 14));
        main.addView(content, Ui.lp(0, Ui.MATCH, 1));

        content.addView(buildTopBar(), Ui.lp(Ui.MATCH, Ui.WRAP));
        host = new FrameLayout(this);
        content.addView(host, Ui.lp(Ui.MATCH, 0, 1));
        content.addView(buildNowBar(), Ui.lp(Ui.MATCH, Ui.WRAP));

        banner = Ui.text(this, "", 26, 0xFF1B1300, true);
        banner.setBackground(Ui.gradient(0xFFF3D58A, Ui.GOLD, Ui.dp(this, 22)));
        banner.setPadding(Ui.dp(this, 36), Ui.dp(this, 18), Ui.dp(this, 36), Ui.dp(this, 18));
        banner.setVisibility(View.GONE);
        banner.setElevation(Ui.dp(this, 20));
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        bp.topMargin = Ui.dp(this, 28);
        root.addView(banner, bp);

        setContentView(root);
        show(0, false);
        navItems.get(0).requestFocus();
    }

    // ---------- layout pieces ----------

    private View buildRail() {
        LinearLayout rail = Ui.vertical(this);
        rail.setBackgroundColor(0x40000000);
        rail.setPadding(Ui.dp(this, 16), Ui.dp(this, 26), Ui.dp(this, 16), Ui.dp(this, 16));

        TextView logo = Ui.text(this, "☾ " + getString(R.string.app_name), 26, Ui.GOLD, true);
        rail.addView(logo);
        TextView tag = Ui.text(this, getString(R.string.tagline), 13, Ui.TEXT_DIM, false);
        rail.addView(tag);
        View gap = new View(this);
        rail.addView(gap, Ui.lp(1, Ui.dp(this, 24)));

        for (int i = 0; i < NAV.length; i++) {
            final int idx = i;
            TextView item = Ui.text(this, ICONS[i] + "   " + getString(LABELS[i]), 17, Ui.TEXT, false);
            item.setFocusable(true);
            item.setClickable(true);
            item.setSingleLine(true);
            item.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12));
            item.setOnFocusChangeListener((v, has) -> {
                styleNav(idx);
                if (has && idx != current) {
                    // Switch pages shortly after the focus settles, so scrolling through the rail stays smooth.
                    if (pendingSwitch != null) handler.removeCallbacks(pendingSwitch);
                    pendingSwitch = () -> show(idx, false);
                    handler.postDelayed(pendingSwitch, 280);
                }
            });
            item.setOnClickListener(v -> {
                if (pendingSwitch != null) handler.removeCallbacks(pendingSwitch);
                show(idx, true);
            });
            LinearLayout.LayoutParams p = Ui.lp(Ui.MATCH, Ui.WRAP);
            p.bottomMargin = Ui.dp(this, 6);
            rail.addView(item, p);
            navItems.add(item);
        }
        return rail;
    }

    private void styleNav(int idx) {
        TextView v = navItems.get(idx);
        boolean focused = v.hasFocus();
        boolean selected = idx == current;
        if (focused) {
            v.setBackground(Ui.round(Ui.GOLD, Ui.dp(this, 14)));
            v.setTextColor(0xFF1B1300);
        } else if (selected) {
            v.setBackground(Ui.round(0x33E8C26A, Ui.dp(this, 14)));
            v.setTextColor(Ui.GOLD);
        } else {
            v.setBackground(null);
            v.setTextColor(Ui.TEXT);
        }
    }

    private View buildTopBar() {
        LinearLayout bar = Ui.horizontal(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        pageTitle = Ui.text(this, "", 30, Ui.TEXT, true);
        bar.addView(pageTitle, Ui.lp(0, Ui.WRAP, 1));
        LinearLayout right = Ui.vertical(this);
        right.setGravity(Gravity.END);
        clock = Ui.text(this, "", 30, Ui.GOLD, true);
        clock.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        dates = Ui.text(this, "", 14, Ui.TEXT_DIM, false);
        dates.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        right.addView(clock, Ui.lp(Ui.WRAP, Ui.WRAP));
        right.addView(dates, Ui.lp(Ui.WRAP, Ui.WRAP));
        bar.addView(right, Ui.lp(Ui.WRAP, Ui.WRAP));
        return bar;
    }

    private View buildNowBar() {
        nowBar = Ui.horizontal(this);
        nowBar.setGravity(Gravity.CENTER_VERTICAL);
        nowBar.setBackground(Ui.gradient(Ui.EMERALD_LIGHT, Ui.EMERALD, Ui.dp(this, 18)));
        nowBar.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));
        nowBar.setVisibility(View.GONE);

        TextView icon = Ui.text(this, "🎧", 26, Ui.GOLD, false);
        nowBar.addView(icon);
        LinearLayout info = Ui.vertical(this);
        info.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        nowTitle = Ui.text(this, "", 17, Ui.TEXT, true);
        nowTitle.setSingleLine(true);
        nowSub = Ui.text(this, "", 13, Ui.TEXT_DIM, false);
        nowSub.setSingleLine(true);
        info.addView(nowTitle);
        info.addView(nowSub);
        nowBar.addView(info, Ui.lp(0, Ui.WRAP, 1));

        pauseBtn = pill(getString(R.string.pause), Player::togglePause);
        nowBar.addView(pauseBtn);
        TextView stop = pill(getString(R.string.stop), Player::stop);
        LinearLayout.LayoutParams sp = Ui.lp(Ui.WRAP, Ui.WRAP);
        sp.setMarginStart(Ui.dp(this, 10));
        nowBar.addView(stop, sp);

        LinearLayout.LayoutParams p = Ui.lp(Ui.MATCH, Ui.WRAP);
        nowBar.setLayoutParams(p);
        return nowBar;
    }

    TextView pill(String label, Runnable action) {
        TextView t = Ui.text(this, label, 15, Ui.TEXT, true);
        t.setFocusable(true);
        t.setClickable(true);
        t.setPadding(Ui.dp(this, 18), Ui.dp(this, 8), Ui.dp(this, 18), Ui.dp(this, 8));
        t.setBackground(Ui.round(Ui.PANEL_LIGHT, Ui.dp(this, 16)));
        t.setOnFocusChangeListener((v, has) -> {
            t.setBackground(Ui.round(has ? Ui.GOLD : Ui.PANEL_LIGHT, Ui.dp(this, 16)));
            t.setTextColor(has ? 0xFF1B1300 : Ui.TEXT);
        });
        t.setOnClickListener(v -> action.run());
        return t;
    }

    // ---------- navigation ----------

    void show(int idx, boolean focusContent) {
        if (idx != current) {
            current = idx;
            if (pendingSwitch != null) handler.removeCallbacks(pendingSwitch);
            // Removing the old page must not drop focus onto some other menu item (which would switch pages).
            if (host.hasFocus()) navItems.get(idx).requestFocus();
            for (int i = 0; i < navItems.size(); i++) styleNav(i);
            pageTitle.setText(getString(LABELS[idx]));
            page = createPage(NAV[idx]);
            host.removeAllViews();
            View v = page.view();
            v.setAlpha(0f);
            host.addView(v, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            v.animate().alpha(1f).setDuration(180).start();
            page.tick(System.currentTimeMillis());
        }
        if (focusContent) focusPage();
    }

    void focusPage() {
        View f = page == null ? null : page.firstFocus();
        if (f != null) f.requestFocus();
    }

    /** Rebuilds the current page (after a setting changed). */
    void refresh() {
        int idx = current;
        boolean inContent = getCurrentFocus() != null && !navItems.contains(getCurrentFocus());
        current = -1;
        show(idx, inContent);
    }

    private Page createPage(String id) {
        switch (id) {
            case "home": return new HomePage(this);
            case "quran": return new QuranPage(this);
            case "prayer": return new PrayerPage(this);
            case "settings": return new SettingsPage(this);
            default: return new ChannelsPage(this, id);
        }
    }

    void goTo(String id) {
        for (int i = 0; i < NAV.length; i++) if (NAV[i].equals(id)) { show(i, true); return; }
    }

    // ---------- YouTube ----------

    /** Opens the broadcaster's official channel (its live stream when {@code live}) in the YouTube app. */
    void openYouTube(String channelId, boolean live) {
        Uri uri = Uri.parse("https://www.youtube.com/channel/" + channelId + (live ? "/live" : ""));
        String[] apps = {"com.google.android.youtube.tv", "com.google.android.youtube", null};
        for (String pkg : apps) {
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            if (pkg != null) i.setPackage(pkg);
            try { startActivity(i); return; } catch (ActivityNotFoundException ignored) { }
        }
        Toast.makeText(this, R.string.no_youtube, Toast.LENGTH_LONG).show();
    }

    // ---------- lifecycle & ticking ----------

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            tick();
            handler.postDelayed(this, 1000 - (System.currentTimeMillis() % 1000));
        }
    };

    private void tick() {
        long now = System.currentTimeMillis();
        JSONObject city = Data.city(this);
        TimeZone z = Data.zone(city);
        Date d = new Date(now);
        clock.setText(Clock.time(d, z));
        dates.setText(Clock.gregorian(d, z) + "   ·   " + Clock.hijri(d, z));
        if (page != null) page.tick(now);
        checkAdhan(now, z);
    }

    private void checkAdhan(long now, TimeZone z) {
        Calendar cal = Calendar.getInstance(z);
        cal.setTimeInMillis(now);
        int minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        int[] t = Clock.today(this, cal);
        for (int p : Clock.FIVE) {
            String key = cal.get(Calendar.DAY_OF_YEAR) + "-" + p;
            if (t[p] == minute && !key.equals(lastAdhanKey)) {
                lastAdhanKey = key;
                banner.setText("🕌  " + getString(R.string.adhan_now, Data.prayerName(this, p)));
                banner.setVisibility(View.VISIBLE);
                banner.setTranslationY(-Ui.dp(this, 120));
                banner.animate().translationY(0).setDuration(400).start();
                handler.postDelayed(() -> banner.animate().translationY(-Ui.dp(this, 160)).setDuration(400)
                        .withEndAction(() -> banner.setVisibility(View.GONE)).start(), 90_000);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Player.listen(this);
        changed();
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
        Player.unlisten(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) Player.stop();
    }

    @Override
    public void changed() {
        Player.Track t = Player.current();
        if (t == null) {
            nowBar.setVisibility(View.GONE);
            return;
        }
        nowBar.setVisibility(View.VISIBLE);
        nowTitle.setText(t.title);
        nowSub.setText(Player.preparing() ? getString(R.string.loading) : t.subtitle);
        pauseBtn.setText(Player.paused() ? getString(R.string.play) : getString(R.string.pause));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            Player.togglePause();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) { Player.stop(); return true; }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (page != null && page.back()) return;
        View f = getCurrentFocus();
        if (f != null && !navItems.contains(f)) {
            // First Back returns to the menu; a second Back on the menu leaves the app.
            navItems.get(Math.max(current, 0)).requestFocus();
            return;
        }
        super.onBackPressed();
    }

    ViewGroup host() { return host; }
}
