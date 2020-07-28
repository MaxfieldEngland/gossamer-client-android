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
import android.os.Bundle;

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

    /** Used to save login status. */
    private SharedPreferences mSharedPreferences;

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
                .commit();
    }

    /**
     * LoginFragmentListener interface method.
     *
     * @param email input from user.
     * @param pwd input from user.
     */
    public void login(String email, String pwd) {
        //TODO - Validate Email and Password against user info stored in database.
        //Logs that the user has logged in.
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.LOGGEDIN), true)
                .apply();
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
     * @param email , new users unique email.
     * @param pwd , new users password.
     */
    @Override
    public void register(String username, String email, String pwd) {
        //TODO - Send new user information to database.

        Intent intent = new Intent(this, PostListActivity.class);
        startActivity(intent);
        finish();
    }
}