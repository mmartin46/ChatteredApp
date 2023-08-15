package com.example.chatterapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.chatterapp.databinding.ActivityMainBinding;
import com.example.chatterapp.utilities.Constants;
import com.example.chatterapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Used to access layout variables and views
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }

    private void setListeners() {

        binding.iconSignOut.setOnClickListener(v -> signOut());
        // Switch to the user's activity layout
        binding.createNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
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

}