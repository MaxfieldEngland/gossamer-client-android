package edu.tacoma.uw.gossamer_client_android;

import org.json.JSONException;
import org.junit.Test;


import java.util.List;

import static org.junit.Assert.*;
import edu.tacoma.uw.gossamer_client_android.home.model.Comment;

public class CommentTest {

    @Test
    public void testOverloadedCommentConstructor() {
        assertNotNull(new Comment("test@uw.edu", "User"
                , "This is the body", "10/20/2020"
                , 3, 3));
    }

    @Test
    public void testCommentConstructor() {
        assertNotNull(new Comment("test@uw.edu", "This is the body"
                , "10/20/2020", 9));
    }

    @Test
    public void testDateTime() {
        //Test correct dateTime
        assertEquals((new Comment("test@uw.edu", "This is the body"
                , "2020-08-04 12:01:24", 9).dateTime())
                , "2020-08-04  12:01:24");
        //Test bad date
        assertNotEquals((new Comment("test@uw.edu", "This is the body"
                        , "2020-08-x0 12:01:24", 9).dateTime())
                , "2020-08-04  12:01:24");
    }

    /*

    NOT TESTING METHODS THAT REQUIRE JSON.

    @Test
    public void testparseCommentJson() throws JSONException {
        Comment testComment = new Comment("test@uw.edu", "User"
                , "This is the body", "2020-08-0 12:01:24"
                , 3, 3);

        String s = "[{\"email\":\"test2@uw.edu\",\"commentbody\":\"Hello!" +
                " I\'m making a comment on your post!\",\"commentdatetime\":\"" +
                "2020-08-04T12:01:24.000Z\",\"displayname\":\"Another test user\",\"" +
                "postid\":1,\"commentid\":1}]";

        List<Comment> test = Comment.parseCommentJson(s);
        List<Comment> test = Comment.parseCommentJson("[{email:test2@uw.edu, commentbody:Hello! Im making a comment on your post!, commentdatetime:2020-08-04T12:01:24.000Z, displayname:Another test user,postid:1,commentid:1}]");

        Comment testMethodComment = test.get(0);

        //parseCommentJson method works correctly on correct input.
        assertEquals(testComment.getmEmail(), testMethodComment.getmEmail());
        assertEquals(testComment.getmCommentBody(), testMethodComment.getmCommentBody());
        assertEquals(testComment.getmCommentDateTime(), testMethodComment.getmCommentDateTime());
        assertEquals(testComment.getmPostID(), testMethodComment.getmPostID());
        assertEquals(testComment.getmCommentID(), testMethodComment.getmCommentID());
    }
     */
}
