package edu.tacoma.uw.gossamer_client_android.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

/**
 * An activity representing a list of Posts. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PostListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private List<Post> mPostList;
    private RecyclerView mRecyclerView;

    //TODO: Match references to layout elements with elements created in the actual layout.
    // Layout details here match the courses details from the lab, and do not reflect the current
    // project at all.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());

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

        mRecyclerView = findViewById(R.id.post_list);
        assert mRecyclerView != null;
        setupRecyclerView((RecyclerView) mRecyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new PostsTask().execute(getString(R.string.posts));
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView){
        if (mPostList != null) {
            mRecyclerView.setAdapter(new SimpleItemRecyclerViewAdapter
                    (this, mPostList, mTwoPane));
        }
    }

    private void launchPostAddFragment() {
        PostAddFragment postAddFragment = new PostAddFragment();
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.post_detail_container, postAddFragment).commit();
        } else {
            Intent intent = new Intent(this, PostDetailActivity.class);
            //intent.putExtra(PostDetailActivity.ADD_POST, true);
            startActivity(intent);
        }
    }

    private class PostsTask extends AsyncTask<String, Void, String> {

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
                    response = "Unable to download posts; Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s){
            if (s.startsWith("Unable to")) {
                Toast.makeText(getApplicationContext(), "Unable to download" + s,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getBoolean("success")) {
                    mPostList = Post.parsePostJson(
                            jsonObject.getString("posts"));

                    if (!mPostList.isEmpty()) {
                        setupRecyclerView((RecyclerView) mRecyclerView);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "JSON Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final PostListActivity mParentActivity;
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
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra(PostDetailFragment.ARG_ITEM_ID, item); //TODO: This one's mad :(

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

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_list_content, parent, false);
            return new ViewHolder(view);
        }

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
            holder.itemView.setOnClickListener(mOnClickListener);
        }



        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}