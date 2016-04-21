package at.aau.nes.mjoelnir.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import at.aau.nes.mjoelnir.R;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by a.monacchi on 24.03.2016.
 */
public class DeviceControllerStub extends WebSocketHandler{

    private static DeviceControllerStub instance;

    private static WebSocketConnection connection = new WebSocketConnection();
    private static boolean asynchConnection;

    private ArrayList<String> connectedDevices;
    private ArrayList<String> enabledDevices;

    private DeviceControllerStub(){
        connectedDevices = new ArrayList<>();
        enabledDevices = new ArrayList<>();
    }

    public static DeviceControllerStub getInstance(){
        if(instance == null) instance = new DeviceControllerStub();
        return instance;
    }

    public boolean isConnected(String deviceID){
        boolean result = false;
        for(int i=0; i<connectedDevices.size() && !result; i++){
            if(connectedDevices.get(i).equals(deviceID))
                result = true;
        }
        return result;
    }

    public boolean isON(String deviceID){
        boolean result = false;
        for(int i=0; i<enabledDevices.size() && !result; i++){
            if(enabledDevices.get(i).equals(deviceID))
                result = true;
        }
        return result;
    }

    public ArrayList<String> getConnectedDevices(){
        return connectedDevices;
    }

    public ArrayList<String> getEnabledDevices(){
        return enabledDevices;
    }

    private String authkey;

    public static void connect(String path, String authkey) throws WebSocketException {
        connection.connect(path, instance);
        instance.authkey = authkey;
        asynchConnection = true;
    }

    public static void control(String deviceID, boolean state) throws JSONException {
        JSONObject o = new JSONObject();

        o.put("authkey", instance.authkey);
        o.put("action", "publish");
        o.put("type", "device_control");

        JSONObject d = new JSONObject();
            d.put("device_id", deviceID);
            d.put("state", state);
        o.put("payload", d);
        connection.sendTextMessage(o.toString());
    }

    @Override
    public void onOpen() {
        System.out.println("******** Successfully connected to the server ******** ");

        //connection.sendTextMessage("Hello, world!");
        // send a subscribe event to our server
        JSONObject o = new JSONObject();
        try {
            o.put("authkey", authkey);
            o.put("action", "subscribe");

            o.put("type", "device_status");
            connection.sendTextMessage(o.toString());

            o.put("type", "latest_control");
            connection.sendTextMessage( o.toString() );

            o.put("type", "latest_status");
            connection.sendTextMessage( o.toString() );
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onTextMessage(String payload) {
        // parse JSON message
        try {
            JSONObject o = new JSONObject(payload);

            // make sure we are in the event space of the user
            if(authkey.equals(o.getString("authkey"))){
                JSONObject p = o.getJSONObject("payload");
                switch(o.getString("type")){

                    case "latest_status":
                        // expects a list of connected devices
                        JSONArray conn_devs = p.getJSONArray("devices");
                        for(int i=0; i < conn_devs.length(); i++){
                            connectedDevices.add(conn_devs.getString(i));
                        }
                        break;

                    case "device_status":
                        // expect 1 device
                        if(p.getString("state").equals("1")){
                            // add to the list if not present
                            boolean containsValue = false;
                            for(int i=0; i<connectedDevices.size() && !containsValue; i++){
                                if(connectedDevices.get(i).equals( p.getString("device_id") )){
                                    containsValue = true; // get out, no need to add it
                                }
                            }
                            if(!containsValue) connectedDevices.add(p.getString("device_id"));
                        }else{
                            // remove from the list if present
                            for(int i=0; i<connectedDevices.size(); ){
                                if(connectedDevices.get(i).equals( p.getString("device_id") )){
                                    connectedDevices.remove(i);
                                }else{
                                    i++;
                                }
                            }
                        }
                        break;

                    case "latest_control":
                        // expects a list of device control statuses
                        JSONArray on_devs = p.getJSONArray("devices");
                        for(int i=0; i < on_devs.length(); i++){
                            enabledDevices.add(on_devs.getString(i));
                        }
                        break;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason) {
        System.out.println("Closing websocket: "+reason);
    }
}
