import java.util.Random;

/**
 * Created by Rickard on 2016-07-28.
 */
public class Walker implements Runnable {

    private Random random = new Random();
    private double latOffset;
    private double lonOffset;

    @Override
    public void run() {
        while(true){

            //generera acceleration i latitud och longitud.
            double latacc = -0.000006 + (0.000012 * random.nextDouble());
            double lonacc = -0.000006 + (0.000012 * random.nextDouble());

            latOffset += latacc;
            lonOffset += lonacc;

            Start.getPokemonGo().setLocation(Start.getCoords()[0] + latOffset, Start.getCoords()[1] + lonOffset, Start.getCoords()[2]);

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
