package com.kevalpatel.ringtonepicker;

import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by Keval on 29-Mar-17.
 */

public class RingtonePickerBuilder {
    public static final int TYPE_RINGTONE = RingtoneManager.TYPE_RINGTONE;
    public static final int TYPE_NOTIFICATION = RingtoneManager.TYPE_NOTIFICATION;
    public static final int TYPE_ALARM = RingtoneManager.TYPE_ALARM;
    public static final int TYPE_MUSIC = 3746;

    private String mTitle = "Select ringtone";

    private String mPositiveButtonText = "Ok";

    private String mCancelButtonText = "Cancel";

    private ArrayList<Integer> mRingtoneType = new ArrayList<>();

    private boolean isPlaySample = false;

    private String mCurrentRingtoneUri;

    private RingtonePickerListener mListener;

    private FragmentManager mFragmentManager;

    public RingtonePickerBuilder(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    /**
     * Set the title of the ringtone picker dialog.
     *
     * @param title title string.
     */
    public RingtonePickerBuilder setTitle(@Nullable String title) {
        mTitle = title;
        return this;
    }

    public RingtonePickerBuilder setPositiveButtonText(@NonNull String positiveButtonText) {
        //noinspection ConstantConditions
        if (positiveButtonText == null)
            throw new IllegalArgumentException("Positive button text cannot be null.");
        mPositiveButtonText = positiveButtonText;
        return this;
    }

    public RingtonePickerBuilder setCancelButtonText(@Nullable String cancelButtonText) {
        mCancelButtonText = cancelButtonText;
        return this;
    }

    public RingtonePickerBuilder addRingtoneType(@RingtoneTypes int ringtoneType) {
        mRingtoneType.add(ringtoneType);
        return this;
    }

    public RingtonePickerBuilder setPlaySampleWhileSelection(boolean playSample) {
        isPlaySample = playSample;
        return this;
    }

    public RingtonePickerBuilder setCurrentRingtoneUri(@Nullable Uri currentRingtoneUri) {
        if (currentRingtoneUri != null) mCurrentRingtoneUri = currentRingtoneUri.toString();
        return this;
    }

    public RingtonePickerBuilder setListener(@NonNull RingtonePickerListener listener) {
        //noinspection ConstantConditions
        if (listener == null)
            throw new IllegalArgumentException("RingtonePickerListener cannot be null.");
        mListener = listener;

        return this;
    }

    public void show() {
        if (mRingtoneType.size() == 0)
            throw new IllegalArgumentException("Select at least one ringtone.");

        RingtonePickerDialog.launchRingtonePicker(mFragmentManager, mTitle,
                mPositiveButtonText,
                mCancelButtonText,
                mRingtoneType,
                mCurrentRingtoneUri,
                mListener,
                isPlaySample);

    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_RINGTONE, TYPE_ALARM, TYPE_MUSIC, TYPE_NOTIFICATION})
    @interface RingtoneTypes {
    }
}
