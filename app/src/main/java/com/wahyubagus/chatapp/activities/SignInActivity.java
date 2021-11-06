package com.wahyubagus.chatapp.activities;

import static com.wahyubagus.chatapp.utilities.Constants.KEY_COLLECTION_USER;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_EMAIL;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_IMAGE;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_IS_SIGNED_IN;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_NAME;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_PASSWORD;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_USER_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wahyubagus.chatapp.R;
import com.wahyubagus.chatapp.databinding.ActivitySignInBinding;
import com.wahyubagus.chatapp.utilities.PreferenceManager;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (preferenceManager.getBoolean(KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
    }

    private void setListener(){
        binding.textCreateNewAccount.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });

        binding.buttonSignIn.setOnClickListener(view -> {
            if (isValidSignInRequest()){
                signIn();
            }
        });
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(KEY_COLLECTION_USER)
                .whereEqualTo(KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                        && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(KEY_NAME, documentSnapshot.getString(KEY_NAME));
                        preferenceManager.putString(KEY_IMAGE, documentSnapshot.getString(KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to Sign in");
                    }
                });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInRequest(){
        if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter a valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().isEmpty()){
            showToast("Enter password");
            return false;
        } else return true;
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}