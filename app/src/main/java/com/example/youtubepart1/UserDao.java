package com.example.youtubepart1;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("select * from User where userName=:userName")
    User getUser(String userName);
    @Insert
    void insert(User user);
}
