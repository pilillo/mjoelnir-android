package at.aau.nes.mjoelnir.cloudMessaging;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import at.aau.nes.mjoelnir.MainActivity;
import at.aau.nes.mjoelnir.R;
import at.aau.nes.mjoelnir.utilities.Model;
import at.aau.nes.mjoelnir.utilities.PreferenceManager;

/**
 * Created by a.monacchi on 29.03.2016.
 */
public class RegistrationIntentService extends IntentService {

    // abbreviated tag name
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Make a call to Instance API
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_defaultSenderId);
        try {
            // request token that will be used by the server to send push notifications
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);

            //Log.d(TAG, "*** GCM Registration Token: " + token);
            System.out.println("*** GCM Registration Token: " + token);

            MainActivity.notificationKey = token;   // dirty, but we do not care!

            // save the notification key as a preference
            PreferenceManager.addOrEditPreference("notification_key", token);

            // pass along this data
            sendRegistrationToServer(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        URL url = null;
        StringBuilder sb = null;
        try {
            url = new URL(getString(R.string.server_url)+
                    "?"
                    + "authkey=" + MainActivity.authentificationKey +
                    "&operation=" + "addnotkey"
                    + "&value=" + token);

            System.out.println("Uploading notification key as "+token);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if(conn.getResponseCode() == 201 || conn.getResponseCode() == 200){
                sb = new StringBuilder();
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String nextLine = "";
                    while ((nextLine = reader.readLine()) != null) {
                        sb.append(nextLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(conn.getResponseMessage());

        } catch (IOException  e) {
            e.printStackTrace();
        }
    }


    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}
