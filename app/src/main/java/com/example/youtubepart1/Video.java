package com.example.youtubepart1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Video implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String title;
    private String url;
    private String thumbnail;
    private String views;
    private String uploadDate;
    private String channelName;
    private String comments;
    private boolean isFile;
    private String userName;
    private String profilePic;
    private boolean isLiked;
    private int likeCount;

    public Video(String title, String url, String thumbnail, String views, String uploadDate, String channelName, String comments, String userName, String profilePic, boolean isFile, boolean isLiked, int likeCount) {
        this.title = title;
        this.url = url;
        this.thumbnail = thumbnail;
        this.views = views;
        this.uploadDate = uploadDate;
        this.channelName = channelName;
        this.isFile = isFile;
        this.userName = userName;
        this.profilePic = profilePic;
        this.comments = comments;
        this.isLiked = isLiked;
        this.likeCount =  likeCount;
    }

    public Video(String title, String url, String thumbnail, String views, String uploadDate, String channelName, String[] initialComments, String userName, String base64ProfilePic) {
        this.title = title;
        this.url = url;
        this.thumbnail = thumbnail;
        this.views = views;
        this.uploadDate = uploadDate;
        this.channelName = channelName;
        isFile = true;
        this.userName = userName;
        profilePic = base64ProfilePic;
        List<Comment> commentsList = new ArrayList<>();
        if (initialComments != null) {
            for (String comment : initialComments) {
                commentsList.add(new Comment("User", "some time", comment, base64ProfilePic)); // Placeholder, update as needed
            }
        }
        Gson gson = new Gson();
        comments = gson.toJson(commentsList);
    }

    public Video(Context context, String title, String url, int thumbnail, String views, String uploadDate, String channelName, String[] initialComments) {
        this.title = title;
        this.url = url;
        Bitmap image = BitmapUtils.getResizedBitmap(BitmapFactory.decodeResource(context.getResources(), thumbnail));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        String imageBase64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        this.thumbnail = imageBase64;
        this.views = views;
        this.uploadDate = uploadDate;
        this.channelName = channelName;
        isFile = false;
        List<Comment> commentsList = new ArrayList<>();
        Bitmap image2 = BitmapUtils.getResizedBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile));
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        image2.compress(Bitmap.CompressFormat.PNG, 100, bos2);
        profilePic = Base64.encodeToString(bos2.toByteArray(), Base64.DEFAULT);
        if (initialComments != null) {
            for (String comment : initialComments) {
                commentsList.add(new Comment("User", "some time", comment, profilePic)); // Placeholder, update as needed
            }
        }
        Gson gson = new Gson();
        comments = gson.toJson(commentsList);
    }

    // Getter methods
    public String getTitle() {
        return title;
    }

    public String getUserName() {
        return userName;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getViews() {
        return views;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setCommentsList(List<Comment> comments) {
        this.comments = new Gson().toJson(comments);
    }

    public List<Comment> getCommentsList() {
        return new Gson().fromJson(comments, new TypeToken<List<Comment>>(){}.getType());
    }

    // Setter method for thumbnail
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    public boolean getIsFile() {
        return isFile;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    // Nested Comment class
    public static class Comment {
        private String name;
        private String time;
        private String text;
        private String userId;
        private String profilePic;

        public Comment(String name, String time, String text, String profilePic) {
            this.name = name;
            this.time = time;
            this.text = text;
            this.profilePic = profilePic;
        }

        public Comment(Context context, String name, String time, String text, int profilePic) {
            this.name = name;
            this.time = time;
            this.text = text;
            Bitmap image2 = BitmapUtils.getResizedBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile));
            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
            image2.compress(Bitmap.CompressFormat.PNG, 100, bos2);
            this.profilePic = Base64.encodeToString(bos2.toByteArray(), Base64.DEFAULT);
        }

        // Getter methods
        public String getName() {
            return name;
        }

        public String getTime() {
            return time;
        }

        public String getText() {
            return text;
        }

        public String getProfilePic() {
            return profilePic;
        }

    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
