package com.example.rubel.gaanwala;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Music>>, ListView.OnItemClickListener{

    private static final int MUSIC_LOADER_ID = 1;
    private static final String MUSICS_DATA = "com.example.rubel.gaanwala.MUSICS";
    private static final String MUSICS_CURRENT = "com.example.rubel.gaanwala.CURRENT_MUSIC";
    private static final String MUSIC_POSITION = "com.example.rubel.gaanwala.POSITION";

    ListView mListViewMusic;

    RecyclerView mRecyclerViewMusic;

    List<Music> musicsList;

    MusicRecyclerAdapter mMusicRecyclerAdapter;

    TextView mEmptyStateTextView;

    ProgressBar mProgressLoading;

    ImageView mOldImageView = null;

    Intent intent;

    // track playing and position
    boolean mPlaying = false;
    int mPosition = -1;

    // Related to bound service
    MusicPlayerService musicPlayerService;
    boolean mBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
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

        // TO DO
        // Add runtime permission for api >= 23 com.android.providers.media.MediaProvider
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(MUSIC_LOADER_ID, null, this);
    }

    private void initializeListViewMusic() {
        mRecyclerViewMusic = (RecyclerView) findViewById(R.id.recycler_view_main_activity_music);
        // mRecyclerViewMusic.setEmptyView(mEmptyStateTextView);
        musicsList = new LinkedList<>();

        mMusicRecyclerAdapter = new MusicRecyclerAdapter(musicsList, getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerViewMusic.setLayoutManager(layoutManager);
        mRecyclerViewMusic.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewMusic.setAdapter(mMusicRecyclerAdapter);
        // mRecyclerViewMusic.setOnItemClickListener(this);
    }


    @Override
    public Loader<List<Music>> onCreateLoader(int i, Bundle bundle) {
        return new MusicLoader(MainActivity.this);
    }


    @Override
    public void onLoadFinished(Loader<List<Music>> loader, List<Music> musics) {

        musicsList = musics;

        mProgressLoading.setVisibility(View.GONE);
        mEmptyStateTextView.setText("No Music found.");

        mMusicRecyclerAdapter.clear();
        if(musicsList != null && !musicsList.isEmpty()){
            mMusicRecyclerAdapter.addAll(musics);
            mEmptyStateTextView.setVisibility(View.GONE);
        }

    }


    @Override
    public void onLoaderReset(Loader<List<Music>> loader) {
        mMusicRecyclerAdapter.clear();
    }


    // TO DO
    // Need to update kichuri code
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        mPosition = position;

        view.setSelected(true);

        ImageView play = (ImageView) view.findViewById(R.id.image_view_music_play);

        if(mOldImageView != null && mOldImageView != play){
            if((int)mOldImageView.getTag() == R.drawable.pause_circle){

                mOldImageView.setImageResource(R.drawable.play);
                mOldImageView.setTag(R.drawable.play);

            }
        }

        if((int)play.getTag() == R.drawable.play){
            play.setImageResource(R.drawable.pause_circle);
            play.setTag(R.drawable.pause_circle);

            mPlaying = true;

            intent = new Intent(MainActivity.this, MusicPlayerService.class);
            intent.putExtra(MUSICS_DATA, (Serializable) musicsList);
            intent.putExtra(MUSICS_CURRENT, position);

            startService(intent);

        }else{

            play.setImageResource(R.drawable.play);
            play.setTag(R.drawable.play);

            if(stopService(intent)){
                mPlaying = false;
                mPosition = -1;
                Toast.makeText(getApplicationContext(), "Music Stopped", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "Music Not Stopped", Toast.LENGTH_SHORT).show();
            }


        }

        mOldImageView = play;

        Intent musicIntent = new Intent(MainActivity.this, MusicActivity.class);
        musicIntent.putExtra(MUSIC_POSITION, position);
        musicIntent.putExtra(MUSICS_DATA, (Serializable) musicsList);
        startActivity(musicIntent);
    }


}
