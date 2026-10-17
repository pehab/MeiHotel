package de.meiapps.meihotel;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class HotelGameTest {
    private void advance(HotelGame game,float seconds){for(int i=0;i<(int)(seconds*40);i++)game.tick(.025f);}
    @Test public void fullGuestCyclePaysOnceAndRoomCanBeCleaned() {
        HotelGame g=new HotelGame();g.player.x=235;g.player.y=450;
        advance(g,7);assertEquals(1,g.served);assertEquals(85,g.coins);
        g.player.go(505,315,g);advance(g,30);
        assertTrue(g.cleaned>=1);assertTrue(g.rooms[0].state==HotelGame.State.READY);
        assertEquals(85,g.coins);
    }
    @Test public void allSixDoorsAreReachableInBothDirections() {
        HotelGame g=new HotelGame();for(HotelGame.Room r:g.rooms)r.state=HotelGame.State.READY;
        for(HotelGame.Room r:g.rooms) {
            List<HotelGame.Point> path=Navigation.path(g,245,450,r.x+185,r.serviceY());
            assertFalse("room "+r.id,path.isEmpty());
            for(HotelGame.Point p:path)assertTrue(Navigation.walkable(g,p.x,p.y));
            assertFalse(Navigation.path(g,r.x+185,r.serviceY(),70,730).isEmpty());
        }
    }
    @Test public void lockedRoomsAndBedsCannotBeCrossed() {
        HotelGame g=new HotelGame();
        assertFalse(Navigation.walkable(g,1100,260));
        assertTrue(Navigation.path(g,245,450,1100,260).isEmpty());
        assertFalse(Navigation.walkable(g,370,270));
        g.player.x=305;g.player.y=450;
        for(int i=0;i<100;i++)g.movePlayer(0,-1,.05f);
        assertTrue("corridor wall",g.player.y>=402);
    }
    @Test public void buildingChargesExactlyOnceAndRequiresHoldingPosition() {
        HotelGame g=new HotelGame();g.coins=500;HotelGame.Room room=g.rooms[2];
        g.player.x=room.centerX();g.player.y=room.padY();advance(g,1);
        assertEquals(HotelGame.State.LOCKED,room.state);
        g.player.y=450;advance(g,1);assertEquals(0,room.work,.001);
        g.player.y=room.padY();advance(g,2);
        assertEquals(HotelGame.State.READY,room.state);assertEquals(400,g.coins);
        advance(g,5);assertEquals(400,g.coins);
    }
    @Test public void helpersKeepHotelRunningWithoutPlayer() {
        HotelGame g=new HotelGame();g.coins=500;
        assertTrue(g.buy(HotelGame.Upgrade.RECEPTION));assertTrue(g.buy(HotelGame.Upgrade.CLEANER));
        g.player.x=70;g.player.y=220;advance(g,200);
        assertTrue(g.served>5);assertTrue(g.cleaned>4);
        assertTrue(g.coins>170);
    }
    @Test public void upgradesCannotOverdrawOrExceedMaximum() {
        HotelGame g=new HotelGame();assertFalse(g.buy(HotelGame.Upgrade.CLEANER));assertEquals(60,g.coins);
        g.coins=10000;assertTrue(g.buy(HotelGame.Upgrade.CLEANER));assertFalse(g.buy(HotelGame.Upgrade.CLEANER));
        for(int i=0;i<3;i++)assertTrue(g.buy(HotelGame.Upgrade.SPEED));
        assertFalse(g.buy(HotelGame.Upgrade.SPEED));assertEquals(3,g.speedLevel);
    }
    @Test public void invalidAndLongFramesDoNotFastForward() {
        HotelGame g=new HotelGame();g.tick(Float.NaN);g.tick(Float.POSITIVE_INFINITY);g.tick(-1);
        assertEquals(0,g.elapsed,0);g.tick(3600);assertEquals(.05,g.elapsed,.0001);
    }
    @Test public void queueIsBoundedWhenPlayerDoesNothing() {
        HotelGame g=new HotelGame();g.player.x=70;g.player.y=200;advance(g,200);
        assertEquals(4,g.waitingGuests());assertEquals(0,g.served);
    }
}
