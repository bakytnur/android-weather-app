package bakha.currentweather.utils;

import android.net.Uri;

/**
 * App configuration
 */

public class Config {
    public static String API_URL = "http://api.openweathermap.org/data/2.5/weather";
    public static String WEATHER_ICON_PATH = "http://openweathermap.org/img/w/";
    public static String WEATHER_MAP_PATH  = "http://openweathermap.org/weathermap?basemap=map&cities=true&layer=temperature";
    public static String API_KEY = "84e5ec838803a0ac88dc52cc7a4c849a";
    public static String QUERY_FOR_CITY = "q";
    public static String APP_ID_KEY = "appid";

    public static String createWeatherUrl(String cityName) {
        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(API_URL)
                .appendQueryParameter(QUERY_FOR_CITY, cityName)
                .appendQueryParameter(APP_ID_KEY, API_KEY);

        return builder.build().toString();
    }

    public static String createWeatherMapUrl(double latitude, double longitude) {
        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(WEATHER_MAP_PATH)
                .appendQueryParameter("lat", String.valueOf(latitude))
                .appendQueryParameter("lon", String.valueOf(longitude))
                .appendQueryParameter("zoom", "8");

        return builder.build().toString();
    }

    public static String createWeatherUrl(double latitude, double longitude) {
        Uri.Builder builder = new Uri.Builder();
        builder.encodedPath(API_URL)
                .appendQueryParameter("lat", String.valueOf(latitude))
                .appendQueryParameter("lon", String.valueOf(longitude))
                .appendQueryParameter(APP_ID_KEY, API_KEY);

        return builder.build().toString();
    }
}
