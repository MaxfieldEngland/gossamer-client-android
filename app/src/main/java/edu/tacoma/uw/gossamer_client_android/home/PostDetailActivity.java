/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.authenticate.SignInActivity;
import edu.tacoma.uw.gossamer_client_android.home.model.Comment;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

/**
 * An activity representing a single Post detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PostListActivity}.
 *
 * @author elijah freeman
 * @author maxfield england
 */
public class PostDetailActivity extends AppCompatActivity implements PostAddFragment.AddListener, PostDetailFragment.AddListener {

    /** Constant required for adding a post. */
    public static final String ADD_POST = "ADD_POST";
    /** Constant required for receiving tag list. */
    public static final String TAG_LIST = "TAG_LIST";

    /** JSON Post object. */
    private JSONObject mPostJSON;
    /** List of JSON Tag objects. */
    private ArrayList<JSONObject> mTagJSON;
    /** JSON Comment object. */
    private JSONObject mCommentJSON;
    /** JSON Delete object. */
    private JSONObject deleteJSON;
    /** Conditional flag value. */
    private boolean writeComment = false;
    /** Conditional flag values. */
    private boolean addTags = false;
    /** Conditional flag values. */
    private boolean lastTag = false;
    /** Conditional flag values. */
    public boolean enableShareOption = false;
    /** Delete Post ID int value. */
    public int deletePostID = -1;
    /** Delete Comment ID int value. */
    public int deleteCommentID = -1;
    /** Number of tags processed. */
    private int tagsProcessed = 0;
    /** List of tags. */
    public ArrayList<String> tagList;
    /** List of user selected tags. */
    public ArrayList<String> selectedTags;

    /**
     * Default onCreate view required to instantiating the Post Detail layout,
     * and inflating associated fragment.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        selectedTags = new ArrayList<String>();
        mTagJSON = new ArrayList<JSONObject>();

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            if (getIntent().getSerializableExtra(PostDetailFragment.ARG_ITEM_ID) != null) {
                arguments.putSerializable(PostDetailFragment.ARG_ITEM_ID,
                        getIntent().getSerializableExtra(PostDetailFragment.ARG_ITEM_ID));
                PostDetailFragment fragment = new PostDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.post_detail_container, fragment, "detail")
                        .commit();
            } else if (getIntent().getBooleanExtra(PostDetailActivity.ADD_POST, false)) {

                if (getIntent().getSerializableExtra(PostDetailActivity.TAG_LIST) != null)
                    tagList = (ArrayList<String>) getIntent().getSerializableExtra(PostDetailActivity.TAG_LIST);

                PostAddFragment fragment = new PostAddFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.post_detail_container, fragment).commit();
            }
        }
    }

    /**
     * Sends a JSON post object which is sent to the database.
     * @param post
     */
    @Override
    public void addPost(Post post) {

        StringBuilder url = new StringBuilder(getString(R.string.addpost));
        mPostJSON = new JSONObject();
        writeComment = false;

        try {
            mPostJSON.put("Email", post.getmEmail());
            mPostJSON.put("PostBody", post.getmPostBody());
            mPostJSON.put("PostDateTime", post.getmPostDateTime());
            mPostJSON.put("isAnonymous", post.mIsAnonymous());
            new AddPostAsyncTask().execute(url.toString());
        } catch (Exception e) {
            Toast.makeText(this, "Error with JSON creation on adding a post: "
                    + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    /**
     * Creates JSON Tag objects and sends to database.
     * @param tagName , name of the tag.
     * @param PostID , ID for associated post.
     */
    public void commitTag(String tagName, int PostID) {

        Log.e("COMMITTAG","ENTERING COMMIT TAG LOOP");

        String url = (getString(R.string.addposttag));
        JSONObject tagJSON = new JSONObject();
        writeComment = false;
        addTags = true;

        try {
            tagJSON.put("TagID", tagName);
            tagJSON.put("PostID", PostID);
            mTagJSON.add(tagJSON);
            new AddPostAsyncTask().execute(url);
        } catch (Exception e) {
            Toast.makeText(this, "Error with JSON creation on adding tags: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
        if (lastTag) finish();
    }

    /**
     * Creates Comment JSON Object and adds comments to database.
     * @param comment , Comment object to be added.
     */
    @Override
    public void addComment(Comment comment) {

        StringBuilder url = new StringBuilder(getString(R.string.addpostcomment));
        mCommentJSON = new JSONObject();
        writeComment = true;
        addTags = false;

        try {
            mCommentJSON.put("Email", comment.getmEmail());
            mCommentJSON.put("CommentBody", comment.getmCommentBody());
            mCommentJSON.put("CommentDateTime", comment.getmCommentDateTime());
            mCommentJSON.put("PostID", comment.getmPostID());
            new AddPostAsyncTask().execute(url.toString());
        } catch (JSONException e) {
            Toast.makeText(this, "Error with JSON creation on adding a comment: "
                    + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uses parent fields to set up json to delete a post
     */
    public void deletePost() {

        StringBuilder url = new StringBuilder(getString(R.string.deletepost));

        deleteJSON = new JSONObject();
        try {
            deleteJSON.put("PostID", deletePostID);
            new AddPostAsyncTask().execute(url.toString());
        }
        catch (JSONException e) {
            Toast.makeText(this, "Error with JSON creation on post deletion: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Uses parent fields to set up json to delete a Comment
     */
    public void deleteComment() {

        StringBuilder url = new StringBuilder(getString(R.string.deletecomment));

        deleteJSON = new JSONObject();
        try {
            deleteJSON.put("CommentID", deleteCommentID);
            new AddPostAsyncTask().execute(url.toString());
        }
        catch (JSONException e) {
            Toast.makeText(this, "Error with JSON creation on comment deletion: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        //Refresh the fragment so that we don't see the deleted comment anymore
        PostDetailFragment thisFrag = (PostDetailFragment) getSupportFragmentManager().findFragmentByTag("detail");
        thisFrag.refresh();
    }

    /**
     * Launches the search activity, using whatever text is in the search bar.
     */
    public void launchSearchActivity(String theQuery) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(SearchActivity.SEARCH_QUERY, theQuery);
        startActivity(intent);
    }

    /**
     * Adds the post to the database. Also will be used to add comments.
     */
    private class AddPostAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setDoOutput(true);
                    OutputStreamWriter wr =
                            new OutputStreamWriter(urlConnection.getOutputStream());

                    boolean deleting = false;
                    if (url.contains("delete")) {
                        Log.i("DELETEPOSTCOMMENT", ""+deleteCommentID);
                        wr.write(deleteJSON.toString());
                        deleting = true;
                    }

                    if (!deleting) {
                        if (writeComment) {
                            Log.i("ADD_COMMENT", mCommentJSON.toString());
                            wr.write(mCommentJSON.toString());
                        } else {
                            if (addTags) {
                                Log.i("ADD_TAG", mTagJSON.get(tagsProcessed).toString());
                                wr.write(mTagJSON.get(tagsProcessed++).toString());
                            } else {
                                Log.i(ADD_POST, mPostJSON.toString());
                                wr.write(mPostJSON.toString());
                            }
                        }
                    }
                    wr.flush();
                    wr.close();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                        Log.v("WEBRESPONSE:", s);
                    }

                } catch (Exception e) {
                    if (writeComment) response = "Unable to add the new comment, Reason: " + e.getMessage();

                    else response = "Unable to add the new post, Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        /**
         * Response handler determining whether adding the post is successful.
         *
         * @param s The server response to the addPost POST request
         */
        @Override
        protected void onPostExecute(String s) {
            if (s.startsWith("Unable to add the new post")) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                return;
            }
            else if (s.startsWith("Unable to add the new comment")) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getBoolean("success")) {

                    if (jsonObject.has("deleteComment") || jsonObject.has("deletePost")) {
                        Log.v("POSTCOMMENTDELETE", s);
                        return;
                    }
                    if (addTags) {
                        Log.v("TAGADDRESPONSE", s);
                        return;
                    }

                        //Post adding: since we successfully added the post, extract the post id returned
                        //So we can add any relevant tags to the post!
                        if (!s.contains("delete")) {

                            JSONObject data = jsonObject.getJSONObject("postid");
                            int postid = data.getInt("postid");
                            int counter = 0;
                            for (String tagName : selectedTags) {
                                Log.v("TAGADD", " " + counter++);
                                commitTag(tagName, postid);
                            }
                            lastTag = true;
                        }
                }
            } catch (JSONException e) {
                String c;
                if (writeComment) c = "comment";
                else c = "post";
                Log.e("DETAILWEBERROR", e.getMessage());
            }
        }
    }

    /** Inner class that handles the Delete Post Confirm Dialog. */
    public static class DeletePostConfirmDialog extends DialogFragment {
        /** Post that is to be deleted. */
        Post mPost;
        /** Parent Activity. */
        PostDetailActivity parent;

        /** Constructor for this class. */
        public DeletePostConfirmDialog(Post p, PostDetailActivity par){
            super();
            mPost = p;
            parent = par;
        }

        /**
         * Creates the dialog object.
         * @param savedInstanceState
         * @return
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            build.setMessage("Are you sure you want to delete the post?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            parent.deletePostID = mPost.getmPostID();
                            parent.deletePost();
                            parent.finish();

                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
            return build.create();
        }
    }

    /** Inner class that handles the Delete Comment Confirm Dialog. */
    public static class DeleteCommentConfirmDialog extends DialogFragment {

        /** Comment that is to be deleted. */
        Comment mComment;
        /** Parent Activity. */
        PostDetailActivity parent;

        /** Constructor for this class. */
        public DeleteCommentConfirmDialog(Comment c, PostDetailActivity par){
            super();
            mComment = c;
            parent = par;
        }

        /**
         * Creates the dialog object.
         * @param savedInstanceState
         * @return
         */
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            build.setMessage("Are you sure you want to delete the comment?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            parent.deleteCommentID = mComment.getmCommentID();
                            parent.deleteComment();

                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            });
            return build.create();
        }
    }

    /**
     * Creates a menu for the toolbar.
     * @param menu , Menu item.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.postdetail_menu, menu);
        MenuItem sharepost = menu.findItem(R.id.share_post);

        if (enableShareOption) {
            if (sharepost != null)
                sharepost.setVisible(true);
        }
        return true;
    }

    /**
    Logout and post share handler
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS),
                    Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(getString(R.string.LOGGEDIN), false).commit();
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
        else if (item.getItemId() == R.id.share_post) {

            PostDetailFragment thisFrag = (PostDetailFragment) getSupportFragmentManager().findFragmentByTag("detail");
            Post p = thisFrag.getPost();

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, p.getmDisplayName() + ":\n"+p.getmPostBody()+"\n\nvia Gossamer");
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}
