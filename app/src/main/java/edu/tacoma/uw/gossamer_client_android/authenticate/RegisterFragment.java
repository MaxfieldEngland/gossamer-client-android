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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.tacoma.uw.gossamer_client_android.R;

/**
 * Registration Fragment that is required to allow a new user to
 * register a new account. Launched by SignInActivity.
 * @author elijah freeman
 * @author maxfield england
 *
 * @version 1.0 (July 27, 2020)
 */
public class RegisterFragment extends Fragment {

    /** Member variable for listener. */
    private RegisterFragmentListener mRegisterFragmentListener;

    /** Listener used to send data from Register Fragment to SignInActivity. */
    public interface RegisterFragmentListener {
        void register(String username, String email, String pwd);
    }

    /** Required empty constructor. */
    public RegisterFragment() {}

    /**
     * Initiates the activity.
     * @param savedInstanceState , saved information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Responsible for inflating the register fragment. Retrieves and sends input information
     * from user to the sign-in activity.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View object.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_register, container, false);

        getActivity().setTitle("Registration");
        mRegisterFragmentListener = (RegisterFragmentListener) getActivity();

        final EditText userNameEText = view.findViewById(R.id.username_edit_text);
        final EditText emailEText = view.findViewById(R.id.email_edit_text);
        final EditText pwdEText = view.findViewById(R.id.password_edit_text);
        final EditText pwdConfirmEText=view.findViewById(R.id.confirm_pwd_edit_text);

        final Button registerButton = view.findViewById(R.id.registration_button);
        //Adds functionality to button and sends data to SignInActivity.
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameEText.getText().toString();
                String email = emailEText.getText().toString();
                String pwd = pwdEText.getText().toString();
                String confirmPwd = pwdConfirmEText.getText().toString();

                if (!validatePassword(pwd, confirmPwd)) {
                    Toast.makeText(getContext(), "Password does not match",Toast.LENGTH_SHORT).show();
                } else { //Sends information to the SignInActivity.
                    mRegisterFragmentListener.register(userName, email, pwd);
                }

            }
        });
        return view;
    }

    private boolean validatePassword(String pwd, String confirmPwd) {
        return pwd.equals(confirmPwd);
    }
}