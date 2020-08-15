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
import edu.tacoma.uw.gossamer_client_android.home.PostListActivity;

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
     * Used to track whether we're attempting login or registration;
     */
    private boolean isRegister;

    /**
     * Stored to pass on to loginPrefs upon successful login or registration.
     */
    private String loginEmail;

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
        loginEmail = "";

        mSharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);

        if (!mSharedPreferences.getBoolean(getString(R.string.LOGGEDIN), false)) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.sign_in_fragment_id, new LoginFragment())
                    .commit();
        } else {
            Intent intent = new Intent(this, PostListActivity.class);
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
                .addToBackStack(null)
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
        isRegister = false;
        loginEmail = email;
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
    public void startMainActivity(boolean priv) {
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.LOGGEDIN), true)
                .putString(getString(R.string.EMAIL), loginEmail)
                .putBoolean(getString(R.string.isAdmin), priv)
                .apply();

        //If we're just registering, let the user know their registration was successful
        if (isRegister)
            Toast.makeText(getBaseContext(), "Registration successful. Welcome to Gossamer!",
                    Toast.LENGTH_LONG).show();

        //Starts a new the main home activity after user logs in.
        Intent intent = new Intent(this, PostListActivity.class);
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

        StringBuilder url = new StringBuilder(getString(R.string.register));
        loginJSON = new JSONObject();
        isRegister = true;
        loginEmail = email;
        try {
            loginJSON.put("displayname", username);
            loginJSON.put("email", email);
            loginJSON.put("password", pwd);
            new LoginAsyncTask().execute(url.toString());
        } catch (JSONException e) {
            //If something went wrong with the JSON, show an error on the screen
            Toast.makeText(this, "Error with JSON creation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the login and register requests and responses.
     */
    private class LoginAsyncTask extends AsyncTask<String, Void, String> {

        /**
         * Handles HTTP requests sent from the app.
         * @param urls A set of URLS to send requests to (in our case, typically one)
         * @return Server response.
         */
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
                    Log.e("LOGIN_REG_ERROR", e.getMessage());
                    response = "Unable to login/register, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        /**
         * Response handler determining whether login is successful.
         * @param s The server response to the login POST request
         */
        @Override
        protected void onPostExecute(String s) {
            if (s.startsWith("Unable to login")) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                return;
            }
            //Try to read JSON response
            try {
                JSONObject jsonObject = new JSONObject(s);
                //If we get 'success' back from our post, we can move on to the main screen and save login data.
                if (jsonObject.getBoolean("success")) {
                    boolean admin = jsonObject.getBoolean("privLoginStatus");
                    //SUCCESSFUL LOGIN
                    startMainActivity(admin);
                }
                //Else, we did not have a successful login:
                else {
                    //If we get back that we did not succeed, we can tell them that the login/register.
                    if (!jsonObject.getBoolean("success")) {

                        //a registration problem: tell them that the email is already taken.
                        if (isRegister) {
                            Toast.makeText(getApplicationContext(), "Email already registered. Please choose another email.",
                                    Toast.LENGTH_LONG).show();
                        }
                        //Otherwise, it's a login issue; tell the user that the login info was not
                        //a match.
                        else {
                            Toast.makeText(getApplicationContext(), "Login failed: Check username and password.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    //If the response didn't include 'success', something else went wrong; display to user
                    else {
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                    }

                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "JSON Parsing error: "
                        + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}