package com.example.youtubepart1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";
    private User user;
    private User existingUser;
    private Uri filepath;
    private ImageView img;
    private LinearLayout videoListLayout;
    private EditText usernameView;
    private EditText emailView;

    private ActivityResultLauncher<Uri> camera = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    img.setImageURI(filepath);
                    Log.d(TAG, "Camera capture successful, filepath: " + filepath);
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(filepath);
                    sendBroadcast(mediaScanIntent);
                } else {
                    Log.d(TAG, "Camera capture failed or was cancelled");
                }
            }
    );

    private ActivityResultLauncher<String> cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    dispatchTakePictureIntent();
                } else {
                    ToastManager.showToast("Camera Permission is required to use camera", UserActivity.this);
                }
            }
    );

    private ActivityResultLauncher<PickVisualMediaRequest> gallery = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    filepath = uri;
                    img.setImageURI(filepath);
                    Log.d(TAG, "Image selected from gallery, filepath: " + filepath);
                } else {
                    Log.d(TAG, "No image selected from gallery");
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Check if a user is logged in
        if (HomeActivity.user == null) {
            ToastManager.showToast("You must be logged in to view profiles", this);
            finish();
            return;
        }

        initializeViews();
        loadUserData();
        setupClickListeners();
        displayUserVideos();
    }
    private void initializeViews() {
        usernameView = findViewById(R.id.username);
        emailView = findViewById(R.id.email);
        img = findViewById(R.id.img);
        videoListLayout = findViewById(R.id.video_list_layout);
    }

    private void loadUserData() {
        user = (User) getIntent().getSerializableExtra("user");
        existingUser = (User) getIntent().getSerializableExtra("existingUser");

        if (user != null) {
            img.setImageBitmap(ImageOperations.base64ToBitmap(user.image));
            usernameView.setText(user.userName);
            emailView.setText(user.email);
            Log.d(TAG, "Loaded user data, image size: " + user.image.length());
        } else {
            Log.e(TAG, "User data is null");
        }
    }

    private void setupClickListeners() {
        AppCompatButton upload = findViewById(R.id.upload);
        AppCompatButton galleryButton = findViewById(R.id.choose_image_gallery);
        AppCompatButton cameraButton = findViewById(R.id.choose_image_camera);

        upload.setOnClickListener(v -> updateUserProfile());

        findViewById(R.id.delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new UserViewModel(UserActivity.this).delete(user);
                        new Server(UserActivity.this).deleteProfile(user);
                        Intent intent = new Intent(UserActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).start();
            }
        });

        if (existingUser == null || user.userName.equals(existingUser.userName)) {
            usernameView.setEnabled(true);
            emailView.setEnabled(true);
            upload.setVisibility(View.VISIBLE);
            galleryButton.setVisibility(View.VISIBLE);
            cameraButton.setVisibility(View.VISIBLE);

            galleryButton.setOnClickListener(v -> gallery.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));
            cameraButton.setOnClickListener(v -> askCameraPermission());
        }
    } private void updateUserProfile() {
        String base64;
        if (filepath != null) {
            try {
                base64 = ImageOperations.bitmapToBase64(ImageOperations.uriToBitmap(this, filepath));
                Log.d(TAG, "New image processed, base64 length: " + base64.length());
            } catch (IOException e) {
                Log.e(TAG, "Error processing image", e);
                ToastManager.showToast("Error processing the image", UserActivity.this);
                return;
            }
        } else {
            base64 = user.image;
            Log.d(TAG, "Using existing image, base64 length: " + base64.length());
        }

        String newUsername = usernameView.getText().toString();
        String newEmail = emailView.getText().toString();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            ToastManager.showToast("Username and email cannot be empty", UserActivity.this);
            return;
        }

        User newUser = new User(newUsername, user.password, newEmail, base64);

        try {
            UserViewModel userViewModel = new UserViewModel(this);
            userViewModel.delete(user);
            userViewModel.insert(newUser);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new Server(UserActivity.this).updateProfile(newUser);
                }
            }).start();
            Log.d(TAG, "User updated in database");
            VideoViewModel videoViewModel = new VideoViewModel(this);
            // Update associated videos
            List<Video> userVideos = videoViewModel.getVideosByUsername(user.userName);
            for (Video video : userVideos) {
                video.setUserName(newUsername);
                video.setProfilePic(base64);
                videoViewModel.update(video);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Server(UserActivity.this).editVideo(video);
                    }
                }).start();
            }
            Log.d(TAG, "Associated videos updated");

            user = newUser;
            ToastManager.showToast("The user was updated successfully", UserActivity.this);

            // Update the UI immediately
            updateUIWithNewProfile(newUser);

        } catch (Exception e) {
            Log.e(TAG, "Error updating user profile", e);
            ToastManager.showToast("Error updating user information", UserActivity.this);
        }
    }

    private void updateUIWithNewProfile(User updatedUser) {
        // Update the ImageView with the new profile picture
        img.setImageBitmap(ImageOperations.base64ToBitmap(updatedUser.image));
        HomeActivity.user.image = updatedUser.image;
        HomeActivity.user.userName = updatedUser.userName;
        // Update other UI elements
        usernameView.setText(updatedUser.userName);
        emailView.setText(updatedUser.email);

        // Refresh the video list
        videoListLayout.removeAllViews();
        displayUserVideos();
    }

    private void displayUserVideos() {
        List<Video> videos = new VideoViewModel(this).getVideosByUsername(user.userName);
        for (Video video : videos) {
            View videoView = getLayoutInflater().inflate(R.layout.item_video_user, null);
            ImageView thumbnail = videoView.findViewById(R.id.video_thumbnail);
            TextView title = videoView.findViewById(R.id.video_title);
            TextView views = videoView.findViewById(R.id.video_views);

            thumbnail.setImageBitmap(ImageOperations.base64ToBitmap(video.getThumbnail()));
            title.setText(video.getTitle());
            views.setText(video.getViews() + " views");

            videoView.setOnClickListener(v -> {
                Intent intent = new Intent(UserActivity.this, VideoPlayerActivity.class);
                intent.putExtra("video", video);
                intent.putExtra("user", user);
                startActivity(intent);
            });

            videoListLayout.addView(videoView);
        }
    }


    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermission.launch(Manifest.permission.CAMERA);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists())
            storageDir.mkdirs();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myapplication.provider",
                        photoFile);
                filepath = photoURI;
                camera.launch(filepath);
            }
        }
    }
}
