package bakha.currentweather.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bakha.currentweather.R;
import bakha.currentweather.data.City;
import bakha.currentweather.data.Coordinate;
import bakha.currentweather.data.WeatherCondition;
import bakha.currentweather.utils.Config;
import bakha.currentweather.utils.Helper;
import bakha.currentweather.utils.RequestHelperSingleton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION_INFO = 100;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private LocationManager locationManager;
    private WeatherUpdateAdapter weatherAdapter;
    private List<City> dataSet;
    private Map<String, ?> rememberedSet;
    private InputMethodManager inputMethodManager;
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> autoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.custom_action_bar);
        actionBar.setDisplayShowCustomEnabled(true);

        sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        rememberedSet = sharedPreferences.getAll();

        final AutoCompleteTextView cityNameEdit = (AutoCompleteTextView) actionBar.getCustomView().findViewById(R.id.edit_enter_city_name);
        autoCompleteAdapter = new ArrayAdapter<>(this,
                R.layout.auto_complete_text_layout, new ArrayList<>(rememberedSet.keySet()));
        cityNameEdit.setAdapter(autoCompleteAdapter);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(cityNameEdit.getWindowToken(), 0);
        layoutManager = new LinearLayoutManager(this);
        dataSet = new ArrayList<>();

        weatherAdapter = new WeatherUpdateAdapter(getApplicationContext(), dataSet);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.weather_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(weatherAdapter);

        cityNameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    String cityName = cityNameEdit.getText().toString();
                    getWeatherInfo(cityName);
                    cityNameEdit.setText("");
                    inputMethodManager.hideSoftInputFromWindow(cityNameEdit.getWindowToken(), 0);
                }
                return false;
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION_INFO);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    protected void onStop () {
        super.onStop();
        RequestHelperSingleton.getInstance(this).cancelAll();
        try {
            locationManager.removeUpdates(locationListener);
        } catch (SecurityException ex) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION_INFO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //getCurrentLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return;
            }
        }
    }

    /**
     * Builds alert message to notify enable gps
     */
    private void buildAlertMessageEnableGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_gps)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Gets the current location from either last known location or from the location updates
     */
    private void getCurrentLocation() {
        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER) ) {
            buildAlertMessageEnableGps();
        }

        try {
            Criteria locationCriteria = new Criteria();
            locationCriteria.setAccuracy(Criteria.ACCURACY_LOW);

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(locationCriteria, true));
            Log.d(TAG, "location " + location);
            if (location != null) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                getWeatherInfo(latitude, longitude);
            } else {
                locationManager.requestLocationUpdates(locationManager.getBestProvider(locationCriteria, true), 0, 0, locationListener);
            }
        } catch (SecurityException ex) {

        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            getWeatherInfo(latitude, longitude);
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException ex) {}
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "location onStatusChanged" + s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "location onProviderEnabled" + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "location onProviderDisabled" + s);
        }
    };

    /**
     * Gets weather info by latitude and longitude
     * @param latitude
     * @param longitude
     */
    public void getWeatherInfo(double latitude, double longitude) {
        String url = Config.createWeatherUrl(latitude, longitude);
        fetchWeatherData(url, true);
    }

    /**
     * Gets weather info by city name
     * @param cityName
     */
    public void getWeatherInfo(String cityName) {
        String url = Config.createWeatherUrl(cityName);
        fetchWeatherData(url, false);
    }

    /**
     * Retrieves weather data from the given url
     * @param url
     * @param isCurrent Indicates whether the city is the current one
     */
    private void fetchWeatherData(String url, final boolean isCurrent) {
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        Coordinate coordinate = Helper.parseCoordinate(response);
                        WeatherCondition weatherCondition = Helper.parseWeatherCondition(response);
                        weatherCondition.setWind(Helper.parseWind(response));
                        weatherCondition.setRain(Helper.parseRain(response));
                        weatherCondition.setSnow(Helper.parseSnow(response));

                        City city = Helper.parseCity(response);
                        city.setCurrent(isCurrent);
                        city.setWeatherCondition(weatherCondition);
                        city.setCoordinate(coordinate);
                        Helper.parseMain(city, response);

                        // add only when the city is not in the list
                        boolean exists = false;
                        for (City c : dataSet) {
                            if (city.getName().equals(c.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            // add to shared preference
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(city.getName(), city.getName());
                            autoCompleteAdapter.add(city.getName());
                            editor.commit();

                            dataSet.add(city);
                            weatherAdapter.updateDataSet(dataSet);
                            recyclerView.scrollToPosition(dataSet.size() - 1);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.city_info_exists, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error : " + error.getLocalizedMessage());
                    }
                });

        RequestHelperSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

}
