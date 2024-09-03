package com.example.youtubepart1;

import android.content.Context;

import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {
    UserRepository repository;
    public UserViewModel(Context context) {
        repository = new UserRepository(context);
    }

    public User getUser(String userName) {
        return repository.getUser(userName);
    }
    public void insert(User user) {
        repository.insert(user);
    }
    public void delete(User user) {
        repository.delete(user);
    }
}
