package at.aau.nes.mjoelnir.aggregatedData;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.aau.nes.mjoelnir.R;
import at.nineyards.anyline.models.AnylineImage;
import at.nineyards.anyline.modules.energy.EnergyResultListener;
import at.nineyards.anyline.modules.energy.EnergyScanView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddReadingDialog extends DialogFragment implements EnergyResultListener {

    public final static int METERREADING = 9876;
    public final static String OCRMETERREADER_READING = "OCRMETERREADER_READING";
    public final static String OCRMETERREADER_TYPE = "OCRMETERREADER_TYPE";
    private EnergyScanView ev;
    private Spinner spinner;

    public AddReadingDialog() {
        // Required empty public constructor
    }

    public static AddReadingDialog newInstance(){
        return new AddReadingDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_reading_dialog, container, false);

        // Set a spinner to select either production or consumption
        spinner = (Spinner) v.findViewById(R.id.spinner_consprod_selection);
        ArrayAdapter<String> device_selector = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Consumption", "Production"});
        device_selector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(device_selector);

        ev = (EnergyScanView) v.findViewById(R.id.energy_scan_view);

        //ev.setConfigFromJsonString(getConfigFile(getResources().openRawResource(R.raw.ocr_configuration)));
        ev.initAnyline(getString(R.string.anyline_license_key), this);

        return v;
    }

    private String getConfigFile(InputStream io){
        String jsonString = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(io));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                //System.out.println(line);
                line = br.readLine();
            }
            jsonString = sb.toString();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    @Override
    public void onResume() {
        super.onResume();

        ev.startScanning();
    }

    @Override
    public void onResult(EnergyScanView.ScanMode scanMode, String s, AnylineImage anylineImage, AnylineImage anylineImage1) {
        //Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();

        // send intent back to the calling dialog
        Intent intent = new Intent();
        intent.putExtra(OCRMETERREADER_READING, s);
        intent.putExtra(OCRMETERREADER_TYPE, (String) spinner.getSelectedItem());

        getTargetFragment().onActivityResult(getTargetRequestCode(), METERREADING, intent);

        this.dismiss();
    }
}
