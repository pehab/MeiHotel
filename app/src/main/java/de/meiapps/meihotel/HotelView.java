package de.meiapps.meihotel;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Resolution-independent vector art, drawn locally. No downloaded assets or web content. */
final class HotelView extends View {
    final HotelGame game;
    private final GameStore store;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect=new RectF();
    private final List<HotelGame.Actor> drawingActors=new ArrayList<>();
    private static final int INK=Color.rgb(30,65,61), GREEN=Color.rgb(29,132,111), GOLD=Color.rgb(235,181,81);
    private static final int CREAM=Color.rgb(251,245,229), MUTED=Color.rgb(109,133,124);
    private final int[] guestColors={0xffdf897a,0xffe2b84f,0xff739bb7,0xffa48bb9,0xff70a58a};
    private float scale=1,offsetX,offsetY,stickX,stickY,touchX,touchY,dx,dy,saveClock;
    private int pointer=-1,safeLeft,safeTop,safeRight,safeBottom;
    private boolean dragging,running,focused=true;
    private long lastFrame;
    private enum Panel { NONE, WELCOME, PAUSE, SHOP, HELP }
    private Panel panel=Panel.WELCOME;

    public HotelView(Context context) { this(context,new HotelGame(),new GameStore(context)); }

    HotelView(Context context,HotelGame game,GameStore store) {
        super(context);this.game=game;this.store=store;
        setFocusable(true);setContentDescription("MeiHotel. Ziehen zum Laufen oder Ziel antippen. Ausbauen und Pause über die Schaltflächen.");
    }
    void setSafeInsets(int l,int t,int r,int b){safeLeft=l;safeTop=t;safeRight=r;safeBottom=b;invalidate();}
    void setRunning(boolean value){running=value;resetInput();lastFrame=0;if(value)postInvalidateOnAnimation();}
    void setFocused(boolean value){focused=value;resetInput();lastFrame=0;if(value)postInvalidateOnAnimation();}
    void showPause(){panel=Panel.PAUSE;resetInput();invalidate();}
    private void resetInput(){pointer=-1;dragging=false;dx=0;dy=0;game.player.route.clear();}

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now=System.nanoTime();
        float dt=lastFrame==0?0:Math.min(.05f,(now-lastFrame)/1_000_000_000f);lastFrame=now;
        if(running&&focused&&panel==Panel.NONE) {
            game.movePlayer(dx,dy,dt);game.tick(dt);saveClock+=dt;
            if(saveClock>=8){if(!store.save(game,false))game.announce("Speichern nicht möglich. Bitte Speicher prüfen.");saveClock=0;}
        }
        canvas.drawColor(0xff183c39);
        scale=Math.max(.01f,Math.min((getWidth()-safeLeft-safeRight)/1280f,(getHeight()-safeTop-safeBottom)/900f));
        offsetX=safeLeft+(getWidth()-safeLeft-safeRight-1280*scale)/2;
        offsetY=safeTop+(getHeight()-safeTop-safeBottom-900*scale)/2;
        canvas.save();canvas.translate(offsetX,offsetY);canvas.scale(scale,scale);
        drawHotel(canvas);drawHud(canvas);
        if(panel!=Panel.NONE)drawPanel(canvas);
        else if(dragging){circle(canvas,stickX,stickY,54,0x44315d53);circle(canvas,stickX+dx*36,stickY+dy*36,25,0x99447e6f);}
        canvas.restore();
        if(running&&focused)postInvalidateOnAnimation();
    }
    private void drawHotel(Canvas c) {
        box(c,0,0,1280,900,0,0xffe5eddf);
        // Garden and softly outlined hotel shell.
        for(int x=40;x<1260;x+=85){circle(c,x,112,13,0xffa9c6a5);circle(c,x+14,105,10,0xffbad1ae);}
        box(c,27,136,1226,644,26,0x18234538);
        box(c,20,128,1226,644,26,0xff6c8d7f);
        box(c,35,142,1196,615,18,0xfff0e4cc);
        // Lobby tile grid.
        for(int x=50;x<300;x+=45)for(int y=160;y<750;y+=45)box(c,x,y,42,42,2,0xffe4d7bd);
        box(c,295,395,915,113,8,0xffd29d74);
        box(c,300,419,904,65,9,0xff789a8b);
        for(int x=315;x<1200;x+=32)box(c,x,425,2,53,1,0x3376b4a0);
        label(c,"EMPFANG",166,195,23,INK,true);
        box(c,75,352,130,69,14,0x1527463b);
        box(c,69,337,137,69,14,0xff9b6a4b);box(c,69,331,137,42,12,0xffedc68c);
        box(c,88,342,32,17,3,INK);circle(c,179,350,8,GOLD);
        label(c,"M",147,362,24,INK,true);
        rug(c,140,580);
        plant(c,67,220);plant(c,269,220);plant(c,1138,790);plant(c,123,790);
        box(c,43,720,70,24,5,0xffede5d3);label(c,"EINGANG",78,736,13,INK,true);
        station(c,235,450,40,GREEN,"Empfang",game.receptionWork/1.3f,game.near(235,450,65));
        if(game.hasReception){actor(c,game.receptionist,0xff335b75,"Empfang");}
        for(HotelGame.Room room:game.rooms)drawRoom(c,room);
        drawingActors.clear();
        for(HotelGame.Actor guest:game.guests) {
            if(guest.room>=0&&!guest.leaving&&game.rooms[guest.room].state==HotelGame.State.OCCUPIED)continue;
            drawingActors.add(guest);
        }
        if(game.hasCleaner)drawingActors.add(game.cleaner);
        drawingActors.add(game.player);drawingActors.sort(Comparator.comparingDouble(a->a.y));
        for(HotelGame.Actor a:drawingActors) {
            if(a==game.player)actor(c,a,GREEN,"DU");
            else if(a==game.cleaner)actor(c,a,0xff5d87b1,"Reinigung");
            else actor(c,a,guestColors[a.color],a.room<0&&!a.leaving?"…":null);
        }
        // Tap target indicator.
        if(!game.player.route.isEmpty()) {
            HotelGame.Point p=game.player.route.get(game.player.route.size()-1);
            ring(c,p.x,p.y,15,GREEN,3);circle(c,p.x,p.y,4,GREEN);
        }
    }
    private void drawRoom(Canvas c,HotelGame.Room r) {
        boolean locked=r.state==HotelGame.State.LOCKED;
        box(c,r.x,r.y,270,242,12,0xff648577);
        box(c,r.x+7,r.y+7,256,227,8,locked?0xffd4d9c9:0xfffaf0d9);
        if(locked) {
            for(int n=0;n<5;n++)line(c,r.x+28+n*43,r.y+45,r.x+28+n*43,r.y+198,0xffc3cbb9,2);
            circle(c,r.centerX(),r.y+115,33,0xffb7c8b5);
            // Construction hammer.
            line(c,r.centerX()-12,r.y+132,r.centerX()+13,r.y+99,INK,8);
            line(c,r.centerX()+1,r.y+93,r.centerX()+24,r.y+110,INK,13);
            label(c,"PLATZ FÜR IDEEN",r.centerX(),r.y+184,18,MUTED,true);
            label(c,"Zimmer "+(r.id+1),r.centerX(),r.y+33,19,INK,true);
            station(c,r.centerX(),r.padY(),29,game.coins>=r.price()?GREEN:MUTED,r.price()+" M",r.work/1.5f,game.near(r.centerX(),r.padY(),33));
            return;
        }
        int accent=guestColors[r.id%guestColors.length];
        // Window, bed, bedside table and seating are all original vector shapes.
        box(c,r.x+90,r.y+15,90,28,5,0xff9dc6c2);
        line(c,r.x+135,r.y+15,r.x+135,r.y+43,CREAM,4);
        box(c,r.x+23,r.y+76,99,121,11,0x16234538);
        box(c,r.x+20,r.y+68,99,121,9,0xffa37c58);
        box(c,r.x+23,r.y+74,93,108,7,CREAM);
        box(c,r.x+28,r.y+78,83,29,6,0xfffffdf6);
        box(c,r.x+23,r.y+111,93,70,6,accent);
        box(c,r.x+23,r.y+118,93,12,0,0x22ffffff);
        box(c,r.x+133,r.y+88,29,29,6,0xffc29d70);circle(c,r.x+147,r.y+100,9,GOLD);
        box(c,r.x+214,r.y+53,24,43,7,accent);
        plant(c,r.x+234,r.y+199);
        if(game.roomLevel>0){box(c,r.x+184,r.y+62,21,22,4,GOLD);label(c,"★",r.x+194,r.y+79,17,INK,true);}
        if(game.roomLevel>1){box(c,r.x+179,r.y+113,53,35,7,0xffe4cda3);circle(c,r.x+205,r.y+129,9,accent);}
        if(game.roomLevel>2){label(c,"★★★",r.centerX(),r.y+61,18,0xffaa7b28,true);}
        String state;
        switch(r.state){
            case OCCUPIED: state="ZU GAST";break;
            case ARRIVING: state="WILLKOMMEN";break;
            case DIRTY: state="SAUBER MACHEN";break;
            default: state="FREI";
        }
        box(c,r.x+20,r.y+207,230,21,6,r.state==HotelGame.State.DIRTY?0xfff3d9b1:0xffd6e8d6);
        label(c,(r.id+1)+"  ·  "+state,r.centerX(),r.y+222,15,INK,true);
        // Door opening connects visually with the navigation corridor.
        box(c,r.centerX()-32,r.doorY()-6,64,17,3,0xfff0e4cc);
        if(r.state==HotelGame.State.DIRTY) {
            for(int i=0;i<4;i++)circle(c,r.x+165+i*15,r.serviceY()+8+(i%2)*13,5,0xffb49870);
            station(c,r.x+185,r.serviceY(),28,0xffb57738,"Besen",r.work/2.5f,false);
            // Broom icon over the service marker.
            line(c,r.x+176,r.serviceY()-8,r.x+191,r.serviceY()+9,CREAM,4);
            line(c,r.x+181,r.serviceY()+11,r.x+195,r.serviceY()-1,CREAM,9);
        } else if(r.state==HotelGame.State.OCCUPIED) {
            label(c,"z Z",r.x+74,r.y+106,22,INK,true);
            progress(c,r.x+170,r.y+170,60,1-r.timer/24,0xff70a58a);
        }
    }
    private void drawHud(Canvas c) {
        box(c,26,20,1228,73,20,INK);
        labelLeft(c,"MeiHotel",50,66,34,CREAM,true);
        labelLeft(c,"DEIN KLEINES TRAUMHOTEL",52,84,11,0xffc1d8c7,true);
        circle(c,357,55,20,GOLD);label(c,"M",357,63,22,INK,true);
        labelLeft(c,String.valueOf(game.coins),389,67,32,CREAM,true);
        labelLeft(c,game.openRooms()+" / 6 Zimmer",570,50,21,CREAM,true);
        labelLeft(c,game.served+" Gäste begrüßt",570,74,17,0xffc1d8c7,false);
        button(c,1014,32,100,48,"Hilfe",0xff365b50,true);
        button(c,1126,32,111,48,"Pause",0xff365b50,true);
        box(c,28,814,894,65,15,CREAM);
        labelLeft(c,game.messageTime>0?game.message:"Ziehen zum Laufen · ein Ziel antippen geht auch",48,839,18,INK,true);
        labelLeft(c,game.objective(),48,864,17,MUTED,false);
        button(c,942,814,309,65,"+  Ausbauen",GREEN,true);
    }
    private void drawPanel(Canvas c) {
        box(c,0,0,1280,900,0,0xaa123b32);
        box(c,280,115,720,670,27,CREAM);
        if(panel==Panel.SHOP) {
            labelLeft(c,"Dein Hotel wächst",318,170,34,INK,true);
            labelLeft(c,"Alles mit erspielten Münzen freischalten",318,200,19,MUTED,false);
            String[] titles={"Empfangshilfe","Reinigungskraft","Flinke Füße","Schönere Zimmer"};
            String[] descriptions={"Checkt Gäste selbstständig ein","Hält alle Zimmer sauber","Du läufst schneller · Stufe "+game.speedLevel+"/3","+10 Münzen pro Gast · Stufe "+game.roomLevel+"/3"};
            HotelGame.Upgrade[] upgrades=HotelGame.Upgrade.values();
            for(int i=0;i<4;i++) {
                float y=231+i*109;
                box(c,309,y,662,96,13,0xffe8e8d7);
                labelLeft(c,titles[i],333,y+34,24,INK,true);labelLeft(c,descriptions[i],333,y+65,16,MUTED,false);
                int cost=game.cost(upgrades[i]);
                button(c,798,y+20,151,57,cost<0?"Erledigt":cost+" M",cost>=0&&game.coins>=cost?GREEN:0xff9cae9d,true);
            }
            button(c,310,700,660,57,"Zurück ins Hotel",INK,true);return;
        }
        String title=panel==Panel.WELCOME?"Willkommen im MeiHotel!":panel==Panel.PAUSE?"Eine kleine Pause":"So spielst du";
        label(c,title,640,184,34,INK,true);
        if(panel==Panel.PAUSE) {
            label(c,"Dein Hotel wartet auf dich.",640,240,23,MUTED,false);
            button(c,350,290,580,73,"Weiterspielen",GREEN,true);
            button(c,350,388,580,73,"So funktioniert’s",INK,true);
            button(c,350,486,580,73,"Über MeiHotel",INK,true);
            button(c,350,628,580,66,"Spielstand zurücksetzen",0xffb27c63,true);
            label(c,"Fortschritt wird automatisch gespeichert.",640,746,18,MUTED,false);return;
        }
        String[] lines={"1   Ziehe mit dem Finger, um zu laufen.","     Oder tippe einfach dein Ziel an.","2   Am Empfang begrüßt du die Gäste.","     Für jeden Check-in gibt es Münzen.","3   Geh zum Besen, um Zimmer zu reinigen.","4   Bleib auf einem Baukreis stehen:","     Mit genug Münzen entsteht ein Zimmer.","5   Unter „Ausbauen“ warten fleißige Helfer."};
        for(int i=0;i<lines.length;i++)labelLeft(c,lines[i],338,260+i*43,22,i%2==0?INK:MUTED,i%2==0);
        label(c,"Offline · werbefrei · ohne Käufe",640,660,20,GREEN,true);
        button(c,340,697,600,61,panel==Panel.WELCOME?"Los geht’s!":"Verstanden",GREEN,true);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN) {
            touchX=(event.getX()-offsetX)/scale;touchY=(event.getY()-offsetY)/scale;
            pointer=event.getPointerId(0);stickX=touchX;stickY=touchY;dragging=false;dx=0;dy=0;return true;
        }
        if(action==MotionEvent.ACTION_CANCEL){resetInput();return true;}
        if(action==MotionEvent.ACTION_POINTER_UP&&event.getPointerId(event.getActionIndex())==pointer){resetInput();return true;}
        int index=event.findPointerIndex(pointer);if(index<0)return true;
        float x=(event.getX(index)-offsetX)/scale,y=(event.getY(index)-offsetY)/scale;
        if(action==MotionEvent.ACTION_MOVE&&panel==Panel.NONE&&touchY>100&&touchY<800) {
            if(Math.hypot(x-stickX,y-stickY)>12)dragging=true;
            if(dragging){float distance=(float)Math.hypot(x-stickX,y-stickY);float norm=Math.max(48,distance);dx=(x-stickX)/norm;dy=(y-stickY)/norm;}
        }
        if(action==MotionEvent.ACTION_UP) {
            if(!dragging&&Math.hypot(x-touchX,y-touchY)<20){handleTap(x,y);performClick();}
            pointer=-1;dragging=false;dx=0;dy=0;
        }
        return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    private static boolean inside(float x,float y,float l,float t,float w,float h){return x>=l&&x<=l+w&&y>=t&&y<=t+h;}
    private void handleTap(float x,float y) {
        if(panel==Panel.SHOP) {
            if(inside(x,y,310,700,660,57)){panel=Panel.NONE;return;}
            for(int i=0;i<4;i++)if(inside(x,y,798,251+i*109,151,57)) {
                if(game.buy(HotelGame.Upgrade.values()[i]))store.save(game,false);
                return;
            }
            return;
        }
        if(panel==Panel.WELCOME||panel==Panel.HELP) {
            if(inside(x,y,340,697,600,61))panel=Panel.NONE;return;
        }
        if(panel==Panel.PAUSE) {
            if(inside(x,y,350,290,580,73))panel=Panel.NONE;
            else if(inside(x,y,350,388,580,73))panel=Panel.HELP;
            else if(inside(x,y,350,486,580,73))new AlertDialog.Builder(getContext()).setTitle("MeiHotel "+BuildConfig.VERSION_NAME).setMessage("Ein Hotelspiel von Peter.\n\nEigene Grafik und Spielwelt.\nKeine Werbung, Käufe, Konten oder Datenerfassung.\nDer Spielstand bleibt auf diesem Gerät.\n\nErste Testversion: ein Hotel mit sechs Zimmern und zwei Helfern.").setPositiveButton("Zurück",null).show();
            else if(inside(x,y,350,628,580,66))confirmReset();
            return;
        }
        if(inside(x,y,1014,32,100,48)){resetInput();panel=Panel.HELP;return;}
        if(inside(x,y,1126,32,111,48)){showPause();return;}
        if(inside(x,y,942,814,309,65)){resetInput();panel=Panel.SHOP;return;}
        if(y<130||y>800)return;
        for(HotelGame.Room r:game.rooms)if(inside(x,y,r.x,r.y,270,242)||Math.hypot(x-r.centerX(),y-r.padY())<34) {
            if(r.state==HotelGame.State.LOCKED) {
                game.player.go(r.centerX(),r.padY(),game);
                if(game.coins<r.price())game.announce("Dieses Zimmer kostet "+r.price()+" Münzen.");
            } else game.player.go(r.x+185,r.serviceY(),game);
            return;
        }
        if(x<290&&y>300&&y<510)game.player.go(235,450,game);
        else game.player.go(x,y,game);
    }
    private void confirmReset() {
        new AlertDialog.Builder(getContext()).setTitle("Wirklich neu anfangen?")
            .setMessage("Alle Zimmer, Münzen und Verbesserungen werden zurückgesetzt.")
            .setNegativeButton("Hotel behalten",null).setPositiveButton("Neu anfangen",(dialog,which)->{
                HotelGame fresh=new HotelGame();game.coins=fresh.coins;game.served=0;game.cleaned=0;
                game.hasCleaner=false;game.hasReception=false;game.speedLevel=0;game.roomLevel=0;
                game.guests.clear();game.player.route.clear();game.player.x=245;game.player.y=450;
                game.spawnTimer=2;game.receptionWork=0;game.elapsed=0;
                for(int i=0;i<6;i++){game.rooms[i].state=fresh.rooms[i].state;game.rooms[i].timer=0;game.rooms[i].work=0;}
                game.announce("Ein neues Hotelabenteuer beginnt!");store.save(game,false);panel=Panel.WELCOME;
            }).show();
    }
    private void actor(Canvas c,HotelGame.Actor a,int color,String name) {
        float bob=a.route.isEmpty()?0:(float)Math.sin(game.elapsed*12)*2;
        oval(c,a.x-15,a.y+7,a.x+15,a.y+17,0x25243a31);
        line(c,a.x-6,a.y+2,a.x-7,a.y+11,INK,6);line(c,a.x+6,a.y+2,a.x+7,a.y+11,INK,6);
        box(c,a.x-12,a.y-22+bob,24,29,8,color);
        circle(c,a.x,a.y-31+bob,12,0xffedc19b);
        oval(c,a.x-12,a.y-44+bob,a.x+12,a.y-30+bob,0xff584a3e);
        if(a==game.player){box(c,a.x-12,a.y-48+bob,24,10,4,CREAM);circle(c,a.x,a.y-25+bob,2,INK);}
        if(name!=null){box(c,a.x-44,a.y-77,88,22,7,a==game.player?GREEN:CREAM);label(c,name,a.x,a.y-61,13,a==game.player?CREAM:INK,true);}
        if(a==game.cleaner){line(c,a.x+18,a.y-24,a.x+18,a.y+9,0xffa57f4f,4);box(c,a.x+11,a.y+4,16,8,2,GOLD);}
    }
    private void station(Canvas c,float x,float y,float radius,int color,String title,float work,boolean active) {
        circle(c,x,y,radius+3,0x25ffffff);circle(c,x,y,radius,color);ring(c,x,y,radius-4,0x99ffffff,2);
        if(title.equals("Besen")) { /* Broom is drawn by caller. */ }
        else label(c,title,x,y+5,title.length()>6?13:17,CREAM,true);
        if(work>0){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(6);paint.setColor(GOLD);rect.set(x-radius-3,y-radius-3,x+radius+3,y+radius+3);c.drawArc(rect,-90,Math.min(1,work)*360,false,paint);paint.setStyle(Paint.Style.FILL);}
        if(active&&work<=0)ring(c,x,y,radius+7,0x66ffffff,2);
    }
    private void rug(Canvas c,float x,float y){box(c,x-47,y-55,94,140,18,0xffd09b79);box(c,x-40,y-48,80,126,15,0xffe0b595);}
    private void plant(Canvas c,float x,float y){box(c,x-12,y-3,24,20,5,0xffc19c75);circle(c,x-9,y-12,13,0xff6d9977);circle(c,x+9,y-15,13,0xff84ac7a);circle(c,x,y-24,13,0xff4e826a);}
    private void progress(Canvas c,float x,float y,float w,float value,int color){box(c,x,y,w,6,3,0xffd7dcc9);box(c,x,y,w*Math.max(0,Math.min(1,value)),6,3,color);}
    private void button(Canvas c,float x,float y,float w,float h,String text,int color,boolean bold){box(c,x,y+3,w,h,12,0x18344434);box(c,x,y,w,h,12,color);label(c,text,x+w/2,y+h/2+8,23,CREAM,bold);}
    private void box(Canvas c,float x,float y,float w,float h,float radius,int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);rect.set(x,y,x+w,y+h);c.drawRoundRect(rect,radius,radius,paint);}
    private void circle(Canvas c,float x,float y,float radius,int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);c.drawCircle(x,y,radius,paint);}
    private void oval(Canvas c,float l,float t,float r,float b,int color){paint.setColor(color);paint.setStyle(Paint.Style.FILL);rect.set(l,t,r,b);c.drawOval(rect,paint);}
    private void ring(Canvas c,float x,float y,float radius,int color,float width){paint.setColor(color);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(width);c.drawCircle(x,y,radius,paint);paint.setStyle(Paint.Style.FILL);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){paint.setColor(color);paint.setStrokeWidth(width);paint.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,paint);}
    private void label(Canvas c,String text,float x,float y,float size,int color,boolean bold){paint.setTextAlign(Paint.Align.CENTER);text(c,text,x,y,size,color,bold);}
    private void labelLeft(Canvas c,String text,float x,float y,float size,int color,boolean bold){paint.setTextAlign(Paint.Align.LEFT);text(c,text,x,y,size,color,bold);}
    private void text(Canvas c,String text,float x,float y,float size,int color,boolean bold){paint.setColor(color);paint.setStyle(Paint.Style.FILL);paint.setTypeface(bold?Typeface.create("sans-serif",Typeface.BOLD):Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextSize(size);c.drawText(text,x,y,paint);}
}
