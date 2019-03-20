package com.work.braincraftdemo;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

public class ShowActionVideo extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_action_video);

        videoView = findViewById(R.id.videoView);

        videoView.setVideoURI(Uri.parse(getIntent().getStringExtra("path")));
        videoView.setZOrderOnTop(true);
        videoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.pause();
    }
}
