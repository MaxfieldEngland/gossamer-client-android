/*
 * Elijah Freeman
 * Maxfield England
 *
 * TCSS 450 - Mobile App Programming
 * Gossamer
 */
package edu.tacoma.uw.gossamer_client_android.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import edu.tacoma.uw.gossamer_client_android.R;

/**
 * Allows the user to add tags to their Posts. Users can select up to five
 * total tags to attach to their post.
 */
public class PostAddTagsFragment extends Fragment {

    /** Listener. */
    private PostAddFragment.AddListener mAddListener;
    /** List of post tags. */
    private ArrayList<String> postTags;
    /** Parent activity. */
    PostDetailActivity parentActivity;
    /** The amount of tags allowed to be applied to a given post */
    public static final int TAG_LIMIT = 4;

    /** Default empty constructor. */
    public PostAddTagsFragment() {
    }

    /**
     * Default onCreate method that is responsible for instantiating the
     * parentActivity and tag list.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postTags = new ArrayList<String>();
        parentActivity = (PostDetailActivity) this.getActivity();
    }

    /**
     * Inflates the PostAddTags Fragment layout. Provides functionality required to
     * add tags to the post.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post_tags_add, container, false);
        Button addTagsButton = (Button) v.findViewById(R.id.addTags);
        //Check list of available tags
        final ArrayList<CheckBox> buttonList = new ArrayList<CheckBox>();
        //Container for the tags.
        LinearLayout tagCon = (LinearLayout) v.findViewById(R.id.tagSelectorLayout);
        LinearLayout.LayoutParams tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // Creates a list of available tags and adds the tags the user selects to
        // a list (selectedTags) in the parent Activity.
        for (String tagName : parentActivity.tagList) {
            CheckBox tagCB = new CheckBox(v.getContext());
            buttonList.add(tagCB);
            tagCB.setText(tagName);
            if (parentActivity.selectedTags.contains(tagName))
                tagCB.toggle();
            tagCon.addView(tagCB);
        }

        addTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (CheckBox check : buttonList) {
                    if (check.isChecked() && !postTags.contains(check.getText().toString()))
                        postTags.add(check.getText().toString());
                    if (!check.isChecked())
                        postTags.remove(check.getText().toString());
                }
                if (postTags.size() > TAG_LIMIT) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Please select at most " + TAG_LIMIT + " tags!", Toast.LENGTH_SHORT).show();
                    Log.v("POSTTAGLIMIT", postTags.size() + " : " + postTags.toString());
                }
                else {
                    parentActivity.selectedTags = postTags;
                    getFragmentManager().popBackStack();
                }
            }
        });
        return v;
    }
}
