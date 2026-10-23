package de.meiapps.meihotel;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.WindowCompat;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

public final class MainActivity extends ComponentActivity {
    private HotelView hotel;
    private GameStore store;
    private UpdateController updates;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        store=new GameStore(this);
        hotel=new HotelView(this,store.load(),store);
        setContentView(hotel);
        if(BuildConfig.GITHUB_UPDATES){updates=new UpdateController(this);hotel.setUpdates(updates);}
        if(android.os.Build.VERSION.SDK_INT>=30) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
            WindowInsetsController controller=getWindow().getInsetsController();
            if(controller!=null){controller.hide(WindowInsets.Type.systemBars());controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);}
            hotel.setOnApplyWindowInsetsListener((view,insets)->{
                android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.displayCutout()|WindowInsets.Type.systemBars());
                hotel.setSafeInsets(safe.left,safe.top,safe.right,safe.bottom);return insets;
            });
        }
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed(){hotel.showPause();}
        });
        if(store.recovered)Toast.makeText(this,"Der Spielstand konnte nicht gelesen werden. Ein neues Hotel wurde geöffnet.",Toast.LENGTH_LONG).show();
    }
    @Override protected void onResume(){super.onResume();hotel.setRunning(true);if(updates!=null)updates.resume();}
    @Override protected void onPause(){if(updates!=null)updates.pause();hotel.setRunning(false);if(!store.save(hotel.game,true))Toast.makeText(this,"Speichern fehlgeschlagen. Bitte freien Speicher prüfen.",Toast.LENGTH_LONG).show();super.onPause();}
    @Override protected void onDestroy(){if(updates!=null)updates.destroy();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(hotel!=null)hotel.setFocused(focused);}
}
