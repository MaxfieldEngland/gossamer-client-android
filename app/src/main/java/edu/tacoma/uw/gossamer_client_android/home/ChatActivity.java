package edu.tacoma.uw.gossamer_client_android.home;
import edu.tacoma.uw.gossamer_client_android.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.tacoma.uw.gossamer_client_android.home.model.Message;
import edu.tacoma.uw.gossamer_client_android.userprofile.UserProfileActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity {

    private String displayname;
    private String email;
    private List<Message> messages;
    private SimpleItemRecyclerViewAdapter messageView;

    private WebSocket webSocket;

    //Case 1: hosted on heroku
    private String SERVER_PATH = "ws://gossamer-backend.herokuapp.com";

    //Case 2: used for local host
    // private String SERVER_PATH = "ws://192.168.1.10:5000";
    private Button sendButton;
    private EditText msgBox;


    /** Recycler view object to hold the Post. */
    private RecyclerView mRecyclerView;

//    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("Gossamer Chat");

        //Use SharedPreferences to retrieve email and displayname to use in chat.
        SharedPreferences prefsOnLaunch = getSharedPreferences(getString(R.string.LOGIN_PREFS)
                , Context.MODE_PRIVATE);
        displayname = prefsOnLaunch.getString("displayname", "User").trim();
        email = prefsOnLaunch.getString("email", null);

        Log.d("CHAT", "Attempting Socket Connection");
        initiateSocketConnection();
        Log.d("CHAT", "Returned from socket connection");

    }

    /**
     * Creates the connection to the socket server.
     */
    private void initiateSocketConnection() {

        Log.d("CHAT", "Hello from socket initiator");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        webSocket.close(1000, "App close");

//        mSocket.emit("unsubscribe");
//        mSocket.disconnect();
//        mSocket.off("receivemsg", onNewMessage);
    }

    private JSONObject jsonifyMessage(Message msg) {

        JSONObject msgJson = new JSONObject();
        try {
            msgJson.put("displayname", msg.getDisplayName());
            msgJson.put("email", msg.getEmail());
            msgJson.put("content", msg.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msgJson;
    }

    /**
     * Consumes chat message data from socket.io to display to the chat client.
     *
     * @param displayname The displayname of the user sending the message.
     * @param email The email address of the user sending the message.
     * @param content The message content; what the user is saying in their message.
     */
    private void addMessage(String displayname, String email, String content) {

        Message sendMsg = new Message(displayname, email, content);
        messages.add(sendMsg);
        messageView.notifyItemInserted(messageView.getItemCount() - 1);
        //Optional: do we scroll when a new message is added?
        mRecyclerView.smoothScrollToPosition(messageView.getItemCount() - 1);
    }

    /**
     * Consumes system message data from socket.io to display to the chat client.
     *
     * @param displayname The displayname of the user triggering the system message.
     * @param status The status to display ("joined" or "left")
     */
    private void addSysMessage(String displayname, String status) {

        String statusToDisplay = displayname + " has " + status + " the room.";
        Message sysMsg = new Message("", "", statusToDisplay);
        messages.add(sysMsg);

        //Now that we added the message, notify the adapter about the change so we update!
        messageView.notifyItemInserted(messageView.getItemCount() - 1);

        Log.d("ChatActivity", "Sysmsg received: " + displayname + " | " + status );
    }

    private void initializeView() {


        sendButton = findViewById(R.id.button_chat_send);
        msgBox = findViewById(R.id.chat_message_box);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String messageContent = msgBox.getText().toString();

                //addMessage(displayname, email, messageContent);
                webSocket.send(jsonifyMessage(new Message(displayname, email, messageContent)).toString());
                msgBox.setText("");

            }
        });
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            Log.d("CHAT", "SocketListener opened");

            runOnUiThread(() -> {
                Log.d("CHAT", "Client Connected");
                initializeView();

                //Initialize recyclerview and create messages
                messages = new ArrayList<Message>();
                messageView = new SimpleItemRecyclerViewAdapter(ChatActivity.this, messages);
                mRecyclerView = findViewById(R.id.chat_recycler_view);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                mRecyclerView.setAdapter(messageView);
            });

            JSONObject sysMsg = new JSONObject();
            try {
                sysMsg.put("status", "joined");
                sysMsg.put("displayname", displayname);
                webSocket.send(sysMsg.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            super.onMessage(webSocket, text);

            Log.d("CHAT","Message received");

            runOnUiThread(() -> {
                try {

                    JSONObject msg = new JSONObject(text);
                    String displayname = (String) msg.get("displayname");

                    if (msg.has("status")){
                        String status = (String) msg.get("status");
                        addSysMessage(displayname, status);
                    }
                    else {

                        String email = (String) msg.get("email");
                        String content = (String) msg.get("content");
                        addMessage(displayname, email, content);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);

            JSONObject sysMsg = new JSONObject();
            try {
                sysMsg.put("status", "left");
                sysMsg.put("displayname", displayname);
                webSocket.send(sysMsg.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Allows views to be recycled.
     */
    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        /** Parent Activity. */
        private final ChatActivity mParentActivity;
        /** List of Posts. */
        private final List<Message> mValues;

        SimpleItemRecyclerViewAdapter(ChatActivity parent,
                                      List<Message> items
                                      ) {
            mValues = items;
            mParentActivity = parent;
        }

        /**
         * Inflates the view for each post.
         * @param parent The Recyclerview.
         * @param viewType
         * @return ViewHolder containing the view for the new message.
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_list_content, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Responsible for binding the ViewHolder.
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.mNameView.setOnClickListener(new View.OnClickListener() {
                    Context context = holder.mNameView.getContext();

                    @Override
                    public void onClick(View view) {
                        String e = mValues.get(position).getEmail();
                        String u = mValues.get(position).getDisplayName();
                        if (e.equals("")) return;
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra(Message.EMAIL, e);
                        intent.putExtra(Message.DISPLAY_NAME, u);
                        context.startActivity(intent);
                    }
                });

            holder.mContentView.setText(mValues.get(position).getContent());
            holder.mNameView.setText(mValues.get(position).getDisplayName());

            //If we are the messager, set align to right and background color to blue
            if (mValues.get(position).getEmail().contentEquals(mParentActivity.email)) {
                holder.mCardView.setCardBackgroundColor(Color.parseColor("#CAE2E3"));
                holder.mContainerView.setGravity(Gravity.RIGHT);
            }
            //If the message has no email, set align to center and neutralize the background (yellow)
            if (mValues.get(position).getEmail().equals("")) {
                holder.mCardView.setCardBackgroundColor(Color.parseColor("#FEFEE3"));
                holder.mContainerView.setGravity(Gravity.CENTER);

            }
        }

        /**
         * Returns the number of posts.
         * @return
         */
        @Override
        public int getItemCount() {
            return mValues.size();
        }

        /**
         * Override recyclerview getitemid method; disable problematic behavior with tag container retrieval
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Override recyclerview getitemviewtype method; disable problematic behavior with tag container retrieval
         */
        @Override
        public int getItemViewType(int position) {
            return position;
        }

        /**
         * View holder that contains the information present in the Recycler view.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mNameView;
            final TextView mContentView;
            final CardView mCardView;
            final LinearLayout mContainerView;
            ViewHolder(View view) {
                super(view);

                mNameView = view.findViewById(R.id.msg_displayname);
                mContentView = view.findViewById(R.id.msg_content);
                mCardView = view.findViewById(R.id.card_msg_other);
                mContainerView = view.findViewById(R.id.chat_message_container);
            }
        }
    }
}