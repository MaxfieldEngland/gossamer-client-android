/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.home;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
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

    /**
     * Constant required for adding a post
     */
    public static final String ADD_POST = "ADD_POST";

    /**
     * Constant required for receiving tag list
     */
    public static final String TAG_LIST = "TAG_LIST";

    /**
     * Member variable for a JSON Post object.
     */
    private JSONObject mPostJSON;
    private ArrayList<JSONObject> mTagJSON;
    private JSONObject mCommentJSON;
    private boolean writeComment = false;
    private boolean addTags = false;
    private boolean lastTag = false;

    private int tagsProcessed = 0;

    public ArrayList<String> tagList;
    public ArrayList<String> selectedTags;

    /**
     * Default onCreate view required to instantiating the Post Detail layout,
     * and inflating associated fragment.
     *
     * @param savedInstanceState     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        selectedTags = new ArrayList<String>();
        mTagJSON = new ArrayList<JSONObject>();

//        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
//        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don"t need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
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
                        .add(R.id.post_detail_container, fragment)
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
     * Allows user to return to previous activity.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, PostListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends a JSON post object which is sent to the database.
     *
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

                    if (writeComment) {
                        Log.i("ADD_COMMENT", mCommentJSON.toString());
                        wr.write(mCommentJSON.toString());
                    }
                    else {
                        if (addTags) {
                            Log.i("ADD_TAG", mTagJSON.get(tagsProcessed).toString());
                            wr.write(mTagJSON.get(tagsProcessed++).toString());
                        } else {
                            Log.i(ADD_POST, mPostJSON.toString());
                            wr.write(mPostJSON.toString());
                        }
                    }
                    wr.flush();
                    wr.close();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
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

                    if (addTags){
                        Log.v("TAGADDRESPONSE", s);
                        return;
                    }

                    String t;
                    if (writeComment) t = "Comment added!";

                    //Post adding: since we successfully added the post, extract the post id returned
                    //So we can add any relevant tags to the post!
                    else {
                        t = "Post added!";

                        //TODO: Look here if it doesn't work. PostID is nested, so we look inside the returned object here:
                        //"postid": {
                            //"postid" :23
                        //}
                        JSONObject data = jsonObject.getJSONObject("postid");
                        int postid = data.getInt("postid");
                        int counter = 0;
                        for (String tagName : selectedTags) {
                            Log.v("TAGADD", " " + counter++);
                            commitTag(tagName, postid);
                        }
                        lastTag = true;
                    }

                    Toast.makeText(getApplicationContext(), t,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                String c;
                if (writeComment) c = "comment";
                else c = "post";
                Toast.makeText(getApplicationContext(), "JSON Parsing error on adding " + c + " "
                        + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

}