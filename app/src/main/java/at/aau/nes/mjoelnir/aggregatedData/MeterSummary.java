package at.aau.nes.mjoelnir.aggregatedData;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import at.aau.nes.mjoelnir.MainActivity;
import at.aau.nes.mjoelnir.R;
import at.aau.nes.mjoelnir.utilities.CallableView;
import at.aau.nes.mjoelnir.utilities.Model;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeterSummary extends Fragment implements CallableView{

    SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Bind(R.id.layout_consumption_reading) LinearLayout layoutConsumptionReading;
    @Bind(R.id.consumption_meter_reading) TextView consumptionMeterReading;
    @Bind(R.id.consumption_meter_date) TextView consumptionMeterDate;

    @Bind(R.id.layout_production_reading) LinearLayout layoutProductionReading;
    @Bind(R.id.production_meter_reading) TextView productionMeterReading;
    @Bind(R.id.production_meter_date) TextView productionMeterDate;

    @Bind(R.id.aggregated_separator) View separator;

    @Bind(R.id.energy_bar_chart) BarChart chart;
    @Bind(R.id.meter_newreading_button) FloatingActionButton fab;

    @Bind(R.id.aggregate_progress) ProgressBar progress;

    private ArrayList<IBarDataSet> dataSets;

    private HashMap<String, Model.PerformRESTCallTask> tasks;

    public MeterSummary() {
        // Required empty public constructor
    }

    public static MeterSummary newInstance(){
        return new MeterSummary();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasks = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_meter_summary, container, false);

        ButterKnife.bind(this, v);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReading();
            }
        });

        chart.setDescription("");
        chart.setNoDataText(getString(R.string.error_unavailable_consumption_data));
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        chart.getAxisLeft().setDrawLabels(true);    // Y AXIS LEFT
        chart.getAxisRight().setDrawLabels(false);  // Y AXIS RIGHT
        //chart.getXAxis().setDrawLabels(false);      // TOP X axis
        chart.getXAxis().setLabelsToSkip(0);

        chart.setDrawValueAboveBar(true);

        chart.animateX(1000);
        chart.animateY(1000);

        chart.getLegend().setEnabled(true);

        chart.setVisibleXRangeMaximum(12);
        chart.setVisibleYRangeMaximum(30, YAxis.AxisDependency.LEFT);

        chart.setData(null);
        chart.invalidate();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chart.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        // Retrieve last meter readings
        Model.PerformRESTCallTask gmr = Model.asynchRequest(this,
                getString(R.string.rest_url) + "?" +
                        "authentification_key=" + MainActivity.authentificationKey +
                        "&operation=getaggmeter"
                , "getMeterReadings");
        tasks.put("getMeterReadings", gmr);

        // Retrieve history of consumption and production over the last days
        Model.PerformRESTCallTask ged = Model.asynchRequest(this,
                getString(R.string.rest_url) + "?" +
                        "authentification_key=" + MainActivity.authentificationKey +
                        "&operation=getaggdata"
                , "getEnergyData");
        tasks.put("getEnergyData", ged);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for(Model.PerformRESTCallTask t : tasks.values()){
            t.cancel(true);
        }
    }

    private void addReading(){
        AddReadingDialog ard = AddReadingDialog.newInstance();
        ard.setTargetFragment(this, ard.METERREADING);
        ard.show(getFragmentManager(), "meterReadingDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == AddReadingDialog.METERREADING){
            String value = data.getStringExtra(AddReadingDialog.OCRMETERREADER_READING);
            //t_reading.setText(value);
            String type = data.getStringExtra(AddReadingDialog.OCRMETERREADER_TYPE).equals("Production")
                    ? "p" : "c";

            Model.PerformRESTCallTask sr = Model.asynchRequest(this,
                    // operation, authkey, production, consumption and timestamp
                    getString(R.string.rest_url) + "?" +
                            "authentification_key=" + MainActivity.authentificationKey +
                            "&operation=addaggmeter" +
                            "&input=" + type +
                            "&value=" + value +
                            "&timestamp=" + (Calendar.getInstance().getTimeInMillis() / 1000L)
                    , "saveReading");
            tasks.put("saveReading", sr);
        }
    }

    @Override
    public void callback(String eventName, JSONObject payload) {
        //System.out.println(payload);

        if (eventName.equals("saveReading")) {
            tasks.remove("saveReading");
            Toast.makeText(getContext(), getString(R.string.successfull_reading_upload), Toast.LENGTH_SHORT).show();

            // update meter summary list
            Model.PerformRESTCallTask ged = Model.asynchRequest(this,
                    getString(R.string.rest_url) + "?" +
                            "authentification_key=" + MainActivity.authentificationKey +
                            "&operation=getaggdata"

                    , "getEnergyData");
            tasks.put("getEnergyData", ged);

        }else if(eventName.equals("getMeterReadings")){
            tasks.remove("getMeterReadings");
            // {"readingC":"10647","timestampC":"1460217711","readingP":"123457","timestampP":"1458601200"}
            try {

                consumptionMeterReading.setText(payload.getString("readingC"));
                Calendar c = Calendar.getInstance();

                c.setTimeInMillis(payload.getLong("timestampC")*1000L);         // received time in secs
                consumptionMeterDate.setText(dt.format(c.getTime()));
                layoutConsumptionReading.setVisibility(View.VISIBLE);

                productionMeterReading.setText(payload.getString("readingP"));
                c.setTimeInMillis(payload.getLong("timestampP") * 1000L);         // received time in secs
                productionMeterDate.setText(dt.format(c.getTime()));
                layoutProductionReading.setVisibility(View.VISIBLE);

                separator.setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }else if(eventName.equals("getEnergyData")){
            tasks.remove("getEnergyData");
            try {
                JSONArray readings = payload.getJSONArray("payload");
                // [["7","42","1458601200"],["0","0","1458514800"],["0","0","1458428400"],["0","0","1458342000"],["17","22","1458255600"]]

                // create a dataset to contain the entries
                dataSets = new ArrayList<>();

                ArrayList<BarEntry> consumptionEntries = new ArrayList<>();
                ArrayList<BarEntry> productionEntries = new ArrayList<>();
                /*
                for(int i=0; i<readings.length(); i++){ // i is the number of days, with the last element being today (so i=0 means i-readings days ago)
                    JSONObject r = readings.getJSONObject(i);
                    consumptionEntries.add(new BarEntry( (float) r.getDouble("consumption") , i));
                    productionEntries.add(new BarEntry( (float) r.getDouble("production") , i));
                }*/

                for(int i=0; i<readings.length(); i++){
                    JSONArray entry = readings.getJSONArray(i);
                    consumptionEntries.add(new BarEntry( (float) entry.getDouble(0) , (readings.length()-1)-i));
                    productionEntries.add(new BarEntry((float) entry.getDouble(1), (readings.length()-1)-i));
                }

                BarDataSet consumptionSet = new BarDataSet(consumptionEntries, getString(R.string.dataset_consumption));
                consumptionSet.setColor(Color.RED);
                consumptionSet.setValueTextSize(getResources().getDimension(R.dimen.text_label_size_chart));
                dataSets.add( consumptionSet );

                BarDataSet productionSet = new BarDataSet(productionEntries, getString(R.string.dataset_production));
                productionSet.setColor(Color.GREEN);
                productionSet.setValueTextSize(getResources().getDimension(R.dimen.text_label_size_chart));
                dataSets.add( productionSet );

                // update the chart according to the passed data
                BarData data = new BarData(getXAxisLabels( consumptionEntries.size() ), dataSets);
                chart.setData(data);
                chart.invalidate();

            } catch (JSONException e) {
                chart.clear();
                chart.invalidate();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            progress.setVisibility(View.GONE);
            chart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<String> getXAxisLabels(int noentries){
        ArrayList<String> xAxis = new ArrayList<>();

        for(int d=0; d<noentries; d++){
            int daysAgo = (noentries-1) -d;
            if(daysAgo > 1)         xAxis.add("-"+daysAgo);
            else if(daysAgo > 0)    xAxis.add(getString(R.string.yesterday));
            else                    xAxis.add(getString(R.string.today));
        }
        return xAxis;
    }

}
