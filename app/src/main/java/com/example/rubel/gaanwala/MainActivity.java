package com.example.rubel.gaanwala;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private static final String MUSICS_CURRENT = "com.example.rubel.gaanwala.NOW";
    private static final String MUSIC_POSITION = "com.example.rubel.gaanwala.POSITION";

    ListView listViewMusic;

    List<Music> musicsList;

    MusicListAdapter musicListAdapter;

    TextView mEmptyStateTextView;

    ProgressBar progressLoading;

    ImageView oldImageView = null;

    Intent intent;

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


        listViewMusic = (ListView) findViewById(R.id.list_view_music);
        mEmptyStateTextView = (TextView) findViewById(R.id.text_view_empty);
        listViewMusic.setEmptyView(mEmptyStateTextView);

        musicsList = new LinkedList<>();
        musicListAdapter = new MusicListAdapter(this, R.layout.music_list_item, musicsList);
        listViewMusic.setAdapter(musicListAdapter);
        listViewMusic.setOnItemClickListener(this);

        progressLoading = (ProgressBar) findViewById(R.id.progressbar_loading_music);

        // TO DO
        // Add runtime permission for api >= 23 com.android.providers.media.MediaProvider
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(MUSIC_LOADER_ID, null, this);

    }

    @Override
    public Loader<List<Music>> onCreateLoader(int i, Bundle bundle) {
        return new MusicLoader(MainActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<List<Music>> loader, List<Music> musics) {

        musicsList = musics;

        progressLoading.setVisibility(View.GONE);
        mEmptyStateTextView.setText("No Music found.");

        musicListAdapter.clear();
        if(musicsList != null && !musicsList.isEmpty()){
            musicListAdapter.addAll(musics);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<Music>> loader) {
        musicListAdapter.clear();
    }


    // TO DO
    // Need to update kichuri code
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        mPosition = position;

        view.setSelected(true);

        ImageView play = (ImageView) view.findViewById(R.id.image_view_music_play);

        if(oldImageView != null && oldImageView != play){
            if((int)oldImageView.getTag() == R.drawable.pause_circle){

                oldImageView.setImageResource(R.drawable.play);
                oldImageView.setTag(R.drawable.play);

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

        oldImageView = play;

        Intent musicIntent = new Intent(MainActivity.this, MusicActivity.class);
        musicIntent.putExtra(MUSIC_POSITION, position);
        musicIntent.putExtra(MUSICS_DATA, (Serializable) musicsList);
        startActivity(musicIntent);

    }


}
