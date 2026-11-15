package app.familytv;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Offline prayer-time calculation (standard astronomical method).
 * Times are returned as minutes after local midnight, in the city's own time zone (DST-aware).
 * Order: FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA.
 */
public final class PrayerTimes {
    public static final int FAJR = 0, SUNRISE = 1, DHUHR = 2, ASR = 3, MAGHRIB = 4, ISHA = 5;

    /** A calculation method: twilight angles (Isha may instead be a fixed delay after Maghrib) and minute adjustments. */
    public static final class Method {
        final String id;
        final double fajrAngle, ishaAngle;
        final int ishaMinutes;           // >0: Isha = Maghrib + minutes (Umm al-Qura)
        final int[] adjust;              // per-prayer minute offsets (Diyanet "temkin")

        Method(String id, double fajr, double isha, int ishaMinutes, int[] adjust) {
            this.id = id; this.fajrAngle = fajr; this.ishaAngle = isha;
            this.ishaMinutes = ishaMinutes; this.adjust = adjust;
        }
    }

    public static final Method DIYANET = new Method("diyanet", 18, 17, 0, new int[]{0, -7, 5, 4, 7, 0});
    public static final Method MWL = new Method("mwl", 18, 17, 0, new int[]{0, 0, 1, 0, 1, 0});
    public static final Method UMM_AL_QURA = new Method("umm_al_qura", 18.5, 0, 90, new int[]{0, 0, 1, 0, 1, 0});
    public static final Method EGYPT = new Method("egypt", 19.5, 17.5, 0, new int[]{0, 0, 1, 0, 1, 0});
    public static final Method KARACHI = new Method("karachi", 18, 18, 0, new int[]{0, 0, 1, 0, 1, 0});
    public static final Method ISNA = new Method("isna", 15, 15, 0, new int[]{0, 0, 1, 0, 1, 0});
    public static final Method[] ALL = {DIYANET, UMM_AL_QURA, MWL, EGYPT, KARACHI, ISNA};

    public static Method byId(String id) {
        for (Method m : ALL) if (m.id.equals(id)) return m;
        return MWL;
    }

    private PrayerTimes() {}

    /**
     * @param day     any instant on the wanted local date (its Y/M/D are read in {@code zone})
     * @param hanafi  true: Asr when shadow = 2x object length; false: 1x (Shafi'i, Maliki, Hanbali)
     * @return minutes after local midnight for the six times
     */
    public static int[] compute(Calendar day, double lat, double lon, TimeZone zone, Method m, boolean hanafi) {
        Calendar c = Calendar.getInstance(zone);
        c.setTimeInMillis(day.getTimeInMillis());
        int y = c.get(Calendar.YEAR), mo = c.get(Calendar.MONTH) + 1, d = c.get(Calendar.DAY_OF_MONTH);
        c.set(y, mo - 1, d, 12, 0, 0);
        double tz = zone.getOffset(c.getTimeInMillis()) / 3600000.0;
        double jd = julian(y, mo, d) - lon / 360.0;

        // First pass with rough guesses, then refine each time once with its own sun position.
        double[] t = {5, 6, 12, 13, 18, 18};
        for (int pass = 0; pass < 2; pass++) {
            double[] f = t.clone();
            for (int i = 0; i < 6; i++) f[i] = f[i] / 24.0;
            t[FAJR] = sunAngleTime(jd, f[FAJR], m.fajrAngle, lat, true);
            t[SUNRISE] = sunAngleTime(jd, f[SUNRISE], 0.833, lat, true);
            t[DHUHR] = midDay(jd, f[DHUHR]);
            t[ASR] = asrTime(jd, f[ASR], hanafi ? 2 : 1, lat);
            t[MAGHRIB] = sunAngleTime(jd, f[MAGHRIB], 0.833, lat, false);
            t[ISHA] = sunAngleTime(jd, f[ISHA], m.ishaAngle > 0 ? m.ishaAngle : 18, lat, false);
        }

        int[] out = new int[6];
        for (int i = 0; i < 6; i++) {
            double h = t[i] + tz - lon / 15.0;
            if (m.ishaMinutes > 0 && i == ISHA) h = t[MAGHRIB] + tz - lon / 15.0 + m.ishaMinutes / 60.0;
            if (Double.isNaN(h)) { out[i] = -1; continue; }
            out[i] = (int) Math.round(h * 60) + m.adjust[i];
            out[i] = ((out[i] % 1440) + 1440) % 1440;
        }
        return out;
    }

    // ---- astronomy (angles in degrees) ----

    private static double julian(int y, int m, int d) {
        if (m <= 2) { y -= 1; m += 12; }
        double a = Math.floor(y / 100.0);
        double b = 2 - a + Math.floor(a / 4);
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + b - 1524.5;
    }

    /** Returns {declination, equation of time in hours} for a Julian date. */
    private static double[] sun(double jd) {
        double D = jd - 2451545.0;
        double g = fix(357.529 + 0.98560028 * D, 360);
        double q = fix(280.459 + 0.98564736 * D, 360);
        double L = fix(q + 1.915 * sin(g) + 0.020 * sin(2 * g), 360);
        double e = 23.439 - 0.00000036 * D;
        double RA = fix(atan2(cos(e) * sin(L), cos(L)) / 15.0, 24);
        double eqt = q / 15.0 - RA;
        return new double[]{asin(sin(e) * sin(L)), eqt};
    }

    private static double midDay(double jd, double frac) {
        double eqt = sun(jd + frac)[1];
        return fix(12 - eqt, 24);
    }

    private static double sunAngleTime(double jd, double frac, double angle, double lat, boolean beforeNoon) {
        double decl = sun(jd + frac)[0];
        double noon = midDay(jd, frac);
        double v = (-sin(angle) - sin(decl) * sin(lat)) / (cos(decl) * cos(lat));
        if (v < -1 || v > 1) return Double.NaN; // sun never reaches that angle (high latitudes)
        double tt = acos(v) / 15.0;
        return noon + (beforeNoon ? -tt : tt);
    }

    private static double asrTime(double jd, double frac, int factor, double lat) {
        double decl = sun(jd + frac)[0];
        double angle = -Math.toDegrees(Math.atan(1.0 / (factor + Math.tan(Math.toRadians(Math.abs(lat - decl))))));
        return sunAngleTime(jd, frac, angle, lat, false);
    }

    private static double sin(double d) { return Math.sin(Math.toRadians(d)); }
    private static double cos(double d) { return Math.cos(Math.toRadians(d)); }
    private static double asin(double x) { return Math.toDegrees(Math.asin(x)); }
    private static double acos(double x) { return Math.toDegrees(Math.acos(x)); }
    private static double atan2(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    private static double fix(double a, double b) { a = a - b * Math.floor(a / b); return a < 0 ? a + b : a; }
}
