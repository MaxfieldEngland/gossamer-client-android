package edu.tacoma.uw.gossamer_client_android.home.model;

import java.io.Serializable;

/**
 * A model for messages used in ChatActivity--represents realtime chat with other users.*
 */
public class Message implements Serializable {
    private final String displayName;
    private final String email;
    private final String content;

    /** Refers to field "email" for putExtra */
    public static final String EMAIL = "email";
    /** Refers to field "displayname" for putExtra */
    public static final String DISPLAY_NAME = "displayname";


    /**
     * Constructs a Message using the
     * @param displayName The name associated with the user profile (as of joining the chat)
     * @param email The email associated with the account, used as a unique ID
     * @param roomName
     * @param content
     */
    public Message(String displayName, String email, String content) {
        this.displayName = displayName;
        this.email = email;
        this.content = content;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getContent() {
        return content;
    }
}
