package at.aau.nes.mjoelnir.disaggregatedData;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import at.aau.nes.mjoelnir.R;
import at.aau.nes.mjoelnir.aggregatedData.AddReadingDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCreditDialog extends DialogFragment
        implements
        SeekBar.OnSeekBarChangeListener,
            View.OnClickListener
{

    public final static int CREDITADDEVENT = 6789;
    public final static String CREDITADD_CREDITFIELD = "CREDITADD_CREDITFIELD";
    public final static String CREDITADD_DEVICENAME = "CREDITADD_DEVICENAME";

    private Spinner spinner;
    private SeekBar creditBar;
    private TextView currentCreditToBeAdded;

    private List<String> devices;

    public AddCreditDialog() {
        // Required empty public constructor
    }

    public static AddCreditDialog newInstance(List<String> devices){
        AddCreditDialog d = new AddCreditDialog();
        d.devices = devices;
        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_credit_dialog, container, false);

        spinner = (Spinner) v.findViewById(R.id.meterreading_devicename);
        ArrayAdapter<String> device_selector = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, devices);
        device_selector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(device_selector);

        creditBar = (SeekBar) v.findViewById(R.id.seekBar_creditadd);
        creditBar.setOnSeekBarChangeListener(this);

        creditBar.setMax(1000);   // at most 10 euros?


        currentCreditToBeAdded = (TextView) v.findViewById(R.id.credit_to_add);
        currentCreditToBeAdded.setText("0 "+ " \u20ac");

        ((Button) v.findViewById(R.id.button_creditadd)).setOnClickListener(this);

        return v;
    }

    public void onClick(View v) {
        // send intent back to the calling dialog
        Intent intent = new Intent();
        intent.putExtra(CREDITADD_DEVICENAME, spinner.getSelectedItemPosition());
        intent.putExtra(CREDITADD_CREDITFIELD, creditBar.getProgress());
        getTargetFragment().onActivityResult(getTargetRequestCode(), CREDITADDEVENT, intent);

        this.dismiss();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(currentCreditToBeAdded != null)
            currentCreditToBeAdded.setText( (10.0 * creditBar.getProgress() / (double) creditBar.getMax()) + " \u20ac" );
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
