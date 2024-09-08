package com.example.youtubepart1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<Video> allVideos;
    private List<Video> filteredVideos;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        user = (User) getIntent().getSerializableExtra("user");
        SearchView searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allVideos = new ArrayList<>();
        filteredVideos = new ArrayList<>();

        videoAdapter = new VideoAdapter(this, new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Video video) {
                Intent intent = new Intent(SearchActivity.this, VideoPlayerActivity.class);
                intent.putExtra("video", video);
                startActivity(intent);
            }
        }, user);

        recyclerView.setAdapter(videoAdapter);

        // Load videos from server
        loadVideosFromServer();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void loadVideosFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                VideoViewModel videoViewModel = new VideoViewModel(SearchActivity.this);
                allVideos = videoViewModel.getVideos();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        filteredVideos.addAll(allVideos);
                        videoAdapter.updateList(filteredVideos);
                    }
                });
            }
        }).start();
    }

    private void filter(String text) {
        filteredVideos.clear();
        if (text.isEmpty()) {
            filteredVideos.addAll(allVideos);
        } else {
            for (Video video : allVideos) {
                if (video.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    filteredVideos.add(video);
                }
            }
        }
        videoAdapter.updateList(filteredVideos);
    }
}
