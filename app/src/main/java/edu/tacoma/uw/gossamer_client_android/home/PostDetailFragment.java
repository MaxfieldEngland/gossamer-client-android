/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.home;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.model.Comment;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

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

    private AddListener mAddListener;

    /** The content this fragment is presenting. */
    private Post mPost;

    /**
     * The recycler view of the detail fragment; shows comments for a given post.
     */
    private RecyclerView mRecyclerView;

    /**
     * The list of comments to be displayed in recyclerview.
     */
    private List<Comment> mCommentList;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostDetailFragment() {}

    /**
     * Interface that is to be implemented by the parent activity.
     */
    public interface AddListener {
        public void addComment(Comment comment);
    }

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

//            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//            if (appBarLayout != null) {
//                if (!mPost.mIsAnonymous()) {
//                    appBarLayout.setTitle(mPost.getmDisplayName());
//                } else {
//                    appBarLayout.setTitle("Anonymous");
//                }
//            }
        }

        mRecyclerView = getActivity().findViewById(R.id.detRecyclerView);
        assert mRecyclerView != null;
        //mRecyclerView.addItemDecoration(new PostListActivity.VerticalSpaceItem(24)); TODO enable
        //Get comments before we setup recycler view!

        setupRecyclerView((RecyclerView) mRecyclerView);


    }

    public void onResume() {
        super.onResume();
        new CommentsTask().execute(getString(R.string.getpostcomments) + "?PostID=" + mPost.getmPostID());
    }

    /**
     * Sets up comment recyclerview.
     * @param recyclerview
     */
    private void setupRecyclerView(@NonNull RecyclerView recyclerview){

        if (mCommentList != null) {
            mRecyclerView.setAdapter(new PostDetailActivity.DetItemRecyclerViewAdapter
                    ((PostDetailActivity) getActivity(), mCommentList, mTwoPane));
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

        //Get post content
        if (mPost != null) {
           //If not anonymous, show the displayname
            if (!mPost.mIsAnonymous())
            ((TextView) rootView.findViewById(R.id.post_detail_id)).setText(mPost.getmDisplayName());
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

    private class CommentsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to download comments; Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.startsWith("Unable to")) {
                Toast.makeText(getActivity().getApplicationContext(), "Unable to download" + s,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getBoolean("success")) {
                    mCommentList = Comment.parseCommentJson(
                            jsonObject.getString("tags"));

                    if (!mCommentList.isEmpty()) {
                        setupRecyclerView((RecyclerView) mRecyclerView);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getActivity().getApplicationContext(), "JSON Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}