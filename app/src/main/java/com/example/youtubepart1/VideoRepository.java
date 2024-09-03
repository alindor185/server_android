package com.example.youtubepart1;

import android.content.Context;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

public class VideoRepository {
    VideoDao videoDao;
    public VideoRepository(Context context) {
        videoDao = AppDatabase.getDatabase(context).videoDao();
    }

    public List<Video> getVideos() {
        return videoDao.getVideos();
    }

    public Video getVideo(int id) {
        return videoDao.getVideo(id);
    }

    public void insert(Video video) {
        videoDao.insert(video);
    }
    public void changeName(int id, String newName) {
        videoDao.changeName(id, newName);
    }
    public void delete(Video video) {
        videoDao.delete(video);
    }
    public void update(Video video) {
        videoDao.update(video);
    }
    public List<Video> getVideosByUsername(String username) {
        return videoDao.getVideosByUsername(username);
    }

    public void deleteAll() {
        videoDao.deleteAll();
    }
}
