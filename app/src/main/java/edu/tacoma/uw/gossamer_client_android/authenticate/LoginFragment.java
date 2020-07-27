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
 * Use the {@link LoginFragment#} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    /** Member variable for the listener.*/
    private LoginFragmentListener mLoginFragmentListener;

    /**
     * Interface for Login functionality
     */
    public interface LoginFragmentListener {
        void createAccount();
        public void login(String email, String pwd);
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mLoginFragmentListener = (LoginFragmentListener) getActivity();

        final EditText emailText = view.findViewById(R.id.email_edit_text);
        final EditText pwdText = view.findViewById(R.id.pwd_edit_text);


        final TextView registerText = view.findViewById(R.id.register_text_view);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginFragmentListener) getActivity()).createAccount();
            }
        });


        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get values
                String email = emailText.getText().toString();
                String pwd = pwdText.getText().toString();

                //ValidateEmail
                if (TextUtils.isEmpty(email) || !email.contains("@")) {
                    Toast.makeText(view.getContext(), "Enter valid email address"
                            , Toast.LENGTH_SHORT)
                            .show();
                    emailText.requestFocus();
                }

                else if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
                    Toast.makeText(view.getContext(), "Enter valid password (at least 6 characters"
                    , Toast.LENGTH_SHORT)
                            .show();
                    pwdText.requestFocus();
                }
                mLoginFragmentListener.login(emailText.getText().toString(), pwdText.getText().toString());
            }
        });

        return view;
    }
}