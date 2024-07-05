package com.example.youtubepart1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VideoDao {
    @Query("select * from video")
    List<Video> getVideos();

    @Insert
    void insert(Video video);

    @Query("update video set title=:newName where id=:id")
    void changeName(int id, String newName);

    @Delete
    void delete(Video video);
    @Update
    void update(Video video);
    @Query("SELECT * FROM video WHERE userName = :username")
    List<Video> getVideosByUsername(String username);


}