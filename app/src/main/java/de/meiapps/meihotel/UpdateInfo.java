package de.meiapps.meihotel;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Validated public preview metadata. No tokens, arbitrary download hosts or downgrades. */
final class UpdateInfo {
    static final long MAX_APK_BYTES=50L*1024*1024;
    final int code;
    final String name,url,sha256;
    final long size;
    UpdateInfo(int code,String name,String url,String sha256,long size) {
        if(code<=0||name==null||!name.matches("[0-9A-Za-z._+-]{1,40}")||sha256==null||!sha256.matches("[a-f0-9]{64}")||size<=0||size>MAX_APK_BYTES)throw new IllegalArgumentException("Invalid update metadata");
        try {
            URI uri=new URI(url);
            if(!"https".equals(uri.getScheme())||!"github.com".equals(uri.getHost())||uri.getPort()!=-1||uri.getUserInfo()!=null||uri.getQuery()!=null||uri.getFragment()!=null
                ||!uri.getRawPath().matches("/pehab/MeiHotel/releases/download/v[0-9A-Za-z._+-]+/MeiHotel\\.apk"))throw new IllegalArgumentException("Unexpected update URL");
        }catch(URISyntaxException|NullPointerException e){throw new IllegalArgumentException("Invalid update URL",e);}
        this.code=code;this.name=name;this.url=url;this.sha256=sha256;this.size=size;
    }
    boolean newerThan(long installed){return code>installed;}
    boolean matches(File file)throws IOException{return file.length()==size&&sha256.equals(digest(file));}
    static String digest(File file)throws IOException {
        try(FileInputStream in=new FileInputStream(file)) {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[16384];int n;
            while((n=in.read(buffer))!=-1)digest.update(buffer,0,n);
            StringBuilder result=new StringBuilder(64);for(byte b:digest.digest()){result.append(Character.forDigit((b>>4)&15,16));result.append(Character.forDigit(b&15,16));}return result.toString();
        }catch(NoSuchAlgorithmException e){throw new IOException("SHA-256 unavailable",e);}
    }
}
