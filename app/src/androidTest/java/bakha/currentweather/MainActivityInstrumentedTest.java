package bakha.currentweather;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.ActionBar;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import bakha.currentweather.data.City;
import bakha.currentweather.ui.MainActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {
    private static final double LATITUDE = 35;
    private static final double LONGITUDE = 139;
    private static final String NAME = "Shuzenji";
    private static final String COUNTRY = "JP";
    @Rule
    public ActivityTestRule<MainActivity> mRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("bakha.currentweather", appContext.getPackageName());
    }

    @Test
    @UiThreadTest
    public void testGetWeatherLocation() throws Exception {
        final City city = new City();
        assertNotNull(city);
        city.setName(NAME);

        TextView cityText = (TextView) mRule.getActivity().getSupportActionBar().getCustomView().findViewById(R.id.edit_enter_city_name);
        assertNotNull(cityText);
        assertEquals(cityText.getText().toString(), "");
        mRule.getActivity().getWeatherInfo(LATITUDE, LONGITUDE);
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                String fullTitle = mRule.getActivity().getString(R.string.weather_in_city_title) + " " + NAME + ", " + COUNTRY;
                assertEquals(city.getName(), fullTitle);
                assertNotNull(city.getWeatherCondition());
                assertNotNull(city.getCoordinate());
                assertEquals(LATITUDE, city.getCoordinate().getLatitude(), 0.0);
                assertEquals(LONGITUDE, city.getCoordinate().getLongitude(), 0.0);
                assertNotNull(city.getSunriseTime());
                assertNotNull(city.getSunsetTime());
            }
        }.start();
    }

    @Test
    @UiThreadTest
    public void testAutoComplete() throws Exception {
        ActionBar actionBar = mRule.getActivity().getSupportActionBar();
        assertNotNull(actionBar);
        final AutoCompleteTextView cityNameEdit = (AutoCompleteTextView) actionBar.getCustomView().findViewById(R.id.edit_enter_city_name);
        assertNotNull(cityNameEdit);
        cityNameEdit.setText(NAME);
        mRule.getActivity().getWeatherInfo(NAME);

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) cityNameEdit.getAdapter();
        assertNotNull(adapter);
        int position = 0;
        boolean isCityFoundAtDropDown = false;
        while (position < adapter.getCount()) {
            String item = adapter.getItem(position);
            assertNotNull(item);
            if (NAME.equals(item)) {
                isCityFoundAtDropDown = true;
            }
            position++;
        }

        assertEquals(isCityFoundAtDropDown, true);
    }
}
