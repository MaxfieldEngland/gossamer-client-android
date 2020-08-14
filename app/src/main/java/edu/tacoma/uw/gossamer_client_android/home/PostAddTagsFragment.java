package edu.tacoma.uw.gossamer_client_android.home;

import android.app.Activity;
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
import edu.tacoma.uw.gossamer_client_android.home.PostAddFragment;
import edu.tacoma.uw.gossamer_client_android.home.model.Post;

public class PostAddTagsFragment extends Fragment {

    private PostAddFragment.AddListener mAddListener;

    private ArrayList<String> postTags;

    public PostAddTagsFragment() {
    }

    PostDetailActivity parentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postTags = new ArrayList<String>();
        parentActivity = (PostDetailActivity) this.getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post_tags_add, container, false);
        Button addTagsButton = (Button) v.findViewById(R.id.addTags);

        final ArrayList<CheckBox> buttonList = new ArrayList<CheckBox>();

        LinearLayout tagCon = (LinearLayout) v.findViewById(R.id.tagSelectorLayout);

        LinearLayout.LayoutParams tagLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

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
                    if (check.isChecked())
                        postTags.add(check.getText().toString());
                    if (!check.isChecked())
                        postTags.remove(check.getText().toString());
                }
                if (postTags.size() > 5) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Please select at most 5 tags!", Toast.LENGTH_SHORT).show();
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
