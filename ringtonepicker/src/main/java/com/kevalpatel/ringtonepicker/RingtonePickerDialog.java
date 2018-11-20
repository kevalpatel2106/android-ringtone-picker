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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Keval on 29-Mar-17.
 * Ringtone picker dialog.
 * Never initiate ringtone picker dialog directly using {@link DialogFragment#show(FragmentManager, String)}.
 * Use {@link Builder} to initiate ringtone picker.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 * @see Builder
 */

public final class RingtonePickerDialog extends DialogFragment implements RingtoneLoaderTask.LoadCompleteListener {

    // Argument names.
    private static final String ARG_DIALOG_TITLE = "arg_dialog_title";
    private static final String ARG_DIALOG_POSITIVE = "arg_dialog_positive";
    private static final String ARG_DIALOG_NEGATIVE = "arg_dialog_negative";
    private static final String ARG_RINGTONE_TYPES = "arg_dialog_types";
    private static final String ARG_CURRENT_URI = "arg_content_uri";
    private static final String ARG_LISTENER = "arg_listener";
    private static final String ARG_IS_PLAY = "arg_is_play";
    private static final String ARG_IS_DISPLAY_DEFAULT = "arg_is_display_default";
    private static final String ARG_IS_DISPLAY_SILENT = "arg_is_display_silent";

    /**
     * {@link Context} of the application. The dialog theme will be derived from this {@link Context}.
     */
    private Context mContext;

    /**
     * {@link RingtonePickerListener} to get notify when new ringtone is selected.
     */
    private RingtonePickerListener mListener;

    /**
     * {@link java.util.Map} of all the ringtone names and {@link Uri} to display in the dialog.
     */
    private LinkedHashMap<String, Uri> mRingTones = new LinkedHashMap<>();
    ;

    /**
     * {@link RingTonePlayer} to play sample of the ringtone if {@link #isPlaySample} is true.
     *
     * @see RingTonePlayer
     */
    private RingTonePlayer mRingTonePlayer;

    /**
     * Boolean to set true if {@link #mRingTonePlayer} should play sample ringtone player.
     */
    private boolean isPlaySample;

    private boolean isDisplayDefault;

    private boolean isDisplaySilent;

    /**
     * Key-value {@link Pair} of the selected ringtone name and {@link Uri}.
     */
    @NonNull
    private Pair<String, Uri> mCurrentRingTone = new Pair<>(null, Uri.EMPTY);

    private ArrayList<Integer> mRingtoneTypes;

    /**
     * {@link RingtoneLoaderTask} to load the list of ringtone on background.
     */
    @Nullable
    private RingtoneLoaderTask mLoaderTask;

    //Dialog building parameters.
    private String mDialogTitle;
    private String mPositiveButtonTitle;
    private String mNegativeButtonTitle;

    private ListView mListView;
    private ViewFlipper mViewFlipper;

    /**
     * Public constructor.
     * <B>Note:</B> Don't use this constructor to create and show dialog. Use {@link RingtonePickerDialog.Builder}
     * instead.
     */
    public RingtonePickerDialog() {
        //Do nothing.
    }

    /**
     * Factory method to create and display the {@link RingtonePickerDialog}.
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
    private static void launchRingtonePicker(@NonNull final FragmentManager fragmentManager,
                                             @Nullable final String title,
                                             @NonNull final String positiveButtonText,
                                             @Nullable final String negativeButtonText,
                                             @NonNull final ArrayList<Integer> ringtoneTypes,
                                             @Nullable final String currentUri,
                                             @NonNull final RingtonePickerListener listener,
                                             final boolean isPlaySample,
                                             final boolean isDisplayDefault,
                                             final boolean isDisplaySilent) {

        // Prepare arguments bundle
        Bundle bundle = new Bundle();
        bundle.putString(ARG_DIALOG_TITLE, title);
        bundle.putString(ARG_DIALOG_POSITIVE, positiveButtonText);
        bundle.putString(ARG_DIALOG_NEGATIVE, negativeButtonText);
        bundle.putIntegerArrayList(ARG_RINGTONE_TYPES, ringtoneTypes);
        bundle.putString(ARG_CURRENT_URI, currentUri);
        bundle.putBoolean(ARG_IS_PLAY, isPlaySample);
        bundle.putBoolean(ARG_IS_DISPLAY_DEFAULT, isDisplayDefault);
        bundle.putBoolean(ARG_IS_DISPLAY_SILENT, isDisplaySilent);
        bundle.putSerializable(ARG_LISTENER, listener);

        RingtonePickerDialog ringtonePickerDialog = new RingtonePickerDialog();
        ringtonePickerDialog.setRetainInstance(true);
        ringtonePickerDialog.setArguments(bundle);
        ringtonePickerDialog.show(fragmentManager, RingtonePickerDialog.class.getSimpleName());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if the argument
        if (getArguments() == null) {
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        // Ringtone listener
        mListener = (RingtonePickerListener) getArguments().getSerializable(ARG_LISTENER);
        getArguments().remove(ARG_LISTENER);
        if (mListener == null) {
            throw new IllegalArgumentException("Callback listener cannot be null.");
        }

        // Get pre selected ringtone
        if (getArguments().getString(ARG_CURRENT_URI) != null) {
            Uri currentToneUri = Uri.parse(getArguments().getString(ARG_CURRENT_URI));
            mCurrentRingTone = new Pair<>(
                    RingtoneUtils.getRingtoneName(mContext, currentToneUri),
                    currentToneUri
            );
        }

        //Get the dialog parameters
        if (getArguments().getString(ARG_DIALOG_TITLE) == null) {
            throw new IllegalArgumentException("Title of the dialog is not provided.");
        }
        mDialogTitle = getArguments().getString(ARG_DIALOG_TITLE);
        if (getArguments().getString(ARG_DIALOG_NEGATIVE) == null) {
            throw new IllegalArgumentException("Title of the negative dialog button is not provided.");
        }
        mNegativeButtonTitle = getArguments().getString(ARG_DIALOG_NEGATIVE);
        if (getArguments().getString(ARG_DIALOG_POSITIVE) == null) {
            throw new IllegalArgumentException("Title of the positive dialog button is not provided.");
        }
        mPositiveButtonTitle = getArguments().getString(ARG_DIALOG_POSITIVE);

        //Should play sample sound?
        isPlaySample = getArguments().getBoolean(ARG_IS_PLAY, false);
        isDisplayDefault = getArguments().getBoolean(ARG_IS_DISPLAY_DEFAULT, false);
        isDisplaySilent = getArguments().getBoolean(ARG_IS_DISPLAY_SILENT, false);

        //Parse ringtone types.
        mRingtoneTypes = getArguments().getIntegerArrayList(ARG_RINGTONE_TYPES);

        //Initialize media player
        mRingTonePlayer = new RingTonePlayer(mContext);
    }

    /**
     * Prepare the {@link LinkedHashMap} of the items to display in the list. This will prepare
     * {@link LinkedHashMap} with title of the ringtone as the key and {@link Uri} as the value.
     * <p>
     * - If {@link #isDisplayDefault} is true the first item of the list will have title "Default"
     * and value will be system ringtone {@link Uri}.
     * - If  {@link #isDisplaySilent} is true the second item of the list will have title "Silent"
     * and value will be null.
     *
     * @param types List {@link RingtoneTypes} to add.
     */
    @SuppressLint("MissingPermission")
    private void prepareRingtoneList(final ArrayList<Integer> types) {
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("At least one ringtone type must be added.");
        }

        mRingTones = new LinkedHashMap<>();

        //Add default item first
        if (isDisplayDefault) {
            mRingTones.put(getString(R.string.title_default_list_item), RingtoneUtils.getSystemRingtoneTone());
        }

        //Add silent item first
        if (isDisplaySilent) {
            mRingTones.put(getString(R.string.title_silent_list_item), null /* No ringtone */);
        }

        mLoaderTask = new RingtoneLoaderTask(mContext.getApplicationContext(), this);
        //noinspection unchecked
        mLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, types);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Find the current selected item

        @SuppressLint("InflateParams")
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.layout_ringtone_dialog, null);

        //Set list
        mListView = customView.findViewById(R.id.ringtone_list);

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String currentToneTitle = (String) mListView.getAdapter().getItem(position);
                mCurrentRingTone = new Pair<>(
                        currentToneTitle,
                        mRingTones.get(currentToneTitle)
                );

                //Play the tone
                if (isPlaySample) {
                    try {
                        mRingTonePlayer.playRingtone(mCurrentRingTone.second);
                    } catch (IOException e) {
                        //Cannot play ringtone
                        e.printStackTrace();
                    }
                }
            }
        });

        mViewFlipper = customView.findViewById(R.id.view_flipper);
        mViewFlipper.setDisplayedChild(0);

        //Load the ringtone
        prepareRingtoneList(mRingtoneTypes);

        //Prepare the dialog
        return new AlertDialog.Builder(mContext)
                .setTitle(mDialogTitle)
                .setView(customView)
                .setPositiveButton(mPositiveButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCurrentRingTone.first != null){
                            mListener.OnRingtoneSelected(mCurrentRingTone.first, mCurrentRingTone.second);
                        }else{
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(mNegativeButtonTitle, null)
                .create();
    }

    /**
     * Get the position of the ringtone in {@link #mRingTones} based on the uri.
     *
     * @param ringTones List of ringtone with name and {@link Uri}.
     * @param uri       Uri of the ringtone to find.
     * @return position of the uri in {@link #mRingTones} hash map.
     */
    private int getUriPosition(@NonNull final HashMap<String, Uri> ringTones,
                               @Nullable final Uri uri) {
        if (uri != null && uri != Uri.EMPTY) {

            Uri[] values = ringTones.values().toArray(new Uri[ringTones.size()]);
            for (int i = 0; i < values.length; i++) {
                if (values[i].toString().equals(uri.toString())) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoaderTask != null && !mLoaderTask.isCancelled()) mLoaderTask.cancel(true);

        //Release media player
        mRingTonePlayer.close();
    }

    @Override
    public void onLoadComplete(@NonNull HashMap<String, Uri> ringtone) {
        mViewFlipper.setDisplayedChild(1);
        mRingTones.putAll(ringtone);

        final String[] itemTitles = mRingTones.keySet().toArray(new String[mRingTones.size()]);
        int currentSelectionPos = getUriPosition(mRingTones, mCurrentRingTone.second);

        mListView.setAdapter(new ArrayAdapter<>(mContext,
                android.R.layout.select_dialog_singlechoice,
                itemTitles));
        mListView.setSelection(currentSelectionPos);
        mListView.setItemChecked(currentSelectionPos, true);
    }

    /**
     * This class takes every parameters of ringtone picker and initiate {@link RingtonePickerDialog}.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
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

        /**
         * List of all the {@link RingtoneTypes}.
         */
        @NonNull
        private ArrayList<Integer> mRingtoneType = new ArrayList<>();

        /**
         * True if the dialog should play sample ringtone else false. Default value is <code>false</code>.
         *
         * @see #setPlaySampleWhileSelection(boolean)
         */
        private boolean isPlaySample = false;

        /**
         * Boolean to decide weather to add a row at the top of the list with "Default" label or not.
         * Whenever user selects "Default", it will return ringtone that is selected in the settings
         * of the phone. Default value is <code>false</code>.
         *
         * @see #displayDefaultRingtone(boolean)
         */
        private boolean isDisplayDefault = false;

        /**
         * Boolean to decide weather to add a row at the top of the list with "Silent" label or not.
         * Whenever user selects "Silent", it indicates that user don't want to add any ringtone.
         * Default value is <code>false</code>.
         *
         * @see #displaySilentRingtone(boolean)
         */
        private boolean isDisplaySilent = false;

        /**
         * Currently selected ringtone {@link Uri}.
         *
         * @see #setCurrentRingtoneUri(Uri)
         */
        @Nullable
        private String mCurrentRingtoneUri = null;

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
        public Builder(@NonNull final Context context,
                       @NonNull final FragmentManager fragmentManager) {
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
        public Builder setTitle(@Nullable final String title) {
            mTitle = title;
            return this;
        }

        /**
         * Set the string resource for the title of the ringtone picker dialog. This is optional
         * parameter to set. Default title is "Select ringtone".
         *
         * @param title title string.
         * @throws IllegalArgumentException if the title text is null.
         */
        public Builder setTitle(@StringRes final int title) {
            return setPositiveButtonText(mContext.getString(title));
        }

        /**
         * Set the text to display on positive button. This is optional parameter to set.
         * Default value is "Ok".
         *
         * @param positiveButtonText text to display.
         * @return {@link Builder}
         */
        public Builder setPositiveButtonText(@NonNull final String positiveButtonText) {
            //noinspection ConstantConditions
            if (positiveButtonText != null) mPositiveButtonText = positiveButtonText;
            return this;
        }

        /**
         * Set the text to display on positive button. This is optional parameter to set.
         * Default value is "Ok".
         *
         * @param positiveButtonText text to display.
         * @return {@link Builder}
         */
        public Builder setPositiveButtonText(@StringRes final int positiveButtonText) {
            return setPositiveButtonText(mContext.getString(positiveButtonText));
        }

        /**
         * Set the text to display on negative/cancel button. This is optional parameter to set.
         * Default value is "Cancel".
         *
         * @param cancelButtonText text to display on the button or null if you don't want to display
         *                         the cancel button.
         * @return {@link Builder}
         */
        public Builder setCancelButtonText(@Nullable final String cancelButtonText) {
            mCancelButtonText = cancelButtonText;
            return this;
        }

        /**
         * Set the text to display on negative/cancel button. This is optional parameter to set.
         * Default value is "Cancel".
         *
         * @param cancelButtonText text to display on the button or null if you don't want to display the cancel button.
         * @return {@link Builder}
         */
        public Builder setCancelButtonText(@StringRes final int cancelButtonText) {
            return setCancelButtonText(mContext.getString(cancelButtonText));
        }

        /**
         * Add the ringtone type to display in the ringtone selection list.
         *
         * @param ringtoneType type of the ringtone.
         * @return {@link Builder}
         * @throws IllegalStateException if the {@link Manifest.permission#READ_EXTERNAL_STORAGE}
         *                               permission not granted and ringtoneType is {@link #TYPE_MUSIC}.
         */
        public Builder addRingtoneType(@RingtoneTypes final int ringtoneType) {
            if (ringtoneType == TYPE_MUSIC && !RingtoneUtils.checkForStorageReadPermission(mContext)) {
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
         * @return {@link Builder}
         */
        public Builder setPlaySampleWhileSelection(final boolean playSample) {
            isPlaySample = playSample;
            return this;
        }

        /**
         * Set the Uri of the ringtone show as selected when dialog shows. If the given Uri is not
         * in the ringtone list, no ringtone will displayed as selected by default. This is optional
         * parameter to set.
         *
         * @param currentRingtoneUri Uri of the ringtone.
         * @return {@link Builder}
         */
        public Builder setCurrentRingtoneUri(@Nullable final Uri currentRingtoneUri) {
            if (currentRingtoneUri != null && currentRingtoneUri != Uri.EMPTY)
                mCurrentRingtoneUri = currentRingtoneUri.toString();
            return this;
        }

        /**
         * Set the call back listener. This is required parameter to set.
         *
         * @param listener {@link RingtonePickerListener}.
         * @return {@link Builder}
         * @throws IllegalArgumentException if the {@link RingtonePickerListener} is null.
         */
        public Builder setListener(@NonNull final RingtonePickerListener listener) {
            //noinspection ConstantConditions
            if (listener == null)
                throw new IllegalArgumentException("RingtonePickerListener cannot be null.");
            mListener = listener;

            return this;
        }

        /**
         * Method to add a row at the top of the list with "Default" label. Whenever user
         * selects "Default", it will return ringtone that is selected in the settings of the phone.
         * This is optional parameter to set.
         *
         * @param display True to display the default item in the list.
         * @return {@link Builder}
         * @see #isDisplayDefault
         */
        public Builder displayDefaultRingtone(final boolean display) {
            isDisplayDefault = display;
            return this;
        }

        /**
         * Method to will add a row at the top of the list with "Silent" label. Whenever user
         * selects "Silent", it indicates that user don't want to add any ringtone. This is optional
         * parameter to set.
         *
         * @param display True to display the "Silent" item in the list.
         * @return {@link Builder}
         * @see #isDisplaySilent
         */
        public Builder displaySilentRingtone(final boolean display) {
            isDisplaySilent = display;
            return this;
        }

        /**
         * Show {@link RingtonePickerDialog}.
         *
         * @throws IllegalArgumentException if any ringtone type is not selected.
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
                    isPlaySample,
                    isDisplayDefault,
                    isDisplaySilent);
        }
    }
}
