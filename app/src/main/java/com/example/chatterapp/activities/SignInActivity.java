package com.example.chatterapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatterapp.R;
import com.example.chatterapp.databinding.ActivitySignInBinding;
import com.example.chatterapp.utilities.Constants;
import com.example.chatterapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        preferenceManager = new PreferenceManager(getApplicationContext());

        // If the user is signed in go the main activity class.
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
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

        binding.submitBtn.setOnClickListener(v -> {
            if (isUserValid()) {
                signIn();
            }
        });
    }

    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_ALL_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.textEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.textPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                        && task.getResult().getDocuments().size() > 0) {

                        // Loads the data is successful match found.
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_ICON, documentSnapshot.getString(Constants.KEY_ICON));

                        // Load the main activity.
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("User not found. Please try again.");
                    }
                });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.submitBtn.setVisibility(View.INVISIBLE);
            binding.progBar.setVisibility(View.VISIBLE);
        } else {
            binding.submitBtn.setVisibility(View.VISIBLE);
            binding.progBar.setVisibility(View.INVISIBLE);
        }
    }

    // Shows a message.
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Checks if the user's credentials are valid.
    private Boolean isUserValid() {
        if (binding.textEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter your email address");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.textEmail.getText().toString()).matches()) {
            showToast("Enter a valid email address");
            return false;
        } else if (binding.textPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter your password");
            return false;
        } else {
            return true;
        }
    }

}