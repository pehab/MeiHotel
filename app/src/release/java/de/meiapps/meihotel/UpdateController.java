package de.meiapps.meihotel;

import android.app.Activity;

/** Store builds use Google Play updates and include no sideloading or networking implementation. */
final class UpdateController {
    UpdateController(Activity activity) { }
    boolean automatic(){return false;}
    void toggle(){ }
    void check(boolean manual){ }
    void resume(){ }
    void pause(){ }
    void destroy(){ }
}
