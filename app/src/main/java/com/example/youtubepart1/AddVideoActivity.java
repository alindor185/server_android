package com.example.youtubepart1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddVideoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;

    private EditText videoNameEditText;
    private EditText videoDescriptionEditText;
    private EditText videoUrlEditText;
    private ImageView thumbnailImageView;
    private Button uploadThumbnailButton;
    private Button uploadVideoButton;
    private Button addButton;
    private Button cancelButton;
    private String thumbnailPath;
    private String filePath;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        // Check if the user is logged in
        if (getIntent().getExtras() != null)
            user = (User)getIntent().getExtras().getSerializable("user");
        if (user == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(AddVideoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        videoNameEditText = findViewById(R.id.videoNameEditText);
        videoDescriptionEditText = findViewById(R.id.videoDescriptionEditText);
        videoUrlEditText = findViewById(R.id.videoUrlEditText);
        thumbnailImageView = findViewById(R.id.thumbnailImageView);
        uploadThumbnailButton = findViewById(R.id.uploadThumbnailButton);
        uploadVideoButton = findViewById(R.id.uploadVideoButton);
        addButton = findViewById(R.id.addButton);
        cancelButton = findViewById(R.id.cancelButton);

        uploadThumbnailButton.setOnClickListener(v -> openImageChooser());
        uploadVideoButton.setOnClickListener(v -> openVideoChooser());

        addButton.setOnClickListener(v -> {
            String name = videoNameEditText.getText().toString().trim();
            String description = videoDescriptionEditText.getText().toString().trim();
            String url = videoUrlEditText.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || (url.isEmpty() && filePath == null)) {
                Toast.makeText(AddVideoActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("videoName", name);
            resultIntent.putExtra("videoDescription", description);
            resultIntent.putExtra("videoUrl", filePath);
            if (thumbnailPath != null) {
                resultIntent.putExtra("thumbnailUri", thumbnailPath);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openVideoChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            thumbnailPath = savefile(data.getData());
            try {
                thumbnailImageView.setImageBitmap(ImageOperations.uriToBitmap(this, data.getData()));
                thumbnailImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = savefile(data.getData());
            videoUrlEditText.setText(filePath);
        }
    }

    String savefile(Uri sourceuri) {
        String destinationFilename = getExternalFilesDir(null).getAbsolutePath() + File.separatorChar+System.nanoTime()+".mp4";

        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(getContentResolver().openInputStream(sourceuri));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BufferedOutputStream bos = null;

        try {
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return destinationFilename;
    }
}
