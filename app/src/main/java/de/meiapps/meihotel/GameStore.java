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
            game.claimedGoals=bounded(data.optInt("goals",0),0,15);
            game.hasCleaner=data.getBoolean("cleaner");game.hasReception=data.getBoolean("reception");
            JSONArray rooms=data.getJSONArray("rooms");
            if(rooms.length()!=6)throw new JSONException("Invalid rooms");
            for(int i=0;i<6;i++) {
                JSONObject saved=rooms.getJSONObject(i);
                HotelGame.Room room=game.rooms[i];room.state=HotelGame.State.valueOf(saved.getString("state"));
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
            float x=(float)data.optDouble("x",245),y=(float)data.optDouble("y",450);
            if(Navigation.walkable(game,x,y)){game.player.x=x;game.player.y=y;}
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
            data.put("cargo",game.cargo);data.put("deliveries",game.deliveries);
            data.put("breakfast",game.breakfastLevel);data.put("goals",game.claimedGoals);
            data.put("speed",game.speedLevel);data.put("quality",game.roomLevel);
            data.put("cleaner",game.hasCleaner);data.put("reception",game.hasReception);
            data.put("x",game.player.x);data.put("y",game.player.y);
            JSONArray rooms=new JSONArray();
            for(HotelGame.Room r:game.rooms) {
                JSONObject entry=new JSONObject();entry.put("state",r.state.name());entry.put("timer",r.timer);entry.put("request",r.request);rooms.put(entry);
            }
            data.put("rooms",rooms);
            SharedPreferences.Editor editor=preferences.edit().putString("save",data.toString());
            if(synchronous)return editor.commit();
            editor.apply();return true;
        } catch(JSONException exception) { return false; }
    }
    private static int bounded(int value,int min,int max) throws JSONException {
        if(value<min||value>max)throw new JSONException("Out of bounds");return value;
    }
}
