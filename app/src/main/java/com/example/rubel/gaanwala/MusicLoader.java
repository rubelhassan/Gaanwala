package com.example.rubel.gaanwala;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by rubel on 9/22/2016.
 */

public class MusicLoader extends AsyncTaskLoader<List<Music>> {

    public MusicLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * load all mp3(music)s in background thread
     * @return list of Music
     */
    @Override
    public List<Music> loadInBackground() {

        ArrayList<Music> musics = new ArrayList<>();

        ContentResolver contentResolver = getContext().getContentResolver();
        Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(mediaUri, projection, selection, null, sortOrder);

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()){

                Music music = new Music(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5)
                );

                musics.add(music);
            }
        }

        return musics;
    }
}
