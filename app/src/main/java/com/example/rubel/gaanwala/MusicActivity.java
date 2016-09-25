package com.example.rubel.gaanwala;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private static final String MUSIC_POSITION = "com.example.rubel.gaanwala.POSITION";
    private static final String MUSICS_DATA = "com.example.rubel.gaanwala.MUSICS";

    // Related to bound service
    MusicPlayerService musicPlayerService;
    boolean mBound = false;
    boolean mPlaying = false;

    ImageButton mBtnPlay;
    ImageButton mBtnNext;
    ImageButton mBtnPrev;
    TextView mTvMusicTitle;
    SeekBar mSeekbar;
    TextView mSeekbarStart;
    TextView mSeekbarMax;
    ImageView mImageViewMusic;

    Music mMusic;



    Handler mMusicHandler = new Handler();

    Runnable mMusicRunnable = new Runnable() {
        @Override
        public void run() {
            if(mPlaying)
                mSeekbar.setProgress(musicPlayerService.getCurrentPosition());

            mMusicHandler.postDelayed(this, 1000);
        }
    };

    List<Music> musics = new ArrayList<>();

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            mBound = true;
            mPlaying = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Now Playing");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        mMusic = null;

        int position = -1;

        if(intent != null){
            position = getIntent().getIntExtra(MUSIC_POSITION, -1);
            musics = (List<Music>) getIntent().getSerializableExtra(MUSICS_DATA);
        }


        mTvMusicTitle = (TextView) findViewById(R.id.text_view_music_activity_title);
        mBtnPlay = (ImageButton) findViewById(R.id.btn_music_activity_play);
        mBtnNext = (ImageButton) findViewById(R.id.btn_music_activity_next);
        mBtnPrev = (ImageButton) findViewById(R.id.btn_music_activity_prev);
        mSeekbar = (SeekBar) findViewById(R.id.seek_bar_music_activity);
        mSeekbarStart = (TextView) findViewById(R.id.text_view_music_activity_current_time);
        mSeekbarMax = (TextView) findViewById(R.id.text_view_music_activity_duration);
        mImageViewMusic = (ImageView) findViewById(R.id.image_view_music_activity_music_background);



        if(position > -1 && !musics.isEmpty()) {
            mMusic = musics.get(position);

            intent = new Intent(MusicActivity.this, MusicPlayerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            mTvMusicTitle.setText(mMusic.getTitle());

            initializeSeekbarWithValues();

            setAudioMetaImage();

        }

        mMusicHandler.post(mMusicRunnable);

        mBtnPlay.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBound){
                    if(mPlaying){
                        musicPlayerService.pauseMusic();
                        mPlaying = false;
                        mBtnPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_circle_outline, null));
                    }else{
                        musicPlayerService.playMusic();
                        mPlaying = true;
                        mBtnPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_circle_outline, null));
                    }
                }

            }
        });

        mBtnNext.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(mBound){
                    try {
                        Music expectedMusic = musicPlayerService.playNext();
                        initializeCurrentMusic(expectedMusic);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mBtnPrev.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(mBound){
                    try {
                        Music expectedMusic = musicPlayerService.playPrev();
                        initializeCurrentMusic(expectedMusic);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                mSeekbarStart.setText(Utils.getReadableDuration((long)progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                musicPlayerService.seekTo(progress);
            }
        });

    }

    private void initializeSeekbarWithValues(){
        if(mMusic != null){
            mSeekbar.setMax((int)(long)mMusic.getDuration());
            mSeekbarMax.setText(Utils.getReadableDuration(mMusic.getDuration()));
        }

    }

    private void initializeCurrentMusic(Music expectedMusic){
        if(expectedMusic != null){
            mMusic = expectedMusic;
            mTvMusicTitle.setText(mMusic.getTitle());
            mBtnPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_circle_outline, null));
            mPlaying = true;

            initializeSeekbarWithValues();

            setAudioMetaImage();
        }
    }

    private void setAudioMetaImage(){
        if(mMusic != null) {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(mMusic.getPath());

            byte[] data = metadataRetriever.getEmbeddedPicture();

            if (data != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                mImageViewMusic.setImageBitmap(bitmap);
            } else {
                mImageViewMusic.setImageResource(R.drawable.music_background);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBound){
            mBound = false;
            unbindService(mConnection);
        }

    }
}
