package com.wahyubagus.chatapp.activities;

import static com.wahyubagus.chatapp.utilities.Constants.KEY_COLLECTION_USER;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_FCM;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_IMAGE;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_NAME;
import static com.wahyubagus.chatapp.utilities.Constants.KEY_USER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.wahyubagus.chatapp.databinding.ActivityMainBinding;
import com.wahyubagus.chatapp.utilities.PreferenceManager;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }

    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        });
    }
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(KEY_COLLECTION_USER).document(
                        preferenceManager.getString(KEY_USER_ID)
                );
        documentReference.update(KEY_FCM, token)
                //.addOnSuccessListener(unused -> showToast("Token Update Successfully"))
                .addOnFailureListener(e -> showToast("Unable update token"));
    }

    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(KEY_COLLECTION_USER).document(
                        preferenceManager.getString(KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(KEY_FCM, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }
}