package com.example.chatterapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.chatterapp.R;
import com.example.chatterapp.databinding.ActivitySignInBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Used to access layout variables and views
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        // Returns the outermost view in the layout file.
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.createAccountBtn.setOnClickListener(v -> {
            // Swap to Sign Up Activity
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });
    }


}