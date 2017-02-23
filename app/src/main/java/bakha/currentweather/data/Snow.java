package bakha.currentweather.data;



/**
 * Data for snow volume from last 3 hours
 */

public class Snow implements Precipitation {
    private double volume;

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "Snow{" +
                "volume=" + volume +
                '}';
    }
}
