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
                    seed.progress.amenities[0].level=2;seed.progress.contractTier[1]=3;seed.rooms[0].level=4;seed.progress.services=11;
                    seed.hasCleaner=true;seed.breakfastLevel=2;seed.claimedGoals=1;
                    check(upgradeStore.save(seed,true),"seed update save");
                } else {
                    HotelGame kept=upgradeStore.load();
                    check(kept.coins==432&&kept.served==17&&kept.cleaned==8&&kept.hasCleaner
                        &&kept.breakfastLevel==2&&kept.claimedGoals==1&&kept.progress.amenities[0].level==2&&kept.rooms[0].level==4&&kept.progress.contractTier[1]==3&&kept.progress.services==11,"save retained after APK update");
                }
                result.putString("stream","PASS: APK update "+upgradePhase);finish(Activity.RESULT_OK,result);return;
            }
            android.graphics.drawable.Drawable icon=context.getPackageManager().getApplicationIcon(context.getPackageName());
            check(icon instanceof android.graphics.drawable.AdaptiveIconDrawable,"adaptive MeiApps icon installed");
            android.graphics.Bitmap iconBitmap=android.graphics.Bitmap.createBitmap(256,256,android.graphics.Bitmap.Config.ARGB_8888);
            icon.setBounds(0,0,256,256);icon.draw(new android.graphics.Canvas(iconBitmap));
            java.io.ByteArrayOutputStream iconBytes=new java.io.ByteArrayOutputStream();iconBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,iconBytes);iconBitmap.recycle();
            result.putString("iconPreview",android.util.Base64.encodeToString(iconBytes.toByteArray(),android.util.Base64.NO_WRAP));
            check(context.getSharedPreferences("hotel",Context.MODE_PRIVATE).edit().clear().commit(),"clear preferences");
            GameStore store=new GameStore(context);
            HotelGame initial=store.load();check(initial.coins==60,"new game");
            initial.coins=471;initial.hasCleaner=true;initial.hasReception=true;
            initial.breakfastLevel=2;initial.claimedGoals=1;initial.speedLevel=2;initial.roomLevel=3;initial.served=17;initial.cleaned=12;
            initial.rooms[0].state=HotelGame.State.OCCUPIED;initial.rooms[0].timer=9;
            initial.rooms[1].state=HotelGame.State.DIRTY;
            initial.rooms[3].state=HotelGame.State.ARRIVING;
            initial.rooms[0].level=3;initial.progress.cartLevel=2;initial.cargo=1;initial.cargoCount=3;
            initial.progress.amenities[1].level=2;initial.progress.amenities[1].pending=3;initial.progress.amenities[1].timer=12;
            initial.progress.contractBase[0]=9;initial.progress.contractTier[0]=2;initial.progress.stars=2;initial.progress.services=7;
            initial.player.x=505;initial.player.y=595;
            check(store.save(initial,true),"atomic save");
            HotelGame restored=new GameStore(context).load();
            check(restored.rooms[0].level==3&&restored.cargoCount==3&&restored.progress.cartLevel==2&&restored.progress.stars==2&&restored.progress.services==7,"expansion equipment restored");
            check(restored.progress.amenities[1].level==2&&restored.progress.amenities[1].pending==3&&restored.progress.amenities[1].timer==12,"courtyard restored");
            check(restored.progress.contractTier[0]==2&&restored.progress.contractBase[0]==9,"repeatable contracts restored");
            check(restored.breakfastLevel==2&&restored.claimedGoals==1,"new progression restored");
            check(restored.coins==471&&restored.served==17&&restored.cleaned==12,"economy restored");
            check(restored.hasCleaner&&restored.hasReception&&restored.speedLevel==2&&restored.roomLevel==3,"upgrades restored");
            check(restored.rooms[0].state==HotelGame.State.OCCUPIED&&restored.rooms[0].timer==9,"occupied room restored");
            check(restored.rooms[1].state==HotelGame.State.DIRTY,"dirty room restored");
            check(restored.rooms[3].state==HotelGame.State.OCCUPIED&&restored.guests.size()==2,"arriving guests recovered");
            check(restored.player.x==505&&restored.player.y==595,"player restored");
            org.json.JSONObject legacy=new org.json.JSONObject(context.getSharedPreferences("hotel",Context.MODE_PRIVATE).getString("save",""));
            legacy.remove("expansion");legacy.remove("area");legacy.remove("cargoCount");
            for(int i=0;i<6;i++)legacy.getJSONArray("rooms").getJSONObject(i).remove("level");
            context.getSharedPreferences("hotel",Context.MODE_PRIVATE).edit().putString("save",legacy.toString()).commit();
            HotelGame versionTwo=store.load();
            check(versionTwo.coins==471&&versionTwo.breakfastLevel==2&&versionTwo.rooms[0].level==0&&versionTwo.progress.stars==1&&versionTwo.progress.amenityLevels()==0&&versionTwo.cargoCount==1,"0.2 save compatibility");
            legacy.remove("breakfast");legacy.remove("goals");
            context.getSharedPreferences("hotel",Context.MODE_PRIVATE).edit().putString("save",legacy.toString()).commit();
            HotelGame migrated=store.load();
            check(migrated.coins==471&&migrated.served==17&&migrated.breakfastLevel==0&&migrated.claimedGoals==0,"0.1 save compatibility");
            context.getSharedPreferences("hotel",Context.MODE_PRIVATE).edit().putString("save","invalid json").commit();
            GameStore damaged=new GameStore(context);HotelGame recovered=damaged.load();
            check(damaged.recovered&&recovered.coins==60,"corrupt save recovery");
            context.getSharedPreferences("updates",Context.MODE_PRIVATE).edit().putBoolean("automatic",false).commit();
            java.io.File installedApk=new java.io.File(context.getApplicationInfo().sourceDir);
            UpdateInfo self=new UpdateInfo(BuildConfig.VERSION_CODE,BuildConfig.VERSION_NAME,"https://github.com/pehab/MeiHotel/releases/download/v0.4.0-preview-4/MeiHotel.apk",UpdateInfo.digest(installedApk),installedApk.length());
            UpdateController.verifyArchive(context,installedApk,self);
            boolean rejected=false;
            try{UpdateController.verifyArchive(context,installedApk,new UpdateInfo(BuildConfig.VERSION_CODE+1,BuildConfig.VERSION_NAME,self.url,self.sha256,self.size));}catch(java.io.IOException expected){rejected=true;}
            check(rejected,"wrong APK version rejected before installer");
            java.io.File directory=new java.io.File(context.getCacheDir(),"updates");check(directory.isDirectory()||directory.mkdirs(),"update cache directory");
            java.io.File cache=new java.io.File(context.getCacheDir(),"github-update.apk");try(java.io.FileOutputStream out=new java.io.FileOutputStream(cache)){out.write(1);}
            Intent installer=UpdateController.installIntent(context,cache);
            check(installer.getData()!=null&&"content".equals(installer.getData().getScheme())&&(installer.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION)!=0,"private FileProvider installer handoff");
            try(android.os.ParcelFileDescriptor fd=context.getContentResolver().openFileDescriptor(installer.getData(),"r")){check(fd!=null&&fd.getStatSize()==1,"installer URI exposes exact cached APK bytes");}
            boolean readOnly=false;try{context.getContentResolver().openFileDescriptor(installer.getData(),"w");}catch(java.io.FileNotFoundException expected){readOnly=true;}check(readOnly,"installer URI is read only");
            check(cache.delete(),"delete fixture cache");
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
                tap(view[0],1180,55);tap(view[0],640,466);tap(view[0],640,415);
                check(context.getSharedPreferences("updates",Context.MODE_PRIVATE).getBoolean("automatic",false),"enable automatic updates in settings");
                tap(view[0],640,415);check(!context.getSharedPreferences("updates",Context.MODE_PRIVATE).getBoolean("automatic",true),"disable automatic updates in settings");
                tap(view[0],640,727);tap(view[0],640,300);
                view[0].game.coins=1000;
                tap(view[0],1090,847); // Open shop, then exercise the new fifth row.
                tap(view[0],870,650);
                check(view[0].game.breakfastLevel==1&&view[0].game.coins==900,"breakfast shop row touch target");
                tap(view[0],640,727);
                tap(view[0],855,55); // Hotel goals and return.
                tap(view[0],640,727);
                tap(view[0],955,55);check(view[0].game.area==1,"courtyard navigation button");
                tap(view[0],955,55);check(view[0].game.area==0,"return to hotel button");
                view[0].game.coins=50000;
                tap(view[0],1090,847);tap(view[0],720,236);tap(view[0],870,301);
                check(view[0].game.rooms[0].level==1,"individual room purchase via touch");
                tap(view[0],640,733);
                view[0].game.rooms[2].state=HotelGame.State.READY;
                tap(view[0],1090,847);tap(view[0],888,236);tap(view[0],870,310);
                check(view[0].game.progress.amenities[0].level==1,"courtyard purchase via touch");
                tap(view[0],640,733);tap(view[0],955,55);
                check(view[0].game.area==1,"courtyard opens after purchase");
                view[0].game.player.go(300,425,view[0].game);
                check(!view[0].game.player.route.isEmpty(),"courtyard service is reachable");
            });
            final String[] preview={null};
            runOnMainSync(()->{
                view[0].setRunning(false);
                for(int i=0;i<4;i++){view[0].game.progress.amenities[i].level=i+1;view[0].game.progress.amenities[i].pending=2;}
                view[0].game.player.x=640;view[0].game.player.y=465;view[0].game.player.route.clear();
                android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(view[0].getWidth(),view[0].getHeight(),android.graphics.Bitmap.Config.ARGB_8888);
                view[0].draw(new android.graphics.Canvas(bitmap));
                java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,65,bytes);bitmap.recycle();
                preview[0]=android.util.Base64.encodeToString(bytes.toByteArray(),android.util.Base64.NO_WRAP);
            });
            result.putString("preview",preview[0]);
            runOnMainSync(()->{
                android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(2200,820,android.graphics.Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas=new android.graphics.Canvas(bitmap);canvas.drawColor(0xfff5efdc);
                android.graphics.Paint text=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);text.setTextSize(18);text.setColor(0xff244c43);
                HotelArt art=new HotelArt();
                for(int type=0;type<4;type++)for(int level=1;level<=5;level++){
                    canvas.save();canvas.translate((level-1)*440,type*205);canvas.drawText(HotelProgress.AREA_NAMES[type]+" · Stufe "+level,12,24,text);art.amenity(canvas,type,level,0,0);canvas.restore();
                }
                java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,75,bytes);bitmap.recycle();
                result.putString("artPreview",android.util.Base64.encodeToString(bytes.toByteArray(),android.util.Base64.NO_WRAP));
                for(int i=0;i<6;i++){view[0].game.rooms[i].state=HotelGame.State.READY;view[0].game.rooms[i].level=i;}
                view[0].game.area=0;view[0].game.player.x=245;view[0].game.player.y=450;
                android.graphics.Bitmap rooms=android.graphics.Bitmap.createBitmap(view[0].getWidth(),view[0].getHeight(),android.graphics.Bitmap.Config.ARGB_8888);
                view[0].draw(new android.graphics.Canvas(rooms));bytes.reset();rooms.compress(android.graphics.Bitmap.CompressFormat.JPEG,75,bytes);rooms.recycle();
                result.putString("roomPreview",android.util.Base64.encodeToString(bytes.toByteArray(),android.util.Base64.NO_WRAP));
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
