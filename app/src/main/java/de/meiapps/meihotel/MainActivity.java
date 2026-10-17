package de.meiapps.meihotel;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private HotelView hotel;
    private GameStore store;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        store=new GameStore(this);
        hotel=new HotelView(this,store.load(),store);
        setContentView(hotel);
        if(android.os.Build.VERSION.SDK_INT>=30) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller=getWindow().getInsetsController();
            if(controller!=null){controller.hide(WindowInsets.Type.systemBars());controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);}
            hotel.setOnApplyWindowInsetsListener((view,insets)->{
                android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.displayCutout()|WindowInsets.Type.systemBars());
                hotel.setSafeInsets(safe.left,safe.top,safe.right,safe.bottom);return insets;
            });
        }
        if(android.os.Build.VERSION.SDK_INT>=33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> hotel.showPause());
        }
        if(store.recovered)Toast.makeText(this,"Der Spielstand konnte nicht gelesen werden. Ein neues Hotel wurde geöffnet.",Toast.LENGTH_LONG).show();
    }
    @Override protected void onResume(){super.onResume();hotel.setRunning(true);}
    @Override protected void onPause(){hotel.setRunning(false);if(!store.save(hotel.game,true))Toast.makeText(this,"Speichern fehlgeschlagen. Bitte freien Speicher prüfen.",Toast.LENGTH_LONG).show();super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(hotel!=null)hotel.setFocused(focused);}
    @SuppressWarnings("deprecation")
    @Override public void onBackPressed(){hotel.showPause();}
}
