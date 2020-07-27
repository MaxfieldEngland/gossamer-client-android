package edu.tacoma.uw.gossamer_client_android.authenticate;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.tacoma.uw.gossamer_client_android.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private RegisterFragmentListener mRegisterFragmentListener;

    public interface RegisterFragmentListener {
        public void register(String username, String email, String pwd);
    }


    public RegisterFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_register, container, false);

        getActivity().setTitle("Registration");
        mRegisterFragmentListener = (RegisterFragmentListener) getActivity();

        final EditText userNameEText = view.findViewById(R.id.username_edit_text);
        final EditText emailEText = view.findViewById(R.id.email_edit_text);
        final EditText pwdEText = view.findViewById(R.id.password_edit_text);

        Button registerButton = view.findViewById(R.id.registration_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO - Validate the username, email, & password.
                String userName = userNameEText.getText().toString();
                String email = emailEText.getText().toString();
                String pwd = pwdEText.getText().toString();
             mRegisterFragmentListener.register(userName, email, pwd);
            }
        });
        return view;
    }
}