package com.example.rubel.gaanwala;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rubel on 9/29/2016.
 */

public class MusicFragment extends Fragment implements
    ImageView.OnClickListener{

    interface OnClickFragmentMusic{
        public void launchMusicDetailsActivity();
    }

    boolean mPlaying = false;
    View mParentView;


    private static final String MUSICS_DATA = "com.example.rubel.gaanwala.MUSICS";
    private static final String MUSICS_CURRENT = "com.example.rubel.gaanwala.CURRENT_MUSIC";
    private static final String MUSIC_POSITION = "com.example.rubel.gaanwala.POSITION";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View musicView = inflater.inflate(R.layout.fragmeng_single_music, container, false);
        musicView.setOnClickListener(this);
        mParentView = musicView;
        return musicView;
    }

    @Override
    public void onClick(View view) {
        ((OnClickFragmentMusic)getActivity()).launchMusicDetailsActivity();
    }


    public void updateMusic(Music music, boolean isPlaying){
        mPlaying = isPlaying;

        ((TextView)mParentView.findViewById(R.id.text_view_fragment_music_title)).setText(
                music.getTitle());
        ((TextView)mParentView.findViewById(R.id.text_view_fragment_music_artist)).setText(
                music.getArtist());
        ((TextView)mParentView.findViewById(R.id.text_view_fragment_music_length)).setText(
                Utils.getReadableDuration(music.getDuration()));

        if(isPlaying){
            ((ImageView)mParentView.findViewById(R.id.image_view_fragment_music_play)).setImageResource(
                    R.drawable.pause_circle);
        }else{
            ((ImageView)mParentView.findViewById(R.id.image_view_fragment_music_play)).setImageResource(
                    R.drawable.play);
        }
    }
}
