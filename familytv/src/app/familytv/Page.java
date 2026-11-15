package app.familytv;

import android.view.View;

/** One screen of content shown beside the navigation rail. */
abstract class Page {
    final MainActivity a;

    Page(MainActivity a) { this.a = a; }

    abstract View view();

    /** Called once a second while visible. */
    void tick(long now) { }

    /** Return true if the page handled Back itself (e.g. going up from surahs to reciters). */
    boolean back() { return false; }

    /** Where focus should go when the user enters the page from the rail. */
    View firstFocus() { return null; }
}
