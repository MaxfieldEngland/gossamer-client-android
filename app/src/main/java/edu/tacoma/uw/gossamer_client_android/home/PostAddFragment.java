/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.home;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Calendar;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

/**
 * A simple {@link Fragment} subclass. This subclass is responsible for
 * allowing a user to add a post to the post feed on the home screen.
 *
 * @author elijah freeman
 * @author maxfield england
 *
 * @version 1.0 (Aug 3, 2020)
 */
public class  PostAddFragment extends Fragment {

    /** Listener for the Add Post button. */
    private AddListener mAddListener;
    /** A post and related information. */
    private Post mPost;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    /** Determines whether a post is anonymous. */
    private boolean isAnonymous;

    /**
     * Interface that is to be implemented by the parent activity.
     */
    public interface AddListener {
        public void addPost(Post post);
    }

    /** Required empty constructor. */
    public PostAddFragment() {}

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

    /**
     * Default onCreate method.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddListener = (AddListener) getActivity();

        //Change the title of the collapsing toolbar.
        Activity activity = this.getActivity();
        assert activity != null;
        //CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        //if (appBarLayout != null) {
        //    appBarLayout.setTitle("Write a new post");
        //}
    }

    /**
     * Responsible for inflating the post add layout. Provides functionality
     * for user to write a post, select their anonymity preferences, and submit
     * their post using a post button.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view object.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post_add, container, false);
        getActivity().setTitle("Create Post");

        final TextView postBodyEditText = v.findViewById(R.id.add_post_body);
        final Button addButton = v.findViewById(R.id.add_post_button);
        final Switch anonToggle = (Switch) v.findViewById(R.id.anon_toggle);

        //Allows user to Post to the main feed.
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

                //Used to determine whether the Anon toggle is On or Off.
                isAnonymous = anonToggle.isChecked();

                Post post = new Post(email, postBody, postDateTime, isAnonymous);
                if (mAddListener != null && !postBody.equals("")) {
                    mAddListener.addPost(post);
                }
                if (postBody.equals(""))
                    Toast.makeText(getContext(), "You cannot submit empty posts!", Toast.LENGTH_SHORT)
                    .show();
            }
        });
        return v;
    }
}
