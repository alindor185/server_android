package com.example.youtubepart1;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity implements CommentsAdapter.CommentActionsListener {

    private static final String TAG = "VideoPlayerActivity";
    private VideoView videoView;
    private TextView videoTitle;
    private TextView channelViews;
    private ImageView iconLike;
    private ImageView iconDislike;
    private ImageView iconComment;
    private ImageView iconSubscribe;
    private ImageView iconShare;
    private TextView commentsSection;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ImageView btnPlayPause;
    private ImageView btnNext;
    private ImageView btnPrevious;

    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private List<Video.Comment> commentList;
    private EditText addCommentEditText;
    private Button submitCommentButton;

    private boolean isLiked = false;
    private boolean isDisliked = false;
    private int likeCount = 100; // Default like count
    private int dislikeCount = 10; // Default dislike count
    private TextView likeCountText;
    private TextView dislikeCountText;
    private Handler handler = new Handler();
    private User user;
    private Video video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoView);
        videoTitle = findViewById(R.id.videoTitle);
        channelViews = findViewById(R.id.channelViews);
        iconLike = findViewById(R.id.icon_like);
        iconDislike = findViewById(R.id.icon_dislike);
        iconSubscribe = findViewById(R.id.icon_subscribe);
        iconShare = findViewById(R.id.icon_share);
        commentsSection = findViewById(R.id.commentsSection);
        likeCountText = findViewById(R.id.like_count);
        dislikeCountText = findViewById(R.id.dislike_count);
        addCommentEditText = findViewById(R.id.add_comment);
        submitCommentButton = findViewById(R.id.submit_comment);
        RelativeLayout edit_delete = findViewById(R.id.edit_delete);
        ImageView edit = findViewById(R.id.icon_edit);
        ImageView delete = findViewById(R.id.icon_delete);
        video = (Video) getIntent().getSerializableExtra("video");
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View edit_layout = LayoutInflater.from(VideoPlayerActivity.this).inflate(R.layout.edit_layout, null);
                edit_layout.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newTitle = ((EditText)edit_layout.findViewById(R.id.editText)).getText().toString();
                        videoTitle.setText(newTitle);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase.getDatabase(VideoPlayerActivity.this).videoDao().changeName(video.id, newTitle);
                            }
                        }).start();
                    }
                });
                new AlertDialog.Builder(VideoPlayerActivity.this).setView(edit_layout).create().show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getDatabase(VideoPlayerActivity.this).videoDao().delete(video);
                        finish();
                    }
                }).start();
            }
        });
        // Initialize comments RecyclerView
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentList = new ArrayList<>();
        if (getIntent().getExtras() != null)
            user = (User) getIntent().getExtras().getSerializable("user");
        String currentUserName = user != null ? user.userName : null;
        commentsAdapter = new CommentsAdapter(this, commentList, this, currentUserName);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        // Get data from intent
        String title = video.getTitle();
        String url = video.getUrl();
        String views = video.getViews();
        String channelName = video.getChannelName();
        boolean isFile = video.getIsFile();
        if (video.getUserName() != null && video.getUserName().equals(currentUserName)) {
            edit_delete.setVisibility(View.VISIBLE);
        }
        // Set data to views
        if (title != null) {
            videoTitle.setText(title);
        } else {
            videoTitle.setText("No title available");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        if (isDarkMode) {
            videoTitle.setTextColor(Color.WHITE);
        }

        if (channelName != null && views != null) {
            channelViews.setText(String.format("%s • %s", channelName, views));
        } else {
            channelViews.setText("No channel or views information available");
        }

        // Check if videoUrl is not null
        if (url != null) {
            // Log the video URI for debugging purposes
            Log.d(TAG, "Video URL: " + url);

            // Use ContentResolver to open the content URI
            try {
                if (isFile)
                    videoView.setVideoPath(url);
                else
                    videoView.setVideoURI(Uri.parse(url));
                MediaController mediaController = new MediaController(this);
                videoView.setMediaController(mediaController);
                videoView.start();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to open video URI", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to open video URI", e);
            }
        } else {
            // Handle the case where videoUrl is null
            Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Video URL is missing");
        }

        iconLike.setOnClickListener(this::onLikeClicked);
        iconDislike.setOnClickListener(this::onDislikeClicked);
        iconShare.setOnClickListener(v -> showShareDialog());
        isLiked = video.getIsLiked();
        likeCount = video.getLikeCount();
        submitCommentButton.setOnClickListener(v -> submitComment());

        updateLikeDislikeUI();

        // Add sample comments
        addSampleComments();
        commentList.addAll(video.getCommentsList());
    }

    private void showShareDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_share, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void submitComment() {
        if (user != null) {
            String commentText = addCommentEditText.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Video.Comment newComment = new Video.Comment(user.userName, "Just now", commentText, user.image);
            List<Video.Comment> comments = video.getCommentsList();
            comments.add(newComment);
            video.setCommentsList(comments);
            AppDatabase.getDatabase(this).videoDao().update(video);
            commentsAdapter.addComment(newComment);
            addCommentEditText.setText("");
            commentsRecyclerView.smoothScrollToPosition(commentsAdapter.getItemCount() - 1);
        } else
            ToastManager.showToast("You have to log in", VideoPlayerActivity.this);
    }

    private void addSampleComments() {
        commentList.add(new Video.Comment(this, "User1", "2 hours ago", "Great video!", R.drawable.ic_profile));
        commentList.add(new Video.Comment(this, "User2", "1 hour ago", "Thanks for sharing.", R.drawable.ic_profile));
        commentList.add(new Video.Comment(this, "User3", "30 minutes ago", "Very informative.", R.drawable.ic_profile));
        commentsAdapter.notifyDataSetChanged();
    }

    private void onLikeClicked(View view) {
        if (isLiked) {
            likeCount--;
        } else {
            likeCount++;
            if (isDisliked) {
                dislikeCount--;
                isDisliked = false;
            }
        }
        isLiked = !isLiked;
        video.setIsLiked(isLiked);
        video.setLikeCount(likeCount);
        AppDatabase.getDatabase(this).videoDao().update(video);
        updateLikeDislikeUI();
    }

    private void onDislikeClicked(View view) {
        if (isDisliked) {
            dislikeCount--;
        } else {
            dislikeCount++;
            if (isLiked) {
                likeCount--;
                isLiked = false;
            }
        }
        isDisliked = !isDisliked;
        video.setIsLiked(isLiked);
        video.setLikeCount(likeCount);
        AppDatabase.getDatabase(this).videoDao().update(video);
        updateLikeDislikeUI();
    }

    private void updateLikeDislikeUI() {
        likeCountText.setText(String.valueOf(likeCount));
        dislikeCountText.setText(String.valueOf(dislikeCount));
        iconLike.setImageResource(isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like);
        iconDislike.setImageResource(isDisliked ? R.drawable.ic_dislike_filled : R.drawable.ic_dislike);
    }

    private void togglePlayPause() {
        if (videoView.isPlaying()) {
            videoView.pause();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
            videoView.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void updateSeekBar() {
        handler.postDelayed(updateSeekBarRunnable, 1000);
    }

    private Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (videoView != null && videoView.isPlaying()) {
                int currentPosition = videoView.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onEditComment(int position) {
        Video.Comment comment = commentList.get(position);
        addCommentEditText.setText(comment.getText());
        submitCommentButton.setOnClickListener(v -> updateComment(position));
    }
    private void updateComment(int position) {
        String updatedText = addCommentEditText.getText().toString().trim();
        if (updatedText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Video.Comment updatedComment = new Video.Comment(user.userName, "Just now", updatedText, user.image);
        commentsAdapter.updateComment(position, updatedComment);

        List<Video.Comment> comments = video.getCommentsList();
        comments.set(position + comments.size() - commentList.size(), updatedComment);
        video.setCommentsList(comments);

        // Update the database
        new Thread(() -> {
            AppDatabase.getDatabase(this).videoDao().update(video);
        }).start();

        addCommentEditText.setText("");
        submitCommentButton.setOnClickListener(v -> submitComment());
    }

    @Override
    public void onDeleteComment(int position) {
        commentsAdapter.deleteComment(position);

        List<Video.Comment> comments = video.getCommentsList();
        comments.remove(position + comments.size() - commentList.size()-1);
        video.setCommentsList(comments);

        // Update the database
        new Thread(() -> {
            AppDatabase.getDatabase(this).videoDao().update(video);
        }).start();

        submitCommentButton.setOnClickListener(v -> submitComment());
    }
}