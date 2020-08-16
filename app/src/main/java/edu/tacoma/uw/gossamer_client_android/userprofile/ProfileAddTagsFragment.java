package edu.tacoma.uw.gossamer_client_android.userprofile;

import android.content.Intent;
import android.os.Bundle;
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
import edu.tacoma.uw.gossamer_client_android.home.model.Tag;

/**
 * Allows a user to add tags to their profile page.
 */
public class ProfileAddTagsFragment extends Fragment {

    /** Parent activity for this fragment. */
    private UserProfileActivity parentActivity;
    /** List of profile tags. */
    private ArrayList<String> profileTags;
    /** Required empty constructor for fragment. */
    public ProfileAddTagsFragment() {}

    /**
     * Default onCreate method. Instantiates profile tags & parentActivity.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileTags = new ArrayList<String>();
        parentActivity = (UserProfileActivity) this.getActivity();
    }

    /**
     * Default onCreateView method that sets up check list of tags.
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
        final ArrayList<CheckBox> buttonList = new ArrayList<CheckBox>();
        LinearLayout tagCon = (LinearLayout) v.findViewById(R.id.tagSelectorLayout);
        LinearLayout.LayoutParams tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        ArrayList<String> userCurrentTags = Tag.getTagNames(parentActivity.mUserTags);

        for (String tagName : parentActivity.mTagIDs) {
            CheckBox tagCB = new CheckBox(v.getContext());
            buttonList.add(tagCB);
            tagCB.setText(tagName);
            if (userCurrentTags.contains(tagName))
                tagCB.toggle();
            tagCon.addView(tagCB);
        }

        // Creates list of tags that the user selects.
        addTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().findViewById(R.id.edit_done).setVisibility(View.VISIBLE);
                for (CheckBox check : buttonList) {
                    if (check.isChecked())
                        profileTags.add(check.getText().toString());
                    if (!check.isChecked())
                        profileTags.remove(check.getText().toString());
                }
                if (profileTags.size() > 5) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Please select at most 5 tags!", Toast.LENGTH_SHORT).show();
                }
                else {
                    parentActivity.mSelectedTags = profileTags;
                    parentActivity.findViewById(R.id.launchProfileAddTagsFragmentButton).setVisibility(View.VISIBLE);

                    getFragmentManager().popBackStack();
                }
            }
        });
        return v;
    }
}
