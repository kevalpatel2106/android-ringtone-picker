package com.kevalpatel.ringtonepicker;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Keval on 29-Mar-17.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

class RingtonePickerDialog extends DialogFragment {
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

    static void launchRingtonePicker(@NonNull FragmentManager fragmentManager,
                                     @Nullable String title,
                                     @NonNull String positiveButtonText,
                                     @Nullable String negativeButtonText,
                                     @NonNull ArrayList<Integer> ringtoneTypes,
                                     @Nullable String currentUri,
                                     @NonNull RingtonePickerListener listener,
                                     boolean isPlaySample) {
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
        mListener = (RingtonePickerListener) getArguments().getSerializable(ARG_LISTENER);
        getArguments().remove(ARG_LISTENER);

        //initialize media player
        mRingTonePlayer = new RingTonePlayer(getActivity());

        //Prepare the list of items.
        ArrayList<Integer> types = getArguments().getIntegerArrayList(ARG_RINGTONE_TYPES);
        mRingTones = new HashMap<>();
        //noinspection ConstantConditions
        for (int type : types) {
            switch (type) {
                case RingtonePickerBuilder.TYPE_RINGTONE:
                    RingtoneUtils.getRingTone(getActivity(), mRingTones);
                    break;
                case RingtonePickerBuilder.TYPE_ALARM:
                    RingtoneUtils.getRingTone(getActivity(), mRingTones);
                    break;
                case RingtonePickerBuilder.TYPE_MUSIC:
                    //Check for the write permission
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        break;
                    }

                    //noinspection MissingPermission
                    RingtoneUtils.getMusic(getActivity(), mRingTones);
                    break;
                case RingtonePickerBuilder.TYPE_NOTIFICATION:
                    RingtoneUtils.getRingTone(getActivity(), mRingTones);
                    break;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Find the current selected item
        int currentSelectionPos = -1;
        if (getArguments().getString(ARG_CURRENT_URI) != null) {
            mCurrentToneUri = Uri.parse(getArguments().getString(ARG_CURRENT_URI));
            mCurrentToneTitle = RingtoneUtils.getRingtoneName(getActivity(), mCurrentToneUri);
            if (mCurrentToneTitle != null) {    //Ringtone found for the uri
                currentSelectionPos = getUriPosition(mCurrentToneUri);
            } else { //No ringtone found for the uri
                mCurrentToneUri = null;
            }
        }

        //Prepare the dialog
        final String[] titles = mRingTones.keySet().toArray(new String[mRingTones.size()]);
        final boolean isPlaySample = getArguments().getBoolean(ARG_IS_PLAY);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(titles,
                        currentSelectionPos,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCurrentToneTitle = titles[which];
                                mCurrentToneUri = mRingTones.get(mCurrentToneTitle);

                                //Play the tone sample
                                if (isPlaySample) mRingTonePlayer.playRingtone(mCurrentToneUri);
                            }
                        })
                .setPositiveButton(getArguments().getString(ARG_DIALOG_POSITIVE),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.OnRingtoneSelected(mCurrentToneTitle, mCurrentToneUri);
                            }
                        });

        if (getArguments().getString(ARG_DIALOG_TITLE) != null)
            builder.setTitle(getArguments().getString(ARG_DIALOG_TITLE));

        if (getArguments().getString(ARG_DIALOG_NEGATIVE) != null)
            builder.setNegativeButton(getArguments().getString(ARG_DIALOG_NEGATIVE),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing
                        }
                    });
        return builder.create();
    }

    private int getUriPosition(@NonNull Uri uri) {
        Uri[] values = mRingTones.values().toArray(new Uri[mRingTones.size()]);
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
        mRingTonePlayer.release();
    }
}
