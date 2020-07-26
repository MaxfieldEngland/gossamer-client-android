package edu.tacoma.uw.gossamer_client_android.authenticate;

import androidx.appcompat.app.AppCompatActivity;
import edu.tacoma.uw.gossamer_client_android.R;

import android.os.Bundle;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.sign_in_fragment_id, new LoginFragment())
                .commit();
    }
}