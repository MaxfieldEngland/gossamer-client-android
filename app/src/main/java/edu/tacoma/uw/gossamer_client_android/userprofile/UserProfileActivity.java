/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.userprofile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.authenticate.SignInActivity;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

/**
 * Describes the user profile. Displays users previous posts, and editable profile
 * description.
 */
public class UserProfileActivity extends AppCompatActivity {

    /** A list of Post objects to be added to the feed. */
    private List<Post> mPostList;
    /** Recycler view object to hold the Post. */
    private RecyclerView mRecyclerView;
    /** Email of user. */
    private String mUserEmail;
    /** Display name of user. */
    private String mUser;
    /** Description of profile. */
    private String profileDesc;
    /** EditText view to display profile description. */
    private EditText mAboutMe;
    /** Done button. */
    private Button mEditButton;
    /** Required for POST. */
    private JSONObject mProfileJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mUserEmail = (String) bundle.get("email");
            mUser = (String) bundle.get("username");
        }
        setTitle("Profile");

        TextView userName = findViewById(R.id.user_profile_name);
        userName.setText(mUser);

        mAboutMe = findViewById(R.id.user_profile_abt_me);
        mAboutMe.setEnabled(false);

        mEditButton = findViewById(R.id.edit_done);
        mEditButton.setVisibility(View.GONE);

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileDesc = mAboutMe.getText().toString();
                addProfile(mUser, profileDesc, mUserEmail);
                mAboutMe.setEnabled(false);
                mEditButton.setVisibility(View.GONE);
            }
        });

        mRecyclerView = findViewById(R.id.user_profile_RecyclerView);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new UserProfileActivity.VerticalSpaceItem(24));
        setupRecyclerView((RecyclerView) mRecyclerView);
    }

    /** Retrieves the user posts when this activity is resumed. */
    @Override
    protected void onResume() {
        super.onResume();
        new PostsTask().execute(getString(R.string.getuserposts) + "?Email=" + mUserEmail);
        new PostsTask().execute(getString(R.string.getprofile) + "?Email=" + mUserEmail);
    }

    /**
     * Sets up the recyclerview for the user's posts.
     * @param recyclerView
     */
    private void setupRecyclerView(@NonNull RecyclerView recyclerView){
        if (mPostList != null) {
            mRecyclerView.setAdapter(new SimpleItemRecyclerViewAdapter
                    (this, mPostList));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    /**
     * Used to retrieve user posts and profile description.
     */
    private class PostsTask extends AsyncTask<String, Void, String> {

        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.userprofile_progressB);

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
                    publishProgress();
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to download posts; Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        /**
         * Required for the progress bar.
         * @param progress
         */
        @Override
        protected void onProgressUpdate(Void... progress) {
            mProgressBar.setProgress(10);
            mProgressBar.setBackgroundColor(Color.BLACK);
        }

        /**
         * Creates a Get request to retrieve the user's posts from the database.
         * @param s
         */
        @Override
        protected void onPostExecute(String s){
            mProgressBar.setVisibility(View.GONE);

            if (s.startsWith("Unable to")) {
                Toast.makeText(getApplicationContext(), "Unable to download" + s,
                        Toast.LENGTH_LONG).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);

                if (jsonObject.getBoolean("success")) {

                    if (jsonObject.has("posts")) {
                        mPostList = Post.parsePostJson(jsonObject.getString("posts"));
                        if (!mPostList.isEmpty()) {
                            setupRecyclerView((RecyclerView) mRecyclerView);
                        }
                    } else if (jsonObject.has("profData")) {
                        mAboutMe.setText(new JSONObject(jsonObject.getString("profData"))
                                .getString("profiledescription"));
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "JSON Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Adds the post to the database. Also will be used to add comments.
     */
    private class AddProfileAsyncTask extends AsyncTask<String, Void, String> {

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


                    wr.write(mProfileJSON.toString());

                    wr.flush();
                    wr.close();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    response = "Unable to add the new profile description, Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        /**
         * Response handler determining whether adding the profile is successful.
         *
         * @param s The server response to the setProfile POST request
         */
        @Override
        protected void onPostExecute(String s) {
            if (s.startsWith("Unable to add")) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getBoolean("success")) {

                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "JSON Parsing error on adding profile"
                        + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Allows views to be recycled.
     */
    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final UserProfileActivity mParentActivity;
        private final List<Post> mValues;

        SimpleItemRecyclerViewAdapter(UserProfileActivity parent,
                                      List<Post> items) {
            mValues = items;
            mParentActivity = parent;
        }

        /**
         * Inflates the view for each posts.
         * @param parent
         * @param viewType
         * @return
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_post_list_content, parent, false);
            return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        /**
         * Responsible for binding the ViewHolder.
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            //If not anonymous, show the displayname
            if (!mValues.get(position).mIsAnonymous())
                holder.mIdView.setText(mValues.get(position).getmDisplayName());
                //If anonymous, hide the displayname
            else
                holder.mIdView.setText("Anonymous");

            holder.mContentView.setText(mValues.get(position).getmPostBody());
            holder.itemView.setTag(mValues.get(position));
        }

        /**
         * Returns the number of posts.
         * @return
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

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.userprofile_id_text);
                mContentView = (TextView) view.findViewById(R.id.userprofile_content);
            }
        }
    }

    /**
     * Required modify view of card view.
     */
    public static class VerticalSpaceItem extends RecyclerView.ItemDecoration {
        private final int space;

        public VerticalSpaceItem(int sp) {
            space = sp;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = space;
        }
    }

    /**
     * Creates a menu for the toolbar.
     * @param menu , Menu item.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.userprofile_menu, menu);
        return true;
    }

    /*
    This is responsible for the logout action.
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
        } else if (item.getItemId() == R.id.edit_userprofile) {
            mAboutMe.setEnabled(true);
            mEditButton.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates JSON object to send to database.
     * @param username ,display name of user.
     * @param profileDesc ,profile description of user.
     * @param useremail ,email of user.
     */
    public void addProfile(String username, String profileDesc, String useremail) {
        StringBuilder url = new StringBuilder(getString(R.string.setprofile));
        mProfileJSON = new JSONObject();
        try {
            mProfileJSON.put("displayname", username);
            mProfileJSON.put("profiledescription", profileDesc);
            mProfileJSON.put("email", useremail);
            new AddProfileAsyncTask().execute(url.toString());
        } catch (JSONException e) {
            Toast.makeText(this, "Error with JSON creation for profile" + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
        }
    }
}