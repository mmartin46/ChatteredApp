package com.example.chatterapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatterapp.adapters.RecentConversationsAdapter;
import com.example.chatterapp.databinding.ActivityMainBinding;
import com.example.chatterapp.listeners.ConversionListener;
import com.example.chatterapp.models.ChatMessage;
import com.example.chatterapp.models.User;
import com.example.chatterapp.utilities.Constants;
import com.example.chatterapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConversionListener {


    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> convoList;
    private RecentConversationsAdapter convoAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Used to access layout variables and views
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init() {
        convoList = new ArrayList<>();
        convoAdapter = new RecentConversationsAdapter(convoList, this);
        binding.convoRecyclerView.setAdapter(convoAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {

        binding.iconSignOut.setOnClickListener(v -> signOut());
        // Switch to the user's activity layout
        binding.createNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        });
        // Switch to the edit activity layout
        binding.iconProfile.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), EditActivity.class));
        });
    }

    private void loadUserDetails() {
        // Sets the name
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));

        setProfileImage(Constants.KEY_ICON);
    }

    // Sets the Image
    private void setProfileImage(String encodedIcon)
    {
        byte[] bytes = Base64.decode(preferenceManager.getString(encodedIcon), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.iconProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        // FCM Token - used to send messages to respective devices.
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference docRef = database.collection(Constants.KEY_ALL_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        docRef.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut() {
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_ALL_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();

        // FCM Token is removed.
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {

                    // "Return" to the SignInActivity layout
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Unable to logout"));
    }

    /*

    Sender / Receiver Conversations
     */
    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        // Set the receiver
                        chatMessage.convoIcon = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ICON);
                        chatMessage.convoName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.convoId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        // Set the sender
                        chatMessage.convoIcon = documentChange.getDocument().getString(Constants.KEY_SENDER_ICON);
                        chatMessage.convoName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.convoId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    // Set the time
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    convoList.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < convoList.size(); ++i) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (convoList.get(i).senderId.equals(senderId) && convoList.get(i).receiverId.equals(receiverId)) {
                            convoList.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            convoList.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(convoList, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            convoAdapter.notifyDataSetChanged();
            binding.convoRecyclerView.smoothScrollToPosition(0);
            binding.convoRecyclerView.setVisibility(View.VISIBLE);
            binding.progBar.setVisibility(View.GONE);
        }
    };

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}