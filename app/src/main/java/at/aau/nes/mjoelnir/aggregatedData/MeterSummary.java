package at.aau.nes.mjoelnir.aggregatedData;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.Calendar;

import at.aau.nes.mjoelnir.MainActivity;
import at.aau.nes.mjoelnir.R;
import at.aau.nes.mjoelnir.utilities.CallableView;
import at.aau.nes.mjoelnir.utilities.Model;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeterSummary extends Fragment implements CallableView{

    private BarChart chart;
    private ArrayList<IBarDataSet> dataSets;

    public MeterSummary() {
        // Required empty public constructor
    }

    public static MeterSummary newInstance(){
        return new MeterSummary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_meter_summary, container, false);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.meter_newreading_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReading();
            }
        });

        chart = (BarChart) v.findViewById(R.id.energy_bar_chart);

        chart.setDescription("");
        chart.setNoDataText(getString(R.string.error_unavailable_consumption_data));
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        chart.getAxisLeft().setDrawLabels(true);    // Y AXIS LEFT
        chart.getAxisRight().setDrawLabels(false);  // Y AXIS RIGHT
        //chart.getXAxis().setDrawLabels(false);      // TOP X axis
        chart.setDrawValueAboveBar(false);          // too small to be seen

        chart.animateX(1000);
        chart.animateY(1000);

        chart.getLegend().setEnabled(false); //makes no sense to show the legend with only 1 timeserie

        chart.setVisibleXRangeMaximum(12);
        chart.setVisibleYRangeMaximum(30, YAxis.AxisDependency.LEFT);

        chart.setData(null);
        chart.invalidate();

        Model.asynchRequest(this,
                getString(R.string.server_url) + "?" +
                        "authentification_key=" + MainActivity.authentificationKey +
                        "&operation=getaggdata"
                , "getEnergyData");


        return v;
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

            Model.asynchRequest(this,
                    // operation, authkey, production, consumption and timestamp
                    getString(R.string.server_url)+"?"+
                            "authentification_key="+ MainActivity.authentificationKey +
                            "&operation=addaggmeter"+
                            "&input="+type+
                            "&value="+value+
                            "&timestamp="+ (Calendar.getInstance().getTimeInMillis()/1000L)

                    ,"saveReading");
        }
    }

    @Override
    public void callback(String eventName, JSONObject payload) {
        System.out.println(payload);

        if(eventName.equals("saveReading")){
            // show toast
            Toast.makeText(getContext(), getString(R.string.successfull_reading_upload), Toast.LENGTH_SHORT);

            // update meter summary list
            Model.asynchRequest(this,

                    getString(R.string.server_url)+"?"+
                            "authentification_key="+ MainActivity.authentificationKey +
                            "&operation=getaggdata"

                    ,"getEnergyData");

        }else if(eventName.equals("getEnergyData")){
            try {
                JSONArray readings = payload.getJSONArray("payload");

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

                dataSets.add( new BarDataSet(consumptionEntries, getString(R.string.dataset_consumption)));
                dataSets.add(  new BarDataSet(productionEntries, getString(R.string.dataset_production)));

                // update the chart according to the passed data
                BarData data = new BarData(getXAxisLabels(), dataSets);
                chart.setData(data);
                chart.invalidate();

            } catch (JSONException e) {
                chart.clear();
                chart.invalidate();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
            }

        }
    }

    @Override
    public void onFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
    }

    private ArrayList<String> getXAxisLabels(){
        ArrayList<String> xAxis = new ArrayList<>();

        for(int d=0; d<12; d++) xAxis.add(d+"");
        return xAxis;
    }
}
