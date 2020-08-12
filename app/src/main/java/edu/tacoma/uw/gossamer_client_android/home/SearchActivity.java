package edu.tacoma.uw.gossamer_client_android.home;

import android.app.Activity;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.authenticate.SignInActivity;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;
import edu.tacoma.uw.gossamer_client_android.home.model.Tag;
import edu.tacoma.uw.gossamer_client_android.userprofile.UserProfileActivity;

public class SearchActivity extends AppCompatActivity {

    /** Whether or not the activity is in two-pane mode, i.e. running on a tablet device. */
    private boolean mTwoPane;
    /** A list of Post objects to be added to the feed. */
    private List<Post> mPostList;
    /** Recycler view object to hold the Post. */
    private RecyclerView mRecyclerView;

    /** The actual query received to use to search to filter posts/profiles */
    private String searchQuery;

    /**Key for putExtra to give us the query. */
    public static final String SEARCH_QUERY = "SEARCH_QUERY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.app_image_gossamer);

        searchQuery = getIntent().getStringExtra(SEARCH_QUERY);
        setTitle("Search: " + searchQuery);

        //CollapsingToolbarLayout toolbar = findViewById(R.id.toolbar_layout);
        //toolbar.setTitle("Search - " + searchQuery);

        if (findViewById(R.id.post_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        mRecyclerView = findViewById(R.id.post_list);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new PostListActivity.VerticalSpaceItem(24));
        setupRecyclerView((RecyclerView) mRecyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Determine whether we're in profile search mode or post search mode
        //if ()
        new SearchTask().execute(getString(R.string.postsearch) + "?searchterm=" + searchQuery);
    }

    /**
     * Sets up the recyclerview for the Posts.
     * @param recyclerView
     */
    private void setupRecyclerView(@NonNull RecyclerView recyclerView){
        if (mPostList != null) {
            mRecyclerView.setAdapter(new SimpleItemRecyclerViewAdapter
                    (this, mPostList, mTwoPane));
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


    private class SearchTask extends AsyncTask<String, Void, String> {

        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.search_progressB);

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
                    response = "Unable to download; Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
            mProgressBar.setProgress(10);
            mProgressBar.setBackgroundColor(Color.BLACK);

        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.GONE);

            if (s.startsWith("Unable to")) {
                Toast.makeText(getApplicationContext(), "Unable to download" + s,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getBoolean("success")) {

                    if (jsonObject.has("posts")) {

                        mPostList = Post.parsePostJson(jsonObject.getString("posts"));
                        new SearchTask().execute(getString(R.string.getposttags));

                    }
                    if (jsonObject.has("tags")) {

                        Tag.parseTagJson(mPostList, jsonObject.getString("tags"));
                        if (!mPostList.isEmpty()) {
                            setupRecyclerView((RecyclerView) mRecyclerView);
                        }
                        else {
                            mPostList.add(new Post("NULL", "No search results matched that query.", "             ", false, "Oops!", -1));
                            setupRecyclerView((RecyclerView) mRecyclerView);
                        }

                    }
                    //Searching profiles
                    else {

                    }



                }
            }
            catch (JSONException e) {
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

        private final SearchActivity mParentActivity;
        private final List<Post> mValues;
        private final boolean mTwoPane;

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

        SimpleItemRecyclerViewAdapter(SearchActivity parent,
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
        public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
                        intent.putExtra("email", e);
                        intent.putExtra("username", u);
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

            /**
             * Makes a new post search based on the text currently in the search bar
             *
             * @param query The newly entered search term
             * @return Whether we handle the submission via unique behavior (we do)
             */
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchQuery = query;
                setTitle("Search: " + searchQuery);

                //searchQuery = ((SearchView) findViewById(R.id.app_bar_search)).getQuery().toString();
                //new SearchTask().execute(getString(R.string.postsearch) + "?searchterm=" + searchQuery);

                launchSearchActivity(query);


                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
        }
        return super.onOptionsItemSelected(item);
    }

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


}
