package com.example.rubel.gaanwala;

/**
 * Created by rubel on 9/30/2016.
 */

public class ShareData {
    private boolean isPlaying;
    private int position;
    private  int duration;
    private boolean returning = false;

    public boolean isReturning() {
        return returning;
    }

    public void setReturning(boolean returning) {
        this.returning = returning;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private static final ShareData shareData = new ShareData();

    public static ShareData getInstance(){
        return shareData;
    }
}
