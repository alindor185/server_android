package com.example.youtubepart1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Entity
public class Video implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String title;
    private String filePath;
    private String thumbnail;
    private int views;
    private String uploadDate;
    private String comments;
    private String userName;
    private String profilePic;
    private boolean isLiked;
    private int likeCount;
    private String _id;
    private String uploaderId;

    public Video(String title, String filePath, String thumbnail, int views, String uploadDate, String comments, String userName, String profilePic, boolean isLiked, int likeCount, String _id, String uploaderId) {
        this.title = title;
        this.filePath = filePath;
        this.thumbnail = thumbnail;
        this.views = views;
        this.uploadDate = uploadDate;
        this.userName = userName;
        this.profilePic = profilePic;
        this.comments = comments;
        this.isLiked = isLiked;
        this.likeCount =  likeCount;
        this._id = _id;
        this.uploaderId = uploaderId;
    }

    public Video(String title, String url, String thumbnail, int views, String uploadDate, String[] initialComments, String userName, String base64ProfilePic) {
        this.title = title;
        this.filePath = url;
        this.thumbnail = thumbnail;
        this.views = views;
        this.uploadDate = uploadDate;
        this.userName = userName;
        profilePic = base64ProfilePic;
        List<Comment> commentsList = new ArrayList<>();
        if (initialComments != null) {
            for (String comment : initialComments) {
                commentsList.add(new Comment(userName, "", comment, "")); // Placeholder, update as needed
            }
        }
        Gson gson = new Gson();
        comments = gson.toJson(commentsList);
        final int SHORT_ID_LENGTH = 24;
        this._id = getRandomHexString(SHORT_ID_LENGTH);
        this.uploaderId = getRandomHexString(SHORT_ID_LENGTH);
    }

    public String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

    public Video(Context context, JSONObject video) {
        try {
            this.title = video.getString("title");
            String fileUrl = "http://10.0.2.2:5000"+video.getString("videoFile");
            this.filePath = context.getExternalFilesDir(null).getAbsolutePath()+"/file"+System.currentTimeMillis()+".mp4";
            download(fileUrl, filePath);
            URL url = null;
            if (video.getString("thumbnail").startsWith("https://"))
                url = new URL(video.getString("thumbnail"));
            else
                url = new URL("http://10.0.2.2:5000"+video.getString("thumbnail"));
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            this.thumbnail = ImageOperations.bitmapToBase64(BitmapUtils.getResizedBitmap(image));
            this.views = video.getInt("views");
            this.uploadDate = video.getString("uploadDate");
            this.userName = video.getJSONObject("uploader").getString("username");
            Bitmap resizedProfile = BitmapUtils.getResizedBitmap(ImageOperations.base64ToBitmap(video.getJSONObject("uploader").getString("profilePicture").substring("data:image/png;base64,".length())));
            this.profilePic = ImageOperations.bitmapToBase64(resizedProfile);
            ArrayList<Comment> commentsList = new ArrayList<>();
            Gson gson = new Gson();
            this.comments = gson.toJson(commentsList);
            this.isLiked = false;
            this.likeCount = 0;
            this._id = video.getString("_id");
            this.uploaderId = video.getJSONObject("uploader").getString("id");
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void download(String url, String filePath) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter methods
    public String getTitle() {
        return title;
    }

    public String getUserName() {
        return userName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getUploadDate() {
        return uploadDate;
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

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    // Nested Comment class
    public static class Comment {
        private String _id;
        private String name;
        private String time;
        private String text;
        private String userId;
        private String profilePic;

        public Comment(String name, String time, String text, String profilePic) {
            this._id = getRandomHexString(24);
            this.name = name;
            this.time = time;
            this.text = text;
            this.profilePic = profilePic;
        }

        public Comment(String _id, String name, String time, String text, String profilePic) {
            this._id = _id;
            this.name = name;
            this.time = time;
            this.text = text;
            this.profilePic = profilePic;
        }

        private String getRandomHexString(int numchars){
            Random r = new Random();
            StringBuffer sb = new StringBuffer();
            while(sb.length() < numchars){
                sb.append(Integer.toHexString(r.nextInt()));
            }

            return sb.toString().substring(0, numchars);
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

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }
}
