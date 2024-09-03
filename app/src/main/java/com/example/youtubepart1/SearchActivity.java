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
    private List<Video> videoList;
    private List<Video> filteredList;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        user = (User) getIntent().getSerializableExtra("user");
        SearchView searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        filteredList = new ArrayList<>();

        videoAdapter = new VideoAdapter(this, new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Video video) {
                Intent intent = new Intent(SearchActivity.this, VideoPlayerActivity.class);
                intent.putExtra("video", video);
                startActivity(intent);
            }
        }, user);

        recyclerView.setAdapter(videoAdapter);

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

    private void filter(String text) {
        filteredList.clear();
        for (Video video : videoList) {
            if (video.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(video);
            }
        }
        videoAdapter.updateList(filteredList);
    }
}
