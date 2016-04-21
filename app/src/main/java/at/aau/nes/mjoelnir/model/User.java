package at.aau.nes.mjoelnir.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by a.monacchi on 21.04.2016.
 */
public class User {

    @SerializedName("authkey")
    @Expose
    public String authkey;

    public String getAuthenticationKey(){
        return authkey;
    }
}
