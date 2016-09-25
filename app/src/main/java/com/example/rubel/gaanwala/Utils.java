package com.example.rubel.gaanwala;

import java.util.concurrent.TimeUnit;

/**
 * Created by rubel on 9/25/2016.
 */

public class Utils {

    public static String getReadableDuration(Long duration){
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        return String.format("%02d:%02d", minutes, seconds) + "s";
    }

}
