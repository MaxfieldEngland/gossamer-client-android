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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.authenticate.SignInActivity;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;
import edu.tacoma.uw.gossamer_client_android.home.model.Tag;
import edu.tacoma.uw.gossamer_client_android.userprofile.UserProfileActivity;

/**
 * An activity representing a list of Posts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 *
 * @author elijah freeman
 * @author maxfield england
 */
public class PostListActivity extends AppCompatActivity {

    /** Whether or not the activity is in two-pane mode, i.e. running on a tablet device. */
    private boolean mTwoPane;
    /** A list of Post objects to be added to the feed. */
    private List<Post> mPostList;
    /** Recycler view object to hold the Post. */
    private RecyclerView mRecyclerView;
    /** Daily message details. */
    private String dmTopic, dmBody = ""; //dmLink
    /** The array of downloaded string IDs. */
    public ArrayList<String> tagIDs;

    /**
     *  Default onCreate method. Provides functionality to the
     *  addPostFragment.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setIcon(R.mipmap.app_image_gossamer);
        setTitle("Gossamer");



        CollapsingToolbarLayout toolbar =  findViewById(R.id.toolbar_layout);
        toolbar.setTitle("");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPostAddFragment();
            }
        });

        if (findViewById(R.id.post_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        new PostsTask().execute(getString(R.string.dailymessage));
        mRecyclerView = findViewById(R.id.post_list);
        mRecyclerView.addItemDecoration(new VerticalSpaceItem(24));
    }

    /** Retrieves the posts when this activity is resumed. */
    @Override
    protected void onResume() {
        super.onResume();

        tagIDs = new ArrayList<String>();
        new PostsTask().execute(getString(R.string.taglist));
        new PostsTask().execute(getString(R.string.posts));

        if (!dmBody.isEmpty() && !dmTopic.isEmpty()){
            ((TextView) findViewById(R.id.daily_message)).setText(dmBody);
            ((TextView) findViewById(R.id.daily_message_title)).setText("What does " + dmTopic + " mean?");
        }
    }

    /**
     * Sets up the recyclerview for the Posts.
     * @param recyclerView
     */
    private void setupRecyclerView(@NonNull RecyclerView recyclerView){
        mRecyclerView.setAdapter(new SimpleItemRecyclerViewAdapter
                (this, mPostList, mTwoPane));
    }

    /**
     * Launches the PostAddFragment layout.
     */
    private void launchPostAddFragment() {
        PostAddFragment postAddFragment = new PostAddFragment();
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.post_detail_container, postAddFragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.ADD_POST, true);

            intent.putExtra("TAG_LIST", tagIDs);
            startActivity(intent);
        }
    }

    /**
     * Launches the search activity, using whatever text is in the search bar.
     */
    private void launchSearchActivity(String theQuery) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(SearchActivity.SEARCH_QUERY, theQuery);
        startActivity(intent);
    }

    /**
     * PostListActivity web task handler class.
     */
    private class PostsTask extends AsyncTask<String, Void, String> {

        /** Loading bar to make the page less uncomfortable as posts are loaded. */
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.postList_progressB);

        /**
         * Default doInBackground method responsible for connecting to database.
         * @param urls
         * @return
         */
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
                    //Progress bar.
                    publishProgress();
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to download; Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        /**
         * Required for progress bar.
         * @param progress
         */
        @Override
        protected void onProgressUpdate(Void... progress) {
            mProgressBar.setProgress(10);
        }

        /**
         * Handles responses received from web service calls.
         * @param s The response
         */
        @Override
        protected void onPostExecute(String s){
            mProgressBar.setVisibility(View.GONE);

            if (s.startsWith("Unable to")) {
                Toast.makeText(getApplicationContext(), "Unable to download" + s,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            //If we succeeded, there are three behavior branches: taglist retrieval, retrieving posts,
            // and retrieving posttags
            try {
                JSONObject jsonObject = new JSONObject(s);

                if(jsonObject.getBoolean("success")) {

                    int state;

                    if (jsonObject.has("tagnames")) state = 0; //Get tag list state (init)
                    else if (jsonObject.has("posts")) state = 1; //Get all posts state
                    else if (jsonObject.has("tags")) state = 2; //Get all post tags state
                    else if (jsonObject.has("message")) state = 3;//Get daily message state
                    else state = -1;

                    switch (state) {

                        //Getting tag list
                        case 0:
                            tagIDs = Tag.parseTagIDJson(jsonObject.getString("tagnames"));
                            break;

                        //Getting post list, and then initializing posttags
                        case 1:
                            mPostList = Post.parsePostJson(jsonObject.getString("posts"));
                            new PostsTask().execute(getString(R.string.getposttags));

                            break;

                        //Getting posttags
                        case 2:
                            Tag.parseTagJson(mPostList, jsonObject.getString("tags"));
                            if (!mPostList.isEmpty()) {
                                setupRecyclerView((RecyclerView) mRecyclerView);
                            }
                            break;

                        //Getting daily message
                        case 3:
                            //Set our fields so we can reload them if possible
                            dmBody = jsonObject.getJSONObject("message").getString("messagebody");
                            dmTopic = jsonObject.getJSONObject("message").getString("messagetopic");

                            //Then make them appear properly in the app
                            ((TextView) findViewById(R.id.daily_message)).setText(dmBody);
                            ((TextView) findViewById(R.id.daily_message_title)).setText("What does " + dmTopic + " mean?");
                            break;

                        //We didn't get anything we wanted!
                        case -1:
                            Toast.makeText(getApplicationContext(), "Web retrieval error." +
                                            " Please reload the app and try again.",
                                    Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "JSON Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Allows views to be recycled.
     */
    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        /** Parent Activity. */
        private final PostListActivity mParentActivity;
        /** List of Posts. */
        private final List<Post> mValues;
        /** Required for two pane devices. */
        private final boolean mTwoPane;
        /** onClickListener that allows individual posts to be clickable. */
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Post item = (Post) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putSerializable(PostDetailFragment.ARG_ITEM_ID, item);
                    PostDetailFragment fragment = new PostDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.post_detail_container, fragment)
                            .commit();
                }
                else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra(PostDetailFragment.ARG_ITEM_ID, item);
                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(PostListActivity parent,
                                      List<Post> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        /**
         * Inflates the view for each post.
         * @param parent
         * @param viewType
         * @return
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_list_content, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Responsible for binding the ViewHolder.
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            if (!mValues.get(position).mIsAnonymous()) {
                holder.mIdView.setOnClickListener(new View.OnClickListener() {
                    Context context = holder.mIdView.getContext();

                    @Override
                    public void onClick(View view) {
                        String e = mValues.get(position).getmEmail();
                        String u = mValues.get(position).getmDisplayName();
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra(Post.EMAIL, e);
                        intent.putExtra(Post.DISPLAY_NAME, u);
                        context.startActivity(intent);
                    }
                });
            }

            //If not anonymous, show the displayname
            if (!mValues.get(position).mIsAnonymous())
                holder.mIdView.setText(mValues.get(position).getmDisplayName());
            //If anonymous, hide the displayname
            else
                holder.mIdView.setText("Anonymous");

            holder.mContentView.setText(mValues.get(position).getmPostBody());

            ArrayList<Tag> tags = mValues.get(position).getTags();
            //Use a hashset to make sure we don't duplicate tags
            HashSet<Tag> tagsContained = new HashSet<Tag>();
            LinearLayout.LayoutParams tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tagLayout.setMargins(0, 0, 10, 0);

            for (Tag tag : tags) {
                if (tagsContained.contains(tag)) continue;
                tagsContained.add(tag);
                final Button tagButton;
                tagButton = new Button(mParentActivity);
                tagButton.setText(tag.getName());
                tagButton.setTextSize(10);
                tagButton.setMinHeight(10);
                tagButton.setMinimumHeight(10);
                tagButton.setMinWidth(100);
                tagButton.setMinimumWidth(200);

                tagButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mParentActivity.launchSearchActivity(tagButton.getText().toString());
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
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mTagContainer = (LinearLayout) view.findViewById(R.id.tagContainer);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }

    /**
     * Creates a menu for the toolbar.
     * @param menu , Menu item.
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        final SearchView search = (SearchView) MenuItemCompat.getActionView(searchItem);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            /** Launches the Search Activity. */
            @Override
            public boolean onQueryTextSubmit(String query) {
                launchSearchActivity(query);
                return true;
            }
            /** Return true by default. */
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        return true;
    }

    /*
    This is responsible for the logout action.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.LOGIN_PREFS),
                Context.MODE_PRIVATE);

        if (item.getItemId() == R.id.action_logout) {
            sharedPreferences.edit().putBoolean(getString(R.string.LOGGEDIN), false).commit();
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }

        if (item.getItemId() == R.id.action_viewprofile) {
            String email = sharedPreferences.getString(getString(R.string.EMAIL), null);

            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(Post.EMAIL, email);
            //We plan on loading the displayname after we get to the profile
            intent.putExtra(Post.DISPLAY_NAME, " ");
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Required to enhance the layout of the recylerview.
     */
    public static class VerticalSpaceItem extends RecyclerView.ItemDecoration {
        private final int space;
        /** Default constructor. */
        public VerticalSpaceItem(int sp) {
            space = sp;
        }
        /** Returns rectangle separation distance. */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = space;
        }
    }
}