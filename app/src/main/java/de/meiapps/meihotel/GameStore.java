package de.meiapps.meihotel;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** One atomic preference entry; transient queues and partial work are deliberately not persisted. */
final class GameStore {
    private final SharedPreferences preferences;
    boolean recovered;
    GameStore(Context context) { preferences=context.getSharedPreferences("hotel",Context.MODE_PRIVATE); }
    HotelGame load() {
        HotelGame game=new HotelGame();
        String raw=preferences.getString("save",null);
        if(raw==null)return game;
        try {
            JSONObject data=new JSONObject(raw);
            if(data.getInt("schema")!=1)throw new JSONException("Unknown schema");
            game.coins=bounded(data.getInt("coins"),0,HotelGame.MAX_COINS);
            game.served=bounded(data.getInt("served"),0,Integer.MAX_VALUE);
            game.cleaned=bounded(data.getInt("cleaned"),0,Integer.MAX_VALUE);
            game.speedLevel=bounded(data.getInt("speed"),0,3);
            game.roomLevel=bounded(data.getInt("quality"),0,3);
            game.cargo=bounded(data.optInt("cargo",0),0,2);
            game.deliveries=bounded(data.optInt("deliveries",0),0,Integer.MAX_VALUE);
            game.breakfastLevel=bounded(data.optInt("breakfast",0),0,3);
            game.claimedGoals=bounded(data.optInt("goals",0),0,255);
            game.cargoCount=game.cargo==0?0:bounded(data.optInt("cargoCount",1),1,4);
            game.area=bounded(data.optInt("area",0),0,1);
            game.hasCleaner=data.getBoolean("cleaner");game.hasReception=data.getBoolean("reception");
            JSONArray rooms=data.getJSONArray("rooms");
            if(rooms.length()!=6)throw new JSONException("Invalid rooms");
            for(int i=0;i<6;i++) {
                JSONObject saved=rooms.getJSONObject(i);
                HotelGame.Room room=game.rooms[i];room.state=HotelGame.State.valueOf(saved.getString("state"));
                room.level=bounded(saved.optInt("level",0),0,5);
                room.request=bounded(saved.optInt("request",0),0,2);
                if(room.state!=HotelGame.State.OCCUPIED)room.request=0;
                room.timer=(float)saved.getDouble("timer");
                if(!Float.isFinite(room.timer))throw new JSONException("Invalid timer");
                room.timer=Math.max(0,Math.min(40,room.timer));
                if(i<2&&room.state==HotelGame.State.LOCKED)throw new JSONException("Starter room locked");
                if(room.state==HotelGame.State.ARRIVING) {room.state=HotelGame.State.OCCUPIED;room.timer=20;}
                if(room.state==HotelGame.State.OCCUPIED) {
                    HotelGame.Actor guest=new HotelGame.Actor(room.x+185,room.serviceY(),i%5);
                    guest.room=i;game.guests.add(guest);
                }
            }
            JSONObject expansion=data.optJSONObject("expansion");
            if(expansion!=null) {
                HotelProgress p=game.progress;
                p.receptionTraining=bounded(expansion.optInt("receptionTraining",0),0,4);
                p.cleanerTraining=bounded(expansion.optInt("cleanerTraining",0),0,4);
                p.cartLevel=bounded(expansion.optInt("cart",0),0,3);
                p.tipLevel=bounded(expansion.optInt("tips",0),0,5);
                p.stars=bounded(expansion.optInt("stars",1),1,5);
                p.services=bounded(expansion.optInt("services",0),0,Integer.MAX_VALUE);
                JSONArray areas=expansion.getJSONArray("areas"),tiers=expansion.getJSONArray("tiers"),bases=expansion.getJSONArray("bases");
                if(areas.length()!=4||tiers.length()!=3||bases.length()!=3)throw new JSONException("Invalid expansion arrays");
                for(int i=0;i<4;i++) {
                    JSONObject item=areas.getJSONObject(i);HotelProgress.Amenity a=p.amenities[i];
                    a.level=bounded(item.getInt("level"),0,5);a.pending=bounded(item.getInt("pending"),0,3);
                    a.timer=(float)item.getDouble("timer");
                    if(!Float.isFinite(a.timer))throw new JSONException("Invalid amenity timer");
                    a.timer=Math.max(0,Math.min(35,a.timer));if(a.level==0)a.pending=0;
                }
                for(int i=0;i<3;i++){p.contractTier[i]=bounded(tiers.getInt(i),0,10000);p.contractBase[i]=bounded(bases.getInt(i),0,Integer.MAX_VALUE);}
            }
            float x=(float)data.optDouble("x",245),y=(float)data.optDouble("y",450);
            if(Navigation.playerWalkable(game,x,y)){game.player.x=x;game.player.y=y;}
            if(!Navigation.playerWalkable(game,game.player.x,game.player.y)){game.player.x=640;game.player.y=465;}
            game.announce("Willkommen zurück in deinem Hotel!");
            return game;
        } catch(JSONException|IllegalArgumentException exception) {
            recovered=true;return new HotelGame();
        }
    }
    boolean save(HotelGame game, boolean synchronous) {
        try {
            JSONObject data=new JSONObject();data.put("schema",1);data.put("coins",game.coins);
            data.put("served",game.served);data.put("cleaned",game.cleaned);
            data.put("area",game.area);data.put("cargoCount",game.cargo==0?0:Math.max(1,game.cargoCount));
            data.put("cargo",game.cargo);data.put("deliveries",game.deliveries);
            data.put("breakfast",game.breakfastLevel);data.put("goals",game.claimedGoals);
            data.put("speed",game.speedLevel);data.put("quality",game.roomLevel);
            data.put("cleaner",game.hasCleaner);data.put("reception",game.hasReception);
            data.put("x",game.player.x);data.put("y",game.player.y);
            JSONArray rooms=new JSONArray();
            for(HotelGame.Room r:game.rooms) {
                JSONObject entry=new JSONObject();entry.put("level",r.level);entry.put("state",r.state.name());entry.put("timer",r.timer);entry.put("request",r.request);rooms.put(entry);
            }
            data.put("rooms",rooms);
            HotelProgress p=game.progress;JSONObject expansion=new JSONObject();
            expansion.put("receptionTraining",p.receptionTraining);expansion.put("cleanerTraining",p.cleanerTraining);
            expansion.put("cart",p.cartLevel);expansion.put("tips",p.tipLevel);expansion.put("stars",p.stars);expansion.put("services",p.services);
            JSONArray areas=new JSONArray(),tiers=new JSONArray(),bases=new JSONArray();
            for(HotelProgress.Amenity a:p.amenities){JSONObject item=new JSONObject();item.put("level",a.level);item.put("pending",a.pending);item.put("timer",a.timer);areas.put(item);}
            for(int i=0;i<3;i++){tiers.put(p.contractTier[i]);bases.put(p.contractBase[i]);}
            expansion.put("areas",areas);expansion.put("tiers",tiers);expansion.put("bases",bases);data.put("expansion",expansion);
            SharedPreferences.Editor editor=preferences.edit().putString("save",data.toString());
            if(synchronous)return editor.commit();
            editor.apply();return true;
        } catch(JSONException exception) { return false; }
    }
    private static int bounded(int value,int min,int max) throws JSONException {
        if(value<min||value>max)throw new JSONException("Out of bounds");return value;
    }
}
