package de.meiapps.meihotel;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ViewGroup;

/** Dependency-free on-device checks for real Android preferences and Activity startup. */
public final class SmokeInstrumentation extends Instrumentation {
    private String upgradePhase;
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);upgradePhase=arguments==null?null:arguments.getString("upgradePhase");start();}
    private static void check(boolean success,String description){if(!success)throw new AssertionError(description);}
    @Override public void onStart() {
        Bundle result=new Bundle();
        try {
            Context context=getTargetContext();
            if(upgradePhase!=null) {
                GameStore upgradeStore=new GameStore(context);
                if(upgradePhase.equals("seed")) {
                    HotelGame seed=new HotelGame();seed.coins=432;seed.served=17;seed.cleaned=8;
                    seed.hasCleaner=true;seed.breakfastLevel=2;seed.claimedGoals=1;
                    check(upgradeStore.save(seed,true),"seed update save");
                } else {
                    HotelGame kept=upgradeStore.load();
                    check(kept.coins==432&&kept.served==17&&kept.cleaned==8&&kept.hasCleaner
                        &&kept.breakfastLevel==2&&kept.claimedGoals==1,"save retained after APK update");
                }
                result.putString("stream","PASS: APK update "+upgradePhase);finish(Activity.RESULT_OK,result);return;
            }
            check(context.getSharedPreferences("hotel",Context.MODE_PRIVATE).edit().clear().commit(),"clear preferences");
            GameStore store=new GameStore(context);
            HotelGame initial=store.load();check(initial.coins==60,"new game");
            initial.coins=471;initial.hasCleaner=true;initial.hasReception=true;
            initial.breakfastLevel=2;initial.claimedGoals=1;initial.speedLevel=2;initial.roomLevel=3;initial.served=17;initial.cleaned=12;
            initial.rooms[0].state=HotelGame.State.OCCUPIED;initial.rooms[0].timer=9;
            initial.rooms[1].state=HotelGame.State.DIRTY;
            initial.rooms[3].state=HotelGame.State.ARRIVING;
            initial.player.x=505;initial.player.y=595;
            check(store.save(initial,true),"atomic save");
            HotelGame restored=new GameStore(context).load();
            check(restored.breakfastLevel==2&&restored.claimedGoals==1,"new progression restored");
            check(restored.coins==471&&restored.served==17&&restored.cleaned==12,"economy restored");
            check(restored.hasCleaner&&restored.hasReception&&restored.speedLevel==2&&restored.roomLevel==3,"upgrades restored");
            check(restored.rooms[0].state==HotelGame.State.OCCUPIED&&restored.rooms[0].timer==9,"occupied room restored");
            check(restored.rooms[1].state==HotelGame.State.DIRTY,"dirty room restored");
            check(restored.rooms[3].state==HotelGame.State.OCCUPIED&&restored.guests.size()==2,"arriving guests recovered");
            check(restored.player.x==505&&restored.player.y==595,"player restored");
            org.json.JSONObject legacy=new org.json.JSONObject(context.getSharedPreferences("hotel",Context.MODE_PRIVATE).getString("save",""));
            legacy.remove("breakfast");legacy.remove("goals");
            context.getSharedPreferences("hotel",Context.MODE_PRIVATE).edit().putString("save",legacy.toString()).commit();
            HotelGame migrated=store.load();
            check(migrated.coins==471&&migrated.served==17&&migrated.breakfastLevel==0&&migrated.claimedGoals==0,"0.1 save compatibility");
            context.getSharedPreferences("hotel",Context.MODE_PRIVATE).edit().putString("save","invalid json").commit();
            GameStore damaged=new GameStore(context);HotelGame recovered=damaged.load();
            check(damaged.recovered&&recovered.coins==60,"corrupt save recovery");
            store.save(new HotelGame(),true);
            Activity activity=startActivitySync(new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();SystemClock.sleep(800);
            final HotelView[] view={null};
            runOnMainSync(()->{
                view[0]=(HotelView)((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);
                check(view[0].getWidth()>0&&view[0].getHeight()>0,"surface laid out");
                float scale=Math.min(view[0].getWidth()/1280f,view[0].getHeight()/900f);
                float x=view[0].getWidth()/2f;
                float y=(view[0].getHeight()-900*scale)/2+727*scale;
                long time=SystemClock.uptimeMillis();
                MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,x,y,0);
                MotionEvent up=MotionEvent.obtain(time,time+50,MotionEvent.ACTION_UP,x,y,0);
                view[0].onTouchEvent(down);view[0].onTouchEvent(up);down.recycle();up.recycle();
            });
            SystemClock.sleep(1400);
            runOnMainSync(()->check(view[0].game.elapsed>0,"welcome button starts simulation"));
            runOnMainSync(()->{
                view[0].game.coins=1000;
                tap(view[0],1090,847); // Open shop, then exercise the new fifth row.
                tap(view[0],870,620);
                check(view[0].game.breakfastLevel==1&&view[0].game.coins==900,"breakfast shop row touch target");
                tap(view[0],640,727);
                tap(view[0],900,55); // Hotel goals and return.
                tap(view[0],640,727);
            });
            result.putString("stream","\nPASS: Android save/load, legacy migration, corrupt save recovery, Activity launch, touch start, shop and hotel goals.\n");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable failure) {
            result.putString("stream","\nFAIL: "+failure+"\n");
            result.putString("shortMsg",failure.toString());finish(Activity.RESULT_CANCELED,result);
        }
    }
    private static void tap(HotelView view,float logicalX,float logicalY) {
        float scale=Math.min(view.getWidth()/1280f,view.getHeight()/900f);
        float x=(view.getWidth()-1280*scale)/2+logicalX*scale;
        float y=(view.getHeight()-900*scale)/2+logicalY*scale;
        long time=SystemClock.uptimeMillis();
        MotionEvent down=MotionEvent.obtain(time,time,MotionEvent.ACTION_DOWN,x,y,0);
        MotionEvent up=MotionEvent.obtain(time,time+50,MotionEvent.ACTION_UP,x,y,0);
        view.onTouchEvent(down);view.onTouchEvent(up);down.recycle();up.recycle();
    }
}
