package de.meiapps.meihotel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.PackageInfoCompat;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** GitHub preview updater. Release builds leave distribution and updates to Google Play. */
final class UpdateController {
    private static final String FEED="https://github.com/pehab/MeiHotel/releases/download/preview/MeiHotel-update.json";
    private final Activity activity;
    private final SharedPreferences prefs;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private boolean resumed,busy,waitingPermission,destroyed;
    private Runnable deferred;
    private UpdateInfo available;
    UpdateController(Activity activity){this.activity=activity;prefs=activity.getSharedPreferences("updates",Context.MODE_PRIVATE);
        try{available=parse(prefs.getString("candidate",""));if(!available.newerThan(BuildConfig.VERSION_CODE))available=null;}catch(JSONException|IllegalArgumentException e){available=null;}
        waitingPermission=prefs.getBoolean("waitingPermission",false);
    }
    boolean automatic(){return prefs.getBoolean("automatic",true);}
    void toggle(){prefs.edit().putBoolean("automatic",!automatic()).apply();}
    void resume(){
        resumed=true;
        if(waitingPermission){waitingPermission=false;prefs.edit().putBoolean("waitingPermission",false).apply();if(activity.getPackageManager().canRequestPackageInstalls())install();else message("Die Installation wurde nicht freigegeben. Du kannst sie später erneut starten.");return;}
        if(deferred!=null){Runnable task=deferred;deferred=null;task.run();return;}
        if(automatic()&&System.currentTimeMillis()-prefs.getLong("lastCheck",0)>24*60*60*1000L)check(false);
    }
    void pause(){resumed=false;}
    void destroy(){destroyed=true;worker.shutdownNow();deferred=null;}
    private void ui(Runnable task){activity.runOnUiThread(()->{if(destroyed||activity.isFinishing()||activity.isDestroyed())return;if(resumed)task.run();else deferred=task;});}
    private void message(String text){Toast.makeText(activity,text,Toast.LENGTH_LONG).show();}
    void check(boolean manual){
        if(busy){if(manual)message("Die Update-Prüfung oder der Download läuft bereits.");return;}
        busy=true;prefs.edit().putLong("lastCheck",System.currentTimeMillis()).apply();
        if(manual)message("Suche nach einer neuen MeiHotel-Version …");
        worker.execute(()->{
            try {
                String json=new String(read(FEED,65536),StandardCharsets.UTF_8);UpdateInfo info=parse(json);
                ui(()->{
                    busy=false;
                    if(!info.newerThan(BuildConfig.VERSION_CODE)){if(manual)message("MeiHotel ist aktuell · "+BuildConfig.VERSION_NAME);return;}
                    available=info;prefs.edit().putString("candidate",json).apply();
                    if(manual||automatic())new AlertDialog.Builder(activity).setTitle("MeiHotel "+info.name+" verfügbar")
                        .setMessage("Die neue Version kann jetzt geladen werden. Dein Hotel bleibt erhalten. Android fragt vor der Installation noch einmal nach.")
                        .setNegativeButton("Später",null).setPositiveButton("Update laden",(d,w)->download()).show();
                });
            }catch(IOException|JSONException|IllegalArgumentException e){ui(()->{busy=false;if(manual)message("Updates gerade nicht erreichbar. Bitte später erneut versuchen.");});}
        });
    }
    static UpdateInfo parse(String json)throws JSONException {
        JSONObject d=new JSONObject(json);if(d.getInt("schema")!=1)throw new JSONException("Unknown update schema");
        return new UpdateInfo(d.getInt("versionCode"),d.getString("versionName"),d.getString("url"),d.getString("sha256"),d.getLong("size"));
    }
    private File destination(){return new File(new File(activity.getCacheDir(),"updates"),"MeiHotel-"+available.code+".apk");}
    private void download(){
        if(busy||available==null)return;
        busy=true;message("Update wird geladen. Du kannst weiterspielen.");
        UpdateInfo info=available;
        worker.execute(()->{
            File file=destination(),temporary=new File(file.getParentFile(),"download.part");
            try {
                if(!file.getParentFile().isDirectory()&&!file.getParentFile().mkdirs())throw new IOException("No cache directory");
                if(!file.exists()||!info.matches(file)) {
                    transfer(info.url,temporary,info.size);
                    if(!info.matches(temporary))throw new IOException("Download checksum mismatch");
                    if(file.exists()&&!file.delete())throw new IOException("Cannot replace cache");
                    if(!temporary.renameTo(file))throw new IOException("Cannot commit cache");
                }
                verifyArchive(activity,file,info);
                ui(()->{busy=false;new AlertDialog.Builder(activity).setTitle("Update bereit").setMessage("MeiHotel "+info.name+" ist geladen. Jetzt mit erhaltenem Spielstand aktualisieren?")
                    .setNegativeButton("Später",null).setPositiveButton("Installieren",(d,w)->install()).show();});
            }catch(IOException|PackageManager.NameNotFoundException|IllegalArgumentException e){ui(()->{busy=false;message("Das Update konnte nicht sicher geladen werden. Bitte erneut versuchen.");});}
            finally{if(temporary.exists()&&!temporary.delete())temporary.deleteOnExit();}
        });
    }
    private void install(){
        if(available==null||!available.newerThan(BuildConfig.VERSION_CODE)||busy)return;
        if(!activity.getPackageManager().canRequestPackageInstalls()) {
            new AlertDialog.Builder(activity).setTitle("Installation erlauben")
                .setMessage("Android benötigt einmalig die Freigabe „Aus dieser Quelle zulassen“ für MeiHotel. Auf einem Kindertablet kann dafür eine Freigabe der Eltern nötig sein.")
                .setNegativeButton("Später",null).setPositiveButton("Android-Einstellungen",(d,w)->{
                    try{waitingPermission=true;prefs.edit().putBoolean("waitingPermission",true).apply();activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName())));}
                    catch(ActivityNotFoundException e){waitingPermission=false;prefs.edit().putBoolean("waitingPermission",false).apply();message("Die Installationsfreigabe ist auf diesem Gerät nicht verfügbar.");}
                }).show();return;
        }
        busy=true;File file=destination();UpdateInfo info=available;
        worker.execute(()->{
            try {
                if(!info.matches(file))throw new IOException("Modified cache");verifyArchive(activity,file,info);
                ui(()->{busy=false;try{activity.startActivity(installIntent(activity,file));}catch(ActivityNotFoundException|SecurityException e){message("Android kann das Update gerade nicht installieren. Bitte die Gerätefreigaben prüfen.");}});
            }catch(IOException|PackageManager.NameNotFoundException e){ui(()->{busy=false;message("Die Update-Datei fehlt oder ist ungültig. Bitte erneut nach Updates suchen.");});}
        });
    }
    static Intent installIntent(Context context,File file){
        Uri uri=FileProvider.getUriForFile(context,context.getPackageName()+".updates",file);
        return new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }
    @SuppressWarnings("deprecation")
    static void verifyArchive(Context context,File file,UpdateInfo info)throws IOException,PackageManager.NameNotFoundException {
        PackageManager pm=context.getPackageManager();int flags=Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES;
        PackageInfo apk=pm.getPackageArchiveInfo(file.getAbsolutePath(),flags),installed=pm.getPackageInfo(context.getPackageName(),flags);
        if(apk==null||!context.getPackageName().equals(apk.packageName)||PackageInfoCompat.getLongVersionCode(apk)!=info.code||!info.name.equals(apk.versionName))throw new IOException("Unexpected APK identity");
        Signature[] expected=Build.VERSION.SDK_INT>=28&&installed.signingInfo!=null?installed.signingInfo.getApkContentsSigners():installed.signatures;
        Signature[] actual=Build.VERSION.SDK_INT>=28&&apk.signingInfo!=null?apk.signingInfo.getApkContentsSigners():apk.signatures;
        if(expected==null||actual==null||expected.length==0||!new HashSet<>(Arrays.asList(expected)).equals(new HashSet<>(Arrays.asList(actual))))throw new IOException("Unexpected signer");
    }
    private static boolean trusted(URL url){String host=url.getHost();return "https".equals(url.getProtocol())&&(url.getPort()==-1||url.getPort()==443)&&url.getUserInfo()==null&&("github.com".equals(host)||host.endsWith(".githubusercontent.com"));}
    private static HttpURLConnection connection(String address)throws IOException {
        URL url=URI.create(address).toURL();
        for(int redirect=0;redirect<6;redirect++) {
            if(!trusted(url))throw new IOException("Unexpected redirect");
            HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(10000);c.setReadTimeout(20000);c.setInstanceFollowRedirects(false);c.setUseCaches(false);c.setRequestProperty("User-Agent","MeiHotel-Preview-Updater");
            int status=c.getResponseCode();
            if(status==200)return c;
            String location=c.getHeaderField("Location");c.disconnect();
            if(status>=300&&status<400&&location!=null){url=new URL(url,location);continue;}
            throw new IOException("HTTP "+status);
        }
        throw new IOException("Too many redirects");
    }
    private static byte[] read(String url,int limit)throws IOException {
        HttpURLConnection c=connection(url);
        try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1){if(out.size()+n>limit)throw new IOException("Metadata too large");out.write(buffer,0,n);}return out.toByteArray();
        }finally{c.disconnect();}
    }
    private static void transfer(String url,File file,long expected)throws IOException {
        HttpURLConnection c=connection(url);
        try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(file)){
            byte[] buffer=new byte[16384];long total=0;int n;while((n=in.read(buffer))!=-1){if(Thread.currentThread().isInterrupted()||(total+=n)>expected)throw new IOException("Download interrupted or too large");out.write(buffer,0,n);}if(total!=expected)throw new IOException("Incomplete download");
        }finally{c.disconnect();}
    }
}
