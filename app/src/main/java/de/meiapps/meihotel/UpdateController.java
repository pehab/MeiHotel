package de.meiapps.meihotel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.io.File;
import java.io.IOException;
import de.meiapps.updates.GitHubUpdates;

/** Same source routing for Play releases and directly installed APKs. */
final class UpdateController {
    private final GitHubUpdates controller;
    UpdateController(Activity activity, Runnable save) { controller=GitHubUpdates.attach(activity,"pehab/MeiHotel",save); }
    boolean automatic(){return controller.automatic();}
    boolean fromPlay(){return controller.fromPlay();}
    void toggle(){controller.toggle();}
    void check(boolean manual){controller.check(manual);}
    void resume(){controller.resume();}
    void pause(){controller.pause();}
    void destroy(){controller.destroy();}
    static void verifyArchive(Context context,File file,UpdateInfo info)throws IOException,PackageManager.NameNotFoundException {
        if(!info.matches(file))throw new IOException("APK checksum mismatch");
        GitHubUpdates.verifyArchive(context,file,info.code,info.name);
    }
    static Intent installIntent(Context context,File file){return GitHubUpdates.installIntent(context);}
}
