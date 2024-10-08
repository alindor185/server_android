package com.example.youtubepart1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Video> videoList;
    private OnItemClickListener listener;
    private Activity context;
    private User user;

    public interface OnItemClickListener {
        void onItemClick(Video video);
    }

    public VideoAdapter(Activity context, OnItemClickListener listener, User user) {
        super();
        this.user = user;
        this.context = context;
        videoList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                VideoAdapter.this.videoList.addAll(new VideoViewModel(context).getVideos());
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }).start();
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videoList.get(position);
        holder.bind(video, listener);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateList(Activity context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                VideoAdapter.this.videoList = new VideoViewModel(context).getVideos();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public void updateList(List<Video> videos) {
        videoList = videos;
        notifyDataSetChanged();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;
        private TextView title;
        private TextView views;
        private TextView uploadDate;
        private ImageView iconProfile; // Ensure this variable is correctly named
        private TextView username;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);
            views = itemView.findViewById(R.id.video_views);
            uploadDate = itemView.findViewById(R.id.video_upload_date);
            iconProfile = itemView.findViewById(R.id.icon_profile); // Ensure this line is correct
            username = itemView.findViewById(R.id.username);
        }

        public void bind(final Video video, final OnItemClickListener listener) {
            thumbnail.setImageBitmap(ImageOperations.base64ToBitmap(video.getThumbnail()));
            title.setText(video.getTitle());
            SharedPreferences sharedPreferences = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
            boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
            if (isDarkMode) {
                title.setTextColor(Color.WHITE);
            }
            views.setText(video.getViews()+"");
            uploadDate.setText(video.getUploadDate());
            iconProfile.setImageBitmap(ImageOperations.base64ToBitmap(video.getProfilePic())); // Ensure this line is correct
            username.setText(video.getUserName()); // Ensure this line is correct

            itemView.setOnClickListener(v -> listener.onItemClick(video));
            itemView.findViewById(R.id.icon_profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (video.getUserName() != null) {
                        Intent intent = new Intent(context, UserActivity.class);
                        intent.putExtra("user", new UserViewModel(context).getUser(video.getUserName()));
                        intent.putExtra("existingUser", user);
                        context.startActivity(intent);
                    } else
                        ToastManager.showToast("It's not a real user", context);
                }
            });
        }
    }
}
