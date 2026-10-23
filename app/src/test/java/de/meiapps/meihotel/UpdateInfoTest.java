package de.meiapps.meihotel;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.File;

public class UpdateInfoTest {
    private static final String URL="https://github.com/pehab/MeiHotel/releases/download/v0.4.0-preview-4/MeiHotel.apk";
    private static final String HASH="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private UpdateInfo info(String url){return new UpdateInfo(4,"0.4.0",url,HASH,1024);}
    @Test public void onlyStrictlyNewerVersionsAreUpdates(){assertTrue(info(URL).newerThan(3));assertFalse(info(URL).newerThan(4));assertFalse(info(URL).newerThan(5));}
    @Test public void unrelatedOrAmbiguousDownloadUrlsAreRejected(){
        String[] invalid={"http://github.com/pehab/MeiHotel/releases/download/v4/MeiHotel.apk",URL.replace("github.com","github.com.evil.example"),URL.replace("pehab","other"),URL+"?redirect=evil",URL+"#fragment",URL.replace("/v0.4.0-preview-4/","/../"),URL.replace("github.com/","github.com:444/"),URL.replace("github.com/","user@github.com/"),URL.replace("MeiHotel.apk","other.apk")};
        for(String url:invalid){try{info(url);throw new AssertionError("Accepted "+url);}catch(IllegalArgumentException expected){assertTrue(true);}}
    }
    @Test public void malformedSizeHashOrVersionRejected(){
        Object[][] values={{0,"0.4.0",HASH,1L},{4,"bad name",HASH,1L},{4,"0.4.0","garbage",1L},{4,"0.4.0",HASH,0L},{4,"0.4.0",HASH,UpdateInfo.MAX_APK_BYTES+1}};
        for(Object[] v:values){try{new UpdateInfo((Integer)v[0],(String)v[1],URL,(String)v[2],(Long)v[3]);throw new AssertionError("Invalid metadata accepted");}catch(IllegalArgumentException expected){assertTrue(true);}}
    }
    @Test public void checksumDetectsChangedAndTruncatedDownloads()throws Exception {
        File file=File.createTempFile("hotel-update",".apk");
        try {
            Files.write(file.toPath(),"original signed bytes".getBytes(StandardCharsets.UTF_8));
            UpdateInfo info=new UpdateInfo(4,"0.4.0",URL,UpdateInfo.digest(file),file.length());assertTrue(info.matches(file));
            Files.write(file.toPath(),"modified signed bytes".getBytes(StandardCharsets.UTF_8));assertFalse(info.matches(file));
            Files.write(file.toPath(),new byte[1]);assertFalse(info.matches(file));
        }finally{Files.deleteIfExists(file.toPath());}
    }
}
