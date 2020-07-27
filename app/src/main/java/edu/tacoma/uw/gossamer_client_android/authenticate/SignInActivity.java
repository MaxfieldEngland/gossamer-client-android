package edu.tacoma.uw.gossamer_client_android.authenticate;

import androidx.appcompat.app.AppCompatActivity;
import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SignInActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.sign_in_fragment_id, new LoginFragment())
                .commit();
    }



    public void login(String email, String pwd) {

        //TODO - Validate Email and Password against user info stored in database.
        Toast.makeText(this, "Clicked Login Button", Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}