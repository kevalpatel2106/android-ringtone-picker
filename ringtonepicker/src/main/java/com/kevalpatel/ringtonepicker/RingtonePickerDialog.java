/*
 * Copyright 2017 Keval Patel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance wit
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *  the specific language governing permissions and limitations under the License.
 */

package com.kevalpatel.ringtonepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Keval on 29-Mar-17.
 * Ringtone picker dialog.
 * Never initiate ringtone picker dialog directly using {@link DialogFragment#show(FragmentManager, String)}.
 * Use {@link Builder} to initiate ringtone picker.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 * @see Builder
 */

public final class RingtonePickerDialog extends DialogFragment {

    // Argument names.
    private static final String ARG_DIALOG_TITLE = "arg_dialog_title";
    private static final String ARG_DIALOG_POSITIVE = "arg_dialog_positive";
    private static final String ARG_DIALOG_NEGATIVE = "arg_dialog_negative";
    private static final String ARG_RINGTONE_TYPES = "arg_dialog_types";
    private static final String ARG_CURRENT_URI = "arg_content_uri";
    private static final String ARG_LISTENER = "arg_listener";
    private static final String ARG_IS_PLAY = "arg_is_play";

    private RingtonePickerListener mListener;
    private HashMap<String, Uri> mRingTones;
    private String mCurrentToneTitle;
    private Uri mCurrentToneUri;
    private RingTonePlayer mRingTonePlayer;

    /**
     * Public constructor.
     * <B>Note:</B> Don't use this constructor to create and show dialog. Use {@link RingtonePickerDialog.Builder}
     * instead.
     */
    public RingtonePickerDialog() {
        //Do nothing.
    }

    /**
     * Create and display the {@link RingtonePickerDialog}.
     *
     * @param fragmentManager    Support {@link FragmentManager}.
     * @param title              Title of the dialog.
     * @param positiveButtonText Title for the positive button.
     * @param negativeButtonText Title for the negative button.
     * @param ringtoneTypes      {@link java.util.List} of the {@link RingtoneTypes} to display.
     * @param currentUri         Current ringtone {@link Uri}.
     * @param listener           {@link RingtonePickerListener} to get notify when new ringtone is
     *                           selected.
     * @param isPlaySample       True if the dialog should play sample ringtone else false.
     */
    private static void launchRingtonePicker(@NonNull FragmentManager fragmentManager,
                                             @Nullable String title,
                                             @NonNull String positiveButtonText,
                                             @Nullable String negativeButtonText,
                                             @NonNull ArrayList<Integer> ringtoneTypes,
                                             @Nullable String currentUri,
                                             @NonNull RingtonePickerListener listener,
                                             boolean isPlaySample) {

        // Prepare arguments bundle
        Bundle bundle = new Bundle();
        bundle.putString(ARG_DIALOG_TITLE, title);
        bundle.putString(ARG_DIALOG_POSITIVE, positiveButtonText);
        bundle.putString(ARG_DIALOG_NEGATIVE, negativeButtonText);
        bundle.putIntegerArrayList(ARG_RINGTONE_TYPES, ringtoneTypes);
        bundle.putString(ARG_CURRENT_URI, currentUri);
        bundle.putBoolean(ARG_IS_PLAY, isPlaySample);
        bundle.putSerializable(ARG_LISTENER, listener);

        RingtonePickerDialog ringtonePickerDialog = new RingtonePickerDialog();
        ringtonePickerDialog.setRetainInstance(true);
        ringtonePickerDialog.setArguments(bundle);
        ringtonePickerDialog.show(fragmentManager, RingtonePickerDialog.class.getSimpleName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        mListener = (RingtonePickerListener) getArguments().getSerializable(ARG_LISTENER);
        getArguments().remove(ARG_LISTENER);
        if (mListener == null) {
            throw new IllegalArgumentException("Callback listener cannot be null.");
        }

        //Prepare the list of items.
        ArrayList<Integer> types = getArguments().getIntegerArrayList(ARG_RINGTONE_TYPES);
        if (types == null) {
            throw new IllegalArgumentException("At least one ringtone type must be added.");
        }

        // Get pre selected ringtone
        if (getArguments().getString(ARG_CURRENT_URI) != null) {
            mCurrentToneUri = Uri.parse(getArguments().getString(ARG_CURRENT_URI));
            mCurrentToneTitle = RingtoneUtils.getRingtoneName(getActivity(), mCurrentToneUri);
        }

        if (getArguments().getString(ARG_DIALOG_TITLE) == null) {
            throw new IllegalArgumentException("Title of the dialog is not provided.");
        }
        if (getArguments().getString(ARG_DIALOG_NEGATIVE) == null) {
            throw new IllegalArgumentException("Title of the negative dialog button is not provided.");
        }
        if (getArguments().getString(ARG_DIALOG_POSITIVE) == null) {
            throw new IllegalArgumentException("Title of the positive dialog button is not provided.");
        }


        //Initialize media player
        mRingTonePlayer = new RingTonePlayer(getActivity());

        prepareRingtoneList(types);
    }

    @SuppressLint("MissingPermission")
    private void prepareRingtoneList(final ArrayList<Integer> types) {
        mRingTones = new HashMap<>();
        for (int type : types) {
            switch (type) {
                case Builder.TYPE_RINGTONE:
                    RingtoneUtils.getRingTone(getActivity(), mRingTones);
                    break;
                case Builder.TYPE_ALARM:
                    RingtoneUtils.getAlarmTones(getActivity(), mRingTones);
                    break;
                case Builder.TYPE_MUSIC:
                    //Check for the read permission
                    if (!RingtoneUtils.checkForStorageReadPermission(getActivity())) {
                        throw new IllegalArgumentException("Storage permission is not available.");
                    }

                    RingtoneUtils.getMusic(getActivity(), mRingTones);
                    break;
                case Builder.TYPE_NOTIFICATION:
                    RingtoneUtils.getNotificationTones(getActivity(), mRingTones);
                    break;
                case Builder.TYPE_DEFAULT:
                    //TODO Add at the first always
                    mRingTones.put(getString(R.string.title_default_list_item), RingtoneUtils.getSystemRingtoneTone());
                    break;
                case Builder.TYPE_SILENT:
                    //TODO Add at the second always
                    mRingTones.put(getString(R.string.title_silent_list_item), null);
                    break;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Find the current selected item
        int currentSelectionPos = -1;
        if (mCurrentToneTitle != null) {    //Ringtone found for the uri
            currentSelectionPos = getUriPosition(mRingTones, mCurrentToneUri);
        } else { //No ringtone found for the uri
            mCurrentToneUri = null;
        }

        //Prepare the dialog
        final String[] titles = mRingTones.keySet().toArray(new String[mRingTones.size()]);
        final boolean isPlaySample = getArguments().getBoolean(ARG_IS_PLAY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(ARG_DIALOG_TITLE))
                .setSingleChoiceItems(titles,   /* Name of all the ringtone */
                        currentSelectionPos,    /* Preselected item */
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO Handle what happens for TYPE_SILENT and TYPE_DEFAULT

                                mCurrentToneTitle = titles[which];
                                mCurrentToneUri = mRingTones.get(mCurrentToneTitle);

                                //Play the tone
                                if (isPlaySample) {
                                    try {
                                        mRingTonePlayer.playRingtone(mCurrentToneUri);
                                    } catch (IOException e) {
                                        //Cannot play ringtone
                                        e.printStackTrace();
                                    }
                                }
                            }
                        })
                .setPositiveButton(getArguments().getString(ARG_DIALOG_POSITIVE),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.OnRingtoneSelected(mCurrentToneTitle, mCurrentToneUri);
                            }
                        })
                .setNegativeButton(getArguments().getString(ARG_DIALOG_NEGATIVE),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        });
        return builder.create();
    }

    /**
     * Get the position of the ringtone in {@link #mRingTones} based on the uri.
     *
     * @param uri Uri of the ringtone to find.
     *
     * @return position of the uri in {@link #mRingTones} hash map.
     */
    private int getUriPosition(@NonNull HashMap<String, Uri> ringTones, @NonNull Uri uri) {
        Uri[] values = ringTones.values().toArray(new Uri[ringTones.size()]);
        for (int i = 0; i < values.length; i++) {
            if (values[i].toString().equals(uri.toString())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Release media player
        mRingTonePlayer.release();
    }

    /////////////////////////////////////////////////////////////////
    //Ringtone picker builder
    /////////////////////////////////////////////////////////////////

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            Builder.TYPE_DEFAULT,
            Builder.TYPE_SILENT,
            Builder.TYPE_RINGTONE,
            Builder.TYPE_ALARM,
            Builder.TYPE_MUSIC,
            Builder.TYPE_NOTIFICATION
    })
    @interface RingtoneTypes {
    }

    /**
     * This class takes every parameters of ringtone picker and initiate {@link RingtonePickerDialog}.
     */
    public static class Builder {
        /**
         * Call ringtone type. Application can set the type using {@link #addRingtoneType(int)}.
         *
         * @see #addRingtoneType(int)
         */
        public static final int TYPE_RINGTONE = RingtoneManager.TYPE_RINGTONE;

        /**
         * Notification tone type. Application can set the type using {@link #addRingtoneType(int)}.
         *
         * @see #addRingtoneType(int)
         */
        public static final int TYPE_NOTIFICATION = RingtoneManager.TYPE_NOTIFICATION;

        /**
         * Alarm tone type. Application can set the type using {@link #addRingtoneType(int)}.
         *
         * @see #addRingtoneType(int)
         */
        public static final int TYPE_ALARM = RingtoneManager.TYPE_ALARM;

        /**
         * All the music files from the external storage. READ_STORAGE permission is required for
         * this type. Application can set the type using {@link #addRingtoneType(int)}.
         *
         * @see #addRingtoneType(int)
         */
        public static final int TYPE_MUSIC = 3746;

        /**
         * This type will add a row at the top of the list with "Default" label. Whenever user
         * selects "Default", it will return ringtone that is selected in the settings of the phone.
         * Application can set the type using {@link #addRingtoneType(int)}.
         *
         * @see #addRingtoneType(int)
         */
        public static final int TYPE_DEFAULT = 6743;

        /**
         * This type will add a row at the top of the list with "Silent" label. Whenever user
         * selects "Silent", it indicates that user don't want to add any ringtone. Application can
         * set the type using {@link #addRingtoneType(int)}.
         *
         * @see #addRingtoneType(int)
         */
        public static final int TYPE_SILENT = 7030;

        /**
         * The title of the ringtone picker dialog. This value can be set from {@link #setTitle(String)}.
         *
         * @see #setTitle(String)
         */
        private String mTitle;

        /**
         * Title for the positive button.
         *
         * @see #setPositiveButtonText(int)
         * @see #setPositiveButtonText(String)
         */
        private String mPositiveButtonText;

        /**
         * Title for the negative/cancel button.
         *
         * @see #setCancelButtonText(int)
         * @see #setCancelButtonText(String)
         */
        private String mCancelButtonText;

        private ArrayList<Integer> mRingtoneType = new ArrayList<>();

        /**
         * True if the dialog should play sample ringtone else false.
         *
         * @see #setPlaySampleWhileSelection(boolean)
         */
        private boolean isPlaySample = false;

        /**
         * Currently selected ringtone {@link Uri}.
         *
         * @see #setCurrentRingtoneUri(Uri)
         */
        private String mCurrentRingtoneUri;

        /**
         * {@link RingtonePickerListener} to get notify when new ringtone is selected.
         *
         * @see #setListener(RingtonePickerListener)
         */
        private RingtonePickerListener mListener;

        /**
         * Instance of the caller.
         */
        private Context mContext;

        /**
         * Support {@link FragmentManager}.
         */
        private FragmentManager mFragmentManager;

        /**
         * Create a {@link Builder} for the {@link RingtonePickerDialog}.
         *
         * @param context         Instance of the caller.
         * @param fragmentManager Support {@link FragmentManager}.
         */
        public Builder(@NonNull Context context,
                       @NonNull FragmentManager fragmentManager) {
            mContext = context;
            mFragmentManager = fragmentManager;

            //Set default values
            mTitle = mContext.getString(R.string.ringtone_picker_default_title);
            mPositiveButtonText = mContext.getString(android.R.string.ok);
            mCancelButtonText = mContext.getString(android.R.string.cancel);
        }

        /**
         * Set the title of the ringtone picker dialog. This is optional parameter to set.
         * Default title is "Select ringtone".
         *
         * @param title title string.
         */
        public Builder setTitle(@Nullable String title) {
            mTitle = title;
            return this;
        }

        /**
         * Set the string resource for the title of the ringtone picker dialog. This is optional
         * parameter to set. Default title is "Select ringtone".
         *
         * @param title title string.
         */
        public Builder setTitle(@StringRes int title) {
            mTitle = mContext.getString(title);
            return this;
        }

        /**
         * Set the text to display on positive button. This is optional parameter to set.
         * Default value is "Ok".
         *
         * @param positiveButtonText text to display.
         *
         * @return {@link Builder}
         */
        public Builder setPositiveButtonText(@NonNull String positiveButtonText) {
            //noinspection ConstantConditions
            if (positiveButtonText == null)
                throw new IllegalArgumentException("Positive button text cannot be null.");
            mPositiveButtonText = positiveButtonText;
            return this;
        }

        /**
         * Set the text to display on positive button. This is optional parameter to set.
         * Default value is "Ok".
         *
         * @param positiveButtonText text to display.
         *
         * @return {@link Builder}
         */
        public Builder setPositiveButtonText(@StringRes int positiveButtonText) {
            return setPositiveButtonText(mContext.getString(positiveButtonText));
        }

        /**
         * Set the text to display on negative/cancel button. This is optional parameter to set.
         * Default value is "Cancel".
         *
         * @param cancelButtonText text to display on the button or null if you don't want to display
         *                         the cancel button.
         *
         * @return {@link Builder}
         */
        public Builder setCancelButtonText(@Nullable String cancelButtonText) {
            mCancelButtonText = cancelButtonText;
            return this;
        }

        /**
         * Set the text to display on negative/cancel button. This is optional parameter to set.
         * Default value is "Cancel".
         *
         * @param cancelButtonText text to display on the button or null if you don't want to display the cancel button.
         *
         * @return {@link Builder}
         */
        public Builder setCancelButtonText(@StringRes int cancelButtonText) {
            return setCancelButtonText(mContext.getString(cancelButtonText));
        }

        /**
         * Add the ringtone type to display in the ringtone selection list.
         *
         * @param ringtoneType type of the ringtone.
         *
         * @return {@link Builder}
         */
        public Builder addRingtoneType(@RingtoneTypes int ringtoneType) {
            if (ringtoneType == TYPE_MUSIC && ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new IllegalStateException("android.permission.READ_EXTERNAL_STORAGE is required for TYPE_MUSIC.");
            } else {
                mRingtoneType.add(ringtoneType);
            }
            return this;
        }

        /**
         * Boolean to indicate weather to play sample of selected ringtone while use select any
         * ringtone from the list or not? Default value is false. This is optional parameter to set.
         *
         * @param playSample if true, selected ringtone will play for one time.
         *
         * @return {@link Builder}
         */
        public Builder setPlaySampleWhileSelection(boolean playSample) {
            isPlaySample = playSample;
            return this;
        }

        /**
         * Set the Uri of the ringtone show as selected when dialog shows. If the given Uri is not
         * in the ringtone list, no ringtone will displayed as selected by default. This is optional
         * parameter to set.
         *
         * @param currentRingtoneUri Uri of the ringtone.
         *
         * @return {@link Builder}
         */
        public Builder setCurrentRingtoneUri(@Nullable Uri currentRingtoneUri) {
            if (currentRingtoneUri != null) mCurrentRingtoneUri = currentRingtoneUri.toString();
            return this;
        }

        /**
         * Set the call back listener. This is required parameter to set.
         *
         * @param listener {@link RingtonePickerListener}.
         *
         * @return {@link Builder}
         */
        public Builder setListener(@NonNull RingtonePickerListener listener) {
            //noinspection ConstantConditions
            if (listener == null)
                throw new IllegalArgumentException("RingtonePickerListener cannot be null.");
            mListener = listener;

            return this;
        }

        /**
         * Show {@link RingtonePickerDialog}.
         */
        public void show() {
            //Validate the input.
            if (mRingtoneType.size() == 0)
                throw new IllegalArgumentException("Select at least one ringtone.");

            //Launch ringtone picker dialog.
            RingtonePickerDialog.launchRingtonePicker(mFragmentManager, mTitle,
                    mPositiveButtonText,
                    mCancelButtonText,
                    mRingtoneType,
                    mCurrentRingtoneUri,
                    mListener,
                    isPlaySample);
        }
    }
}
