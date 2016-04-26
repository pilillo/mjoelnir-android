package at.aau.nes.mjoelnir.utilities;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by a.monacchi on 13.03.2016.
 */
public class PreferenceManager {
    private static PreferenceManager instance;
    private static SharedPreferences settings;

    protected PreferenceManager(Activity context){
        settings = context.getPreferences(context.MODE_PRIVATE);
    }

    protected PreferenceManager(SharedPreferences preferences){
        settings = preferences;
    }

    public static PreferenceManager getInstance(Activity context){
        if(instance == null){
            instance = new PreferenceManager(context);
        }
        return instance;
    }

    public static PreferenceManager getInstance(SharedPreferences preferences){
        if(instance == null){
            instance = new PreferenceManager(preferences);
        }
        return instance;
    }

    public static void addOrEditPreference(String key, String value){
        if(instance != null){
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(key, value);
            editor.commit();
        }

    }

    public static boolean containsKeys(String[] keys){
        boolean result = true;
        for(int i=0; i<keys.length && result; i++){
            if(!settings.contains(keys[i]))
                result = false; // get out as soon as a key is not available
        }
        return result;
    }

    public static String getString(String key){
        return settings.getString(key,
                null); // default value
    }

    public static boolean getBoolean(String key){
        return settings.getBoolean(key, false); // default value
    }

    public static float getFloat(String key){
        return settings.getFloat(key, 0f);
    }

}
