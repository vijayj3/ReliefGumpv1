package com.example.android.reliefgumpv1;


import android.os.Handler;
import android.util.Log;

/**
 * Created by AV JAYARAMAN on 15-Feb-16.
 */
public abstract class Countup implements Runnable {

    private long timeElapsed;
    private Handler handler;
    private boolean isKilled = false;

    public Countup(Handler handler) {
        this.handler = handler;
        timeElapsed = 0;
    }

    public void start() {
        isKilled = false;
        handler.postDelayed(this, 1000);
    }

    public void pause() {
        isKilled = true;
    }

    public void stop() {
        isKilled = true;
        timeElapsed = 0;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public static String convertToString(long time) {
        int totalSeconds = (int) (time / 1000);
        int hours = 0;
        int minutes = 0;
        int totalMinutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (totalMinutes > 59) {
            hours = totalMinutes / 60;
            minutes = totalMinutes % 60;
        } else {
            hours = 0;
            minutes = totalMinutes;
        }
        String hourString = (hours < 10) ? "0" + hours : hours + "";
        String minuteString = (minutes < 10) ? "0" + minutes : minutes + "";
        String secondString = (seconds < 10) ? "0" + seconds : seconds + "";
        Log.v("VJ", hourString + minuteString + secondString);
        return hourString + ":" + minuteString + ":" + secondString;
    }

    @Override
    public void run() {
        if (!isKilled) {
            Log.v("VJ", "timer Started");
            updateUI(timeElapsed);
            timeElapsed += 1000;
            handler.postDelayed(this, 1000);
        }

    }

    public abstract void updateUI(long timeElapsed);
}
