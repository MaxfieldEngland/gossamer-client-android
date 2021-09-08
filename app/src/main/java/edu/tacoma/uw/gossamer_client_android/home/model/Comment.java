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

    /**Represents comment email in JSON parsing and putExtra */
    public static final String EMAIL = "email";
    /**Represents display name in parsing and putExtra */
    public static final String DISPLAY_NAME = "displayname";
    /**Represents comment body in JSON parsing */
    public static final String COMMENT_BODY = "commentbody";
    /**Represents post' date time in JSON parsing */
    public static final String COMMENT_DATE_TIME = "commentdatetime";
    /**Represents post ID in JSON parsing and putextra */
    public static final String POST_ID = "postid";
    /**Represents pronouns of user in JSON parsing */
    public static final String PRONOUNS =  "pronouns";
    /**Represents comment ID in JSON parsing */
    public static final String COMMENT_ID = "commentid";
    /**Represents comment body in JSON post */
    public static final String COMMENT_BODY_CAP = "CommentBody";
    /**Represents comment date time in JSON post */
    public static final String COMMENT_DATE_TIME_CAP = "CommentDateTime";
    /**Represents comment id in JSON post */
    public static final String COMMENT_ID_CAP = "CommentID";

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
    /** The ID of the comment. */
    private int mCommentID;
    /** The pronouns of the commenter */
    private String mCommentPronouns;

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

    public int getmCommentID() {
        return mCommentID;
    }

    public String getmCommentPronouns() {
        return mCommentPronouns;
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
     * @param commentID The ID of the comment itself, used to identify the comment for deletion.
     */
    public Comment(String email, String displayName, String commentBody, String commentDateTime,
                   int postID, int commentID, String pronouns) {

        mEmail = email;
        mDisplayName = displayName;
        mCommentBody = commentBody;
        mCommentDateTime = commentDateTime;
        mPostID = postID;
        mCommentID = commentID;
        mCommentPronouns = pronouns;
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

    /**
     * Returns the date and time of this particular comment.
     * @return String , Date and Time.
     */
    public String dateTime() {
        String date = "";
        String time = "";
        if (mCommentDateTime != null && mCommentDateTime.length() >= 19) {
            date = this.mCommentDateTime.substring(0, 10);
            time = this.mCommentDateTime.substring(11, 16);
        } else {
            throw new IllegalArgumentException("DateTime must be valid");
        }
        return date + "  " + time;
    }

    /**
     * Receives a JSON object that contains comments and related information and converts it into
     * a list of Comment objects.
     * @param commentJson post information stored in the database.
     * @return commentList, a list of Comment objects.
     * @throws JSONException exception
     */
    public static List<Comment> parseCommentJson(String commentJson) throws JSONException {

        List<Comment> commentList = new ArrayList<>();
        if (commentJson != null) {
            JSONArray arr = new JSONArray(commentJson);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Comment c = new Comment(obj.getString(EMAIL), obj.getString(DISPLAY_NAME)
                        , obj.getString(COMMENT_BODY), obj.getString(COMMENT_DATE_TIME)
                        , obj.getInt(POST_ID), obj.getInt(COMMENT_ID), obj.getString(PRONOUNS));
                commentList.add(c);
            }
        }

        return commentList;
    }
}
