package com.example.rubel.gaanwala;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.example.rubel.gaanwala.Utils.getReadableDuration;

/**
 * Created by rubel on 9/22/2016.
 */

public class MusicListAdapter extends ArrayAdapter<Music> {

    Context mContext;
    List<Music> musicList;
    int layoutID;

    public MusicListAdapter(Context context, int resource, List<Music> musicList) {
        super(context, resource, musicList);

        this.musicList = musicList;
        this.mContext = context;
        this.layoutID = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        Log.d("ADAPTER", "Running adapter");

        MusicDataHolder musicDataHolder = null;

        if(row == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(layoutID, parent, false);

            musicDataHolder = new MusicDataHolder(
                    (ImageView) row.findViewById(R.id.image_view_music),
                    (ImageView) row.findViewById(R.id.image_view_music_play),
                    (TextView) row.findViewById(R.id.text_view_music_title),
                    (TextView) row.findViewById(R.id.text_view_music_artist),
                    (TextView) row.findViewById(R.id.text_view_music_length)
            );

            row.setTag(musicDataHolder);
        }

        musicDataHolder = (MusicDataHolder) row.getTag();

        Music music = musicList.get(position);

        musicDataHolder.getImgBanner().setImageResource(R.drawable.music_circle);
        musicDataHolder.getImgPlay().setImageResource(R.drawable.play);
        musicDataHolder.getImgPlay().setTag(R.drawable.play);
        musicDataHolder.getTvTitle().setText(music.getTitle());
        musicDataHolder.getTvArtist().setText(music.getArtist());
        musicDataHolder.getTvLength().setText(getReadableDuration(music.getDuration()));

        return row;
    }


    class MusicDataHolder {
        ImageView imgBanner;
        ImageView imgPlay;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvLength;

        public MusicDataHolder(ImageView imgBanner, ImageView imgPlay, TextView tvTitle, TextView tvArtist, TextView tvLength) {
            this.imgBanner = imgBanner;
            this.imgPlay = imgPlay;
            this.tvTitle = tvTitle;
            this.tvArtist = tvArtist;
            this.tvLength = tvLength;
        }

        public ImageView getImgBanner() {
            return imgBanner;
        }

        public void setImgBanner(ImageView imgBanner) {
            this.imgBanner = imgBanner;
        }

        public ImageView getImgPlay() {
            return imgPlay;
        }

        public void setImgPlay(ImageView imgPlay) {
            this.imgPlay = imgPlay;
        }

        public TextView getTvTitle() {
            return tvTitle;
        }

        public void setTvTitle(TextView tvTitle) {
            this.tvTitle = tvTitle;
        }

        public TextView getTvArtist() {
            return tvArtist;
        }

        public void setTvArtist(TextView tvArtist) {
            this.tvArtist = tvArtist;
        }

        public TextView getTvLength() {
            return tvLength;
        }

        public void setTvLength(TextView tvLength) {
            this.tvLength = tvLength;
        }

    }
}
