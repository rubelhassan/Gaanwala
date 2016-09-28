package com.example.rubel.gaanwala;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.example.rubel.gaanwala.Utils.getReadableDuration;

/**
 * Created by rubel on 9/29/2016.
 */

public class MusicRecyclerAdapter extends RecyclerView.Adapter<MusicRecyclerAdapter.MusicDataHolder> {

    private List<Music> mMusicsList;
    private Context mContext;
    LayoutInflater mLayoutInflater;

    public MusicRecyclerAdapter(List<Music> mMusicsList, Context mContext) {
        this.mMusicsList = mMusicsList;
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MusicDataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View currentView = mLayoutInflater.inflate(R.layout.music_list_item, parent, false);
        return new MusicDataHolder(currentView);
    }

    @Override
    public void onBindViewHolder(MusicDataHolder musicDataHolder, int position) {
        Music music = mMusicsList.get(position);
        musicDataHolder.getImgBanner().setImageResource(R.drawable.music_circle);
        musicDataHolder.getImgPlay().setImageResource(R.drawable.play);
        musicDataHolder.getImgPlay().setTag(R.drawable.play);
        musicDataHolder.getTvTitle().setText(music.getTitle());
        musicDataHolder.getTvArtist().setText(music.getArtist());
        musicDataHolder.getTvLength().setText(getReadableDuration(music.getDuration()));
    }

    @Override
    public int getItemCount() {
        return mMusicsList.size();
    }

    public void addAll(List<Music> musics){
        mMusicsList.clear();
        mMusicsList.addAll(musics);
        notifyDataSetChanged();
    }

    public void clear(){
        mMusicsList.clear();
        notifyDataSetChanged();
    }

    class MusicDataHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        ImageView imgPlay;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvLength;

        public MusicDataHolder(View view) {
            super(view);
            this.imgBanner = (ImageView) view.findViewById(R.id.image_view_music);
            this.imgPlay = (ImageView) view.findViewById(R.id.image_view_music_play);
            this.tvTitle = (TextView) view.findViewById(R.id.text_view_music_title);
            this.tvArtist = (TextView) view.findViewById(R.id.text_view_music_artist);
            this.tvLength = (TextView) view.findViewById(R.id.text_view_music_length);
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
