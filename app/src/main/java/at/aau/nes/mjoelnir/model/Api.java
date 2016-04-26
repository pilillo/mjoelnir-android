package at.aau.nes.mjoelnir.model;

import at.aau.nes.mjoelnir.R;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by a.monacchi on 21.04.2016.
 */
public class Api {

    private static Api instance;
    private ApiInterface iface;
    private Retrofit retrofit;

    private Api(){
        // empty constructor
    }

    public static Api getInstance(){
        if(instance == null) instance = new Api();
        return instance;
    }

    public void startInterface(String baseURL){
        retrofit = new Retrofit.Builder()
                .baseUrl( baseURL )
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        iface = retrofit.create(Api.ApiInterface.class);
    }

    public interface ApiInterface {

        @GET("rest.php?operation=login")
        Call<User> loginUser(@Query("username") String username, @Query("password") String password);
    }

    public Call<User> login(String username, String password){
        Call<User> loginCallback = iface.loginUser(username, password);
        return loginCallback;
    }
}
