package com.example.rubel.gaanwala;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
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
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener{

    private static final String MUSICS_DATA = "com.example.rubel.gaanwala.MUSICS";
    private static final String MUSICS_CURRENT = "com.example.rubel.gaanwala.CURRENT_MUSIC";

    private final IBinder mBinder = new MusicPlayerBinder();

    MediaPlayer mediaPlayer = null;
    Uri currentUri;
    List<Music> musics;
    int currentPos;
    int mLength = -1;

    AudioManager mAudioManager;

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
        if(getAudioFoucus())
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
        currentPos += 1;
        playOnwards();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeMediaPlayer();
        musicChangeObservers = new ArrayList<>();
        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public void playMusic(){
        if(getAudioFoucus())
            mediaPlayer.start();
    }

    public void pauseMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            mAudioManager.abandonAudioFocus(this);
        }
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
        if(mediaPlayer != null && mediaPlayer.isPlaying())
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

    private void notifyAllMusicChangeObservers(Music newMusic){
        for(MusicChangeObserver observer : musicChangeObservers){
            observer.notifyOnChangeMusic(newMusic);
        }
    }


    private void notifyAllMusicChangeObserversOnPause(){
        for(MusicChangeObserver observer : musicChangeObservers){
            observer.notifyOnPauseMusic();
        }
    }

    private void notifyAllMusicChangeObserversOnPlay(){
        for(MusicChangeObserver observer : musicChangeObservers){
            observer.notifyOnPlayMusic();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange){

            case AudioManager.AUDIOFOCUS_GAIN:{
                if(mLength > 0){
                    mediaPlayer.seekTo(mLength);
                    mediaPlayer.start();
                    mLength = -1;
                    notifyAllMusicChangeObserversOnPlay();
                }

                break;

            }

            case AudioManager.AUDIOFOCUS_LOSS:{

                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    mLength = mediaPlayer.getCurrentPosition();
                    notifyAllMusicChangeObserversOnPause();
                }

                break;
            }

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:{
                // temporary pause audio stream
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    mLength = mediaPlayer.getCurrentPosition();
                    notifyAllMusicChangeObserversOnPause();
                }

                break;
            }

            default:
                break;
        }
    }

    private boolean getAudioFoucus(){
        int result = mAudioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return true;
        return false;
    }

    public void playMusicAtPosition(int position){
        currentPos = position;
        if(mediaPlayer != null)
            mediaPlayer.reset();
        playOnwards();
    }

    public int getMusicPosition(){
        return currentPos;
    }
}
