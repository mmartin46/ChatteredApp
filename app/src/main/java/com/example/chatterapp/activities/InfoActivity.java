package com.example.chatterapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.example.chatterapp.models.User;
import com.example.chatterapp.utilities.Constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoActivity extends AppCompatActivity {

    private ActivityInfoBinding binding;
    private FirebaseFirestore database;

    private User receivedUser;
    boolean isIconFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        receivedUser = (User) getIntent().getSerializableExtra("received_user_extra");
        setContentView(binding.getRoot());

        database = FirebaseFirestore.getInstance();
        initUser();
        setListeners();
        iconClicked();
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> onBackPressed());

    }

    private void initUser() {
        binding.userName.setText(receivedUser.name);

        Bitmap iconBitmap = getBitmapFromEncodedString(receivedUser.icon);
        binding.userIcon.setImageBitmap(iconBitmap);
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private CollectionReference getConversations() {
        return database.collection(Constants.KEY_COLLECTION_CONVERSATIONS);
    }

    private void iconClicked() {
        binding.userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FullImageActivity.class);
                intent.putExtra("full_image_user", receivedUser);
                startActivity(intent);
            }
        });
    }
}