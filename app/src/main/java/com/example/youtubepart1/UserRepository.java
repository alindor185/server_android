package com.example.youtubepart1;

import android.content.Context;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

public class UserRepository {
    UserDao userDao;
    public UserRepository(Context context) {
        userDao = AppDatabase.getDatabase(context).userDao();
    }

    public User getUser(String userName) {
        return userDao.getUser(userName);
    }
    public void insert(User user) {
        userDao.insert(user);
    }
    public void delete(User user) {
        userDao.delete(user);
    }

}
