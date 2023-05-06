package com.example.universitywalkingtour;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    boolean TextShow = true;
    TextView myText;
    Button startTrip;
    Button startDirecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("UOP Walk");

        myText = (TextView) findViewById(R.id.txtView);
        myText.setVisibility(View.INVISIBLE);
        //media player
        MediaPlayer mp = new MediaPlayer();
        String audioPath = "android.resource://" + getPackageName() + "/" + R.raw.instructions;
        try {
            mp.setDataSource(this, Uri.parse(audioPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Video
        VideoView videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video_instructions;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController( this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        mp.setVolume(49,49);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                mp.start();
            }
        });


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(TextShow){
                    TextShow = false;
                    videoView.start();
                    mp.start();
                    //myText.setVisibility(View.INVISIBLE);
                } else {
                    mp.pause();
                    videoView.pause();
                    TextShow = true;
                    //myText.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        mp.prepareAsync();
        //Trip Mode button
        startTrip = (Button)findViewById(R.id.button_trip);
        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toStart = new Intent(MainActivity.this, TripActivity.class);
                videoView.stopPlayback();
                mp.stop();
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