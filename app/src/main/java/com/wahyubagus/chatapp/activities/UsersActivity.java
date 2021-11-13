package com.wahyubagus.chatapp.activities;

import static com.wahyubagus.chatapp.utilities.Constants.KEY_COLLECTION_USER;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_EMAIL;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_FCM;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_IMAGE;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_NAME;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_USER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wahyubagus.chatapp.R;
import com.wahyubagus.chatapp.adapters.UserAdapter;
import com.wahyubagus.chatapp.databinding.ActivityUsersBinding;
import com.wahyubagus.chatapp.models.User;
import com.wahyubagus.chatapp.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

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

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(KEY_COLLECTION_USER)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(KEY_FCM);
                            users.add(user);
                        }

                        if (users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(users);
                            binding.userRecyclerView.setAdapter(userAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean loading){
        if (loading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else binding.progressBar.setVisibility(View.INVISIBLE);
    }
}