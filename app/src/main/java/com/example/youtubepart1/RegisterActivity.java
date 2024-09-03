package com.example.youtubepart1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivProfilePicture;
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextView tvUsernameError, tvEmailError, tvPasswordError, tvConfirmPasswordError;
    private Button btnRegister;
    private TextView tvLogin, uploadPhotoButton;
    Bitmap profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvUsernameError = findViewById(R.id.tvUsernameError);
        tvEmailError = findViewById(R.id.tvEmailError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        TextView tvLoginClickable = findViewById(R.id.tvLoginClickable);

        uploadPhotoButton.setOnClickListener(v -> openImageChooser());
        btnRegister.setOnClickListener(v -> validateAndRegister());
        tvLoginClickable.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                profileImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                // Set the chosen image to the ImageView or handle as needed
                ivProfilePicture.setImageBitmap(profileImage); // Adjust this if ivProfilePicture is still used
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateAndRegister() {
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                User existingUser = new UserViewModel(RegisterActivity.this).getUser(username);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean valid = true;
                        if (username.isEmpty()) {
                            tvUsernameError.setText("This is a mandatory field");
                            tvUsernameError.setVisibility(TextView.VISIBLE);
                            valid = false;
                        } else if (existingUser != null) {
                            tvUsernameError.setText("That username is taken. Try another.");
                            tvUsernameError.setVisibility(TextView.VISIBLE);
                            valid = false;
                        } else {
                            tvUsernameError.setVisibility(TextView.GONE);
                        }

                        if (email.isEmpty() || !Pattern.compile("^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$").matcher(email).matches()) {
                            tvEmailError.setText("Email is not correct");
                            tvEmailError.setVisibility(TextView.VISIBLE);
                            valid = false;
                        } else {
                            tvEmailError.setVisibility(TextView.GONE);
                        }

                        if (password.isEmpty() || password.length() < 8 || !Pattern.compile("^(?=.*[a-zA-Z])(?=.*[0-9]).+$").matcher(password).matches()) {
                            tvPasswordError.setText("Password must contain both letters and characters");
                            tvPasswordError.setVisibility(TextView.VISIBLE);
                            valid = false;
                        } else {
                            tvPasswordError.setVisibility(TextView.GONE);
                        }

                        if (!confirmPassword.equals(password)) {
                            tvConfirmPasswordError.setText("Passwords do not match");
                            tvConfirmPasswordError.setVisibility(TextView.VISIBLE);
                            valid = false;
                        } else {
                            tvConfirmPasswordError.setVisibility(TextView.GONE);
                        }

                        if (valid) {
                            // Save user information
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    saveUserInformation(username, email, password);
                                    navigateToMainActivity();
                                }
                            }).start();
                        }
                    }
                });
            }
        }).start();
    }

    private void saveUserInformation(String username, String email, String password) {
        Bitmap image = ImageOperations.resourceToBitmap(this, R.drawable.ic_profile);
        if (profileImage != null)
            image = BitmapUtils.getResizedBitmap(profileImage);
        String imageBase64 = ImageOperations.bitmapToBase64(image);
        new UserViewModel(this).insert(new User(username, password, email, imageBase64));
        Server server = new Server(this);
        System.out.println(server.register(email, username, password, imageBase64));
        ToastManager.showToast("Registration successful", this);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}