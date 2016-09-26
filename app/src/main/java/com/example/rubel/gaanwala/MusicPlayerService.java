package com.example.rubel.gaanwala;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;


/**
 * Created by rubel on 9/22/2016.
 */

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    private static final String MUSICS_DATA = "com.example.rubel.gaanwala.MUSICS";
    private static final String MUSICS_CURRENT = "com.example.rubel.gaanwala.NOW";

    private final IBinder mBinder = new MusicPlayerBinder();

    MediaPlayer mediaPlayer = null;
    Uri currentUri;
    List<Music> musics;
    int currentPos;

    List<MusicChangeObserver> musicChangeObservers = new ArrayList<>();

    public class MusicPlayerBinder extends Binder {

        MusicPlayerService getService(){
            return MusicPlayerService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        musics = new ArrayList<>();
        musics = (List<Music>) intent.getSerializableExtra(MUSICS_DATA);
        currentPos = intent.getIntExtra(MUSICS_CURRENT, 0);

        playOnwards();

        return START_STICKY;
    }

    private void playOnwards() {
        if( !musics.isEmpty() && musics.size() > currentPos){
            Music music = musics.get(currentPos);
            currentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(music.getId()));
            try {
                playSong(currentUri);
                notifyAllMusicChangeObservers(music);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Music File Read Error", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            Toast.makeText(getApplicationContext(), music.getTitle() + " is playing.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeMediaPlayer(){
        mediaPlayer = new MediaPlayer();
    }

    private void playSong(Uri uri) throws IOException {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setDataSource(getApplicationContext(), uri);
        mediaPlayer.prepareAsync();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        playOnwards();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        player.reset();
        Toast.makeText(getApplicationContext(), "Audio Completed", Toast.LENGTH_LONG).show();
        currentPos += 1;
        playOnwards();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeMediaPlayer();
        musicChangeObservers = new ArrayList<>();
    }

    public void playMusic(){
        mediaPlayer.start();
        Toast.makeText(getApplicationContext(), "Audio Played", Toast.LENGTH_SHORT).show();
    }

    public void pauseMusic(){
        if(!mediaPlayer.isPlaying());
            mediaPlayer.pause();
        Toast.makeText(getApplicationContext(), "Audio Paused", Toast.LENGTH_SHORT).show();
    }

    public Music playNext() throws IOException {

        Music expectedMusic = null;

        currentPos += 1;
        if(musics.size() > currentPos){
            mediaPlayer.reset();
            expectedMusic = musics.get(currentPos);
            currentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(expectedMusic.getId()));
            playSong(currentUri);
        }

        return expectedMusic;
    }

    public Music playPrev() throws IOException {
        Music expectedMusic = null;

        currentPos -= 1;
        if(musics.size() > currentPos && currentPos >= 0){
            mediaPlayer.reset();
            expectedMusic = musics.get(currentPos);
            currentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(expectedMusic.getId()));
            playSong(currentUri);
        }

        return expectedMusic;
    }

    public int getCurrentPosition(){
        if(mediaPlayer.isPlaying())
            return mediaPlayer.getCurrentPosition();

        return 0;
    }

    public void seekTo(int mills){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(mills);
        }
    }

    public void registerOnMusicChange(MusicChangeObserver observer){
        this.musicChangeObservers.add(observer);
    }

    public void unRegisterOnMusicChange(MusicChangeObserver observer){
        if(musicChangeObservers.contains(observer)){
            musicChangeObservers.remove(observer);
        }
    }

    public void notifyAllMusicChangeObservers(Music newMusic){
        for(MusicChangeObserver observer : musicChangeObservers){
            observer.notifyOnChangeMusic(newMusic);
        }
    }
}
