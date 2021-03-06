package at.aau.nes.mjoelnir.disaggregatedData;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.aau.nes.mjoelnir.MainActivity;
import at.aau.nes.mjoelnir.R;
import at.aau.nes.mjoelnir.model.Device;
import at.aau.nes.mjoelnir.utilities.CallableView;
import at.aau.nes.mjoelnir.utilities.DeviceControllerStub;
import at.aau.nes.mjoelnir.utilities.Model;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceListFragment extends ListFragment
    implements CallableView {

    @Bind(R.id.devicelist_addcredit_button) FloatingActionButton fab;
    @Bind(R.id.disaggregate_progress)       ProgressBar progress;
    @Bind(R.id.disaggregate_emtpy_message)  TextView emptyMessage;

    private DeviceListAdapter adapter;

    public DeviceListFragment() {
        // Required empty public constructor
    }

    public static DeviceListFragment newInstance(){
        return new DeviceListFragment();
    }

    private final String DEVICES = "devices";

    private HashMap<String, Model.PerformRESTCallTask> tasks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasks = new HashMap<>();

        ArrayList<Device> devices = null;
        if(savedInstanceState != null && savedInstanceState.containsKey(DEVICES)){
            devices = savedInstanceState.getParcelableArrayList(DEVICES);
        }else{
            devices = new ArrayList<Device>();
        }

        adapter = new DeviceListAdapter(getActivity(), devices);
        setListAdapter(adapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for(Model.PerformRESTCallTask t : tasks.values()){
            t.cancel(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(DEVICES, (ArrayList<Device>) adapter.getItems());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_device_list, container, false);
        ButterKnife.bind(this, v);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //System.out.println("*** Adding credit! ***");
                addCredit();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //getListView().setEmptyView(emptyMessage);
        getDevices();
    }

    private void getDevices(){
        // start the progress bar and hide everything else
        progress.setVisibility(View.VISIBLE);

        emptyMessage.setVisibility(View.GONE);
        getListView().setVisibility(View.GONE);

        // request device list from the server
        Model.PerformRESTCallTask gd = Model.asynchRequest(this,
                getString(R.string.rest_url) + "?"
                        + "authentification_key=" + MainActivity.authentificationKey + "&operation=" + "getdevices",
                //Model.GET,
                "getDevices");
        tasks.put("getDevices", gd);
    }

    private void addCredit(){
        List<String> devices = ((DeviceListAdapter) getListAdapter() ).getNames();

        if(devices.size() > 0){
            AddCreditDialog ard = AddCreditDialog.newInstance(devices);
            ard.setTargetFragment(this, ard.CREDITADDEVENT);
            ard.show(getFragmentManager(), "creditAddDialog");
        }else{
            Toast.makeText(getContext(), "No devices available!", Toast.LENGTH_SHORT);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == AddCreditDialog.CREDITADDEVENT){
            Double value =  data.getIntExtra(AddCreditDialog.CREDITADD_CREDITFIELD, 0) / 100.0;
            int devicePos = data.getIntExtra(AddCreditDialog.CREDITADD_DEVICENAME, 0);


            Model.PerformRESTCallTask ac = Model.asynchRequest(this,
                    getString(R.string.rest_url) + "?" +
                            "authentification_key=" + MainActivity.authentificationKey +
                            "&operation=credit" +
                            "&device=" + ((DeviceListAdapter) getListAdapter()).getDevice(devicePos).id +
                            "&value=" + value
                    //, Model.GET
                    , "addCredit");
            tasks.put("addCredit", ac);
            //System.out.println("*** Charge "+value+" on device "+((DeviceListAdapter) getListAdapter() ).getDevice(devicePos).name);
        }

    }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            //getListView().set
        }

        @Override
        public void callback(String eventName, JSONObject payload) {
            if(eventName.equals("getDevices")){
                tasks.remove("getDevices");
                //System.out.println("Devices correctly received: "+payload.toString());
                try {
                    adapter.clear();

                    JSONArray devices = payload.getJSONArray("payload");
                    for(int d=0; d<devices.length(); d++){
                        JSONObject device = devices.getJSONObject(d);
                        //System.out.println("Adding device "+ device.getString("device"));
                        adapter.addItem(
                                new Device(device.getString("device"), device.getString("name"), device.getString("credit"),
                                        DeviceControllerStub.getInstance().isConnected( device.getString("device") ),
                                        DeviceControllerStub.getInstance().isON( device.getString("device") )
                        ));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progress.setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
                if(adapter.getCount() == 0) emptyMessage.setVisibility(View.VISIBLE);

            }else if(eventName.equals("addCredit")){
                tasks.remove("addCredit");
                getDevices();
            }
        }

        @Override
        public void onFailed(String message) {
            System.out.println("Error: "+message);
        }


    public class DeviceListAdapter extends BaseAdapter {
        private List<Device> items;
        private Activity context;

        public DeviceListAdapter(Activity context, List<Device> items){
            this.context = context;
            this.items = items;
        }

        public void clear(){
            //this.items = new ArrayList<>();
            this.items.clear();
        }

        public List<String> getNames(){
            List<String> names = new ArrayList<>();
            for(Device d : items){
                names.add(d.name);
            }
            return names;
        }
        public List<Device> getItems(){ return this.items; }

        public Device getDevice(int pos){
            return items.get(pos);
        }

        public void addItem(Device d){
            this.items.add(d);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if(rowView == null){
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.device_summary_entry, null);
            }
            Switch s = (Switch) rowView.findViewById(R.id.device_name_switch);

            s.setEnabled(items.get(position).connectionStatus);
            s.setChecked(items.get(position).controlStatus);
            s.setText(items.get(position).name);
            s.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean currentStatus = ((Switch) v).isChecked();
                    try {
                        DeviceControllerStub.getInstance().control( items.get(position).id, !currentStatus );
                        ((Switch) v).setChecked(!currentStatus);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
                    }
                }
            });

            TextView credit = ((TextView) rowView.findViewById(R.id.device_credit));
            credit.setText(items.get(position).credit + " \u20ac");

            if(Double.parseDouble(items.get(position).credit) < 0) credit.setTextColor(Color.RED);
            //else credit.setTextColor(Color.YELLOW);

            return rowView;
        }
    }
}
