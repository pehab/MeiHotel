package de.meiapps.meihotel;

import org.junit.Test;
import static org.junit.Assert.*;

public class HotelProgressTest {
    private void advance(HotelGame g,float seconds){for(int i=0;i<(int)(seconds*40);i++)g.tick(.025f);}
    private HotelGame resort(){HotelGame g=new HotelGame();g.coins=HotelGame.MAX_COINS;for(HotelGame.Room r:g.rooms)r.state=HotelGame.State.READY;return g;}
    @Test public void roomPurchasesAreIndividualBoundedAndDoNotUnlockRooms(){
        HotelGame g=new HotelGame();g.coins=50000;
        assertFalse(g.progress.buyRoom(2));assertEquals(50000,g.coins);
        for(int i=0;i<5;i++)assertTrue(g.progress.buyRoom(0));
        assertEquals(5,g.rooms[0].level);assertEquals(0,g.rooms[1].level);
        assertEquals(g.roomIncome()+30,g.roomIncome(g.rooms[0]));assertFalse(g.progress.buyRoom(0));
    }
    @Test public void allFourAmenitiesHaveFiveStagesAndRoomGates(){
        HotelGame g=new HotelGame();g.coins=HotelGame.MAX_COINS;
        assertFalse(g.progress.buyAmenity(0));assertEquals(HotelGame.MAX_COINS,g.coins);
        for(HotelGame.Room r:g.rooms)r.state=HotelGame.State.READY;
        for(int i=0;i<4;i++){
            int previous=0;
            for(int stage=0;stage<5;stage++){int cost=g.progress.amenityCost(i);assertTrue(cost>previous);previous=cost;assertTrue(g.progress.buyAmenity(i));}
            assertFalse(g.progress.buyAmenity(i));
        }
        assertEquals(20,g.progress.amenityLevels());assertEquals(85,g.roomIncome());
    }
    @Test public void staffTrainingRequiresStaffAndImprovesService(){
        HotelGame g=resort();assertFalse(g.buy(HotelGame.Upgrade.RECEPTION_TRAINING));assertFalse(g.buy(HotelGame.Upgrade.CLEANER_TRAINING));
        g.hasReception=true;g.hasCleaner=true;
        for(int i=0;i<4;i++){assertTrue(g.buy(HotelGame.Upgrade.RECEPTION_TRAINING));assertTrue(g.buy(HotelGame.Upgrade.CLEANER_TRAINING));}
        assertTrue(g.receptionDuration()<.8f);assertTrue(g.cleaningDuration()<1.5f);
        assertFalse(g.buy(HotelGame.Upgrade.RECEPTION_TRAINING));assertFalse(g.buy(HotelGame.Upgrade.CLEANER_TRAINING));
    }
    @Test public void serviceCartRetainsOtherItemsAfterDelivery(){
        HotelGame g=resort();g.hasCleaner=true;g.hasReception=true;g.progress.cartLevel=2;
        g.spawnTimer=1000;g.player.x=245;g.player.y=570;g.tick(.025f);assertEquals(3,g.cargoCount);
        g.rooms[0].state=HotelGame.State.OCCUPIED;g.rooms[0].timer=35;g.rooms[0].request=1;
        g.player.x=505;g.player.y=315;g.tick(.025f);assertEquals(1,g.cargo);assertEquals(2,g.cargoCount);
        advance(g,1);assertEquals(1,g.deliveries);assertEquals(2,g.cargoCount);
    }
    @Test public void courtyardPadsReachableAndHotelNavigationIndependent(){
        HotelGame g=resort();g.switchArea();assertEquals(1,g.area);
        for(HotelProgress.Amenity a:g.progress.amenities){assertFalse(Navigation.path(g,640,465,a.x,a.y,true).isEmpty());}
        assertFalse(Navigation.playerWalkable(g,900,650));assertFalse(Navigation.playerWalkable(g,300,250));
        assertFalse(Navigation.path(g,270,650,505,315).isEmpty()); // hotel cleaner route
        g.player.go(300,425,g);advance(g,5);assertEquals(300,g.player.x,.01);assertEquals(425,g.player.y,.01);
        g.switchArea();assertEquals(245,g.player.x,0);assertTrue(g.player.route.isEmpty());
    }
    @Test public void beingInCourtyardCannotServeOrCleanHotelRemotely(){
        HotelGame g=new HotelGame();g.area=1;g.player.x=505;g.player.y=315;g.spawnTimer=1000;
        g.rooms[0].state=HotelGame.State.DIRTY;advance(g,5);assertEquals(0,g.cleaned);
        g.hasCleaner=true;g.hasReception=true;g.cargo=1;g.cargoCount=1;
        g.rooms[0].state=HotelGame.State.OCCUPIED;g.rooms[0].timer=35;g.rooms[0].request=1;advance(g,1);
        assertEquals(0,g.deliveries);assertEquals(1,g.rooms[0].request);
    }
    @Test public void hotelHelpersContinueWhileManagerExploresCourtyard(){
        HotelGame g=resort();g.coins=0;g.hasCleaner=true;g.hasReception=true;g.switchArea();advance(g,200);
        assertTrue(g.served>10);assertTrue(g.cleaned>5);assertTrue(g.coins>0);
    }
    @Test public void courtyardJobsAreOptionalCappedAndNeedTwoSecondsOfActualWork(){
        HotelGame g=resort();g.coins=1000;assertTrue(g.progress.buyAmenity(0));int coins=g.coins;
        g.served=1;g.spawnTimer=1000;g.player.x=70;g.player.y=220;advance(g,200);
        assertEquals(3,g.progress.amenities[0].pending);assertEquals(coins,g.coins);assertEquals(0,g.progress.services);
        g.switchArea();g.player.x=300;g.player.y=425;advance(g,1);assertEquals(0,g.progress.services);
        g.player.x=640;advance(g,1);assertEquals(0,g.progress.amenities[0].work,0);
        g.player.x=300;advance(g,2.1f);assertEquals(1,g.progress.services);assertEquals(coins+47,g.coins);
    }
    @Test public void starRequiresMultipleKindsOfProgressAndChargesOnce(){
        HotelGame g=resort();assertFalse(g.progress.buyStar());
        g.deliveries=5;g.rooms[0].level=3;g.progress.amenities[0].level=1;
        int before=g.coins;assertTrue(g.progress.buyStar());assertEquals(2,g.progress.stars);assertEquals(before-800,g.coins);
        assertFalse(g.progress.buyStar());assertEquals(38,g.roomIncome());
    }
    @Test public void repeatableContractNeedsFreshProgressAfterClaim(){
        HotelGame g=new HotelGame();g.served=30;int before=g.coins;
        assertTrue(g.progress.claimContract(0));assertEquals(before+75,g.coins);assertEquals(1,g.progress.contractTier[0]);
        assertFalse(g.progress.claimContract(0));g.served+=18;assertTrue(g.progress.claimContract(0));assertEquals(before+225,g.coins);
    }
    @Test public void resetClearsAllExpansionProgress(){
        HotelGame g=resort();g.progress.buyAmenity(0);g.progress.stars=3;g.progress.services=7;g.progress.contractTier[0]=2;g.progress.cartLevel=2;
        g.progress.reset();assertEquals(1,g.progress.stars);assertEquals(0,g.progress.amenityLevels());assertEquals(0,g.progress.services);assertEquals(0,g.progress.contractTier[0]);assertEquals(0,g.progress.cartLevel);
    }
}
