package at.aau.nes.mjoelnir.utilities;

import android.graphics.drawable.Drawable;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by a.monacchi on 13.03.2016.
 */
public interface CallableView {
    public void callback(String eventName, JSONObject payload);
    public void onFailed(String message);
}
