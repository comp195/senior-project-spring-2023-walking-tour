package com.example.universitywalkingtour;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {
    Button startTrip;
    Button startDirecting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("UOP Walk");

        //Video
        VideoView videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video_instructions;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController( this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        //Trip Mode button
        startTrip = (Button)findViewById(R.id.button_trip);
        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toStart = new Intent(MainActivity.this, TripActivity.class);
                startActivity(toStart);
            }
        });

        //Directing Mode button
        startDirecting = (Button)findViewById(R.id.button_directing);
        startDirecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toStart = new Intent(MainActivity.this, DirectionActivity.class);
                startActivity(toStart);
            }
        });
    }
}