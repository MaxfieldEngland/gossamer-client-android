package edu.tacoma.uw.gossamer_client_android.home.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Tags refer to app specific identifiers based on gender and sexual identity. These are designated
 * in the database, with a color and string text, used for both posts and profiles to identify
 * the poster and the content therein.
 */
public class Tag {

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

}