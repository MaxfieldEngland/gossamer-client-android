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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.content.Intent;

import android.os.Build;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.model.Comment;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;
import edu.tacoma.uw.gossamer_client_android.userprofile.UserProfileActivity;
import edu.tacoma.uw.gossamer_client_android.home.model.Tag;

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
    /** Constant. */
    public static final String ARG_ITEM_ID = "item_id";

    /** Add Listener to add comment. */
    private AddListener mAddListener;
    /** The content this fragment is presenting. */
    private Post mPost;
    /** The recycler view of the detail fragment; shows comments for a given post. */
    private RecyclerView mRecyclerView;
    /** The list of comments to be displayed in recyclerview. */
    private List<Comment> mCommentList;
    /** Whether or not the activity is in two-pane mode, i.e. running on a tablet device. */
    private boolean mTwoPane;
    /** Post Detail Fragment. */
    PostDetailFragment thisFrag = this;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PostDetailFragment() {}

    /** Getter method for post object. */
    public Post getPost() { return mPost; }

    /**
     * Interface that is to be implemented by the parent activity.
     */
    public interface AddListener {
        void addComment(Comment comment);
    }

    /**
     * Default onCreate method.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((PostDetailActivity) getActivity()).enableShareOption = true;

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

    /** Default onResume method. Update the Comments. */
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

        //Create contingent delete button
        SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);

        String email = pref.getString(getString(R.string.EMAIL), null);

        boolean isPostMaster = mPost.getmEmail().equals(email) || pref.getBoolean(getString(R.string.isAdmin), false);

        //Create delete post button if the post is ours (or if we are admin)
        if (isPostMaster) {
            Button deletePostButton = (Button) rootView.findViewById(R.id.deletePostButton);

            final PostDetailActivity parent = (PostDetailActivity) getActivity();

            deletePostButton.setVisibility(View.VISIBLE);
            deletePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PostDetailActivity.DeletePostConfirmDialog dpcd = new PostDetailActivity.DeletePostConfirmDialog(mPost, parent);
                    dpcd.show(parent.getSupportFragmentManager(), "DeletePost");
                }
            });
        }

        //Get post content
        final TextView userProfile = rootView.findViewById(R.id.post_detail_id);

        // If user is not anonymous, make user name clickable and navigate to users profile.
        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mPost.mIsAnonymous()) {
                    String e = mPost.getmEmail();
                    String u = mPost.getmDisplayName();
                    Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                    intent.putExtra("email", e);
                    intent.putExtra("username", u);
                    startActivity(intent);
                }
            }
        });

        if (mPost != null) {
           //If not anonymous, show the displayname
            if (!mPost.mIsAnonymous()) {
                ((TextView) rootView.findViewById(R.id.post_detail_id)).setText(mPost.getmDisplayName());

                TextView pronounDisplay = (TextView) rootView.findViewById(R.id.post_detail_pronouns);
                String pronounText = mPost.getmPronouns();

                if (pronounText.trim().equals(""))
                    pronounDisplay.setVisibility(View.GONE);
                else
                    pronounDisplay.setText("("+mPost.getmPronouns()+")");
            }
            //If anonymous, simply display "Anonymous" for the username and remove the pronoun view.
            else {
                ((TextView) rootView.findViewById(R.id.post_detail_id)).setText("Anonymous");
                ((TextView) rootView.findViewById(R.id.post_detail_pronouns)).setVisibility(View.GONE);
            }
            ((TextView) rootView.findViewById(R.id.post_detail_short_desc))
                    .setText(mPost.getmPostBody());
            ((TextView) rootView.findViewById(R.id.post_detail_long_desc))
                    .setText(mPost.dateTime());

            LinearLayout tagCon = (LinearLayout) rootView.findViewById(R.id.det_tagContainer);

            LinearLayout.LayoutParams tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tagLayout.setMargins(0, 0, 10, 0);

            //Populate det_tagContainer with tags
            for (Tag tag: mPost.getTags()){
                final Button tagButton;
                tagButton = new Button(getActivity());
                tagButton.setText(tag.getName());
                tagButton.setTextSize(10);
                tagButton.setMinHeight(10);
                tagButton.setMinimumHeight(10);
                tagButton.setMinWidth(100);
                tagButton.setMinimumWidth(200);

                tagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((PostDetailActivity) getActivity()).launchSearchActivity(tagButton.getText().toString());
                    }
                });

                //Adding some graphical features that are build version dependent:
                //Get rid of the tag button shadows by getting rid of the state list animator
                if (Build.VERSION.SDK_INT>=21) tagButton.setStateListAnimator(null);

                //Change the shape to be more capsule or rounded rectangle.
                if (Build.VERSION.SDK_INT>=16) {
                    GradientDrawable tagShape = new GradientDrawable();
                    tagShape.setCornerRadius(100);
                    tagShape.setColor(Color.parseColor(tag.getColor()));
                    tagButton.setBackground(tagShape);
                }
                else {
                    tagButton.setBackgroundColor(Color.parseColor(tag.getColor()));
                }
                tagCon.addView(tagButton, tagLayout);
            }

            //Finally load the image, if one exists.
            ImageView postImageView = rootView.findViewById(R.id.post_img);
                if (!mPost.getmImgUrl().contentEquals("null")) {
                    new DownloadImgTask(postImageView).execute(mPost.getmImgUrl());
                }
                else {
                    //If there's no image, hide the postimageview
                    postImageView.setVisibility(View.GONE);
                }


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
                    commentBodyEditText.setText("");

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

    /**
     * onViewCreated sets up the recyclerview.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = getView().findViewById(R.id.detRecyclerView);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new PostListActivity.VerticalSpaceItem(24));
        setupRecyclerView((RecyclerView) mRecyclerView);
    }

    /**
     * Inner class responsible for retrieving image from url.
     */
    private class DownloadImgTask extends AsyncTask<String, Void, Bitmap> {

        ImageView postImg;

        public DownloadImgTask(ImageView postImg) {
            this.postImg = postImg;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imgUrl = urls[0];
            Bitmap imgMap = null;
            try {
                InputStream in = new URL(imgUrl).openStream();
                imgMap = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return imgMap;
        }

        protected void onPostExecute(Bitmap result) {
            postImg.setImageBitmap(result);
        }
    }

    /**
     * Inner class responsible for getting the comments.
     */
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

        /**
         * Determine whether GET request was successful.
         * @param s
         */
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
                    mCommentList = Comment.parseCommentJson(jsonObject.getString("tags"));

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

    /** Refresh current fragment. */
    public void refresh() {
        FragmentTransaction refresh = getFragmentManager().beginTransaction();
        refresh.detach(thisFrag).attach(thisFrag).commit();
    }

    /**
     * Sets up recycler view adapter.
     */
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
         *                    refresh.detach(thisFrag).attach(thisFrag).commit();
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(final DetItemRecyclerViewAdapter.ViewHolder holder, int position) {

            holder.mIdView.setText(mValues.get(position).getmDisplayName());
            holder.mPronounView.setText(mValues.get(position).getmCommentPronouns());

            final int currIndex = position;

            holder.mIdView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String e = mValues.get(currIndex).getmEmail();
                    String u = mValues.get(currIndex).getmDisplayName();
                    Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                    intent.putExtra("email", e);
                    intent.putExtra("username", u);
                    startActivity(intent);

                }
            });

            holder.mContentView.setText(mValues.get(position).getmCommentBody());
            holder.mDateView.setText(mValues.get(position).dateTime());
            holder.itemView.setTag(mValues.get(position));

            final int pos = position;

            SharedPreferences pref = getActivity().getSharedPreferences(getString(R.string.LOGIN_PREFS)
                    , Context.MODE_PRIVATE);

            String email = pref.getString(getString(R.string.EMAIL), null);
            //Create a more robust admin email check; doing a hackier one for the time being.
            boolean isPostMaster = mValues.get(position).getmEmail().equals(email) || pref.getBoolean(getString(R.string.isAdmin), false);

            //Create delete post button if the post is ours
            if (isPostMaster) {
                final PostDetailActivity parent = (PostDetailActivity) getActivity();
                holder.mDeleteButton.setVisibility(View.VISIBLE);
                holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        PostDetailActivity.DeleteCommentConfirmDialog dccd = new PostDetailActivity.DeleteCommentConfirmDialog(mValues.get(pos), parent);
                        dccd.show(parent.getSupportFragmentManager(), "DeletePost");
                    }
                });
            }
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
            final TextView mPronounView;
            final TextView mDateView;
            final Button mDeleteButton;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mPronounView = (TextView) view.findViewById(R.id.comment_pronoun_text);
                mContentView = (TextView) view.findViewById(R.id.content);
                mDateView = (TextView) view.findViewById(R.id.datetime);
                mDeleteButton = (Button) view.findViewById(R.id.deleteCommentbutton);
            }
        }
    }
}