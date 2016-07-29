import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.Point;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EncounterResult;
import com.pokegoapi.api.map.pokemon.NearbyPokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.List;

/**
 * Created by Rickard on 2016-07-28.
 */
public class PokeHunter implements Runnable {
    @Override
    public void run() {

        while(true){
            informAboutNearbyPokemon();     //doesn't seem to find anything ever.
            findPokemonsAndCatchThem();     //doesn't seem to find anything ever.
            findSpawnPointsOnMap();         //Finds thousands of spawn points, WTF is a "spawnpoint"? D:
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

}
