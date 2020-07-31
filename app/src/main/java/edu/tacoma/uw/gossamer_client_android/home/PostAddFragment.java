package edu.tacoma.uw.gossamer_client_android.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.Calendar;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

public class  PostAddFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private AddListener mAddListener;

    public interface AddListener {
        public void addPost(Post post);
    }

    public PostAddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostAddFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostAddFragment newInstance(String param1, String param2) {
        PostAddFragment fragment = new PostAddFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddListener = (AddListener) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post_add, container, false);
        getActivity().setTitle("Write a New Post");

        //TODO - Not sure why these R.id tags cannot be found.
        //TODO: Get the user's email and set it here for a new post.
        EditText postBodyEditText =
                v.findViewById(R.id.add_post_body);

        Button addButton = v.findViewById(R.id.add_post_button);
        //TODO: Add isAnonymous toggle
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS)

                        , Context.MODE_PRIVATE);

                String email = pref.getString(getString(R.string.EMAIL), null);
                //If we don't have an associated saved account, we can't make the post.
                if (email == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Post error: Please sign out and sign in again."
                            , Toast.LENGTH_LONG).show();
                    return;
                }
                String postBody = postBodyEditText.getText().toString();

                String postDateTime = Calendar.getInstance().getTime().toString();

                boolean isAnonymous = false; //Need to add functionality here!


                Post post = new Post(email, postBody, postDateTime, isAnonymous);
                if (mAddListener != null) {
                    mAddListener.addPost(post);
                }
            }
        });
        return v;
    }

}
