package de.meiapps.meihotel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Small navigation grid: guests and tap-to-walk use the same doors and walls as the player. */
public final class Navigation {
    private static final int CELL=10, COLS=128, ROWS=90;
    private Navigation() { }
    public static boolean walkable(HotelGame game,float x,float y) {
        if(!Float.isFinite(x)||!Float.isFinite(y))return false;
        if(x>=50&&x<=295&&y>=170&&y<=750)return true;
        if(x>=280&&x<=1205&&y>=402&&y<=506)return true;
        for(HotelGame.Room r:game.rooms)if(r.state!=HotelGame.State.LOCKED) {
            if(x>=r.x+18&&x<=r.x+252&&y>=r.y+30&&y<=r.y+226) {
                // Bed footprint, including a small shoulder clearance.
                boolean bed=x<r.x+130&&y>r.y+76&&y<r.y+190;
                if(!bed)return true;
            }
            if(Math.abs(x-r.centerX())<=33&&y>=r.doorY()-22&&y<=r.doorY()+40)return true;
        }
        return false;
    }
    public static boolean playerWalkable(HotelGame game,float x,float y){return walkable(game,x,y,game.area==1);}
    private static boolean walkable(HotelGame game,float x,float y,boolean outdoor){
        if(!outdoor)return walkable(game,x,y);
        if(!Float.isFinite(x)||!Float.isFinite(y)||x<70||x>1210||y<170||y>765)return false;
        // Furniture and water are obstacles; the central promenade joins all service pads.
        return !(x>=100&&x<=540&&y>=180&&y<=370)
            &&!(x>=760&&x<=1180&&y>=180&&y<=370)
            &&!(x>=100&&x<=540&&y>=555&&y<=750)
            &&!(x>=760&&x<=1180&&y>=555&&y<=750);
    }
    private static int index(float x,float y){return (int)(y/CELL)*COLS+(int)(x/CELL);}
    private static HotelGame.Point point(int i){return new HotelGame.Point((i%COLS)*CELL+5,(i/COLS)*CELL+5);}
    public static List<HotelGame.Point> path(HotelGame game,float sx,float sy,float tx,float ty) {
        return path(game,sx,sy,tx,ty,false);
    }
    public static List<HotelGame.Point> path(HotelGame game,float sx,float sy,float tx,float ty,boolean outdoor) {
        if(!walkable(game,sx,sy,outdoor)||!walkable(game,tx,ty,outdoor))return Collections.emptyList();
        int start=index(sx,sy), goal=index(tx,ty);
        int[] previous=new int[COLS*ROWS];Arrays.fill(previous,-1);
        ArrayDeque<Integer> queue=new ArrayDeque<>();queue.add(start);previous[start]=start;
        int[] offsets={-1,1,-COLS,COLS};
        while(!queue.isEmpty()&&previous[goal]<0) {
            int cell=queue.removeFirst();
            for(int offset:offsets) {
                int next=cell+offset;
                if(next<0||next>=previous.length||previous[next]>=0)continue;
                if(Math.abs(next%COLS-cell%COLS)+Math.abs(next/COLS-cell/COLS)!=1)continue;
                HotelGame.Point p=point(next);
                if(!walkable(game,p.x,p.y,outdoor))continue;
                previous[next]=cell;queue.addLast(next);
            }
        }
        if(previous[goal]<0)return Collections.emptyList();
        List<HotelGame.Point> route=new ArrayList<>();
        for(int cell=goal;cell!=start;cell=previous[cell])route.add(point(cell));
        Collections.reverse(route);route.add(new HotelGame.Point(tx,ty));return route;
    }
}
