package com.example.rubel.gaanwala;

import android.app.Activity;
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
import android.support.v4.app.NavUtils;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.rubel.gaanwala.R.id.toolbar;

public class MusicActivity extends AppCompatActivity implements
    MusicChangeObserver{

    private static final String MUSIC_POSITION = "com.example.rubel.gaanwala.POSITION";
    private static final String MUSICS_DATA = "com.example.rubel.gaanwala.MUSICS";
    private static final String MUSIC_DURATION = "com.example.rubel.gaanwala.DURATION";
    private static final String MUSIC_PLAYING = "com.example.rubel.gaanwala.MUSIC_PLAYING";

    // Related to bound service
    MusicPlayerService musicPlayerService;
    boolean mBound = false;
    boolean mPlaying = false;

    ImageButton mBtnPlay;
    ImageButton mBtnNext;
    ImageButton mBtnPrev;
    SeekBar mSeekbar;
    TextView mSeekbarStart;
    TextView mSeekbarMax;
    ImageView mImageViewMusic;

    Music mMusic;

    Toolbar mToolbar;

    int mPosition = -1;


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

            if(musicPlayerService != null){
                musicPlayerService.registerOnMusicChange(MusicActivity.this);
            }
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
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setTitle("Now Playing");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        mMusic = null;

        if(intent != null){
            mPosition = getIntent().getIntExtra(MUSIC_POSITION, -1);
            musics = (List<Music>) getIntent().getSerializableExtra(MUSICS_DATA);
        }


        mBtnPlay = (ImageButton) findViewById(R.id.btn_music_activity_play);
        mBtnNext = (ImageButton) findViewById(R.id.btn_music_activity_next);
        mBtnPrev = (ImageButton) findViewById(R.id.btn_music_activity_prev);
        mSeekbar = (SeekBar) findViewById(R.id.seek_bar_music_activity);
        mSeekbarStart = (TextView) findViewById(R.id.text_view_music_activity_current_time);
        mSeekbarMax = (TextView) findViewById(R.id.text_view_music_activity_duration);
        mImageViewMusic = (ImageView) findViewById(R.id.image_view_music_activity_music_background);



        if(mPosition > -1 && !musics.isEmpty()) {
            mMusic = musics.get(mPosition);

            intent = new Intent(MusicActivity.this, MusicPlayerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            mToolbar.setSubtitle(mMusic.getTitle());

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

                setSharedData();

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

                setSharedData();
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

                setSharedData();
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

        setSharedData();

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
            mToolbar.setSubtitle(mMusic.getTitle());
            mBtnPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_circle_outline, null));
            mPlaying = true;

            initializeSeekbarWithValues();

            setAudioMetaImage();

            setSharedData();
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

    @Override
    public void notifyOnChangeMusic(Music newMusic) {

        if(mBound){
            initializeCurrentMusic(newMusic);
            mPosition = musicPlayerService.getMusicPosition();
        }

        setSharedData();
    }

    @Override
    public void notifyOnPauseMusic() {
        mPlaying = false;
        mBtnPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_circle_outline, null));
        setSharedData();
    }

    @Override
    public void notifyOnPlayMusic() {
        mPlaying = true;
        mBtnPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_circle_outline, null));
        setSharedData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (item != null && id == android.R.id.home){
            setSharedData();
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSharedData(){
        ShareData shareData = ShareData.getInstance();
        shareData.setReturning(true);
        shareData.setPlaying(mPlaying);
        if(mBound)
            shareData.setPosition(musicPlayerService.getMusicPosition());
    }
}
