package at.aau.nes.mjoelnir.utilities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by a.monacchi on 13.03.2016.
 */
public class PermissionManager {

    private static PermissionManager instance;
    private static final int REQUEST_MULTIPLE_PERMISSIONS = 123;

    private PermissionManager(){

    }

    public static PermissionManager getInstance(){
        if(instance == null)
            instance = new PermissionManager();
        return instance;
    }

    public static void checkPermissionsIfNecessary(Activity activity, String[] permissions){

        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            checkPermissions(activity, permissions);
        } else {
            // Pre-Marshmallow (permission = True)
            // execute the handler right away
            permissionHandler.handle();
        }

    }

    private static void checkPermissions(Activity activity, String[] permissions){
        List<String> necessaryPermissions = new ArrayList<>();

        for(String p : permissions){
            if(!checkPermissionStatus(activity, p)){
                necessaryPermissions.add(p);
            }
            //System.out.println(p);
        }

        if(necessaryPermissions.size() > 0){
            //System.out.println("Requesting the list of permissions");
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    necessaryPermissions.toArray(new String[necessaryPermissions.size()]),
                    REQUEST_MULTIPLE_PERMISSIONS);
        }else{
            permissionHandler.handle(); // everything was already previously accepted
        }

    }

    public static void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean grantedPermissions = false;
        switch (requestCode) {
            case REQUEST_MULTIPLE_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions = true;
                }
                break;

        }

        if(grantedPermissions)
            permissionHandler.handle();
    }

    private static boolean checkPermissionStatus(Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    // this way we can only handle 1 event at time (do we need more?)
    private static PermissionHandler permissionHandler;

    public static void registerHandler(PermissionHandler handler) {
        permissionHandler = handler;
    }

    public interface PermissionHandler{
        public void handle();
    }
}
