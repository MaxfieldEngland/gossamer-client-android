package edu.tacoma.uw.gossamer_client_android.home.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Post {

    //TODO: We use email as key to get displayname of the user in the database.
    // This probably requires writing a specific webservice.
    private String mEmail;

    /** The text that makes up the bulk of a post.*/
    private String mPostBody;
    /** The date and time that the post was published.*/
    private String mPostDateTime;
    /** Whether or not the post was set to be anonymous (without their name attached).*/
    private boolean mIsAnonymous;

    public String getmEmail() {
        return mEmail;
    }
    public String getmPostBody() {
        return mPostBody;
    }
    public String getmPostDateTime() {
        return mPostDateTime;
    }
    public boolean ismIsAnonymous() {
        return mIsAnonymous;
    }




    public Post(String email, String postBody, String postDateTime, boolean isAnonymous) {

        mEmail = email;
        mPostBody = postBody;
        mPostDateTime = postDateTime;
        mIsAnonymous = isAnonymous;
    }

    public static List<Post> parsePostJson(String postJson) throws JSONException {
        List<Post> postList = new ArrayList<>();
        if (postJson != null) {
            JSONArray arr = new JSONArray(postJson);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Post post = new Post(obj.getString("email"), obj.getString("postbody"),
                        obj.getString("postdatetime"), obj.getBoolean("isanonymous"));
                postList.add(post);
            }
        }
        return postList;
    }

}
