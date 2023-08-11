package com.example.chatterapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatterapp.R;
import com.example.chatterapp.databinding.ActivitySignInBinding;
import com.example.chatterapp.databinding.ActivitySignUpBinding;
import com.example.chatterapp.utilities.Constants;
import com.example.chatterapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Used to access layout variables and views
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        // Returns the outermost view in the layout file.
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.loginBtn.setOnClickListener(v -> {
            // Load the Sign In Layout
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        });

        // Submit signup information.
        binding.submitBtn.setOnClickListener(v -> {
            if (isUserValid()) {
                signUp();
            }
        });

        // Choose an image
        binding.iconLayout.setOnClickListener(v -> {
            // Choose from the file directory.
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Load the image
            chooseIcon.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();

        // Load information to the firebase
        user.put(Constants.KEY_NAME, binding.textName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.textName.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.textName.getText().toString());
        user.put(Constants.KEY_ICON, encodedIcon);
        database.collection(Constants.KEY_ALL_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {

                })
                .addOnFailureListener(exception -> {

                });
    }

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


    // Loads the image
    private final ActivityResultLauncher<Intent> chooseIcon = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            // Load the Image
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.icon.setImageBitmap(bitmap);
                            binding.textAddIcon.setVisibility(View.GONE);
                            encodedIcon = encodeIcon(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    // Check if the user's credentials are valid.
    private Boolean isUserValid() {
        if (encodedIcon == null) {
            showToast("Select a profile icon");
            return false;
        } else if (binding.textName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (binding.textEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email address");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.textEmail.getText().toString()).matches()) {
            showToast("Enter a valid email address");
        } else if (binding.textPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter valid password");
            return false;
        } else if (binding.textConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please confirm your password");
            return false;
        } else if (!binding.textPassword.getText().toString().equals(binding.textConfirmPassword.getText().toString())) {
            showToast("Passwords don't match");
            return false;
        } else {
            return true;
        }
        return true;
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            // If loading hide the progress bar
            // and show the submit button, vise-versa.
            binding.submitBtn.setVisibility(View.INVISIBLE);
            binding.progBar.setVisibility(View.VISIBLE);
        } else {
            binding.progBar.setVisibility(View.INVISIBLE);
            binding.submitBtn.setVisibility(View.VISIBLE);
        }
    }
}