package edu.tacoma.uw.gossamer_client_android.userprofile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.R;
import edu.tacoma.uw.gossamer_client_android.home.PostListActivity;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

public class UserProfileActivity extends AppCompatActivity {


    /** A list of Post objects to be added to the feed. */
    private List<Post> mPostList;
    /** Recycler view object to hold the Post. */
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);




        mRecyclerView = findViewById(R.id.user_profile_RecyclerView);


        assert mRecyclerView != null;

        mRecyclerView.addItemDecoration(new UserProfileActivity.VerticalSpaceItem(24));
        setupRecyclerView((RecyclerView) mRecyclerView);
    }




    /** Retrieves the posts when this activity is resumed. */
    @Override
    protected void onResume() {
        super.onResume();
        new PostsTask().execute(getString(R.string.posts));
    }

    /**
     * Sets up the recyclerview for the Posts.
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
     * Post loading task.
     */
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

        /**
         * Creates a Get request to retrieve the posts from the database.
         * @param s
         */
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