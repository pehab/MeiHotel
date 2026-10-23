package de.meiapps.meihotel;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Original vector furnishings. Each purchase changes the structure within fixed collision footprints. */
final class HotelArt {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r=new RectF();
    private final Path path=new Path();
    private static final int WOOD=0xffaa7951, DARK=0xff365b53, CREAM=0xfffff7de, WATER=0xff62bbd4, GOLD=0xffedbb57;
    void amenity(Canvas c,int type,int level,float x,float y){
        c.save();c.translate(x+14,y+42);
        // Art stays in the 412 x 139 building plot; all service pads remain outside.
        c.clipRect(0,0,412,139);
        if(type==0)cafe(c,level);else if(type==1)playground(c,level);else if(type==2)garden(c,level);else pool(c,level);
        c.restore();
    }
    private void cafe(Canvas c,int n){
        if(n==1){
            box(c,126,58,127,55,7,WOOD);box(c,121,52,137,13,4,0xffe6c18b);
            box(c,144,26,46,29,5,DARK);dot(c,167,38,7,GOLD);cup(c,218,46);
            dot(c,142,119,8,DARK);dot(c,237,119,8,DARK);return;
        }
        float w=n>=4?185:154;
        box(c,28,40,w,83,7,WOOD);box(c,33,48,w-10,29,3,0xffc5dfd5);
        line(c,36,12,36,115,WOOD,6);line(c,28+w-8,12,28+w-8,115,WOOD,6);
        box(c,21,9,w+14,28,5,0xffd39172);
        for(int i=0;i<6;i++)box(c,22+i*(w+12)/6,10,(w+12)/12,26,2,CREAM);
        box(c,24,79,w+8,15,4,0xffe8c58e);box(c,48,47,39,30,4,DARK);dot(c,67,60,7,GOLD);
        cup(c,109,72);cup(c,134,72);
        if(n>=3){box(c,151,54,31,23,3,0xffe9e1ca);dot(c,160,65,5,0xffbf8656);dot(c,174,65,5,GOLD);}
        table(c,275,90,n>=3);
        if(n>=3)table(c,355,61,n>=4);
        if(n>=4){umbrella(c,274,29,0xff7ea69a);planter(c,225,118);}
        if(n>=5){
            box(c,223,11,170,8,3,WOOD);line(c,231,11,231,125,WOOD,5);line(c,385,11,385,125,WOOD,5);
            for(int i=0;i<6;i++)line(c,225+i*32,2,238+i*30,14,0xff9fbcb0,7);
            planter(c,389,121);box(c,50,100,150,8,3,GOLD);
        }
    }
    private void playground(Canvas c,int n){
        if(n<=2){
            box(c,n==1?130:24,57,149,66,12,WOOD);box(c,n==1?138:32,65,133,50,9,0xffead29c);
            box(c,n==1?177:70,84,21,18,4,0xffd78e74);line(c,n==1?213:111,77,n==1?204:102,103,DARK,4);
            if(n==1)return;
        }
        if(n==2){
            line(c,242,16,220,123,WOOD,8);line(c,347,16,370,123,WOOD,8);line(c,242,16,347,16,WOOD,10);
            for(int i=0;i<2;i++){float x=263+i*52;line(c,x,22,x,92,DARK,2);line(c,x+24,22,x+24,92,DARK,2);box(c,x-3,91,30,9,4,0xffd58c72);}return;
        }
        tower(c,77,28,n==5?0xffd39172:0xff80a99a);
        // Wide slide with contrasting side rails, rather than extra blocks.
        curve(c,122,67,150,67,135,114,202,119,0xffbd794e,23);
        curve(c,122,64,150,64,135,111,202,116,GOLD,16);
        if(n>=4){
            oval(c,238,93,370,131,0xff526f68);oval(c,246,96,362,124,0xff8bb4a8);
            for(int i=0;i<4;i++)line(c,251+i*33,119,251+i*33,138,DARK,4);
            for(int i=0;i<4;i++)line(c,245+i*39,94,245+i*39,43,DARK,3);
            line(c,245,43,362,43,0xffa1b9ab,3);
        }else{box(c,257,80,94,43,12,WOOD);box(c,264,86,80,30,8,0xffe8ce92);}
        if(n>=5){
            tower(c,194,11,0xff779bb0);
            line(c,119,62,195,46,WOOD,7);line(c,119,30,195,18,DARK,2);
            for(int i=0;i<6;i++)line(c,126+i*12,29-i*2,126+i*12,62-i*2,DARK,2);
        }
    }
    private void garden(Canvas c,int n){
        if(n==1){bed(c,143,60,125,59);return;}
        box(c,20,10,372,119,12,0xffc8d9b5);
        if(n==2){bed(c,60,42,125,63);bed(c,225,42,125,63);return;}
        for(int i=0;i<7;i++)oval(c,172+i*6,7+i*17,208+i*6,21+i*17,0xffe5dec6);
        bed(c,29,25,113,43);bed(c,29,84,113,41);
        bench(c,275,96);
        if(n>=4){
            line(c,260,20,260,121,WOOD,5);line(c,376,20,376,121,WOOD,5);
            for(int i=0;i<5;i++)line(c,248,20+i*8,387,20+i*8,0xffb99a69,5);
            for(int i=0;i<5;i++)dot(c,260+i*29,17,12,0xff78a17a);
        }
        if(n>=5){
            oval(c,154,49,241,100,0xffaebdb4);oval(c,161,54,234,94,WATER);
            box(c,192,39,13,42,5,0xffe5dec6);oval(c,172,30,223,51,0xffcad3bd);
            curve(c,197,31,177,6,166,15,165,54,0xffa6dce0,3);curve(c,200,31,224,8,234,19,230,61,0xffa6dce0,3);
            for(int i=0;i<4;i++)flower(c,279+i*28,28,0xffe5a0a1);
        }
    }
    private void pool(Canvas c,int n){
        float w=n==1?115:n==2?180:n==3?247:264;
        float h=n==1?70:n==2?91:105;
        float x=n==1?143:n==2?103:35,y=n==1?48:30;
        box(c,x-8,y-3,w+16,h+10,22,0x20395551);
        box(c,x-5,y-9,w+10,h+10,n==1?40:19,0xffc5d4c7);
        box(c,x,y,w,h,n==1?35:16,0xffe9e6cb);
        box(c,x+7,y+7,w-14,h-14,n==1?31:12,WATER);
        line(c,x+20,y+18,x+w-25,y+18,0x99d9f4ed,3);
        for(int i=0;i<3;i++)line(c,x+23+i*(w-55)/3,y+h*.6f,x+39+i*(w-55)/3,y+h*.6f,0x77ffffff,2);
        if(n>=2){line(c,x+w-33,y-6,x+w-33,y+23,DARK,3);line(c,x+w-18,y-6,x+w-18,y+23,DARK,3);line(c,x+w-33,y+12,x+w-18,y+12,CREAM,3);}
        if(n>=3){lounger(c,321,75);umbrella(c,346,29,0xffd59a7d);}
        if(n>=4){
            // A raised access tower and a curved water slide enter the larger pool.
            box(c,305,13,50,38,6,WOOD);line(c,310,43,310,99,WOOD,5);line(c,348,43,348,83,WOOD,5);
            for(int i=0;i<4;i++)line(c,350,54+i*12,374,62+i*12,0xffbaa37e,3);
            line(c,352,41,378,118,WOOD,4);
            curve(c,314,29,269,26,294,82,246,86,0xffb7764c,24);
            curve(c,314,26,269,23,294,79,246,83,GOLD,17);
            oval(c,228,81,264,95,0x99d9f2ed);
        }
        if(n>=5){
            box(c,319,5,42,12,4,0xffd39172);lounger(c,368,78);
            curve(c,89,30,80,1,112,0,116,48,0xffc9f0ee,4);oval(c,105,45,127,53,0x99ffffff);
            planter(c,18,114);planter(c,396,121);
        }
    }
    void room(Canvas c,HotelGame.Room room){
        float x=room.x,y=room.y;int n=room.level;float couchY=room.id<3?53:120;
        // Keep the bed footprint and service spot unchanged for reliable navigation.
        if(n>=1){box(c,x+29,y+86,33,19,5,0xffd0ad69);box(c,x+72,y+86,33,19,5,0xffd0ad69);line(c,x+30,y+171,x+109,y+171,GOLD,5);}
        if(n>=2){box(c,x+160,y+116,78,68,11,0xffd8b991);box(c,x+167,y+122,64,55,8,0xfff0ddbc);line(c,x+173,y+148,x+225,y+148,0xffc1a47c,3);}
        if(n>=3){box(c,x+172,y+couchY,69,38,7,0xff729c91);box(c,x+179,y+couchY+4,25,28,5,0xffa3c0ad);box(c,x+208,y+couchY+4,25,28,5,0xffa3c0ad);box(c,x+176,y+couchY+36,8,6,2,WOOD);box(c,x+228,y+couchY+36,8,6,2,WOOD);}
        if(n>=4){box(c,x+201,y+12,56,35,5,WOOD);box(c,x+206,y+17,46,25,3,DARK);line(c,x+211,y+37,x+246,y+22,0xff83aba0,3);box(c,x+201,y+45,56,5,2,GOLD);}
        if(n>=5){
            box(c,x+99,y+46,73,24,6,GOLD);line(c,x+29,y+76,x+29,y+173,0xffb88a48,3);line(c,x+111,y+76,x+111,y+173,0xffb88a48,3);
            box(c,x+130,y+117,25,32,6,WOOD);oval(c,x+133,y+117,x+152,y+133,0xfff6e4b8);
        }
    }
    private void bed(Canvas c,float x,float y,float w,float h){box(c,x,y,w,h,8,WOOD);box(c,x+5,y+5,w-10,h-10,5,0xff806747);for(int i=0;i<5;i++)flower(c,x+15+i*(w-30)/4,y+h/2, i%2==0?0xffdf9a9a:GOLD);}
    private void flower(Canvas c,float x,float y,int color){line(c,x,y,x,y+12,0xff6f956e,2);dot(c,x-4,y-2,5,color);dot(c,x+4,y-2,5,color);dot(c,x,y-6,5,color);dot(c,x,y,3,GOLD);}
    private void planter(Canvas c,float x,float y){box(c,x-9,y-4,18,17,4,WOOD);dot(c,x-7,y-11,10,0xff709b7a);dot(c,x+6,y-13,10,0xff8eae7c);}
    private void bench(Canvas c,float x,float y){box(c,x,y,82,15,4,WOOD);box(c,x,y-19,82,12,4,0xffc19b6b);line(c,x+9,y+10,x+9,y+24,DARK,4);line(c,x+73,y+10,x+73,y+24,DARK,4);}
    private void tower(Canvas c,float x,float y,int color){box(c,x,y+22,48,54,5,WOOD);box(c,x-7,y+12,62,20,5,color);line(c,x+4,y+75,x+4,127,WOOD,5);line(c,x+43,y+75,x+43,127,WOOD,5);for(int i=0;i<4;i++)line(c,x+7,y+81+i*11,x+35,y+81+i*11,CREAM,3);}
    private void table(Canvas c,float x,float y,boolean seats){oval(c,x-23,y-15,x+23,y+15,0xffbc9970);oval(c,x-20,y-14,x+20,y+10,CREAM);cup(c,x,y-4);if(seats){box(c,x-39,y-9,13,22,4,0xff7eaa99);box(c,x+26,y-9,13,22,4,0xff7eaa99);}}
    private void cup(Canvas c,float x,float y){oval(c,x-9,y-5,x+9,y+5,CREAM);dot(c,x,y,5,0xff916644);}
    private void umbrella(Canvas c,float x,float y,int color){line(c,x,y,x,y+48,WOOD,3);oval(c,x-30,y-11,x+30,y+14,color);line(c,x-23,y+3,x+23,y+3,0x55ffffff,2);}
    private void lounger(Canvas c,float x,float y){box(c,x,y,22,49,5,0xffdfd3b1);box(c,x+3,y+4,16,33,3,0xffd99c80);line(c,x+2,y+18,x+20,y+18,CREAM,3);}
    private void box(Canvas c,float x,float y,float w,float h,float radius,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);r.set(x,y,x+w,y+h);c.drawRoundRect(r,radius,radius,p);}
    private void dot(Canvas c,float x,float y,float radius,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,radius,p);}
    private void oval(Canvas c,float x,float y,float xx,float yy,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);r.set(x,y,xx,yy);c.drawOval(r,p);}
    private void line(Canvas c,float x,float y,float xx,float yy,int color,float width){p.setColor(color);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x,y,xx,yy,p);}
    private void curve(Canvas c,float x,float y,float a,float b,float d,float e,float xx,float yy,int color,float width){path.reset();path.moveTo(x,y);path.cubicTo(a,b,d,e,xx,yy);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);}
}
