import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EncounterResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.List;

/**
 * Created by Rickard on 2016-07-28.
 */
public class PokeHunter implements Runnable {
    @Override
    public void run() {
        try {

            List<CatchablePokemon> pokemons = Start.getPokemonGo().getMap().getCatchablePokemon();
            Start.getLogger().log("" + pokemons.size());

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
                    Thread.sleep(500);
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

        try {
            Thread.sleep(320);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
