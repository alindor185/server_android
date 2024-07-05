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

        videoList = loadSampleVideos(); // Load the sample videos or your actual video list
        filteredList = new ArrayList<>(videoList);

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

    private List<Video> loadSampleVideos() {
        List<Video> sampleVideos = new ArrayList<>();
        sampleVideos.add(new Video(this, "Eden Golan - Hurricane", "android.resource://" + getPackageName() + "/" + R.raw.huricane, R.drawable.huricane, "1M views", "2 days ago", "Eurovision Song Contest", new String[]{"Great video!", "Loved it!", "Awesome!", "Nice song!", "Cool!"}));
        sampleVideos.add(new Video(this, "אושר כהן - מנגן ושר", "android.resource://" + getPackageName() + "/" + R.raw.oshercohen, R.drawable.oshercohen, "550K views", "1 week ago", "Music Channel", new String[]{"Amazing!", "Beautiful!", "Loved it!", "Great performance!", "Wonderful!"}));
        sampleVideos.add(new Video(this, "Java Tutorial", "android.resource://" + getPackageName() + "/" + R.raw.javatoutorial, R.drawable.javatoutorial, "300K views", "3 days ago", "Tutorial Channel", new String[]{"Very helpful!", "Thanks for the tutorial!", "Great explanation!", "Learned a lot!", "Nice tutorial!"}));
        sampleVideos.add(new Video(this, "Dallas vs Minnesota Game 5", "android.resource://" + getPackageName() + "/" + R.raw.dalmin5, R.drawable.dalmin5, "800K views", "5 days ago", "Sports Channel", new String[]{"Great game!", "Awesome match!", "Exciting!", "Loved it!", "Nice game!"}));
        sampleVideos.add(new Video(this, "מאיר בנאי - לך אלי (אודיו)", "android.resource://" + getPackageName() + "/" + R.raw.meirbanai, R.drawable.meirbanai, "400K views", "1 month ago", "Music Channel", new String[]{"Beautiful song!", "Loved it!", "Great voice!", "Amazing!", "Wonderful!"}));
        sampleVideos.add(new Video(this, "Lionel Messi best plays", "android.resource://" + getPackageName() + "/" + R.raw.messi, R.drawable.messi, "1.2M views", "2 weeks ago", "Sports Channel", new String[]{"Awesome plays!", "Great player!", "Loved it!", "Amazing!", "Nice video!"}));
        sampleVideos.add(new Video(this, "דודו טסה - בסוף מתרגלים להכל", "android.resource://" + getPackageName() + "/" + R.raw.dudutasa, R.drawable.dudutasa, "600K views", "3 weeks ago", "Music Channel", new String[]{"Great song!", "Loved it!", "Amazing voice!", "Nice!", "Beautiful!"}));
        sampleVideos.add(new Video(this, "קוקי לבנה - כמה הייתי רוצה", "android.resource://" + getPackageName() + "/" + R.raw.kukilevana, R.drawable.kukilevana, "700K views", "4 days ago", "Music Channel", new String[]{"Wonderful!", "Loved it!", "Amazing!", "Nice song!", "Great!"}));
        sampleVideos.add(new Video(this, "בית הבובות - סיגפו", "android.resource://" + getPackageName() + "/" + R.raw.sigapo, R.drawable.sigapo, "350K views", "2 months ago", "Music Channel", new String[]{"Great song!", "Loved it!", "Nice!", "Wonderful!", "Beautiful!"}));
        sampleVideos.add(new Video(this, "עמיר בניון - ניצחת איתי הכל", "android.resource://" + getPackageName() + "/" + R.raw.mazal, R.drawable.mazal, "900K views", "6 days ago", "Music Channel", new String[]{"Amazing!", "Loved it!", "Great song!", "Wonderful!", "Nice!"}));
        sampleVideos.add(new Video(this, "ABBA - Gimme! Gimme! Gimme! (A Man After Midnight)", "android.resource://" + getPackageName() + "/" + R.raw.gimmegimme, R.drawable.gimmegimme, "2M views", "1 year ago", "Classic Music Channel", new String[]{"Great song!", "Loved it!", "Amazing!", "Nice!", "Wonderful!"}));
        return sampleVideos;
    }
}
