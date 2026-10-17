package de.meiapps.meihotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Pure simulation. All times are active play seconds; no clock, network or Android dependencies. */
public final class HotelGame {
    public static final float WIDTH = 1280, HEIGHT = 900;
    public static final int MAX_COINS = 999_999;
    public enum State { LOCKED, READY, ARRIVING, OCCUPIED, DIRTY }
    public enum Upgrade { RECEPTION, CLEANER, SPEED, ROOMS, BREAKFAST }
    public static final class Point {
        public float x, y;
        public Point(float x, float y) { this.x = x; this.y = y; }
    }
    public static final class Room {
        public final int id;
        public final float x, y;
        public State state;
        public float timer, work;
        public int request; // 0: none, 1: towels, 2: breakfast
        Room(int id) {
            this.id = id; x = 320 + (id % 3) * 290; y = id < 3 ? 146 : 520;
            state = id < 2 ? State.READY : State.LOCKED;
        }
        public float doorY() { return id < 3 ? 388 : 520; }
        public float padY() { return id < 3 ? 416 : 492; }
        public float centerX() { return x + 135; }
        public float serviceY() { return id < 3 ? 315 : 595; }
        public int price() { return 100 + (id - 2) * 100; }
    }
    public static final class Actor {
        public float x, y;
        public final List<Point> route = new ArrayList<>();
        public int room = -1, color;
        public boolean leaving;
        Actor(float x, float y, int color) { this.x=x; this.y=y; this.color=color; }
        public void go(float tx, float ty, HotelGame game) {
            route.clear(); route.addAll(Navigation.path(game, x, y, tx, ty));
        }
        public void move(float dt, float speed) {
            float remaining = dt * speed;
            while (!route.isEmpty() && remaining > 0) {
                Point target = route.get(0);
                float dx=target.x-x, dy=target.y-y;
                float d=(float)Math.hypot(dx,dy);
                if (d <= remaining) { x=target.x; y=target.y; route.remove(0); remaining-=d; }
                else { x+=dx/d*remaining; y+=dy/d*remaining; remaining=0; }
            }
        }
    }
    public final Room[] rooms = new Room[6];
    public final Actor player = new Actor(245,450,0);
    public final Actor cleaner = new Actor(270,650,0);
    public final Actor receptionist = new Actor(105,400,0);
    public final List<Actor> guests = new ArrayList<>();
    public int coins=60, served, cleaned, speedLevel, roomLevel, breakfastLevel, claimedGoals, cargo, deliveries;
    public static final int[] GOAL_GUESTS={10,25,50,100};
    public static final int[] GOAL_REWARDS={50,100,180,300};
    public boolean hasReception, hasCleaner;
    public float receptionWork, spawnTimer=2, elapsed;
    public String message = "Willkommen! Geh zur Rezeption.";
    public float messageTime=7;
    private final Random random = new Random(1742);
    private int cleanerRoom=-1;

    public HotelGame() { for (int i=0;i<rooms.length;i++) rooms[i]=new Room(i); }
    public int openRooms() { int n=0; for (Room r:rooms) if(r.state!=State.LOCKED) n++; return n; }
    public int waitingGuests() { int n=0; for(Actor a:guests) if(a.room<0&&!a.leaving)n++;return n; }
    public int roomIncome() { return 25 + roomLevel*10 + breakfastLevel*8; }
    public int cost(Upgrade u) {
        switch(u) {
            case RECEPTION: return hasReception ? -1 : 150;
            case CLEANER: return hasCleaner ? -1 : 180;
            case SPEED: return speedLevel>=3 ? -1 : 80 + speedLevel*100;
            case ROOMS: return roomLevel>=3 ? -1 : 120 + roomLevel*160;
            case BREAKFAST: return breakfastLevel>=3 ? -1 : 100 + breakfastLevel*150;
            default: throw new IllegalArgumentException();
        }
    }
    public boolean buy(Upgrade upgrade) {
        int price=cost(upgrade);
        if(price<0||coins<price)return false;
        coins-=price;
        switch(upgrade) {
            case RECEPTION: hasReception=true; break;
            case CLEANER: hasCleaner=true; break;
            case SPEED: speedLevel++; break;
            case ROOMS: roomLevel++; break;
            case BREAKFAST: breakfastLevel++; break;
        }
        announce("Verbesserung freigeschaltet!"); return true;
    }
    public boolean near(float x,float y,float radius) { return Math.hypot(player.x-x,player.y-y)<radius; }
    public void announce(String text) { message=text;messageTime=4; }
    public void movePlayer(float dx,float dy,float dt) {
        if (dx==0&&dy==0)return;
        player.route.clear();
        float length=(float)Math.hypot(dx,dy);
        float distance=Math.min(dt,.05f)*(165+speedLevel*35)*Math.min(length,1);
        float nx=player.x+dx/length*distance, ny=player.y+dy/length*distance;
        // Axis separation allows sliding along walls instead of getting stuck.
        if(Navigation.walkable(this,nx,player.y))player.x=nx;
        if(Navigation.walkable(this,player.x,ny))player.y=ny;
    }
    public void tick(float seconds) {
        if(!Float.isFinite(seconds)||seconds<=0)return;
        float dt=Math.min(seconds,.05f); elapsed+=dt;messageTime=Math.max(0,messageTime-dt);
        player.move(dt,165+speedLevel*35);
        spawnTimer-=dt;
        if(spawnTimer<=0) {
            if(waitingGuests()<4)guests.add(new Actor(70,730,random.nextInt(5)));
            spawnTimer=7;
        }
        int queue=0;
        for(Actor a:guests) {
            if(a.room<0&&!a.leaving) {
                float qy=500+queue*50;
                if(a.route.isEmpty()&&Math.hypot(a.x-150,a.y-qy)>3)a.go(150,qy,this);
                queue++;
            }
            a.move(dt,110);
        }
        guests.removeIf(a->a.leaving&&a.route.isEmpty());
        Room ready=null;
        for(Room r:rooms)if(r.state==State.READY){ready=r;break;}
        Actor first=null;
        for(Actor a:guests)if(a.room<0&&!a.leaving){first=a;break;}
        boolean atDesk=hasReception||near(235,450,65);
        if(atDesk&&ready!=null&&first!=null&&Math.hypot(first.x-150,first.y-500)<12) {
            receptionWork+=dt;
            if(receptionWork>=1.3f) {
                first.room=ready.id;ready.state=State.ARRIVING;
                first.go(ready.x+185,ready.serviceY(),this);
                coins=Math.min(MAX_COINS,coins+roomIncome());served++;receptionWork=0;
                if(served==1)announce("Dein erster Gast! +"+roomIncome()+" Münzen");
            }
        } else receptionWork=0;
        for(Room r:rooms) {
            if(r.state==State.ARRIVING) {
                for(Actor a:guests)if(a.room==r.id&&!a.leaving&&a.route.isEmpty()) {
                    r.state=State.OCCUPIED;r.timer=16+random.nextFloat()*8;
                    if(hasCleaner&&hasReception) {
                        r.timer+=16;r.request=breakfastLevel>0&&random.nextBoolean()?2:1;
                    }
                }
            } else if(r.state==State.OCCUPIED) {
                r.timer-=dt;
                if(r.timer<=0) {
                    r.state=State.DIRTY;r.request=0;
                    for(Actor a:guests)if(a.room==r.id&&!a.leaving){a.leaving=true;a.go(70,730,this);}
                }
            }
            if(r.state==State.LOCKED) {
                if(near(r.centerX(),r.padY(),33)&&coins>=r.price()) {
                    r.work+=dt;
                    if(r.work>=1.5f){coins-=r.price();r.state=State.READY;r.work=0;announce("Ein neues Zimmer! Willkommen in Zimmer "+(r.id+1));}
                } else r.work=0;
            } else if(r.state==State.DIRTY) {
                boolean helper=hasCleaner&&cleanerRoom==r.id&&cleaner.route.isEmpty()&&Math.hypot(cleaner.x-(r.x+185),cleaner.y-r.serviceY())<3;
                if(near(r.x+185,r.serviceY(),48)||helper) {
                    r.work+=dt;
                    if(r.work>=2.5f){r.state=State.READY;r.work=0;cleaned++;if(cleaned==1)announce("Blitzblank! Das Zimmer ist wieder frei.");}
                } else r.work=0;
            }
        }
        // Optional manager work: helpers never collect or deliver guest wishes.
        if(hasCleaner&&hasReception) {
            if(cargo==0) {
                if(near(245,570,30))cargo=1;
                else if(breakfastLevel>0&&near(92,650,32))cargo=2;
            }
            for(Room r:rooms)if(r.state==State.OCCUPIED&&r.request!=0&&r.request==cargo&&near(r.x+185,r.serviceY(),42)) {
                int tip=r.request==1?18:22+breakfastLevel*4;
                cargo=0;r.request=0;deliveries++;coins=Math.min(MAX_COINS,coins+tip);
                announce("Danke! Zimmerservice geliefert · +"+tip+" Münzen");break;
            }
        }
        if(hasCleaner) {
            if(cleanerRoom<0||rooms[cleanerRoom].state!=State.DIRTY) {
                boolean finishedTask=cleanerRoom>=0;
                cleanerRoom=-1;
                for(Room r:rooms)if(r.state==State.DIRTY){cleanerRoom=r.id;cleaner.go(r.x+185,r.serviceY(),this);break;}
                // Keep the return route across frames; a new dirty room can interrupt it.
                if(cleanerRoom<0&&(finishedTask||cleaner.route.isEmpty())&&Math.hypot(cleaner.x-270,cleaner.y-650)>2)
                    cleaner.go(270,650,this);
            }
            cleaner.move(dt,140);
        }
        for(int i=0;i<GOAL_GUESTS.length;i++)if(served>=GOAL_GUESTS[i]&&(claimedGoals&(1<<i))==0) {
            claimedGoals|=1<<i;coins=Math.min(MAX_COINS,coins+GOAL_REWARDS[i]);
            announce("Hotelziel: "+GOAL_GUESTS[i]+" Gäste! +"+GOAL_REWARDS[i]+" Münzen");
        }
    }
    public String objective() {
        if(served==0)return "Geh zum Empfang · Gäste einchecken";
        if(cleaned==0)return "Nach dem Besuch: zum Besen im Zimmer laufen";
        if(openRooms()<3)return "Spare 100 Münzen · öffne Zimmer 3 am Baukreis";
        if(!hasCleaner)return "Unter „Ausbauen“ eine Reinigungskraft einstellen";
        if(!hasReception)return "Empfangshilfe einstellen · mehr Zeit zum Erkunden";
        if(cargo>0)return (cargo==1?"Handtücher":"Frühstück")+" dabei · zum passenden Gästewunsch im Zimmer laufen";
        for(Room r:rooms)if(r.request!=0)return r.request==1?"Zimmerservice: Handtücher bei der Wäscheecke holen":"Zimmerservice: Frühstück am Buffet abholen";
        if(openRooms()<6)return "Dein Hotel wächst · schalte alle 6 Zimmer frei";
        if(breakfastLevel==0)return "Eröffne dein Frühstücksbuffet unter „Ausbauen“";
        if(roomLevel<3)return "Verschönere die Zimmer · mehr Münzen pro Gast";
        return "Dein Traumhotel! Erfülle Gästewünsche für extra Münzen.";
    }
}
