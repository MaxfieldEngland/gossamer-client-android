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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.Calendar;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

/**
 * A simple {@link Fragment} subclass. This subclass is responsible for
 * allowing a user to add a post to the post feed on the home screen.
 *
 * @author elijah freeman
 * @author maxfield england
 * @version 1.0 (Aug 3, 2020)
 */
public class  PostAddFragment extends Fragment {

    /** Listener for the Add Post button. */
    private AddListener mAddListener;
    /** A post and related information. */
    private Post mPost;
    /** Current text in post body (saved when tags are selected) */
    private String currPostBody;
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
     * Default onCreate method.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddListener = (AddListener) getActivity();
        Activity activity = this.getActivity();
        assert activity != null;
    }

    /** Default onResume method. Sets the the post body upon resume. */
    @Override
    public void onResume() {
        super.onResume();
        if (currPostBody != null) {
            TextView postBody = (TextView) getView().findViewById(R.id.add_post_body);
            postBody.setText(currPostBody);
        }
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
        final TextView postImgUrlEditText = v.findViewById(R.id.add_post_img_url);
        final Button addButton = v.findViewById(R.id.add_post_button);
        final Button addTagButton = v.findViewById(R.id.launchAddTagsFragmentButton);
        final Switch anonToggle = (Switch) v.findViewById(R.id.anon_toggle);

        //Launch the tag selection fragment, and be sure to save the post body.
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currPostBody = postBodyEditText.getText().toString();

                PostAddTagsFragment fragment = new PostAddTagsFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.post_detail_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

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
                String imgUrl = postImgUrlEditText.getText().toString();
                Log.d("IMG URL DEBUG:", imgUrl);
                String postDateTime = Calendar.getInstance().getTime().toString();

                //Used to determine whether the Anon toggle is On or Off.
                isAnonymous = anonToggle.isChecked();

                Post post = new Post(email, postBody, imgUrl, postDateTime, isAnonymous);
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
