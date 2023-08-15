package com.example.chatterapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatterapp.adapters.UsersAdapter;
import com.example.chatterapp.databinding.ActivityUsersBinding;
import com.example.chatterapp.listeners.UserListener;
import com.example.chatterapp.models.User;
import com.example.chatterapp.utilities.Constants;
import com.example.chatterapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> {
           Intent intent = new Intent(getApplicationContext(), MainActivity.class);
           startActivity(intent);
        });
    }

    private void getUsers() {
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_ALL_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();

                        // Append the users.
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.icon = queryDocumentSnapshot.getString(Constants.KEY_ICON);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }

                        if (users.size() > 0) {
                            // Changes the recyclerview.
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.userRecyclerView.setAdapter(usersAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            // No users
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMsg.setText(String.format("%s", "Looks like it's just you."));
        binding.textErrorMsg.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progBar.setVisibility(View.VISIBLE);
        } else {
            binding.progBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}