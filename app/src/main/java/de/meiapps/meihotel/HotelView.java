package de.meiapps.meihotel;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
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
    private final LinearGradient gardenLight=new LinearGradient(0,95,1280,810,0xffe4efd7,0xffb9d1ba,Shader.TileMode.CLAMP);
    private final LinearGradient glassLight=new LinearGradient(0,0,0,30,0xff8ec9cf,0xffd8f0e8,Shader.TileMode.CLAMP);
    private final List<HotelGame.Actor> drawingActors=new ArrayList<>();
    private static final int INK=Color.rgb(30,65,61), GREEN=Color.rgb(29,132,111), GOLD=Color.rgb(235,181,81);
    private static final int CREAM=Color.rgb(251,245,229), MUTED=Color.rgb(109,133,124);
    private final int[] guestColors={0xffdf897a,0xffe2b84f,0xff739bb7,0xffa48bb9,0xff70a58a};
    private float scale=1,offsetX,offsetY,stickX,stickY,touchX,touchY,dx,dy,saveClock;
    private int pointer=-1,safeLeft,safeTop,safeRight,safeBottom;
    private boolean dragging,running,focused=true;
    private long lastFrame;
    private enum Panel { NONE, WELCOME, PAUSE, SHOP, HELP, GOALS }
    private Panel panel=Panel.WELCOME;
    private int shopPage,goalPage;

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
        if(game.area==0)drawHotel(canvas);else drawCourtyard(canvas);drawHud(canvas);
        if(panel!=Panel.NONE)drawPanel(canvas);
        else if(dragging){circle(canvas,stickX,stickY,54,0x44315d53);circle(canvas,stickX+dx*36,stickY+dy*36,25,0x99447e6f);}
        canvas.restore();
        if(running&&focused)postInvalidateOnAnimation();
    }
    private void drawHotel(Canvas c) {
        paint.setShader(gardenLight);c.drawRect(0,0,1280,900,paint);paint.setShader(null);
        for(int x=55;x<1260;x+=56) {
            circle(c,x,785,3,0xffffe7a0);circle(c,x+13,795,3,0xfff2b4ac);
            line(c,x,788,x,799,0xff80a17d,2);
        }
        // Garden and softly outlined hotel shell.
        for(int x=40;x<1260;x+=85){circle(c,x,112,13,0xffa9c6a5);circle(c,x+14,105,10,0xffbad1ae);}
        box(c,27,136,1226,644,26,0x18234538);
        box(c,20,128,1226,644,26,0xff6c8d7f);
        box(c,35,142,1196,615,18,0xfff0e4cc);
        // Lobby tile grid.
        for(int x=50;x<300;x+=45)for(int y=160;y<750;y+=45)box(c,x,y,42,42,2,0xffe4d7bd);
        box(c,295,395,915,113,8,0xffd29d74);
        box(c,300,415,904,73,9,0xffecd3a4);
        box(c,300,419,904,65,9,0xff568477);
        line(c,314,425,1190,425,0xffd1b46c,2);line(c,314,478,1190,478,0xffd1b46c,2);
        for(int x=315;x<1200;x+=32)box(c,x,425,2,53,1,0x3376b4a0);
        label(c,"EMPFANG",166,195,23,INK,true);
        box(c,75,352,130,69,14,0x1527463b);
        box(c,69,337,137,69,14,0xff9b6a4b);box(c,69,331,137,42,12,0xffedc68c);
        box(c,88,342,32,17,3,INK);circle(c,179,350,8,GOLD);
        label(c,"M",147,362,24,INK,true);
        rug(c,140,580);
        // Lobby lounge, clock and the unlockable breakfast corner.
        box(c,65,264,61,36,10,0x20223b32);box(c,62,256,61,34,10,0xff739a94);
        box(c,66,258,23,26,7,0xff93b7a7);box(c,94,258,23,26,7,0xff93b7a7);
        circle(c,242,274,21,0xffb89158);circle(c,242,274,17,CREAM);
        line(c,242,274,242,264,INK,2);line(c,242,274,251,278,INK,2);
        if(game.hasCleaner&&game.hasReception) {
            box(c,221,540,48,50,7,0xffaf9677);box(c,226,543,38,12,3,CREAM);
            box(c,226,560,38,12,3,0xffc5ded8);box(c,226,576,38,10,3,CREAM);
            label(c,"WÄSCHE",245,611,12,INK,true);
            ring(c,245,570,29,0x7778a598,2);
            if(game.breakfastLevel>0)ring(c,92,650,31,0x7778a598,2);
        }
        if(game.hasCleaner){box(c,248,676,37,20,5,0xff8ba9a2);circle(c,253,700,4,INK);circle(c,280,700,4,INK);}
        if(game.breakfastLevel>0) {
            box(c,65,617,61,87,8,0x22223b32);box(c,60,608,61,87,8,0xffaa7f58);
            box(c,57,603,67,83,8,0xfff3dfb7);
            for(int n=0;n<game.breakfastLevel;n++) {
                circle(c,89,620+n*23,10,CREAM);oval(c,82,616+n*23,97,624+n*23,GOLD);
            }
            label(c,"BUFFET",91,714,12,INK,true);
        }
        plant(c,67,220);plant(c,269,220);plant(c,1138,790);plant(c,123,790);
        box(c,43,720,70,24,5,0xffede5d3);label(c,"EINGANG",78,736,13,INK,true);
        station(c,235,450,40,GREEN,"Empfang",game.receptionWork/game.receptionDuration(),game.near(235,450,65));
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
    private void drawCourtyard(Canvas c) {
        paint.setShader(gardenLight);c.drawRect(0,0,1280,900,paint);paint.setShader(null);
        label(c,"DEIN HOTELHOF",640,142,24,INK,true);
        box(c,65,375,1150,180,30,0xffddd2b5);
        for(int x=85;x<1210;x+=42)for(int y=391;y<550;y+=39)box(c,x,y,38,34,4,0xffece1c6);
        for(int i=0;i<4;i++) {
            HotelProgress.Amenity a=game.progress.amenities[i];float x=i%2==0?100:760,y=i<2?180:555;
            box(c,x+5,y+8,440,195,18,0x25243b32);
            box(c,x,y,440,195,18,a.level==0?0xffbdc6b1:CREAM);
            label(c,HotelProgress.AREA_NAMES[i]+(a.level==0?" · BAUPLATZ":" · Stufe "+a.level+"/5"),x+220,y+28,22,INK,true);
            if(a.level==0) {
                label(c,"Antippen und unter Hof freischalten",x+220,y+105,18,INK,false);
                label(c,game.progress.amenityRequirement(i).isEmpty()?game.progress.amenityCost(i)+" M":game.progress.amenityRequirement(i),x+220,y+144,21,GREEN,true);
            }else {
                if(i==0) {
                    box(c,x+27,y+62,166,89,10,0xffa57d57);box(c,x+23,y+55,174,32,6,0xffe7b96a);
                    box(c,x+50,y+34,58,34,5,INK);circle(c,x+79,y+49,10,GOLD);
                    for(int n=0;n<a.level;n++){float tx=x+238+(n%3)*60,ty=y+80+(n/3)*65;circle(c,tx,ty,22,0xffc8a47a);circle(c,tx,ty,13,CREAM);circle(c,tx,ty,7,0xff865c3f);}
                }else if(i==1) {
                    box(c,x+40,y+62,115,95,10,0xff8cb8ab);box(c,x+61,y+40,72,35,8,GOLD);
                    line(c,x+118,y+75,x+198,y+153,0xffd8977a,22);
                    for(int n=0;n<a.level+2;n++)box(c,x+235+(n%4)*35,y+82+(n/4)*40,29,29,5,guestColors[n%5]);
                }else if(i==2) {
                    for(int row=0;row<a.level;row++)for(int col=0;col<7;col++) {
                        float fx=x+48+col*55,fy=y+65+row*24;
                        box(c,fx-15,fy+8,30,10,3,0xffaa855f);line(c,fx,fy,fx,fy+9,GREEN,3);
                        circle(c,fx,fy,8,guestColors[(row+col)%5]);circle(c,fx,fy,3,GOLD);
                    }
                }else {
                    box(c,x+29,y+45,376,129,19,0xffb4d3cb);box(c,x+39,y+55,356,109,16,0xff6cb4c6);
                    for(int n=0;n<6;n++)line(c,x+62+n*53,y+95,x+86+n*53,y+95,0x66ffffff,4);
                    for(int n=0;n<a.level;n++){circle(c,x+80+n*62,y+124,13,guestColors[n%5]);circle(c,x+80+n*62,y+124,7,0xff6cb4c6);}
                }
                for(int n=0;n<a.pending;n++) {
                    float gx=a.x+100+n*31,gy=a.y;
                    oval(c,gx-10,gy+1,gx+10,gy+9,0x33243731);box(c,gx-8,gy-18,16,22,5,guestColors[(i+n)%5]);circle(c,gx,gy-25,8,0xffedc19b);
                }
                station(c,a.x,a.y,30,a.pending>0?GREEN:MUTED,a.pending>0?"+"+(35+a.level*12+game.progress.tipLevel*3):"Pause",a.work/2,game.area==1&&Math.hypot(game.player.x-a.x,game.player.y-a.y)<38);
                label(c,a.pending>0?HotelProgress.AREA_JOBS[i]:"Alles bereit · keine Eile",a.x,i<2?398:545,16,INK,true);
            }
        }
        plant(c,625,220);plant(c,665,720);plant(c,35,400);plant(c,1245,550);
        actor(c,game.player,GREEN,"DU");
        if(!game.player.route.isEmpty()){HotelGame.Point target=game.player.route.get(game.player.route.size()-1);ring(c,target.x,target.y,14,GREEN,3);}
    }
    private HotelGame.Upgrade shopUpgrade(int row){
        HotelGame.Upgrade[] hotel={HotelGame.Upgrade.RECEPTION,HotelGame.Upgrade.CLEANER,HotelGame.Upgrade.SPEED,HotelGame.Upgrade.ROOMS,HotelGame.Upgrade.BREAKFAST};
        HotelGame.Upgrade[] team={HotelGame.Upgrade.RECEPTION_TRAINING,HotelGame.Upgrade.CLEANER_TRAINING,HotelGame.Upgrade.CART,HotelGame.Upgrade.TIPS};
        return shopPage==0?hotel[row]:team[row];
    }
    private void drawShop(Canvas c) {
        labelLeft(c,"Dein Hotel wächst",318,170,34,INK,true);
        labelLeft(c,"Deine Münzen: "+game.coins+" · jeder Ausbau bleibt erhalten",318,200,18,MUTED,false);
        String[] tabs={"Hotel","Team","Zimmer","Hof"};
        for(int i=0;i<4;i++)button(c,310+i*167,215,160,43,tabs[i],shopPage==i?GREEN:INK,true);
        if(shopPage==2) {
            for(int i=0;i<6;i++) {
                HotelGame.Room room=game.rooms[i];int cost=game.progress.roomCost(i);float y=270+i*66;
                box(c,310,y,660,59,10,0xffe8e8d7);
                labelLeft(c,"Zimmer "+(i+1)+" · Stufe "+room.level+"/5",329,y+23,21,INK,true);
                labelLeft(c,room.state==HotelGame.State.LOCKED?"Erst im Hotel am Baukreis öffnen":room.level>=5?game.roomIncome(room)+" M pro Gast · Suite fertig":game.roomIncome(room)+" M pro Gast · nächster Ausbau +6 M",329,y+45,16,MUTED,false);
                button(c,798,y+7,151,48,cost<0?(room.state==HotelGame.State.LOCKED?"Zu":"Fertig"):cost+" M",cost>=0&&game.coins>=cost?GREEN:MUTED,true);
            }
        }else {
            String[] hotel={"Empfangshilfe","Reinigungskraft","Flinke Füße","Schönere Zimmer","Frühstücksbuffet"};
            String[] hotelInfo={"Checkt Gäste selbstständig ein","Hält alle Zimmer sauber","Lauftempo · Stufe "+game.speedLevel+"/3","+10 M je Gast · Stufe "+game.roomLevel+"/3","+8 M je Gast · Stufe "+game.breakfastLevel+"/3"};
            String[] team={"Empfang schulen","Reinigung schulen","Servicewagen","Trinkgeldkasse"};
            String[] teamInfo={"Schneller einchecken · "+game.progress.receptionTraining+"/4","Schneller laufen und putzen · "+game.progress.cleanerTraining+"/4",(1+game.progress.cartLevel)+" Waren pro Abholung · Stufe "+game.progress.cartLevel+"/3","Mehr M je Gast und Service · "+game.progress.tipLevel+"/5"};
            for(int i=0;i<(shopPage==0?5:4);i++) {
                float y=275+i*84;int cost;String title,info;
                if(shopPage==3){cost=game.progress.amenityCost(i);title=HotelProgress.AREA_NAMES[i]+" · "+game.progress.amenities[i].level+"/5";info=game.progress.amenityRequirement(i);if(info.isEmpty())info=game.progress.amenities[i].level==0?"+3 M je Gast · neuer Service im Hof":"Nächste Stufe: +3 M je Gast, +12 M je Hofservice";}
                else {cost=game.cost(shopUpgrade(i));title=shopPage==0?hotel[i]:team[i];info=shopPage==0?hotelInfo[i]:teamInfo[i];}
                boolean ready=shopPage!=3||game.progress.amenityRequirement(i).isEmpty();
                box(c,310,y,660,76,12,0xffe8e8d7);labelLeft(c,title,329,y+28,23,INK,true);labelLeft(c,info,329,y+56,16,MUTED,false);
                boolean needsHelper=shopPage==1&&((i==0&&!game.hasReception)||(i==1&&!game.hasCleaner));
                button(c,798,y+12,151,53,needsHelper?"Erst Helfer":cost<0?"Fertig":cost+" M",cost>=0&&game.coins>=cost&&ready?GREEN:MUTED,true);
            }
        }
        button(c,310,710,660,47,"Zurück zum Spielen",INK,true);
    }
    private void drawGoals(Canvas c) {
        label(c,"Deine nächsten Ziele",640,175,34,INK,true);
        label(c,"Aufträge wiederholen sich · Ausbau bleibt für immer",640,201,18,MUTED,false);
        String[] tabs={"Aufträge","Hotelsterne","Meilensteine"};
        for(int i=0;i<3;i++)button(c,310+i*222,215,216,43,tabs[i],goalPage==i?GREEN:INK,true);
        if(goalPage==0) {
            String[] names={"Gäste begrüßen","Zimmerservice liefern","Service im Hotelhof"};
            for(int i=0;i<3;i++) {
                float y=275+i*128;int goal=game.progress.contractGoal(i),value=game.progress.contractProgress(i);
                box(c,310,y,660,114,13,0xffe8e8d7);labelLeft(c,names[i]+" · Auftrag "+(game.progress.contractTier[i]+1),330,y+30,23,INK,true);
                labelLeft(c,Math.min(value,goal)+" / "+goal+" · Belohnung "+game.progress.contractReward(i)+" M",330,y+62,18,MUTED,false);
                progress(c,330,y+85,430,(float)value/goal,GREEN);
                button(c,795,y+17,155,53,value>=goal?"Abholen":"Läuft",value>=goal?GREEN:MUTED,true);
            }
        }else if(goalPage==1) {
            label(c,new String(new char[game.progress.stars]).replace("\0","★"),640,321,50,GOLD,true);
            label(c,game.progress.stars+" von 5 Hotelsternen",640,366,28,INK,true);
            label(c,"Nächster Schritt:",640,425,22,INK,true);
            label(c,game.progress.starRequirement(),640,465,21,GREEN,true);
            label(c,"Jeder neue Stern bringt +10 M für jeden Gast.",640,525,19,MUTED,false);
            label(c,"Zimmer, Hof und Zimmerservice zählen zusammen.",640,561,18,MUTED,false);
            int cost=game.progress.starCost();
            button(c,350,625,580,58,cost<0?"Traumhotel erreicht":cost+" M · Hotelstern kaufen",game.progress.starReady()&&game.coins>=cost?GREEN:MUTED,true);
        }else {
            for(int i=0;i<HotelGame.GOAL_GUESTS.length;i++) {
                float x=310+(i%2)*335,y=275+(i/2)*103;int goal=HotelGame.GOAL_GUESTS[i];boolean done=(game.claimedGoals&(1<<i))!=0;
                box(c,x,y,325,92,12,done?0xffd9e7d4:0xffe8e8d7);
                labelLeft(c,goal+" Gäste · +"+HotelGame.GOAL_REWARDS[i]+" M",x+17,y+30,20,INK,true);
                labelLeft(c,done?"Geschafft!":Math.min(game.served,goal)+" / "+goal,x+17,y+57,17,MUTED,false);
                progress(c,x+17,y+73,290,(float)game.served/goal,GREEN);
            }
        }
        button(c,310,710,660,47,"Zurück zum Spielen",INK,true);
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
        int accent=guestColors[(r.id+r.level)%guestColors.length];
        // Staggered parquet, wall trim and a soft rug give rooms depth.
        for(int row=0;row<8;row++)for(int col=0;col<5;col++) {
            float px=r.x+9+col*50,py=r.y+12+row*24;
            box(c,px,py,48,22,2,(row+col)%2==0?0x0ba37c58:0x14a37c58);
        }
        box(c,r.x+8,r.y+7,254,43,5,0xffe5d9bf);
        line(c,r.x+9,r.y+50,r.x+261,r.y+50,0xffc9b48d,3);
        oval(c,r.x+139,r.y+113,r.x+240,r.y+187,0x1b6b8c79);
        // Window, bed, bedside table and seating are all original vector shapes.
        c.save();c.translate(r.x+90,r.y+15);paint.setShader(glassLight);rect.set(0,0,90,28);c.drawRoundRect(rect,5,5,paint);paint.setShader(null);c.restore();
        line(c,r.x+96,r.y+35,r.x+120,r.y+20,0x88ffffff,3);
        box(c,r.x+78,r.y+12,14,38,4,accent);box(c,r.x+179,r.y+12,14,38,4,accent);
        line(c,r.x+76,r.y+11,r.x+196,r.y+11,0xffa37c58,3);
        line(c,r.x+135,r.y+15,r.x+135,r.y+43,CREAM,4);
        box(c,r.x+23,r.y+76,99,121,11,0x16234538);
        box(c,r.x+20,r.y+68,99,121,9,0xffa37c58);
        box(c,r.x+23,r.y+74,93,108,7,CREAM);
        box(c,r.x+28,r.y+78,83,29,6,0xfffffdf6);
        box(c,r.x+23,r.y+111,93,70,6,accent);
        box(c,r.x+23,r.y+118,93,12,0,0x22ffffff);
        line(c,r.x+33,r.y+136,r.x+107,r.y+136,0x22ffffff,2);
        line(c,r.x+33,r.y+158,r.x+107,r.y+158,0x22ffffff,2);
        box(c,r.x+25,r.y+80,37,23,6,0xfffffdf6);box(c,r.x+68,r.y+80,37,23,6,0xfff3e8ce);
        // Framed landscape and reading lamp.
        box(c,r.x+209,r.y+17,36,26,3,0xffae855a);box(c,r.x+213,r.y+21,28,18,1,0xffb3d1c0);
        oval(c,r.x+214,r.y+30,r.x+237,r.y+39,0xff709a7b);
        circle(c,r.x+236,r.y+26,4,GOLD);circle(c,r.x+147,r.y+100,17,0x22ffd88a);
        box(c,r.x+133,r.y+88,29,29,6,0xffc29d70);circle(c,r.x+147,r.y+100,9,GOLD);
        box(c,r.x+214,r.y+53,24,43,7,accent);
        plant(c,r.x+234,r.y+199);
        if(game.roomLevel>0){box(c,r.x+184,r.y+62,21,22,4,GOLD);label(c,"★",r.x+194,r.y+79,17,INK,true);}
        if(game.roomLevel>1){box(c,r.x+179,r.y+113,53,35,7,0xffe4cda3);circle(c,r.x+205,r.y+129,9,accent);}
        if(game.roomLevel>2){label(c,"★★★",r.centerX(),r.y+61,18,0xffaa7b28,true);}
        if(r.level>=1)line(c,r.x+30,r.y+172,r.x+108,r.y+172,GOLD,4);
        if(r.level>=2){box(c,r.x+166,r.y+151,74,35,10,0xffdcb98e);line(c,r.x+175,r.y+166,r.x+231,r.y+166,CREAM,2);}
        if(r.level>=3){box(c,r.x+171,r.y+47,34,39,4,0xffae855a);for(int n=0;n<4;n++)box(c,r.x+175+n*7,r.y+53,5,24,1,guestColors[n]);}
        if(r.level>=4){ring(c,r.x+147,r.y+100,13,GOLD,3);box(c,r.x+7,r.y+7,256,4,2,GOLD);}
        if(r.level>=5){box(c,r.x+100,r.y+46,70,23,6,GOLD);label(c,"SUITE",r.centerX(),r.y+63,14,INK,true);}
        String state;
        switch(r.state){
            case OCCUPIED: state="ZU GAST";break;
            case ARRIVING: state="WILLKOMMEN";break;
            case DIRTY: state="SAUBER MACHEN";break;
            default: state="FREI";
        }
        box(c,r.x+20,r.y+207,230,21,6,r.state==HotelGame.State.DIRTY?0xfff3d9b1:0xffd6e8d6);
        label(c,(r.id+1)+" · "+r.level+"★ · "+state,r.centerX(),r.y+222,14,INK,true);
        // Door opening connects visually with the navigation corridor.
        box(c,r.centerX()-32,r.doorY()-6,64,17,3,0xfff0e4cc);
        if(r.state==HotelGame.State.DIRTY) {
            for(int i=0;i<4;i++)circle(c,r.x+165+i*15,r.serviceY()+8+(i%2)*13,5,0xffb49870);
            station(c,r.x+185,r.serviceY(),28,0xffb57738,"Besen",r.work/game.cleaningDuration(),false);
            // Broom icon over the service marker.
            line(c,r.x+176,r.serviceY()-8,r.x+191,r.serviceY()+9,CREAM,4);
            line(c,r.x+181,r.serviceY()+11,r.x+195,r.serviceY()-1,CREAM,9);
        } else if(r.state==HotelGame.State.OCCUPIED) {
            label(c,"z Z",r.x+74,r.y+106,22,INK,true);
            if(r.request!=0) {
                box(c,r.x+142,r.y+128,112,53,9,0xfffcf5df);
                label(c,r.request==1?"Handtuch":"Frühstück",r.x+198,r.y+150,16,INK,true);
                label(c,"+"+((r.request==1?18:22+game.breakfastLevel*4)+game.progress.tipLevel*3)+" M",r.x+198,r.y+172,16,GREEN,true);
            } else progress(c,r.x+170,r.y+170,60,1-r.timer/40,0xff70a58a);
        }
    }
    private void drawHud(Canvas c) {
        box(c,26,20,1228,73,20,INK);
        labelLeft(c,"MeiHotel",50,66,34,CREAM,true);
        labelLeft(c,"DEIN KLEINES TRAUMHOTEL",52,84,11,0xffc1d8c7,true);
        circle(c,357,55,20,GOLD);label(c,"M",357,63,22,INK,true);
        labelLeft(c,String.valueOf(game.coins),389,67,32,CREAM,true);
        labelLeft(c,game.openRooms()+" Zimmer · "+game.progress.stars+(game.progress.stars==1?" Stern":" Sterne"),570,50,21,CREAM,true);
        labelLeft(c,game.served+" Gäste · "+game.deliveries+" Lieferungen",570,74,16,0xffc1d8c7,false);
        button(c,813,32,85,48,game.progress.hasRewardReady()?"Ziele!":"Ziele",0xff94713f,true);
        button(c,910,32,90,48,game.area==0?"Hof":"Hotel",GREEN,true);
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
        if(panel==Panel.GOALS){drawGoals(c);return;}
        if(panel==Panel.SHOP){drawShop(c);return;}
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
        String[] lines={"1   Ziehe mit dem Finger, um zu laufen.","     Oder tippe einfach dein Ziel an.","2   Am Empfang begrüßt du die Gäste.","     Für jeden Check-in gibt es Münzen.","3   Geh zum Besen, um Zimmer zu reinigen.","4   Bleib auf einem Baukreis stehen:","     Mit genug Münzen entsteht ein Zimmer.","5   Mit beiden Helfern: Gästewünsche erfüllen!"};
        for(int i=0;i<lines.length;i++)labelLeft(c,lines[i],338,260+i*43,22,i%2==0?INK:MUTED,i%2==0);
        label(c,"Hof: neue Bereiche · Ziele: Aufträge und Hotelsterne",640,629,18,GREEN,true);
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
        if(panel==Panel.GOALS) {
            if(inside(x,y,310,710,660,47)){panel=Panel.NONE;return;}
            for(int i=0;i<3;i++)if(inside(x,y,310+i*222,215,216,43)){goalPage=i;return;}
            boolean changed=false;
            if(goalPage==0)for(int i=0;i<3;i++)if(inside(x,y,795,292+i*128,155,53))changed=game.progress.claimContract(i);
            if(goalPage==1&&inside(x,y,350,625,580,58))changed=game.progress.buyStar();
            if(changed)store.save(game,false);return;
        }
        if(panel==Panel.SHOP) {
            if(inside(x,y,310,710,660,47)){panel=Panel.NONE;return;}
            for(int i=0;i<4;i++)if(inside(x,y,310+i*167,215,160,43)){shopPage=i;return;}
            boolean changed=false;
            if(shopPage==2){for(int i=0;i<6;i++)if(inside(x,y,798,277+i*66,151,48))changed=game.progress.buyRoom(i);}
            else for(int i=0;i<(shopPage==0?5:4);i++)if(inside(x,y,798,287+i*84,151,53)) {
                changed=shopPage==3?game.progress.buyAmenity(i):game.buy(shopUpgrade(i));
            }
            if(changed)store.save(game,false);return;
        }
        if(panel==Panel.WELCOME||panel==Panel.HELP) {
            if(inside(x,y,340,697,600,61))panel=Panel.NONE;return;
        }
        if(panel==Panel.PAUSE) {
            if(inside(x,y,350,290,580,73))panel=Panel.NONE;
            else if(inside(x,y,350,388,580,73))panel=Panel.HELP;
            else if(inside(x,y,350,486,580,73))new AlertDialog.Builder(getContext()).setTitle("MeiHotel "+BuildConfig.VERSION_NAME).setMessage("Ein Hotelspiel von Peter.\n\nEigene Grafik und Spielwelt.\nKeine Werbung, Käufe, Konten oder Datenerfassung.\nDer Spielstand bleibt auf diesem Gerät.\n\nZimmerausbau, Hotelhof, Teamtraining, Aufträge und fünf Hotelsterne.").setPositiveButton("Zurück",null).show();
            else if(inside(x,y,350,628,580,66))confirmReset();
            return;
        }
        if(inside(x,y,910,32,90,48)){resetInput();game.switchArea();store.save(game,false);return;}
        if(inside(x,y,813,32,85,48)){resetInput();panel=Panel.GOALS;return;}
        if(inside(x,y,1014,32,100,48)){resetInput();panel=Panel.HELP;return;}
        if(inside(x,y,1126,32,111,48)){showPause();return;}
        if(inside(x,y,942,814,309,65)){resetInput();panel=Panel.SHOP;return;}
        if(y<130||y>800)return;
        if(game.area==1) {
            for(int i=0;i<4;i++) {
                float left=i%2==0?100:760,top=i<2?180:555;
                HotelProgress.Amenity a=game.progress.amenities[i];
                if(inside(x,y,left,top,440,195)||Math.hypot(x-a.x,y-a.y)<40) {
                    if(a.level==0){resetInput();shopPage=3;panel=Panel.SHOP;}
                    else game.player.go(a.x,a.y,game);
                    return;
                }
            }
            game.player.go(x,y,game);return;
        }
        if(game.hasCleaner&&game.hasReception) {
            if(inside(x,y,215,535,60,80)) {
                if(game.cargo==2){game.cargo=0;game.cargoCount=0;game.announce("Frühstück zurückgelegt. Jetzt Handtücher holen.");}
                game.player.go(245,570,game);return;
            }
            if(game.breakfastLevel>0&&inside(x,y,55,600,75,120)) {
                if(game.cargo==1){game.cargo=0;game.cargoCount=0;game.announce("Handtücher zurückgelegt. Jetzt Frühstück holen.");}
                game.player.go(92,650,game);return;
            }
        }
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
                HotelGame fresh=new HotelGame();game.progress.reset();game.area=0;game.cargoCount=0;game.coins=fresh.coins;game.served=0;game.cleaned=0;
                game.breakfastLevel=0;game.claimedGoals=0;game.cargo=0;game.deliveries=0;game.resetStaff();
                game.hasCleaner=false;game.hasReception=false;game.speedLevel=0;game.roomLevel=0;
                game.guests.clear();game.player.route.clear();game.player.x=245;game.player.y=450;
                game.spawnTimer=2;game.receptionWork=0;game.elapsed=0;
                for(int i=0;i<6;i++){game.rooms[i].state=fresh.rooms[i].state;game.rooms[i].timer=0;game.rooms[i].work=0;game.rooms[i].request=0;game.rooms[i].level=0;}
                game.announce("Ein neues Hotelabenteuer beginnt!");store.save(game,false);panel=Panel.WELCOME;
            }).show();
    }
    private void actor(Canvas c,HotelGame.Actor a,int color,String name) {
        float bob=a.route.isEmpty()?0:(float)Math.sin(game.elapsed*12)*2;
        oval(c,a.x-15,a.y+7,a.x+15,a.y+17,0x25243a31);
        float step=a.route.isEmpty()?0:(float)Math.sin(game.elapsed*12)*5;
        line(c,a.x-6,a.y+2,a.x-7,a.y+11+step,INK,6);line(c,a.x+6,a.y+2,a.x+7,a.y+11-step,INK,6);
        line(c,a.x-13,a.y-17+bob,a.x-18,a.y-3-step,0xffedc19b,5);
        line(c,a.x+13,a.y-17+bob,a.x+18,a.y-3+step,0xffedc19b,5);
        box(c,a.x-12,a.y-22+bob,24,29,8,color);
        box(c,a.x-8,a.y-18+bob,4,19,2,0x25ffffff);
        circle(c,a.x,a.y-31+bob,12,0xffedc19b);
        oval(c,a.x-12,a.y-44+bob,a.x+12,a.y-30+bob,0xff584a3e);
        circle(c,a.x-4,a.y-29+bob,1.3f,INK);circle(c,a.x+4,a.y-29+bob,1.3f,INK);
        line(c,a.x-2,a.y-24+bob,a.x+2,a.y-24+bob,0xffa86956,1.5f);
        if(a!=game.player&&a!=game.cleaner&&a!=game.receptionist) {
            box(c,a.x+17,a.y-5,13,18,3,0xffae7f54);line(c,a.x+20,a.y-6,a.x+26,a.y-6,INK,2);
            circle(c,a.x+20,a.y+15,2,INK);circle(c,a.x+27,a.y+15,2,INK);
        }
        if(a==game.player){box(c,a.x-12,a.y-48+bob,24,10,4,CREAM);circle(c,a.x,a.y-25+bob,2,INK);}
        if(name!=null){box(c,a.x-44,a.y-77,88,22,7,a==game.player?GREEN:CREAM);label(c,name,a.x,a.y-61,13,a==game.player?CREAM:INK,true);}
        if(a==game.player&&game.cargo>0) {
            if(game.cargoCount>1)label(c,"×"+game.cargoCount,a.x+30,a.y-5,15,INK,true);
            box(c,a.x-18,a.y-12+bob,36,17,4,game.cargo==1?0xfff6f1df:0xffbe955e);
            if(game.cargo==1){line(c,a.x-14,a.y-4+bob,a.x+14,a.y-4+bob,0xff87bab1,3);}
            else {circle(c,a.x,a.y-9+bob,9,CREAM);oval(c,a.x-6,a.y-13+bob,a.x+6,a.y-6+bob,GOLD);}
        }
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
