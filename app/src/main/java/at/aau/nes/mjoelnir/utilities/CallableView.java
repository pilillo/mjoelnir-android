package at.aau.nes.mjoelnir.utilities;

import android.graphics.drawable.Drawable;

import org.json.JSONObject;

import java.util.ArrayList;

public interface CallableView {
    public void callback(String eventName, JSONObject payload);
    public void onFailed(String message);
}
