package com.example.universitywalkingtour;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    boolean TextShow = true;
    TextView myText =  (TextView) findViewById(R.id.txtView);
    Button startTrip;
    Button startDirecting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("UOP Walk");

        //media player
        MediaPlayer mp = new MediaPlayer();
        String audioPath = "android.resource://" + getPackageName() + "/" + R.raw.instructions;
        try {
            mp.setDataSource(this, Uri.parse(audioPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.d("debug","value: " + audioPath);



        //Video
        VideoView videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.video_instructions;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        MediaController mediaController = new MediaController( this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                mp.start();
            }
        });
        mp.prepareAsync();

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(TextShow == true ){
                    TextShow = false;
                    myText.setVisibility(v.INVISIBLE);

                }
                else{
                    TextShow = true;
                    myText.setVisibility(v.VISIBLE);
                }
                return false;
            }

        });

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