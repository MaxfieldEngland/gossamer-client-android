/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;
import edu.tacoma.uw.gossamer_client_android.userprofile.UserProfile;

/**
 * A fragment representing a single Post detail screen.
 * This fragment is either contained in a {@link PostListActivity}
 * in two-pane mode (on tablets) or a {@link PostDetailActivity}
 * on handsets.
 *
 * @author elijah freeman
 * @author maxfield england
 */
public class PostDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";

    /** The dummy content this fragment is presenting. */
    private Post mPost;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostDetailFragment() {}

    /**
     * Default onCreate method.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mPost = (Post) getArguments().getSerializable(ARG_ITEM_ID);

            //Changes the title of the collapsing toolbar to reflect the name of the username
            // of the post.
            Activity activity = this.getActivity();
            assert activity != null;
        }
    }

    /**
     * Inflates the Post detail fragment layout. Displays information about
     * the selected users posts.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.post_detail, container, false);

        final TextView userProfile = rootView.findViewById(R.id.post_detail_id);

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserProfile.class);
                startActivity(intent);
            }
        });

        // Show the dummy content as text in a TextView.
        if (mPost != null) {
           //If not anonymous, show thes displayname
            if (!mPost.mIsAnonymous())
            ((TextView) rootView.findViewById(R.id.post_detail_id)).setText(mPost.getmDisplayName()); //TODO: Make conditional on isAnonymous
            else
                ((TextView) rootView.findViewById(R.id.post_detail_id)).setText("Anonymous");
            ((TextView) rootView.findViewById(R.id.post_detail_short_desc))
                    .setText(mPost.getmPostBody());
            ((TextView) rootView.findViewById(R.id.post_detail_long_desc))
                    .setText(mPost.getmPostDateTime());

            //TODO: Edit view to display comments? We're gonna need another recyclerview eventually for that, right? (yes)

        }
        return rootView;
    }
}