package edu.tacoma.uw.gossamer_client_android.home.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Tags refer to app specific identifiers based on gender and sexual identity. These are designated
 * in the database, with a color and string text, used for both posts and profiles to identify
 * the poster and the content therein.
 */
public class Tag implements Serializable {

    private String mColor;
    private String mTagName;

    public String getColor() {
        return mColor;
    }

    public String getName() {
        return mTagName;
    }

    public Tag(String name, String color){
        mTagName = name;
        mColor = color;
    }

    /**
     * Receives a JSON object that contains tags associated with posts, and adds each tag to its corresponding post.
     * @param tagJson information stored in the database.
     * @return tagList, a list of Post objects.
     * @throws JSONException exception
     */
    public static void parseTagJson(List<Post> postList, String tagJson) throws JSONException {

        if (tagJson != null) {
            JSONArray arr = new JSONArray(tagJson);

            //For each item in the JSON array,
            for (int i = 0; i < arr.length(); i++ ) {

                //We receive a new tag:
                JSONObject obj = arr.getJSONObject(i);

                //Find the post associated with that tag,
                for (Post post : postList) {
                    if (post.getmPostID() == obj.getInt("postid")) {

                        //And add that tag to the post's tag list.
                        post.addTag(new Tag(obj.getString("tagid"), obj.getString("color")));
                        break;
                    }

                }
            }
        }

    }

    /**
     * Receives a JSON object that contains tags as associated with a single profile, and adds each
     * @param tagJson
     * @throws JSONException
     */
    public static ArrayList<Tag> parseTagJson(String tagJson) throws JSONException {

        ArrayList<Tag> tags = new ArrayList<Tag>();

        if (tagJson != null) {
            JSONArray arr = new JSONArray(tagJson);

            for (int i = 0; i < arr.length(); i++){
                JSONObject tag = arr.getJSONObject(i);
                tags.add(new Tag(tag.getString("tagid"), tag.getString("color")));
            }

        }

        return tags;

    }

    public static ArrayList<String> parseTagIDJson(String tagJson) throws JSONException {

        ArrayList<String> tags = new ArrayList<String>();

        if (tagJson != null) {
            JSONArray arr = new JSONArray(tagJson);

            for (int i =0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                tags.add(obj.getString("tagid"));
            }
        }
        return tags;
    }

    /**
     * Return a list of String names corresponding to a list of tag objects
     * @param tags an arraylist of of tags
     * @return A corresponding arraylist only of strings representing tag text.
     */
    public static ArrayList<String> getTagNames(ArrayList<Tag> tags){
        ArrayList<String> names = new ArrayList<String>();
        for (Tag tag : tags) {

            names.add(tag.getName());
        }
        return names;

    }

    @Override
    public boolean equals(Object theOther) {

        if (theOther == null) return false;
        if (theOther.getClass() != this.getClass()) return false;
        Tag o = (Tag) theOther;
        if (o.getColor().equals(this.mColor) && o.getName().equals(this.mTagName)){
            return true;
        }
        else return false;

    }

}
