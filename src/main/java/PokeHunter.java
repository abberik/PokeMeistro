import POGOProtos.Map.Pokemon.MapPokemonOuterClass;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.Point;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EncounterResult;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by Rickard on 2016-07-28.
 */
public class PokeHunter implements Runnable {
    @Override
    public void run() {

        while(true){
            //informAboutNearbyPokemon();     //doesn't seem to find anything ever.
            //findPokemonsAndCatchThem();     //doesn't seem to find anything ever.
            // findSpawnPointsOnMap();         //Finds 700  spawn points, WTF is a "spawnpoint"? D:
            // findPokeStopsInfo();         //finds pokestops succesfully
            lootNearestPokeStop();
            findPokemonFromMapObjects();
            //chill for a bit to avoid soft-ban
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    public void informAboutNearbyPokemon(){

        try{
            List<NearbyPokemon> pokemons = Start.getPokemonGo().getMap().getNearbyPokemon();

            Start.log("Number of nearby pokemons: " + pokemons.size());

            for(NearbyPokemon pokemon : pokemons){

                Start.log("Nearby pokemon with ID=" + pokemon.getPokemonId() + " that is " + pokemon.getDistanceInMeters() + " meters away");

            }
        }catch(LoginFailedException lfex){
            lfex.printStackTrace();
        }catch(RemoteServerException rsex){
            rsex.printStackTrace();
        }





    }

    public void findPokemonsAndCatchThem(){
    try{
        List<CatchablePokemon> pokemons = Start.getPokemonGo().getMap().getCatchablePokemon();
        Start.getLogger().log("Number of catchable pokemons: " + pokemons.size());

        for(CatchablePokemon pokemon : pokemons){


            // You need to Encounter first.
            EncounterResult encResult = pokemon.encounterPokemon();
            // if encounter was succesful, catch
            if (encResult.wasSuccessful()) {
                Start.getLogger().log("Encounted:" + pokemon.getPokemonId());
                CatchResult result = pokemon.catchPokemonWithRazzBerry();
                Start.getLogger().log("Attempt to catch:" + pokemon.getPokemonId() + " " + result.getStatus());
            }


            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    } catch (LoginFailedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (RemoteServerException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    }

    public void findSpawnPointsOnMap(){

        try{

            List<Point> spawnPunkter = Start.getPokemonGo().getMap().getSpawnPoints();
            Start.log("Found " + spawnPunkter.size() + " spawn points.");

            for(Point p : spawnPunkter){
                Start.log("Spawn point on longitude: " + p.getLongitude() + " latitude: " + p.getLatitude());
            }




        }catch(LoginFailedException lfex){
            lfex.printStackTrace();
        }catch (RemoteServerException rsex){
            rsex.printStackTrace();
        }


    }

    public void findPokeStopsInfo(){
        MapObjects mo = null;
        try {
            mo = Start.getPokemonGo().getMap().getMapObjects();
            Collection<Pokestop> pokestops = mo.getPokestops();
            Start.log("Found " + pokestops.size() + " pokestops");
            for(Pokestop pokestop : pokestops){
                Start.log("Found pokestop " + pokestop.getId() + " at  lat " + pokestop.getLatitude() + "  lon " + pokestop.getLongitude());

            }
        } catch (LoginFailedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            e.printStackTrace();
        }


    }

    public void lootNearestPokeStop(){

        try {

            for(Pokestop p : Start.getPokemonGo().getMap().getMapObjects().getPokestops()){


                double distance = Start.coordsToMeters((float)p.getLatitude(),(float)p.getLongitude(),(float)Start.getPokemonGo().getLatitude(),(float)Start.getPokemonGo().getLongitude());
                if(distance < 100){

                    Random random = new Random();

                    double latrand = -0.000006 + (0.000012 * random.nextDouble());
                    double lonrand = -0.000006 + (0.000012 * random.nextDouble());

                    Start.getPokemonGo().setLatitude(p.getLatitude() + latrand);
                    Start.getPokemonGo().setLongitude(p.getLongitude() + lonrand);

                    boolean success = false;
                    for(int j = 0; j < 10; j++){
                        if(p.canLoot()){
                            PokestopLootResult lootres = p.loot();
                            Start.log("Gained " + lootres.getExperience() + "xp , tostring:" + lootres.toString());

                            /*if(lootres.wasSuccessful()){
                                //spara stopdata
                                try {
                                    BufferedWriter bw = new BufferedWriter(new FileWriter("StopData " + p.getDetails().getName() + ".txt" ));
                                    bw.write("\nLongitude:" + p.getLongitude());
                                    bw.write("\nLatitude:" + p.getLatitude());
                                    bw.write("\nID:" + p.getId());
                                    bw.write("\nName:" + p.getDetails().getName());
                                    bw.write("\nDescription:" + p.getDetails().getDescription());
                                    bw.write("\nIMGURL:" + p.getDetails().getImageUrl());
                                    bw.write("\nFort sponsor name:" + p.getFortData().getSponsor().name());
                                    bw.write("\nfort sponsor number:" + p.getFortData().getSponsor().getNumber());
                                    bw.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }*/

                            success = true;

                            break;
                        }
                    }
                    if(success)break;
                }
            }




        } catch (LoginFailedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            e.printStackTrace();
        }


    }

    public void findPokemonFromMapObjects(){

        try {
            Collection<MapPokemonOuterClass.MapPokemon> catchablePokemons = Start.getPokemonGo().getMap().getMapObjects().getCatchablePokemons();
            Start.log("Found " + catchablePokemons.size() + " pokemons.");
            for(MapPokemonOuterClass.MapPokemon poke : catchablePokemons){
                Start.log("Found a " + poke.getPokemonId());
            }

        } catch (LoginFailedException e) {
            e.printStackTrace();
        } catch (RemoteServerException e) {
            e.printStackTrace();
        }


    }

}
