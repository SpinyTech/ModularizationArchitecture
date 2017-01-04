package com.spinytech.musicdemo;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by wanglei on 2016/12/27.
 */
public class Music {
    MediaPlayer mp;

    public Music(Context context) {
        mp = MediaPlayer.create(context, R.raw.music);
    }

    public void play() {
        try {
            if (mp != null) {
                mp.stop();
            }
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (mp != null) {
                mp.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}