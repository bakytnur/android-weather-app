package bakha.currentweather.utils;

import android.util.Log;

import org.json.JSONObject;

import bakha.currentweather.data.City;
import bakha.currentweather.data.Coordinate;
import bakha.currentweather.data.Precipitation;
import bakha.currentweather.data.Rain;
import bakha.currentweather.data.Snow;
import bakha.currentweather.data.WeatherCondition;
import bakha.currentweather.data.Wind;

/**
 * Json parsing helper class
 */

public class Helper {
    private static String TAG = "Helper";
    public static Coordinate parseCoordinate(JSONObject response) {
        Coordinate coordinate = new Coordinate();
        try {
            JSONObject coordObject = response.getJSONObject("coord");
            coordinate.setLatitude(coordObject.getDouble("lat"));
            coordinate.setLongitude(coordObject.getDouble("lon"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception at coordinate:" + ex.getCause());
        }

        return coordinate;
    }

    public static WeatherCondition parseWeatherCondition(JSONObject response) {
        WeatherCondition weatherCondition = new WeatherCondition();
        try {
            JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
            weatherCondition.setId(weather.getInt("id"));
            weatherCondition.setMain(weather.getString("main"));
            weatherCondition.setDescription(weather.getString("description"));
            weatherCondition.setIcon(weather.getString("icon"));

            JSONObject mainObject = response.getJSONObject("main");
            weatherCondition.setTemperature(mainObject.getDouble("temp"));
            weatherCondition.setPressure(mainObject.getDouble("pressure"));
            weatherCondition.setHumidity(mainObject.getInt("humidity"));
            weatherCondition.setTemperatureMin(mainObject.getDouble("temp_min"));
            weatherCondition.setTemperatureMax(mainObject.getDouble("temp_max"));
            weatherCondition.setLastUpdate(response.getInt("dt"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception at weather:" + ex.getCause());
        }

        return weatherCondition;
    }

    public static Wind parseWind(JSONObject response) {
        Wind wind = new Wind();
        try {
            JSONObject windObject = response.getJSONObject("wind");
            wind.setDegree(windObject.getDouble("deg"));
            wind.setSpeed(windObject.getDouble("speed"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception at wind:" + ex.getCause());
        }

        return wind;
    }

    public static Precipitation parseRain(JSONObject response) {
        Rain rain = new Rain();
        try {
            JSONObject rainObject = response.getJSONObject("rain");
            rain.setVolume(rainObject.getDouble("3h"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception at rain:" + ex.getCause());
        }
        return rain;
    }

    public static Precipitation parseSnow(JSONObject response) {
        Snow snow = new Snow();
        try {
            JSONObject snowObject = response.getJSONObject("snow");
            snow.setVolume(snowObject.getDouble("3h"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception at snow:" + ex.getCause());
        }
        return snow;
    }

    public static City parseCity(JSONObject response) {
        City city = new City();
        try {
            JSONObject systemObject = response.getJSONObject("sys");
            city.setCountry(systemObject.getString("country"));
            city.setSunriseTime(systemObject.getInt("sunrise"));
            city.setSunsetTime(systemObject.getInt("sunset"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception at city:" + ex.getCause());
        }
        return city;
    }

    public static void parseMain(City city, JSONObject response) {
        try {
            city.setName(response.getString("name"));
            city.setId(response.getInt("id"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception at city:" + ex.getCause());
        }
    }
}
