package edu.tacoma.uw.gossamer_client_android.home.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Comment object, of every associated user comment in response to a
 * given user post from users of Gossamer. Includes displayname, to attach names to the commenter;
 * the email (hidden from users) associated with the comment, the date and time it was published,
 * and the comment itself.
 */
public class Comment implements Serializable {

    /** Email of the user.  */
    private String mEmail;
    /** Display Name of the user.  */
    private String mDisplayName;
    /** The text that makes up the bulk of a comment.  */
    private String mCommentBody;
    /** The date and time that the comment was published.  */
    private String mCommentDateTime;
    /** The ID of the associated post. */
    private int mPostID;

    //Getters
    public String getmEmail() {
        return mEmail;
    }

    public String getmDisplayName() {
        return mDisplayName;
    }

    public String getmCommentBody() {
        return mCommentBody;
    }

    public String getmCommentDateTime() {
        return mCommentDateTime;
    }

    public int getmPostID() {
        return mPostID;
    }

    /**
     * Comment-reading constructor: Display name will be retrieved from the server, with the email serving
     * as the user foreign key, without being displayed.
     * @param email The email address of the user who wrote the given comment.
     * @param displayName The name the user sets to display alongside their content.
     * @param commentBody The bulk text of the comment.
     * @param commentDateTime The date and time that the comment was published.
     * @param postID The ID of the post associated with the given comment; under which post should
     *               the comment display?
     */
    public Comment(String email, String displayName, String commentBody, String commentDateTime,
                   int postID) {

        mEmail = email;
        mDisplayName = displayName;
        mCommentBody = commentBody;
        mCommentDateTime = commentDateTime;
        mPostID = postID;

    }

    /**
     * Comment-writing constructor: We don't need to gather the displayname, since the server already
     * knows which email comes with which displayname; why store who they were when they wrote it?
     */
    public Comment(String email, String commentBody, String commentDateTime, int postID) {
        mEmail = email;
        mCommentBody = commentBody;
        mCommentDateTime = commentDateTime;
        mPostID = postID;
    }

    public static List<Comment> parseCommentJson(String commentJson) throws JSONException {

        List<Comment> commentList = new ArrayList<>();
        if (commentJson != null) {
            JSONArray arr = new JSONArray(commentJson);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Comment c = new Comment(obj.getString("email"), obj.getString("displayname")
                        , obj.getString("commentbody"), obj.getString("commentdatetime")
                        , obj.getInt("postid"));
                commentList.add(c);
            }
        }
        Log.e("CommentsRead", commentList.toString());
        return commentList;
    }

}
