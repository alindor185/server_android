package com.example.youtubepart1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONException;
import org.json.JSONObject;

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
                                new VideoViewModel(VideoPlayerActivity.this).changeName(video.id, newTitle);
                                new Server(VideoPlayerActivity.this).editVideo(new VideoViewModel(VideoPlayerActivity.this).getVideo(video.id));
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
                        new VideoViewModel(VideoPlayerActivity.this).delete(video);
                        new Server(VideoPlayerActivity.this).deleteVideo(video);
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
        String url = video.getFilePath();
        int views = video.getViews();
        channelViews.setText(views+" views");
        new Thread(new Runnable() {
            @Override
            public void run() {
                video.setViews(video.getViews()+1);
                new VideoViewModel(VideoPlayerActivity.this).delete(video);
                new VideoViewModel(VideoPlayerActivity.this).insert(video);
                new Server(VideoPlayerActivity.this).incrementVideoViews(video);
            }
        }).start();
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

        // Check if videoUrl is not null
        if (url != null) {
            // Log the video URI for debugging purposes
            Log.d(TAG, "Video URL: " + url);

            // Use ContentResolver to open the content URI
            try {
                videoView.setVideoPath(url);
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
            new VideoViewModel(VideoPlayerActivity.this).update(video);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject comment = new Server(VideoPlayerActivity.this).addComment(newComment, video.get_id());
                    List<Video.Comment> comments2 = video.getCommentsList();
                    try {
                        comments2.get(comments2.size()-1).set_id(comment.getString("_id"));
                        video.setCommentsList(comments2);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            commentsAdapter.addComment(newComment);
            addCommentEditText.setText("");
            commentsRecyclerView.smoothScrollToPosition(commentsAdapter.getItemCount() - 1);
        } else
            ToastManager.showToast("You have to log in", VideoPlayerActivity.this);
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
        new VideoViewModel(VideoPlayerActivity.this).update(video);
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
        new VideoViewModel(this).update(video);
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
    }@Override
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
        String oldId = comments.get(position).get_id();
        comments.set(position + comments.size() - commentList.size(), updatedComment);
        video.setCommentsList(comments);

        // Update the database
        new Thread(() -> {
            try {
                new VideoViewModel(VideoPlayerActivity.this).update(video);
                updatedComment.set_id(oldId);
                JSONObject comment = new Server(VideoPlayerActivity.this).editComment(updatedComment, video.get_id());

                if (comment != null) {
                    Log.d(TAG, "Server response: " + comment.toString());

                    if (comment.has("_id")) {
                        String newId = comment.getString("_id");
                        Log.d(TAG, "Successfully retrieved new comment ID: " + newId);
                        List<Video.Comment> comments2 = video.getCommentsList();
                        if (position >= 0 && position < comments2.size()) {
                            Log.d(TAG, "Updating comment at position: " + position + " with new ID: " + newId);
                            comments2.get(position).set_id(newId);
                            video.setCommentsList(comments2);
                            new Server(VideoPlayerActivity.this).editVideo(video);
                        } else {
                            Log.e(TAG, "Invalid position: " + position);
                        }
                    } else {
                        Log.e(TAG, "Server response does not contain _id field");
                    }
                } else {
                    Log.e(TAG, "Received null response from server");
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error", e);
            } catch (Exception e) {
                Log.e(TAG, "Error updating comment", e);
            }

            runOnUiThread(() -> {
                addCommentEditText.setText("");
                submitCommentButton.setOnClickListener(v -> submitComment());
            });
        }).start();
    }

    @Override
    public void onDeleteComment(int position) {
        commentsAdapter.deleteComment(position);

        List<Video.Comment> comments = video.getCommentsList();
        Video.Comment toDelete = comments.remove(position + comments.size() - commentList.size()-1);
        video.setCommentsList(comments);

        // Update the database
        new Thread(() -> {
            try {
                new VideoViewModel(VideoPlayerActivity.this).update(video);
                JSONObject response = new Server(VideoPlayerActivity.this).deleteComment(toDelete, video.get_id());

                if (response != null) {
                    Log.d(TAG, "Delete comment server response: " + response.toString());
                    if (response.has("message")) {
                        String message = response.getString("message");
                        Log.d(TAG, "Delete comment message: " + message);
                    } else {
                        Log.e(TAG, "Delete comment response does not contain message field");
                    }
                } else {
                    Log.e(TAG, "Received null response from server for delete comment");
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error in delete comment", e);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting comment", e);
            }

            runOnUiThread(() -> {
                submitCommentButton.setOnClickListener(v -> submitComment());
            });
        }).start();
    }
}
