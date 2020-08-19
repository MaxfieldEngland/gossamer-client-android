package edu.tacoma.uw.gossamer_client_android;

import org.junit.Test;

import java.util.ArrayList;

import edu.tacoma.uw.gossamer_client_android.home.model.Post;
import edu.tacoma.uw.gossamer_client_android.home.model.Tag;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * Test class for Post. Does not test trivial getter/setter methods unless
 * methods perform validation. Does not test methods that require JSON objects/methods.
 */
public class PostTest {

    /** Test overloaded constructor. */
    @Test
    public void testOverloadedPostConstructor() {
        assertNotNull(new Post("test@uw.edu", "This is the body"
        , "10/20/2020", false, "User", 3));
    }

    /** Test constructor. */
    @Test
    public void testPostConstructor() {
        assertNotNull(new Post("test@uw.edu", "This is the body"
                , "10/20/2020", false));
    }

    /** Test good tag.  */
    @Test
    public void testAddTag() {
        Post testPost = new Post("test@uw.edu", "This is the body"
                , "10/20/2020", false);
        Tag testGoodTag = new Tag("testTag", "red");
        ArrayList<Tag> testList = new ArrayList<>();
        testList.add(testGoodTag);

        try {
            // Testing method performs validation.
            testPost.addTag(testGoodTag);
            //Testing contents of list.
            assertEquals(testPost.getTags(), testList);
        } catch (IllegalArgumentException e) {
            fail("Valid tag not added to Post tagList. " + e);
        }
    }

    /** Test bad tags. */
    @Test
    public void testAddBadTag() {
        Post testPost = new Post("test@uw.edu", "This is the body"
                , "10/20/2020", false);
        //Test empty string values.
        Tag testBadColorTag = new Tag("testTag", "");
        Tag testBadNameTag = new Tag("", "red");
        //Test null values.
        Tag testNullColorTag = new Tag("testTag", null);
        Tag testNullNameTag = new Tag(null, "red");

        try {
            testPost.addTag(testBadColorTag);
            testPost.addTag(testBadNameTag);
            testPost.addTag(testNullColorTag);
            testPost.addTag(testNullNameTag);
            fail("Non-valid tag added to Posts tag list");
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }
    }

    /**
     * Tests DateTime equality.
     */
    @Test
    public void testEqualityDateTime() {
        //Test correct dateTime
        assertEquals((new Post("test@uw.edu", "This is the body"
                        , "2020-08-04 12:01:24", false).dateTime())
                , "2020-08-04  12:01:24");
        //Test bad date
        assertNotEquals((new Post("test@uw.edu", "This is the body"
                        , "2020-08-x0 12:01:24", false).dateTime())
                , "2020-08-04  12:01:24");
    }

    /**
     * Tests validity of valid date time.
     */
    @Test
    public void testGoodDateTime() {
        Post testPost = new Post("test@uw.edu", "This is the body"
                , "2020-08-04 12:01:24", false);

        try {
            testPost.dateTime();
        } catch (IllegalArgumentException e) {
            fail("Valid dateTime failed");
        }
    }

    /**
     * Tests bad datetime length (validation check).
     */
    @Test
    public void testBadLengthDateTime() {
        Post testPost = new Post("test@uw.edu", "This is the body"
                , "2020-08-04 12:", false);

        try {
            testPost.dateTime();
            fail("Bad length for Post dateTime passed");
        } catch (IllegalArgumentException e) {
            System.out.println("Exception caught: " + e);
        }
    }

    /**
     * Tests null date time (validation check).
     */
    @Test
    public void testNullDateTime() {
        Post testPost = new Post("test@uw.edu", "This is the body"
                , null, false);

        try {
            testPost.dateTime();
            fail("Null date time for post passed");
        } catch (IllegalArgumentException e) {
            System.out.println("Exception caught: " + e);
        }
    }
}
