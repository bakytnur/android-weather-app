package bakha.currentweather.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

import bakha.currentweather.R;
import bakha.currentweather.data.City;
import bakha.currentweather.ui.DataSetChangeObserver;
import bakha.currentweather.utils.Config;
import bakha.currentweather.utils.RequestHelperSingleton;

public class WeatherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static String TAG = "WeatherViewHolder";
    private static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private static final String BLANK_SPACE = " ";

    private final Context context;
    private DataSetChangeObserver observer;
    private City city;
    private LinearLayout linearLayout;
    private TextView weatherInCityText;
    private ImageView weatherIcon;
    private TextView temperatureText;
    private TextView weatherConditionText;
    private TextView windText;
    private TextView cloudinessText;
    private TextView pressureText;
    private TextView humidityText;
    private TextView rainText;
    private TextView snowText;
    private TextView sunriseText;
    private TextView sunsetText;
    private TextView coordinatesText;
    private TextView lastUpdatedText;
    private ImageView removeView;

    public WeatherViewHolder(Context context, LinearLayout linearLayout, DataSetChangeObserver observer) {
        super(linearLayout);
        this.linearLayout = linearLayout;
        this.context = context;
        this.observer = observer;
        init();
    }

    /**
     * Initializes the views
     */
    private void init() {
        Log.d(TAG, "init");
        weatherInCityText = (TextView) linearLayout.findViewById(R.id.weather_in_city_text);
        weatherIcon = (ImageView) linearLayout.findViewById(R.id.weather_icon);
        temperatureText = (TextView) linearLayout.findViewById(R.id.temperature_text);
        weatherConditionText = (TextView) linearLayout.findViewById(R.id.weather_condition_text);
        windText = (TextView) linearLayout.findViewById(R.id.wind_text);
        cloudinessText = (TextView) linearLayout.findViewById(R.id.cloudiness_text);
        pressureText = (TextView) linearLayout.findViewById(R.id.pressure_text);
        humidityText = (TextView) linearLayout.findViewById(R.id.humidity_text);
        rainText = (TextView) linearLayout.findViewById(R.id.rain_text);
        snowText = (TextView) linearLayout.findViewById(R.id.snow_text);
        sunriseText = (TextView) linearLayout.findViewById(R.id.sunrise_text);
        sunsetText = (TextView) linearLayout.findViewById(R.id.sunset_text);
        coordinatesText = (TextView) linearLayout.findViewById(R.id.geo_coordinates_text);
        lastUpdatedText = (TextView) linearLayout.findViewById(R.id.last_updated_text);
        removeView = (ImageView) linearLayout.findViewById(R.id.remove_view);
        removeView.setOnClickListener(this);
    }

    /**
     * Binds the data with the view
     * @param city
     */
    public void bindView(City city) {
        this.city = city;
        weatherInCityText.setText(context.getString(R.string.weather_in_city_title) + BLANK_SPACE + city.getName());
        if (city.getCountry() != null && !city.getCountry().isEmpty()) {
            weatherInCityText.setText(weatherInCityText.getText() + ", " + city.getCountry());
        }
        String imagePath = Config.WEATHER_ICON_PATH + city.getWeatherCondition().getIcon() + ".png";
        RequestHelperSingleton.getInstance(context).getImageLoader().get(imagePath, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                weatherIcon.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error occurred while retrieving the icon");
            }
        });

        String temperatureInF = toFahrenheit(city.getWeatherCondition().getTemperature());
        String temperatureInC = toCelsius(city.getWeatherCondition().getTemperature());
        temperatureText.setText(temperatureInF + " °F / " +  temperatureInC + " °C");
        weatherConditionText.setText(capitalizeFirstLetter(city.getWeatherCondition().getDescription()));
        windText.setText(city.getWeatherCondition().getWind().getSpeed() + " m/s");
        cloudinessText.setText(capitalizeFirstLetter(city.getWeatherCondition().getMain()));
        pressureText.setText(city.getWeatherCondition().getPressure() + " hpa");
        humidityText.setText(city.getWeatherCondition().getHumidity() + " %");

        if (city.getWeatherCondition().getRain().getVolume() > 0) {
            rainText.setText(String.valueOf(city.getWeatherCondition().getRain().getVolume()));
            rainText.setVisibility(View.VISIBLE);
        }

        if (city.getWeatherCondition().getSnow().getVolume() > 0){
            snowText.setText(String.valueOf(city.getWeatherCondition().getSnow().getVolume()));
            snowText.setVisibility(View.VISIBLE);
        }
        sunriseText.setText(longToDate(city.getSunriseTime()));
        sunsetText.setText(longToDate(city.getSunsetTime()));

        String weatherMapPath = Config.createWeatherMapUrl(city.getCoordinate().getLatitude(), city.getCoordinate().getLongitude());
        String pathText = "[" + city.getCoordinate().getLatitude() + ", " + city.getCoordinate().getLongitude() + "]";
        coordinatesText.setText(Html.fromHtml("<a href=\"" + weatherMapPath + "\">" + pathText + "</a>"));
        coordinatesText.setMovementMethod(LinkMovementMethod.getInstance());
        lastUpdatedText.setText(longToDate(city.getWeatherCondition().getLastUpdate()));

        // remove button visible for only non-current cities
        removeView.setVisibility(city.isCurrent() ? View.GONE : View.VISIBLE);
    }

    /**
     * Converts long value to date string
     * @param epoch
     * @return
     */
    private String longToDate(long epoch) {
        String date = SIMPLE_DATE_FORMAT.format(new Date(epoch * 1000));
        return date;
    }

    /**
     * Capitalizes the first letter
     */
    private String capitalizeFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Converts kelvin to celsius
     * @param kelvin
     * @return
     */
    private String toCelsius(double kelvin) {
        double degree = kelvin - 273.15F;
        return String.format("%.1f", degree);
    }

    /**
     * Converts kelvin to fahrenheit
     * @param kelvin
     * @return
     */
    private String toFahrenheit(double kelvin) {
        double degree = ((kelvin - 273) * 9/5) + 32;
        return String.format("%.1f", degree);
    }

    @Override
    public void onClick(View view) {
        observer.onRemoveCity(city);
    }
}