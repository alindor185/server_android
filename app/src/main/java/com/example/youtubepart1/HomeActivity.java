package com.example.youtubepart1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorWindow;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.TimeUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class HomeActivity extends AppCompatActivity {

    private static final int ADD_VIDEO_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private ImageView iconAdd;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private boolean isLoggedIn = false;
    private ActivityResultLauncher<Intent> addVideoActivityResultLauncher;
    public static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        try {
            Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getIntent().getExtras() != null)
            user = (User)getIntent().getExtras().getSerializable("user");
        // Initialize the ActivityResultLauncher
        addVideoActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String videoName = data.getStringExtra("videoName");
                            String videoUrl = data.getStringExtra("videoUrl");
                            String thumbnailUri = data.getStringExtra("thumbnailUri");
                            Uri uri = Uri.fromFile(new File(thumbnailUri));
                            String thumbnail = "";
                            try {
                                thumbnail = ImageOperations.bitmapToBase64(ImageOperations.uriToBitmap(this, uri));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            Video newVideo = new Video(videoName, videoUrl, thumbnail, 0, "now", new String[0], user.userName, user.image);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    JSONObject object = new Server(HomeActivity.this).createVideo(newVideo);
                                    try {
                                        newVideo.setUploaderId(object.getJSONObject("uploader").getString("id"));
                                        newVideo.set_id(object.getString("_id"));
                                        new VideoViewModel(HomeActivity.this).insert(newVideo);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            videoAdapter.updateList(HomeActivity.this);
                                            videoAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                });

        // Check login status on app start
        isLoggedIn = user != null;
        if (isLoggedIn) {
            ImageView profileImage = findViewById(R.id.icon_profile);
            profileImage.setImageBitmap(ImageOperations.base64ToBitmap(user.image));
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView search = findViewById(R.id.icon_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageView iconSignIn = findViewById(R.id.icon_sign_in);
        TextView navSignIn = findViewById(R.id.nav_sign_in);
        ImageView iconSignOut = findViewById(R.id.icon_sign_out);
        TextView navSignOut = findViewById(R.id.nav_sign_out);

        toggle = new ActionBarDrawerToggle(HomeActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        if (isDarkMode) {
            ImageView youtube = findViewById(R.id.logo);
            youtube.setImageResource(R.drawable.ic_youtube_dark);
            youtube = findViewById(R.id.icon_dark_mode);
            youtube.setImageResource(R.drawable.ic_darkmode_dark);
            youtube = findViewById(R.id.icon_search);
            youtube.setImageResource(R.drawable.search_white);
            youtube = findViewById(R.id.icon_menu);
            youtube.setImageResource(R.drawable.menu_white);
            youtube = findViewById(R.id.icon_rss);
            youtube.setImageResource(R.drawable.rss_white);

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Handle navigation view item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Handle the home action
            } else if (id == R.id.nav_shorts) {
                // Handle the shorts action
            } else if (id == R.id.nav_settings) {
                // Handle the settings action
            } else if (id == R.id.nav_sign_in) {
                // Handle the sign-in action
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }

            else if (id == R.id.nav_sign_out) {
                // Handle the sign-in action
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        iconSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        navSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
            startActivity(intent);
            user=null;
        });
        iconSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
            startActivity(intent);
            user=null;
        });

        navSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.icon_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user == null) {
                    ToastManager.showToast("You have to log in.", HomeActivity.this);
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, UserActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("existingUser", user);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        iconAdd = findViewById(R.id.icon_add);

        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));

        // Check login status
        if (isLoggedIn) {
            iconAdd.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AddVideoActivity.class);
                intent.putExtra("user", user);
                addVideoActivityResultLauncher.launch(intent);
            });
        } else {
            iconAdd.setOnClickListener(v -> {
                // Redirect to sign-in
                ToastManager.showToast("You have to log in", HomeActivity.this);
            });
        }
        findViewById(R.id.icon_dark_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
                boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
                if (isDarkMode)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                sharedPreferences.edit().putBoolean("darkMode", !isDarkMode).apply();
            }
        });
        loadSampleVideos();
        ImageView iconMenu = findViewById(R.id.icon_menu);
        iconMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (videoAdapter != null)
            videoAdapter.updateList(this);
    }

    private void loadSampleVideos() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (File file : getExternalFilesDir(null).listFiles())
                    file.delete();
                VideoViewModel videoViewModel = new VideoViewModel(HomeActivity.this);
                videoViewModel.deleteAll();
                JSONArray videos = new Server(HomeActivity.this).getVideos();
                if (videos != null) {
                    long oldTime = System.currentTimeMillis();
                    CountDownLatch latch = new CountDownLatch(videos.length());
                    for (int i = 0; i < videos.length(); i++) {
                        final int threadI = i;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    videoViewModel.insert(new Video(HomeActivity.this, videos.getJSONObject(threadI)));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                latch.countDown();
                            }
                        }).start();
                    }
                    try {
                        latch.await();
                    } catch (InterruptedException ignored) {
                    }
                    System.out.println(System.currentTimeMillis()-oldTime);
                }
                videoAdapter = new VideoAdapter(HomeActivity.this, video -> {
                    Intent intent = new Intent(HomeActivity.this, VideoPlayerActivity.class);
                    intent.putExtra("video", video);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }, user);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(videoAdapter);
                        drawerLayout.setVisibility(View.VISIBLE);
                        findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }
}
