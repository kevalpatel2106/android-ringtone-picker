package com.kevalpatel.ringtonepicker;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Created by Keval on 29-Mar-17.
 * This class plays the ringtone for the com.ringtonepicker.sample whenever user selects any from the list.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */
class RingTonePlayer {
    private Context mContext;
    private MediaPlayer mMediaPlayer;

    RingTonePlayer(Context context) {
        mContext = context;
        mMediaPlayer = new MediaPlayer();
    }

    /**
     * Play the ringtone for the given uri.
     *
     * @param uri uri of the ringtone to play.
     * @throws IOException if it cannot play the ringtone.
     */
    void playRingtone(@NonNull Uri uri) throws IOException,
            IllegalArgumentException,
            SecurityException,
            IllegalStateException {

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }

        mMediaPlayer.setDataSource(mContext, uri);
        mMediaPlayer.prepare();
        mMediaPlayer.start();
    }

    /**
     * Release the {@link MediaPlayer} instance. Remember to call this method in on destroy.
     */
    void release() {
        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
        mMediaPlayer.release();
    }
}
