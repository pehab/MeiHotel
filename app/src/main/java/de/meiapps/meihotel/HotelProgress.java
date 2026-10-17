package de.meiapps.meihotel;

/** Long-term purchases and optional jobs. No deadlines, penalties or real-money economy. */
public final class HotelProgress {
    public static final String[] AREA_NAMES={"Café","Spielecke","Blumengarten","Pool"};
    public static final String[] AREA_JOBS={"Kaffee servieren","Spielsachen ordnen","Blumen gießen","Pool pflegen"};
    public static final int[] AREA_PRICES={350,600,900,1400};
    public static final class Amenity {
        public int level, pending;
        public float timer=25, work;
        public final float x,y;
        Amenity(int i){x=i%2==0?300:980;y=i<2?425:500;}
    }
    public final Amenity[] amenities=new Amenity[4];
    public final int[] contractTier=new int[3], contractBase=new int[3];
    public int receptionTraining, cleanerTraining, cartLevel, tipLevel, stars=1, services;
    private final HotelGame game;
    HotelProgress(HotelGame game){this.game=game;for(int i=0;i<4;i++)amenities[i]=new Amenity(i);}
    public int amenityLevels(){int n=0;for(Amenity a:amenities)n+=a.level;return n;}
    public int roomLevels(){int n=0;for(HotelGame.Room r:game.rooms)n+=r.level;return n;}
    public int incomeBonus(){return amenityLevels()*3+(stars-1)*10+tipLevel*2;}
    public int amenityCost(int i){int level=amenities[i].level;return level>=5?-1:AREA_PRICES[i]*(1+level*level);}
    public String amenityRequirement(int i){return game.openRooms()<i+3?(i+3)+" offene Zimmer nötig":"";}
    public boolean buyAmenity(int i){
        int price=amenityCost(i);
        if(price<0||game.coins<price||!amenityRequirement(i).isEmpty())return false;
        game.coins-=price;Amenity a=amenities[i];a.level++;if(a.level==1){a.pending=1;a.timer=25;}
        game.announce(AREA_NAMES[i]+" · Stufe "+a.level+"! Im Hof wartet dein Team.");return true;
    }
    public int roomCost(int i){HotelGame.Room r=game.rooms[i];return r.state==HotelGame.State.LOCKED||r.level>=5?-1:180*(r.level+1)*(r.level+1)+i*30;}
    public boolean buyRoom(int i){int cost=roomCost(i);if(cost<0||game.coins<cost)return false;game.coins-=cost;game.rooms[i].level++;game.announce("Zimmer "+(i+1)+" verschönert! +6 Münzen pro Besuch");return true;}
    public int starCost(){return stars>=5?-1:800*stars*stars;}
    public String starRequirement(){
        if(stars>=5)return "Dein Fünf-Sterne-Hotel!";
        int[] rooms={0,3,4,5,6},quality={0,3,8,15,24},areas={0,1,4,8,14},orders={0,5,15,35,70};
        if(game.openRooms()<rooms[stars])return rooms[stars]+" Zimmer öffnen";
        if(roomLevels()<quality[stars])return quality[stars]+" einzelne Zimmerausbauten ("+roomLevels()+")";
        if(amenityLevels()<areas[stars])return areas[stars]+" Hof-Ausbaustufen ("+amenityLevels()+")";
        if(game.deliveries<orders[stars])return orders[stars]+" Zimmerservice-Lieferungen ("+game.deliveries+")";
        return "Bereit für den nächsten Hotelstern!";
    }
    public boolean starReady(){return stars<5&&starRequirement().startsWith("Bereit");}
    public boolean buyStar(){int cost=starCost();if(cost<0||!starReady()||game.coins<cost)return false;game.coins-=cost;stars++;game.announce(stars+" Hotelsterne! +10 Münzen pro Gast");return true;}
    public int contractCounter(int i){return i==0?game.served:i==1?game.deliveries:services;}
    public int contractGoal(int i){return (i==0?12:i==1?5:4)+Math.min(10000,contractTier[i])*(i==0?6:i==1?3:2);}
    public int contractReward(int i){return 75+i*25+Math.min(10000,contractTier[i])*75;}
    public int contractProgress(int i){return Math.max(0,contractCounter(i)-contractBase[i]);}
    public boolean claimContract(int i){
        if(contractProgress(i)<contractGoal(i))return false;
        game.coins=Math.min(HotelGame.MAX_COINS,game.coins+contractReward(i));
        contractBase[i]=contractCounter(i);contractTier[i]=Math.min(10000,contractTier[i]+1);
        game.announce("Auftrag geschafft! Die nächste Aufgabe wartet schon.");return true;
    }
    public void tick(float dt){
        for(Amenity a:amenities)if(a.level>0){
            if(game.served>0){a.timer-=dt;if(a.timer<=0){a.pending=Math.min(3,a.pending+1);a.timer=35;}}
            if(a.pending>0&&game.area==1&&Math.hypot(game.player.x-a.x,game.player.y-a.y)<38){
                a.work+=dt;
                if(a.work>=2){a.work=0;a.pending--;services++;int tip=35+a.level*12+tipLevel*3;game.coins=Math.min(HotelGame.MAX_COINS,game.coins+tip);game.announce("Hofservice erledigt! +"+tip+" Münzen");}
            }else a.work=0;
        }
    }
    public void reset(){
        receptionTraining=0;cleanerTraining=0;cartLevel=0;tipLevel=0;stars=1;services=0;
        for(int i=0;i<3;i++){contractTier[i]=0;contractBase[i]=0;}
        for(Amenity a:amenities){a.level=0;a.pending=0;a.timer=25;a.work=0;}
    }
}
