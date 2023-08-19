package com.example.chatterapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.chatterapp.databinding.ActivityFullImageBinding;
import com.example.chatterapp.models.User;

public class FullImageActivity extends AppCompatActivity {

    private ActivityFullImageBinding binding;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = (User) getIntent().getSerializableExtra("full_image_user");


        init();
        setListeners();
    }

    private void init() {
        binding.userIcon.setImageBitmap(
                getBitmapFromEncodedString(user.icon)
        );
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

}