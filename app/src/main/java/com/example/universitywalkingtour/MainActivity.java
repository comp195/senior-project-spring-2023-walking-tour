package com.example.universitywalkingtour;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;

public class MainActivity extends AppCompatActivity {

    Button Start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VideoView videoView = findViewById(R.id.videoView);
        String videoPath = "android.resource://"+ getPackageName() + "/" + R.raw.Video_Instructions;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        MediaController mediaController = new MediaController( context: this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        Start = (Button)findViewById(R.id.button);
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toStart = new Intent(MainActivity.this, start_page.class);
                startActivity(toStart);

            }
        });
    }
}