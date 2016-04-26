package at.aau.nes.mjoelnir;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import at.aau.nes.mjoelnir.aggregatedData.MeterSummary;
import at.aau.nes.mjoelnir.cloudMessaging.RegistrationIntentService;
import at.aau.nes.mjoelnir.disaggregatedData.DeviceListFragment;
import at.aau.nes.mjoelnir.utilities.DeviceControllerStub;
import at.aau.nes.mjoelnir.utilities.PreferenceManager;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class MainActivity extends AppCompatActivity {

    public static String authentificationKey; // = "a955f40c6c94c0543a4a89d489101f5953ce670c5bfcabe8f9794e7726eda819";
    public static String notificationKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authentificationKey = getIntent().getStringExtra("authkey");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_main, MainFragment.newInstance());
        transaction.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // get the notification key and send it to the server (start a background disposable service)
        if(PreferenceManager.containsKeys(new String[]{"notification_key"})){
            notificationKey = PreferenceManager.getString("notification_key");  // if it exists get the old one
        }else{
            startService(new Intent(this, RegistrationIntentService.class));    // if it does not exist get a new one from Google
        }

        // establish a websocket connection with the server
        WebsocketConnectionTask t = new WebsocketConnectionTask();
        t.execute((Void) null);
    }

    private class WebsocketConnectionTask extends AsyncTask<Void, Void, Boolean> {
        String errorMessage;
        boolean result;

        @Override
        protected Boolean doInBackground(Void... params) {

            result = false;

            try {
                DeviceControllerStub.getInstance().connect(getString(R.string.websocket_connection), authentificationKey);
                System.err.println( "**** Websocket created ****" );
                result = true;
            } catch (WebSocketException e) {
                errorMessage = e.getMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if(!result){
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
