package bakha.currentweather.ui;

import bakha.currentweather.data.City;

/**
 * Observer for city list changes
 */

public interface DataSetChangeObserver {
    void onRemoveCity(City city);
}
