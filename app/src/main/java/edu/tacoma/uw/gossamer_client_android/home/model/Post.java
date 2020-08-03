/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.home.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Post object, of every shareable text submission from users of Gossamer.
 * Includes the displayname, in the case that the post is viewable by users; the email (hidden from users)
 * associated with the post, the date and time that it was published, the text itself, and whether
 * the post should be published anonymously.
 */
public class Post implements Serializable {

    /** Email of the user.  */
    private String mEmail;
    /** Display Name of user. */
    private String mDisplayName;
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
    public String getmDisplayName() {
        return mDisplayName;
    }
    public String getmPostDateTime() {
        return mPostDateTime;
    }
    public boolean mIsAnonymous() {
        return mIsAnonymous;
    }


    /**
     * Constructor used for reading posts. Displayname is retrieved from the server to be displayed
     * on posts, while email serves as the user primary key, and does not need to be displayed.
     *
     * @param email The email address of the user who wrote the given post.
     * @param postBody The body of text included in the post.
     * @param postDateTime The time and date that the post was published.
     * @param isAnonymous Whether or not the post is to be displayed anonymously.
     * @param displayName The profile name of the user who displays the post.
     */
    public Post(String email, String postBody, String postDateTime, boolean isAnonymous, String displayName) {
        mEmail = email;
        mPostBody = postBody;
        mPostDateTime = postDateTime.substring(0, 10);
        mIsAnonymous = isAnonymous;
        mDisplayName = displayName;
    }

    /**
     * Constructor used for adding new posts: we don't care about the displayname because the server
     * doesnt post this.
     * @param email The email address of the account making the post.
     * @param postBody The body of text included in the post.
     * @param postDateTime The date and time that the post is submitted.
     * @param isAnonymous Whether the post is set to be displayed anonymously or not.
     */
    public Post(String email, String postBody, String postDateTime, boolean isAnonymous) {
        mEmail = email;
        mPostBody = postBody;
        mPostDateTime = postDateTime;
        mIsAnonymous = isAnonymous;
        mDisplayName = "WriteMode";
    }

    /**
     * Receives a JSON object that contains posts and related information and converts it into
     * a list of Post objects.
     * @param postJson post information stored in the database.
     * @return postList, a list of Post objects.
     * @throws JSONException exception
     */
    public static List<Post> parsePostJson(String postJson) throws JSONException {
        List<Post> postList = new ArrayList<>();
        if (postJson != null) {
            JSONArray arr = new JSONArray(postJson);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Post post = new Post(obj.getString("email"), obj.getString("postbody"),
                        obj.getString("postdatetime"), obj.getBoolean("isanonymous"), obj.getString("displayname"));
                postList.add(post);
            }
        }
        return postList;
    }

}
