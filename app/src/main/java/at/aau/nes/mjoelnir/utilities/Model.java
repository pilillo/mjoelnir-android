package at.aau.nes.mjoelnir.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Model {

    private static Model instance;

    private Model(){

    }

    public static Model getInstance(){
        if(instance == null) instance = new Model();
        return instance;
    }

    public static void asynchRequest(CallableView outcome, String path, String eventName){
        PerformRESTCallTask t = new PerformRESTCallTask(eventName, path);
        t.updateUIOnCallback(outcome);
        t.execute((Void) null);
    }


    public static class PerformRESTCallTask extends AsyncTask<Void, Void, Boolean> {
        String errorMessage;
        String eventName;
        String path;

        CallableView view;
        JSONObject data;

        public PerformRESTCallTask(String eventName, String path){
            this.path = path;
            this.eventName = eventName;
        }

        public void updateUIOnCallback(CallableView view){
            this.view = view;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            URL url = null;
            StringBuilder sb = null;
            try {
                url = new URL(path);
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

                    try {
                        data = new JSONObject(sb.toString());
                    }catch(JSONException e){
                        data = new JSONObject();
                        data.put(
                                "payload",
                                new JSONArray( sb.toString() )
                        );
                    }

                    result = true;
                }

            } catch (JSONException e) {
                errorMessage = sb.toString();
            }catch(IOException  e) {
                errorMessage = e.getMessage();
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                // parse
                view.callback(eventName, data);
            }else{
                view.onFailed(
                        errorMessage != null && errorMessage.length() > 0 ?
                                errorMessage : "User not authenticated"
                );
            }
        }
    }
}
