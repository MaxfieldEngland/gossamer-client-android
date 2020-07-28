/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */

package edu.tacoma.uw.gossamer_client_android.authenticate;

import androidx.appcompat.app.AppCompatActivity;
import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.MainActivity;

import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Sign-In activity that launches the Login & Register Fragments. Connects to
 * database to store/validate user email and password. Uses SharedPreferences API.
 *
 * @author elijah freeman
 * @author maxfield england
 *
 * @version 1.0 (July 27, 2020)
 */
public class SignInActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener,
                            RegisterFragment.RegisterFragmentListener {

    /**
     * Used to save login status.
     */
    private SharedPreferences mSharedPreferences;

    /**
     * Used to send credential check attempts to the server.
     */
    private JSONObject loginJSON;

    /**
     * Sets the activity layout for SignInActivity. Performs check against SharedPreferences
     * to determine whether user has previously logged in, if so then navigates user to home view.
     * If user has not logged in previously, then navigate to login page.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mSharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);

        if (!mSharedPreferences.getBoolean(getString(R.string.LOGGEDIN), false)) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.sign_in_fragment_id, new LoginFragment())
                    .commit();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * LoginFragmentListener interface method. Replaces previous fragment with
     * a Register fragment that allows user to register.
     */
    @Override
    public void createAccount() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.sign_in_fragment_id, new RegisterFragment())
                .commit();
    }

    /**
     * LoginFragmentListener interface method.
     *
     * @param email input from user.
     * @param pwd   input from user.
     */
    @Override
    public void login(String email, String pwd) {

        StringBuilder url = new StringBuilder(getString(R.string.login));
        loginJSON = new JSONObject();
        try {
            loginJSON.put("email", email);
            loginJSON.put("password", pwd);
            new LoginAsyncTask().execute(url.toString());
        } catch (JSONException e) {
            //If something went wrong with the JSON, show an error on the screen
            Toast.makeText(this, "Error with JSON creation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Logs that the user has logged in IFF login procedure has completed successfully.

    }

    /**
     * Only on verified login, the user's sharedPreferences are made to reflect their login status,
     * and they are put through to the main activity.
     */
    public void startMainActivity() {
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.LOGGEDIN), true)
                .apply();
        //Starts a new the main home activity after user logs in.
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * RegisterFragmentListener method, registers a new user. Information
     * obtained from Register Fragment.
     *
     * @param username , new users preferred name.
     * @param email    , new users unique email.
     * @param pwd      , new users password.
     */
    @Override
    public void register(String username, String email, String pwd) {
        //TODO - Send new user information to database.

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private class LoginAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setDoOutput(true);
                    OutputStreamWriter wr =
                            new OutputStreamWriter(urlConnection.getOutputStream());

                    wr.write(loginJSON.toString());
                    wr.flush();
                    wr.close();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    Log.e("LOGINERROR", e.getMessage());
                    response = "Unable to login, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.startsWith("Unable to login")) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getBoolean("success")) {
                    //SUCCESSFUL LOGIN
                    startMainActivity();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Login failed: Check username and password.",
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "JSON Parsing error on login"
                        + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}