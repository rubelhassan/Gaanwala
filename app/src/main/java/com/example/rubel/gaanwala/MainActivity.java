package com.example.rubel.gaanwala;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Music>>, MusicRecyclerAdapter.OnItemClickListener,
        MusicFragment.OnClickFragmentMusic, MusicChangeObserver{

    private static final int MUSIC_LOADER_ID = 1;
    private static final String MUSICS_DATA = "com.example.rubel.gaanwala.MUSICS";
    private static final String MUSICS_CURRENT = "com.example.rubel.gaanwala.CURRENT_MUSIC";
    private static final String MUSIC_POSITION = "com.example.rubel.gaanwala.POSITION";
    private static final String MUSIC_DURATION = "com.example.rubel.gaanwala.DURATION";
    private static final String MUSIC_PLAYING = "com.example.rubel.gaanwala.MUSIC_PLAYING";
    private static final int MUSIC_ACTIVITY_REQUEST = 1;
    private static final int MUSIC_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 101;

    RecyclerView mRecyclerViewMusic;

    List<Music> musicsList;
     
    MusicRecyclerAdapter mMusicRecyclerAdapter;

    TextView mEmptyStateTextView;

    ProgressBar mProgressLoading;

    ImageView mOldImageView = null;

    Intent mStartServiceIntent;
    Intent mBindServiceIntent;

    // track playing and position
    boolean mPlaying = false;
    int mPosition = -1;
    int fragmentPosition = 0;
    MusicFragment mFragment = null;

    // Related to bound service
    MusicPlayerService mMusicPlayerService;
    boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            mMusicPlayerService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyStateTextView = (TextView) findViewById(R.id.text_view_empty);

        initializeListViewMusic();

        mProgressLoading = (ProgressBar) findViewById(R.id.progressbar_loading_music);

        if((ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED)){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){

            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MUSIC_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }else{
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(MUSIC_LOADER_ID, null, this);
        }

    }

    private void initializeListViewMusic() {
        mRecyclerViewMusic = (RecyclerView) findViewById(R.id.recycler_view_main_activity_music);
        // mRecyclerViewMusic.setEmptyView(mEmptyStateTextView);
        musicsList = new LinkedList<>();

        mMusicRecyclerAdapter = new MusicRecyclerAdapter(musicsList, getApplicationContext());
        mMusicRecyclerAdapter.setOnItemClickListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerViewMusic.setLayoutManager(layoutManager);
        mRecyclerViewMusic.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewMusic.setAdapter(mMusicRecyclerAdapter);
    }


    @Override
    public Loader<List<Music>> onCreateLoader(int i, Bundle bundle) {
        return new MusicLoader(MainActivity.this);
    }


    @Override
    public void onLoadFinished(Loader<List<Music>> loader, List<Music> musics) {

        musicsList.clear();
        musicsList.addAll(musics);

        mProgressLoading.setVisibility(View.GONE);
        mEmptyStateTextView.setText("No Music found.");

        // mMusicRecyclerAdapter.clear();
        if(musicsList != null && !musicsList.isEmpty() && !mBound){
            mPosition = 0;
            //mMusicRecyclerAdapter.addAll(musics);
            mEmptyStateTextView.setVisibility(View.GONE);
            mMusicRecyclerAdapter.notifyDataSetChanged();
            initializeFragmentOnDataLoad();

            mStartServiceIntent = new Intent(MainActivity.this, MusicPlayerService.class);
            mStartServiceIntent.putExtra(MUSICS_DATA, (Serializable) musicsList);
            mStartServiceIntent.putExtra(MUSICS_CURRENT, mPosition);

            startService(mStartServiceIntent);

            mBindServiceIntent = new Intent(MainActivity.this, MusicPlayerService.class);
            bindService(mBindServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        }

    }


    @Override
    public void onLoaderReset(Loader<List<Music>> loader) {
        musicsList.clear();
    }


    @Override
    public void onItemClick(View itemView, int position) {
        mPosition = position;

        ImageView play = (ImageView) itemView.findViewById(R.id.image_view_music_play);

        if(mOldImageView != null && mOldImageView != play){
            if((int)mOldImageView.getTag() == R.drawable.pause_circle){

                mOldImageView.setImageResource(R.drawable.play);
                mOldImageView.setTag(R.drawable.play);

            }
        }

        if((int)play.getTag() == R.drawable.play){
            play.setImageResource(R.drawable.pause_circle);
            play.setTag(R.drawable.pause_circle);

            if(mBound){
                mPlaying = true;
                mMusicPlayerService.pauseMusic();
                mMusicPlayerService.playMusicAtPosition(mPosition);
            }else{
                mBindServiceIntent = new Intent(MainActivity.this, MusicPlayerService.class);
                bindService(mBindServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

                if(mBound){
                    mMusicPlayerService.playMusicAtPosition(mPosition);
                    mPlaying = true;
                }
            }

        }else{

            play.setImageResource(R.drawable.play);
            play.setTag(R.drawable.play);

            if(mBound){
                mPlaying = false;
                mMusicPlayerService.pauseMusic();
            }
        }

        mOldImageView = play;

        if(mPosition > -1 && !musicsList.isEmpty())
            updateFragmentWithMusicState(musicsList.get(mPosition), mPlaying);
    }


    public void initializeFragmentOnDataLoad(){
        FragmentManager fm = getFragmentManager();
        MusicFragment musicFragment = (MusicFragment) fm.findFragmentById(
                R.id.fragment_main_activity_music);
        MusicFragment newFragment = new MusicFragment();
        FragmentTransaction ft = fm.beginTransaction();

        if(musicFragment != null){
            mFragment = musicFragment;
            musicFragment.updateMusic(musicsList.get(fragmentPosition), false);
        }else{
            mFragment = newFragment;
            ft.add(R.id.fragment_main_activity_music, newFragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void updateFragmentWithMusicState(Music music, boolean isPlaying){
        FragmentManager fm = getFragmentManager();
        MusicFragment musicFragment = (MusicFragment) fm.findFragmentById(
                R.id.fragment_main_activity_music);

        if(musicFragment != null){
            mFragment = musicFragment;
            musicFragment.updateMusic(music, isPlaying);
        }
    }

    @Override
    public void launchMusicDetailsActivity() {
        View viewItem = mRecyclerViewMusic.getLayoutManager().findViewByPosition(mPosition);
        ImageView iv = (ImageView) viewItem.findViewById(R.id.image_view_music_play);
        iv.setImageResource(R.drawable.play);

        Intent musicIntent = new Intent(MainActivity.this, MusicActivity.class);
        musicIntent.putExtra(MUSIC_POSITION, mPosition);
        musicIntent.putExtra(MUSICS_DATA, (Serializable) musicsList);
        startActivity(musicIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        stopService(mStartServiceIntent);
    }

    @Override
    public void notifyOnChangeMusic(Music newMusic) {
        mPosition += 1;
        updateFragmentWithMusicState(newMusic, mPlaying);
    }

    @Override
    public void notifyOnPauseMusic() {
        mPlaying = false;
        updateFragmentWithMusicState(musicsList.get(mPosition), mPlaying);
    }

    @Override
    public void notifyOnPlayMusic() {
        mPlaying = true;
        updateFragmentWithMusicState(musicsList.get(mPosition), mPlaying);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MUSIC_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE:{
                if((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.initLoader(MUSIC_LOADER_ID, null, this);
                }else{
                    mEmptyStateTextView.setText("Restart and Check Permission");
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ShareData shareData = ShareData.getInstance();
        if(shareData.isReturning()){
            shareData.setReturning(false);
            int position = shareData.getPosition();
            boolean playing = shareData.isPlaying();

            if(mBound && mFragment != null){
                View viewItem = mRecyclerViewMusic.getLayoutManager().findViewByPosition(position);
                if(viewItem != null){
                    ImageView iv = (ImageView) viewItem.findViewById(R.id.image_view_music_play);
                    iv.setImageResource(R.drawable.pause_circle);
                }

                if(position > musicsList.size())
                    position = musicsList.size()-1;

                mRecyclerViewMusic.scrollToPosition(position);
                updateFragmentWithMusicState(musicsList.get(position), playing);
                mPosition = position;
            }else{
                mBindServiceIntent = new Intent(MainActivity.this, MusicPlayerService.class);
                bindService(mBindServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

                if(mBound){
                    mRecyclerViewMusic.scrollToPosition(position);
                    updateFragmentWithMusicState(musicsList.get(position), playing);
                }
            }
        }
    }
}
