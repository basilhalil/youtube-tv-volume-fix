/*
 * YouTube Volume Fix - Copyright (C) 2026 basilhalil
 * Licensed under the GNU GPL v3 with an additional term (section 7b): see LICENSE and NOTICE.
 *
 * This app is dedicated to the soul of the late Ahmed Khalil (المرحوم أحمد خليل).
 * The dedication shown in the app must be kept in any copy or modified version.
 * Request: please also keep the message asking users to recite Al-Fatiha for his soul.
 */
package app.ytvolumefix;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String ADB_CMD =
            "adb shell settings put secure enabled_accessibility_services "
            + "app.ytvolumefix/app.ytvolumefix.VolumeKeyService\n"
            + "adb shell settings put secure accessibility_enabled 1";

    private TextView status;
    private Button enable;
    private Button recheck;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(dp(48), dp(32), dp(48), dp(32));

        TextView title = text(getString(R.string.app_name), 30, Color.WHITE);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(title);

        status = text("", 22, Color.WHITE);
        status.setPadding(0, dp(20), 0, dp(12));
        col.addView(status);

        enable = new Button(this);
        enable.setText(R.string.open_settings);
        enable.setTextSize(20);
        enable.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showDisclosure(); }
        });
        col.addView(enable);

        recheck = new Button(this);
        recheck.setText(R.string.recheck_btn);
        recheck.setTextSize(18);
        recheck.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getSharedPreferences(VolumeKeyService.PREFS, MODE_PRIVATE).edit()
                        .putInt(VolumeKeyService.KEY_MODE, VolumeKeyService.MODE_UNKNOWN).apply();
                Toast.makeText(MainActivity.this, R.string.recheck_done, Toast.LENGTH_LONG).show();
                refreshStatus();
            }
        });
        col.addView(recheck);

        TextView howTitle = text(getString(R.string.how_title), 22, Color.WHITE);
        howTitle.setTypeface(Typeface.DEFAULT_BOLD);
        howTitle.setPadding(0, dp(28), 0, dp(6));
        col.addView(howTitle);
        col.addView(text(getString(R.string.how_msg), 18, Color.rgb(220, 220, 220)));

        TextView ded = text(getString(R.string.dedication), 18, Color.rgb(255, 214, 140));
        ded.setPadding(0, dp(28), 0, 0);
        col.addView(ded);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(32, 18, 18));
        scroll.addView(col);
        setContentView(scroll);

        showFatiha();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        enable.requestFocus();
    }

    /** Tells the user plainly whether their device needs the app, based on what the service learned. */
    private void refreshStatus() {
        VolumeKeyService.migrate(this);
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int mode = getSharedPreferences(VolumeKeyService.PREFS, MODE_PRIVATE)
                .getInt(VolumeKeyService.KEY_MODE, VolumeKeyService.MODE_UNKNOWN);
        int msg;
        boolean good;
        if (am != null && VolumeKeyService.isFixedVolume(am)) {
            msg = R.string.status_fixed; good = false;
        } else if (!isServiceEnabled()) {
            msg = R.string.status_off; good = false;
        } else if (mode == VolumeKeyService.MODE_FIX) {
            msg = R.string.status_on; good = true;
        } else if (mode == VolumeKeyService.MODE_NATIVE) {
            msg = R.string.status_native; good = false;
        } else if (mode == VolumeKeyService.MODE_UNSUPPORTED) {
            msg = R.string.status_unsupported; good = false;
        } else {
            msg = R.string.status_learning; good = true;
        }
        status.setText(msg);
        status.setTextColor(good ? Color.rgb(165, 214, 167) : Color.rgb(255, 204, 128));
        recheck.setVisibility(mode == VolumeKeyService.MODE_UNKNOWN ? View.GONE : View.VISIBLE);
    }

    /** Shown every time the app is opened, as the app's author asked. Never blocks the app. */
    private void showFatiha() {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.fatiha_title)
                .setMessage(R.string.fatiha_msg)
                .setPositiveButton(R.string.fatiha_done, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface di, int w) {
                        Toast.makeText(MainActivity.this, R.string.thanks, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.fatiha_later, null)
                .create();
        d.show();
        Button ok = d.getButton(AlertDialog.BUTTON_POSITIVE);
        if (ok != null) ok.requestFocus();
    }

    /** Explain exactly what the Accessibility service does before sending the user to turn it on. */
    private void showDisclosure() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.disclosure_title)
                .setMessage(R.string.disclosure_msg)
                .setPositiveButton(R.string.continue_btn, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface di, int w) { openAccessibilitySettings(); }
                })
                .setNegativeButton(R.string.cancel_btn, null)
                .show();
    }

    private void openAccessibilitySettings() {
        Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (i.resolveActivity(getPackageManager()) != null) {
            try {
                startActivity(i);
                return;
            } catch (Exception ignored) {
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_settings_title)
                .setMessage(getString(R.string.no_settings_msg) + "\n\n" + ADB_CMD)
                .setPositiveButton(R.string.ok_btn, null)
                .show();
    }

    private boolean isServiceEnabled() {
        if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0) != 1) {
            return false;
        }
        String list = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (list == null) return false;
        ComponentName me = new ComponentName(this, VolumeKeyService.class);
        TextUtils.SimpleStringSplitter sp = new TextUtils.SimpleStringSplitter(':');
        sp.setString(list);
        while (sp.hasNext()) {
            ComponentName c = ComponentName.unflattenFromString(sp.next());
            if (me.equals(c)) return true;
        }
        return false;
    }

    private TextView text(String s, int sp, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setLineSpacing(0, 1.15f);
        return t;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
