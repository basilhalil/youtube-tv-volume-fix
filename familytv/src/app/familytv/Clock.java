package app.familytv;

import android.content.Context;
import android.icu.util.IslamicCalendar;
import android.icu.util.ULocale;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Clock, Gregorian and Hijri dates, and "what is the next prayer" for the chosen city. */
final class Clock {
    private Clock() {}

    static final int[] FIVE = {PrayerTimes.FAJR, PrayerTimes.DHUHR, PrayerTimes.ASR, PrayerTimes.MAGHRIB, PrayerTimes.ISHA};

    static Locale locale() { return Locale.getDefault(); }

    static String time(Date d, TimeZone z) {
        SimpleDateFormat f = new SimpleDateFormat("HH:mm", locale());
        f.setTimeZone(z);
        return f.format(d);
    }

    static String gregorian(Date d, TimeZone z) {
        SimpleDateFormat f = new SimpleDateFormat("EEEE، d MMMM yyyy", locale());
        if (!"ar".equals(Data.lang())) f = new SimpleDateFormat("EEEE, d MMMM yyyy", locale());
        f.setTimeZone(z);
        return f.format(d);
    }

    /** Hijri date by the Umm al-Qura calendar (Saudi official), localized month names. */
    static String hijri(Date d, TimeZone z) {
        try {
            // Western digits, to match the clock and the Gregorian date beside it.
            ULocale ul = new ULocale(Data.lang() + "@calendar=islamic-umalqura;numbers=latn");
            IslamicCalendar cal = new IslamicCalendar(android.icu.util.TimeZone.getTimeZone(z.getID()), ul);
            cal.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA);
            cal.setTime(d);
            android.icu.text.SimpleDateFormat f = new android.icu.text.SimpleDateFormat("d MMMM y", ul);
            f.setCalendar(cal);
            String s = f.format(d);
            return "ar".equals(Data.lang()) ? s + " هـ" : s;
        } catch (Throwable t) {
            return "";
        }
    }

    static String hm(int minutes) {
        return String.format(locale(), "%02d:%02d", minutes / 60, minutes % 60);
    }

    /** "2:05:30" or "45:10" until the target. */
    static String countdown(long ms) {
        long s = Math.max(0, ms / 1000);
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        return h > 0 ? String.format(locale(), "%d:%02d:%02d", h, m, sec) : String.format(locale(), "%02d:%02d", m, sec);
    }

    static int[] today(Context c, Calendar day) {
        JSONObject city = Data.city(c);
        return PrayerTimes.compute(day, city.optDouble("lat"), city.optDouble("lon"), Data.zone(city),
                Data.method(c), Data.hanafi(c));
    }

    /** {prayerIndex, epochMillis} of the next of the five daily prayers (tomorrow's Fajr after Isha). */
    static long[] next(Context c, long now) {
        JSONObject city = Data.city(c);
        TimeZone z = Data.zone(city);
        Calendar cal = Calendar.getInstance(z);
        cal.setTimeInMillis(now);
        for (int dayOffset = 0; dayOffset < 2; dayOffset++) {
            Calendar day = (Calendar) cal.clone();
            day.add(Calendar.DAY_OF_MONTH, dayOffset);
            int[] t = today(c, day);
            for (int p : FIVE) {
                if (t[p] < 0) continue;
                Calendar at = (Calendar) day.clone();
                at.set(Calendar.HOUR_OF_DAY, t[p] / 60);
                at.set(Calendar.MINUTE, t[p] % 60);
                at.set(Calendar.SECOND, 0);
                at.set(Calendar.MILLISECOND, 0);
                if (at.getTimeInMillis() > now) return new long[]{p, at.getTimeInMillis()};
            }
        }
        return new long[]{PrayerTimes.FAJR, now};
    }
}
