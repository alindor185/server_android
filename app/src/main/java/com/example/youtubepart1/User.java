package com.example.youtubepart1;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class User implements Serializable {
    public User(String userName, String password, String email, String image) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.image = image;
    }
    @NonNull
    @PrimaryKey
    public String userName;
    public String password;
    public String email;
    public String image;
}
