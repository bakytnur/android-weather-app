package bakha.currentweather.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import bakha.currentweather.R;
import bakha.currentweather.data.City;
import bakha.currentweather.ui.view.WeatherViewHolder;

/**
 * RecyclerView.Adapter for populating weather data
 */
public class WeatherUpdateAdapter extends RecyclerView.Adapter<WeatherViewHolder> implements DataSetChangeObserver {
    private List<City> dataSet;
    private Context context;

    public void updateDataSet(List<City> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public WeatherUpdateAdapter(Context context, List<City> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
    }

    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.city_weather_view, parent, false);
        WeatherViewHolder viewHolder = new WeatherViewHolder(context, view, this);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(WeatherViewHolder holder, int position) {
        // - get element from your dataSet at this position
        // - replace the contents of the view with that element
        holder.bindView(dataSet.get(position));
    }

    // Return the size of your dataSet (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onRemoveCity(City city) {
        dataSet.remove(city);
        notifyDataSetChanged();
    }
}
