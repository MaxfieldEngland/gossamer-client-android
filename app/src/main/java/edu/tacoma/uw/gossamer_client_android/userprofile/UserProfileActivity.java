/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.userprofile;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.authenticate.SignInActivity;
import edu.tacoma.uw.gossamer_client_android.home.PostDetailActivity;
import edu.tacoma.uw.gossamer_client_android.home.PostDetailFragment;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;
import edu.tacoma.uw.gossamer_client_android.home.model.Tag;

/**
 * Describes the user profile. Displays users previous posts, and editable profile
 * description.
 */
public class UserProfileActivity extends AppCompatActivity {

    /** List of Post objects to be added to the feed. */
    private List<Post> mPostList;
    /** List of user tags. */
    private ArrayList<Tag> mUserTags;
    /** List of Tags. */
    private ArrayList<Tag> mTagList;
    /** List of tags the user selected. */
    protected ArrayList<String> mSelectedTags;
    /** List of TagIds. */
    protected ArrayList<String> mTagIDs;
    /** List of Tag JSON objects. */
    private ArrayList<JSONObject> mTagJSON;
    /** Recycler view object to hold the Post. */
    private RecyclerView mRecyclerView;
    /** Email of user. */
    private String mUserEmail;
    /** Display name of user. */
    private String mUser;
    /** Description of profile. */
    private String mProfileDescription;
    /** EditText view to display profile description. */
    private EditText mDescriptionEditText;
    /** Done button. */
    private Button mEditButton;
    /** Tag button. */
    private Button mTagButton;
    /** Required for POST. */
    private JSONObject mProfileJSON;
    /** Layout for the tag container. */
    private LinearLayout mTagContainer;
    /** Flag value adding tags. */
    private boolean mAddTags = false;
    /** Number of tags that have been processed. */
    private int mTagsProcessed = 0;

    /**
     * Default onCreate method. Provides functionality for to the UserProfile Activity.
     * @param savedInstanceState , saved state.
     */
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

        //Sets users name.
        TextView userName = findViewById(R.id.user_profile_name);
        userName.setText(mUser);

        //Profile Description EditText.
        mDescriptionEditText = findViewById(R.id.user_profile_abt_me);
        mDescriptionEditText.setEnabled(false);

        // Done button - user presses they finish editing their profile.
        mEditButton = findViewById(R.id.edit_done);
        mEditButton.setVisibility(View.GONE);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileDescription = mDescriptionEditText.getText().toString();
                addProfile(mUser, mProfileDescription, mUserEmail);
                mDescriptionEditText.setEnabled(false);
                mEditButton.setVisibility(View.GONE);
                mTagButton.setVisibility(View.GONE);

                //TODO - We need to refresh this activity after we click the done button
                // so that the new tag appears on the profile immediately.
            }
        });

        //Tag button to select tags.
        mTagButton = findViewById(R.id.launchProfileAddTagsFragmentButton);
        mTagButton.setVisibility(View.GONE);

        //Launches tag selection fragment.
        mTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProfileAddTagsFragment frag = new ProfileAddTagsFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.linearLayout3, frag)
                        .addToBackStack(null)
                        .commit();

                //Hide buttons.
                mEditButton.setVisibility(View.GONE);
                mTagButton.setVisibility(View.GONE);
            }
        });

        //Set up the recycler view for the users posts.
        mRecyclerView = findViewById(R.id.user_profile_RecyclerView);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new UserProfileActivity.VerticalSpaceItem(24));
        setupRecyclerView((RecyclerView) mRecyclerView);

        //Set up member  variables.
        mTagJSON = new ArrayList<>();
        mSelectedTags = new ArrayList<>();
        mTagContainer = (LinearLayout) findViewById(R.id.profile_tagContainer);
        mTagList = new ArrayList<>();
        mUserTags = new ArrayList<>();


    }

    /** Retrieves the user posts when this activity is resumed. */
    @Override
    protected void onResume() {
        super.onResume();
        mTagIDs = new ArrayList<>();
        new PostsTask().execute(getString(R.string.taglist));
        new PostsTask().execute(getString(R.string.getuserposts) + "?Email=" + mUserEmail);
        new PostsTask().execute(getString(R.string.getprofile) + "?Email=" + mUserEmail);
        new PostsTask().execute(getString(R.string.getuserstags) + "?Email=" + mUserEmail);
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
     * Used to retrieve user posts, profile description, and user tags.
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
         * Creates a Get request to retrieve the user's posts, profile description, and user tags
         * from the database.
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
                    // Get Users Posts.
                    if (jsonObject.has("posts")) {
                        mPostList = Post.parsePostJson(jsonObject.getString("posts"));
                        new PostsTask().execute(getString(R.string.getposttags));
                    }
                    // Get users profile description.
                    else if (jsonObject.has("profData")) {
                        mDescriptionEditText.setText(new JSONObject(jsonObject.getString("profData"))
                                .getString("profiledescription"));
                    }
                    // Get tags associated with users posts.
                    else if (jsonObject.has("tags")) {
                        Tag.parseTagJson(mPostList, jsonObject.getString("tags"));
                        if (!mPostList. isEmpty()) {
                            setupRecyclerView((RecyclerView) mRecyclerView);
                        }
                    }
                    // Get list of available tags for tag selection view.
                    else if (jsonObject.has("tagnames")) {
                        mTagIDs = Tag.parseTagIDJson(jsonObject.getString("tagnames"));
                    }
                    // Get list of users profile tags.
                    else if (jsonObject.has("taglist")) {
                        mTagList = Tag.parseTagJson(jsonObject.getString("taglist"));
                        displayUserTags();
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

                    if (url.contains("delete")){
                        Log.v("DELETETAGS", "Deleting tags from email " + mUserEmail);
                        wr.write(new JSONObject().put("email", mUserEmail).toString());
                    }
                    else {

                        if (mAddTags) {
                            wr.write(mTagJSON.get(mTagsProcessed++).toString());
                        } else {
                            wr.write(mProfileJSON.toString());
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
                    response = "Unable to add the new profile data, Reason: " + e.getMessage();
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
                if (jsonObject.getBoolean("success") && jsonObject.has("changedProfile")) {

                    new AddProfileAsyncTask().execute(getString(R.string.deleteusertags));
                    // Create new Tags based on tags selected by users.
                    for (String tagName : mSelectedTags) {
                        addUserTags(mUserEmail, tagName);
                    }
                    finish();
                    startActivity(getIntent());
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

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Post item = (Post) view.getTag();

                    Context context = view.getContext();
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra(PostDetailFragment.ARG_ITEM_ID, item);
                    context.startActivity(intent);
            }
        };


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

            LinearLayout tagContainer = (LinearLayout) mParentActivity.findViewById(R.id.profile_tagContainer);
            ArrayList<Tag> tags = mValues.get(position).getTags();
            LinearLayout.LayoutParams tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tagLayout.setMargins(0, 0, 10, 0);

            for (Tag tag : tags) {
                Button tagButton;
                tagButton = new Button(mParentActivity);
                tagButton.setText(tag.getName());
                tagButton.setTextSize(10);
                tagButton.setMinHeight(10);
                tagButton.setMinimumHeight(10);
                tagButton.setMinWidth(100);
                tagButton.setMinimumWidth(200);
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
                //tagContainer.addView(tagButton, tagLayout);
                holder.mTagContainer.addView(tagButton, tagLayout);
            }
            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);

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
            final LinearLayout mTagContainer;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.userprofile_id_text);
                mTagContainer = (LinearLayout) view.findViewById(R.id.user_tagContainer);
                mContentView = (TextView) view.findViewById(R.id.userprofile_content);
            }
        }
    }

    /**
     * Required to modify view of card view.
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

        SharedPreferences pref = getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);

        String email = pref.getString(getString(R.string.EMAIL), null);
        boolean isProfileMaster = mUserEmail.equals(email) || pref.getBoolean(getString(R.string.isAdmin), false);

        if (!isProfileMaster) {
            MenuItem item = menu.findItem(R.id.edit_userprofile);
            item.setVisible(false);
        }
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
            mDescriptionEditText.setEnabled(true);
            mEditButton.setVisibility(View.VISIBLE);
            mTagButton.setVisibility(View.VISIBLE);
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

    /**
     * Creates layout for profile Tags and adds to profile.
     */
    private void displayUserTags() {

        //TODO - Remove once we get the appropriate tag colors from the database.
        for (Tag t : mTagList) {
            mUserTags.add(t);
        }

        LinearLayout.LayoutParams tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tagLayout.setMargins(0, 0, 10, 0);

        for (Tag tag : mUserTags) {
            Button tagButton;
            tagButton = new Button(this);
            tagButton.setText(tag.getName());
            tagButton.setTextSize(10);
            tagButton.setMinHeight(10);
            tagButton.setMinimumHeight(10);
            tagButton.setMinWidth(100);
            tagButton.setMinimumWidth(200);
            //Adding some graphical features that are build version dependent:
            //Get rid of the tag button shadows by getting rid of the state list animator
            if (Build.VERSION.SDK_INT >= 21) tagButton.setStateListAnimator(null);

            //Change the shape to be more capsule or rounded rectangle.
            if (Build.VERSION.SDK_INT >= 16) {
                GradientDrawable tagShape = new GradientDrawable();
                tagShape.setCornerRadius(100);
                tagShape.setColor(Color.parseColor(tag.getColor()));
                tagButton.setBackground(tagShape);
            } else {
                tagButton.setBackgroundColor(Color.parseColor(tag.getColor()));
            }
            mTagContainer.addView(tagButton, tagLayout);
        }
    }

    /**
     * Builds JSON object for a Tag and sends it to the users profile.
     * @param email , users email.
     * @param tagid , the tag id.
     */
    public void addUserTags(String email, String tagid) {
        StringBuilder url = new StringBuilder(getString(R.string.adduserstags));
        JSONObject tagJSON = new JSONObject();
        mAddTags = true;

        try {
            tagJSON.put("email", email);
            tagJSON.put("tagid", tagid);
            mTagJSON.add(tagJSON);
            new AddProfileAsyncTask().execute(url.toString());
        } catch (JSONException e) {
            Toast.makeText(this, "Error with JSON creation for profile tags" + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}