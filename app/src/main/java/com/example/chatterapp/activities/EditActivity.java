package com.example.chatterapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatterapp.R;
import com.example.chatterapp.databinding.ActivityEditBinding;
import com.example.chatterapp.utilities.Constants;
import com.example.chatterapp.utilities.PreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class EditActivity extends AppCompatActivity {

    private ActivityEditBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String encodedIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> {
            // Load the main activity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });

        binding.submitBtn.setOnClickListener(v -> {
            // Load login activity
            submit();
        });

        binding.userIconLayout.setOnClickListener(v -> {
            // Choose from the file directory.
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Load the image
            chooseIcon.launch(intent);

            binding.userIconText.setVisibility(View.GONE);
        });
    }

    private void loadUserDetails() {

    }

    private void submit() {

        if (areCredentialsValid()) {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        }
    }


    private Boolean areCredentialsValid() {
        if (encodedIcon == null) {
            showToast("Select a profile icon");
            return false;
        } else if (binding.origUserName.getText().toString().trim().isEmpty()) {
            showToast("Enter your current username");
            return false;
        } else if (binding.newUserName.getText().toString().trim().isEmpty()) {
            showToast("Enter a valid username");
            return false;
        } else if (binding.origUserName.getText().toString()
                .equals(binding.newUserName.getText().toString())) {
            showToast("Usernames shouldn't match");
            return false;
        } else if (binding.userPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter a valid password.");
            return false;
        } else if (binding.confirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please confirm your password");
            return false;
        } else if (binding.userPassword.getText().toString()
                .equals(binding.confirmPassword.getText().toString())) {
            showToast("Passwords don't match");
            return false;
        } else {
            // TODO:
            // Check to make sure the credentials have a match
            return true;
        }
    }


    // Load the icon
    private final ActivityResultLauncher<Intent> chooseIcon = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri iconUri = result.getData().getData();
                        try {
                            // Load the Icon
                            InputStream inputStream = getContentResolver().openInputStream(iconUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            // Set the Icon in the layout
                            binding.userIcon.setImageBitmap(bitmap);
                            encodedIcon = encodeIcon(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeIcon(Bitmap bitmap) {
        int prevWidth = 150;
        int prevHeight = bitmap.getHeight() + prevWidth / bitmap.getWidth();

        Bitmap prevBitmap = Bitmap.createScaledBitmap(bitmap, prevWidth, prevHeight, false);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Writes a compress version of the bitmap to the byteArrayOutputStream
        prevBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        // Returns as a string.
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }



    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


}