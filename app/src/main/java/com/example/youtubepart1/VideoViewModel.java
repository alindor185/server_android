package com.example.youtubepart1;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.util.List;

public class VideoViewModel extends ViewModel {
    VideoRepository repository;
    public VideoViewModel(Context context) {
        repository = new VideoRepository(context);
    }

    public List<Video> getVideos() {
        return repository.getVideos();
    }
    public void insert(Video video) {
        repository.insert(video);
    }
    public void changeName(int id, String newName) {
        repository.changeName(id, newName);
    }
    public void delete(Video video) {
        repository.delete(video);
    }
    public void update(Video video) {
        repository.update(video);
    }
    public List<Video> getVideosByUsername(String username) {
        return repository.getVideosByUsername(username);
    }
}
