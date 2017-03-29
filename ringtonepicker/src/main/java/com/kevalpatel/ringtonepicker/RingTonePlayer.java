package com.kevalpatel.ringtonepicker;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Created by Keval on 29-Mar-17.
 */

class RingTonePlayer {

    private Context mContext;
    private MediaPlayer mMediaPlayer;

    RingTonePlayer(Context context) {
        mContext = context;
        mMediaPlayer = new MediaPlayer();
    }

    void playRingtone(@NonNull Uri uri) {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }

            mMediaPlayer.setDataSource(mContext, uri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    void release() {
        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
        mMediaPlayer.release();
    }
}
