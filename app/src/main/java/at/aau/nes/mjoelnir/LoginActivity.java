package at.aau.nes.mjoelnir;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import at.aau.nes.mjoelnir.model.Api;
import at.aau.nes.mjoelnir.model.User;
import at.aau.nes.mjoelnir.utilities.CallableView;
import at.aau.nes.mjoelnir.utilities.Model;
import at.aau.nes.mjoelnir.utilities.PreferenceManager;


public class LoginActivity extends AppCompatActivity
        implements
            View.OnClickListener,
            CallableView
        //, Callback<User>
{

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    private boolean ongoingLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        Button signInButton = (Button) findViewById(R.id.email_sign_in_button);
        signInButton.setOnClickListener(this);

        // fill in the fields if they were set in the preference before
        if( PreferenceManager.getInstance(this).containsKeys(new String[]{"username", "password"}) ){
            mEmailView.setText(PreferenceManager.getInstance(this).getString("username"));
            mPasswordView.setText(PreferenceManager.getInstance(this).getString("password"));
        }
    }

    @Override
    public void onClick(View v) {
        // if already attempting to login, discard the additional request
        if (ongoingLogin) {
            return;
        }else{
            ongoingLogin = true;
        }

        // attempt login
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // make sure there is network connection
        if(!isNetworkAvailable()){
            Toast.makeText(LoginActivity.this, getString(R.string.error_network_unavailable), Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // login
            //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //startActivity(intent);
            //System.out.println("**** Making login request to server ****");

            Model.getInstance().asynchRequest(this,
                    getString(R.string.server_url) +
                            "?operation=login&username="+mEmailView.getText().toString()+
                            "&password="+mPasswordView.getText().toString(),
                    "LOGIN");

            //Api.getInstance().startInterface( getString(R.string.server_url)+"/" );
            //Api.getInstance().login("Administrator", "administrator").enqueue(this);
        }
    }

    private boolean isEmailValid(String email) {
        //return email.contains("@") && email.contains(".");
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;  // 8 chars for the password
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void callback(String eventName, JSONObject payload) {
        if(eventName.equals("LOGIN")){
            // check if the user is correctly logged
            //System.out.println("*** PAYLOAD ***");
            //System.out.println(payload.toString());

            try {
                // get the authentication key from the payload
                String authkey = payload.getString("authkey");

                // if yes add the credentials to the shared preferences
                PreferenceManager.addOrEditPreference("username", mEmailView.getText().toString());
                PreferenceManager.addOrEditPreference("password", mPasswordView.getText().toString());
                //PreferenceManager.addOrEditPreference("authkey", authkey);

                // start the main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("authkey", authkey);

                //intent.putExtra("username", mEmailView.getText().toString());
                //intent.putExtra("password", mPasswordView.getText().toString());
                startActivity(intent);

                // let the task die
                //finish();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        ongoingLogin = false;
    }

    @Override
    public void onFailed(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

        //mPasswordView.setError(getString(R.string.error_incorrect_password));
        //mPasswordView.requestFocus();
        ongoingLogin = false;
    }

    /*
    @Override
    public void onResponse(Call<User> call, Response<User> response) {
        System.out.println(response.body().authkey);
    }

    @Override
    public void onFailure(Call<User> call, Throwable t) {
        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
    }
    */
}
