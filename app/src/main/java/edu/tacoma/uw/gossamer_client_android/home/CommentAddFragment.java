//package edu.tacoma.uw.gossamer_client_android.home;
//
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//
//import edu.tacoma.uw.gossamer_client_android.home.model.Comment;
//
///**
// * This fragment subclass is responsible for allowing a user to add a comment to a post,
// * visible from the post detail page.
// */
//public class CommentAddFragment extends Fragment {
//
//    /** Listener for the add comment button. */
//    private AddListener mAddListener;
//    /** The associated comment to be written. */
//    private Comment mComment;
//
//    /** Unused: remove elegantly */
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//    public interface AddListener {
//        public void addComment(Comment comment);
//    }
//
//    public CommentAddFragment() {
//    }
//
//    public static CommentAddFragment newInstance(String param1, String param2) {
//        CommentAddFragment fragment = new CommentAddFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    /**
//     * onCreate behavior:
//     */
//
//
//
//}
