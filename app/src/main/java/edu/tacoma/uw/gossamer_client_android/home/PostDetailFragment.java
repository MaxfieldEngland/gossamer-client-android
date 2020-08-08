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
import android.os.AsyncTask;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.model.Comment;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;
import edu.tacoma.uw.gossamer_client_android.userprofile.UserProfileActivity;

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

    PostDetailFragment thisFrag = this;

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
        mAddListener = (AddListener) getActivity();

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
            mRecyclerView.setAdapter(new DetItemRecyclerViewAdapter
                    (this, mCommentList, mTwoPane));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
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
        final TextView userProfile = rootView.findViewById(R.id.post_detail_id);

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        // Show the dummy content as text in a TextView.
        if (mPost != null) {
           //If not anonymous, show thes displayname
            if (!mPost.mIsAnonymous())
            ((TextView) rootView.findViewById(R.id.post_detail_id)).setText(mPost.getmDisplayName());
            else
                ((TextView) rootView.findViewById(R.id.post_detail_id)).setText("Anonymous");
            ((TextView) rootView.findViewById(R.id.post_detail_short_desc))
                    .setText(mPost.getmPostBody());
            ((TextView) rootView.findViewById(R.id.post_detail_long_desc))
                    .setText(mPost.getmPostDateTime());

        }

        //Get comment add fields
        final Button commentButton = rootView.findViewById(R.id.add_comment_button);
        final TextView commentBodyEditText = rootView.findViewById(R.id.add_comment_body);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Use sharedpreferences to get the email address of the user
                SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS)
                        , Context.MODE_PRIVATE);

                String email = pref.getString(getString(R.string.EMAIL), null);
                //If we don't have an associated saved account, we can't make the post.
                if (email == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Post error: Please sign out and sign in again."
                            , Toast.LENGTH_LONG).show();
                    return;
                }
                //Collect comment info to send to server
                String commentBody = commentBodyEditText.getText().toString();
                String commentDateTime = Calendar.getInstance().getTime().toString();
                int commentPostID = mPost.getmPostID();

                Comment comment = new Comment(email, commentBody, commentDateTime, commentPostID);
                if (mAddListener != null && !commentBody.equals("")) {
                    mAddListener.addComment(comment);

                    //Detach and reattach the fragment to reload after the comment was added.
                    FragmentTransaction refresh = getFragmentManager().beginTransaction();
                    refresh.detach(thisFrag).attach(thisFrag).commit();

                }
                if (commentBody.equals(""))
                    Toast.makeText(getContext(), "You cannot submit empty comments!", Toast.LENGTH_SHORT)
                            .show();

                }
            });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = getView().findViewById(R.id.detRecyclerView);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new PostListActivity.VerticalSpaceItem(24));

        setupRecyclerView((RecyclerView) mRecyclerView);


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

    public class DetItemRecyclerViewAdapter extends RecyclerView.Adapter<DetItemRecyclerViewAdapter.ViewHolder> {
        private final PostDetailFragment mParentActivity;
        private final List<Comment> mValues;
        private final boolean mTwoPane;

        DetItemRecyclerViewAdapter(PostDetailFragment parent,
                                   List<Comment> items,
                                   boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        /**
         * Inflates the view for each posts.
         *
         * @param parent
         * @param viewType
         * @return
         */
        @NonNull
        @Override
        public DetItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_list_content, parent, false);
            return new DetItemRecyclerViewAdapter.ViewHolder(view);
        }

        /**
         * Responsible for binding the ViewHolder.
         *
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(final DetItemRecyclerViewAdapter.ViewHolder holder, int position) {
            //If not anonymous, show the displayname

            holder.mIdView.setText(mValues.get(position).getmDisplayName());
            holder.mContentView.setText(mValues.get(position).getmCommentBody());
            holder.mDateView.setText(mValues.get(position).getmCommentDateTime());
            holder.itemView.setTag(mValues.get(position));
        }

        /**
         * Returns the number of comments.
         *
         * @return Size of comment list (i.e. num of comments)
         */
        @Override
        public int getItemCount() {
            return mValues.size();
        }

        /**
         * View holder that contains the information present in the Recycler view.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;
            final TextView mDateView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
                mDateView = (TextView) view.findViewById(R.id.datetime);
            }
        }


    }


}