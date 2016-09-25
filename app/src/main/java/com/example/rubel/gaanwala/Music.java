package com.example.rubel.gaanwala;

import java.io.Serializable;

/**
 * Created by rubel on 9/22/2016.
 */
public class Music implements Serializable{

    String title;
    String artist;
    Long duration;
    String path;
    String displayName;
    String id;

    public Music(String id, String path, String title, String displayName, String artist, String duration) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.displayName = displayName;
        this.artist = artist;
        this.duration = Long.parseLong(duration);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displaName) {
        this.displayName = displaName;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

}
