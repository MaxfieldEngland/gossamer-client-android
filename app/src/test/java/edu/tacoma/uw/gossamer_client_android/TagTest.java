package edu.tacoma.uw.gossamer_client_android;

import org.junit.Test;

import java.util.ArrayList;

import edu.tacoma.uw.gossamer_client_android.home.model.Tag;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * Test class for Tag. Does not test trivial getter/setter methods unless
 * methods perform validation. Does not test methods that require JSON objects/methods.
 */
public class TagTest {

    @Test
    public void testConstructor() {
        assertNotNull(new Tag("testName", "testColor"));
    }

    @Test
    public void testGetTagNames() {
        //Set up test list
        ArrayList<String> testList = new ArrayList<>();
        testList.add("test1");
        testList.add("test2");
        testList.add("test3");

        //Set up Tag list that we will test.
        ArrayList<Tag> testTagList = new ArrayList<>();
        testTagList.add(new Tag("test1", "color1"));
        testTagList.add(new Tag("test2", "color2"));
        testTagList.add(new Tag("test3", "color3"));

        assertEquals(testList, Tag.getTagNames(testTagList));
        testList.remove(2);
        assertNotEquals(testList, Tag.getTagNames(testTagList));
    }

    /**
     * Test that equal tags return true.
     * Test Overridden Equals method. */
    @Test
    public void testEquals() {
        Tag testTag = new Tag("test1", "color1");
        Tag testAgainstEqualTag = new Tag("test1", "color1");
        assertTrue(testTag.equals(testAgainstEqualTag));
    }

    /**
     * Test non equal Tags.
     * Test Overridden equals method. */
    @Test
    public void testNonEquals() {
        Tag testTag = new Tag("test1", "color1");

        //Tests
        Tag testDifferentNameColorTag = new Tag("test2", "color2");
        assertFalse(testTag.equals(testDifferentNameColorTag));

        Tag testDifferentNameTag = new Tag("test2", "color1");
        assertFalse(testTag.equals(testDifferentNameTag));

        Tag testDifferentColorTag = new Tag("test1", "color2");
        assertFalse(testTag.equals(testDifferentColorTag));

        //Test null
        assertFalse(testTag.equals(null));
        assertFalse(testTag.equals(new Tag(null, "color1")));
        assertFalse(testTag.equals(new Tag(null, null)));
        assertFalse(testTag.equals(new Tag("test1", null)));
    }
}
