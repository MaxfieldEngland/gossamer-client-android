/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.authenticate;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import edu.tacoma.uw.gossamer_client_android.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment} factory method to
 * create an instance of this fragment.
 *
 * @author elijah freeman
 * @author maxfield england
 *
 * @version 1.0 (July 27, 2020)
 */
public class LoginFragment extends Fragment {

    /** Member variable for the listener.*/
    private LoginFragmentListener mLoginFragmentListener;

    /** Interface for Login functionality. */
    public interface LoginFragmentListener {
        void createAccount();
        void login(String email, String pwd);
    }

    /** Required Constructor. */
    public LoginFragment() {}

    /**
     * Default onCreate method.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Responsible for inflating the the Login Fragment. Adds functionality to the login button
     * and text view.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view object
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Login");

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mLoginFragmentListener = (LoginFragmentListener) getActivity();

        final EditText emailText = view.findViewById(R.id.email_edit_text);
        final EditText pwdText = view.findViewById(R.id.pwd_edit_text);
        final TextView registerText = view.findViewById(R.id.register_text_view);
        final Button loginButton = view.findViewById(R.id.login_button);

        //Adds Click functionality for the TextView.
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginFragmentListener) getActivity()).createAccount();
            }
        });

        //Retrieves input information upon button click, sends to SignInActivity.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get input values
                String email = emailText.getText().toString();
                String pwd = pwdText.getText().toString();

                //Validate Email
                if (TextUtils.isEmpty(email) || !email.contains("@")) {
                    Toast.makeText(view.getContext(), "Enter valid email address"
                            , Toast.LENGTH_SHORT)
                            .show();
                    emailText.requestFocus();
                }
                //Sends user information to the SignInActivity.
                mLoginFragmentListener.login(emailText.getText().toString(), pwdText.getText().toString());
            }
        });
        return view;
    }
}