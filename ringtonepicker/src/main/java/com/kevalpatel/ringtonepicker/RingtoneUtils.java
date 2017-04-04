package com.kevalpatel.ringtonepicker;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.HashMap;

/**
 * Created by Keval on 20-Feb-17.
 * This class contains utility classes for the ringtone.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */

@SuppressWarnings("WeakerAccess")
public final class RingtoneUtils {
    /**
     * Load the list of all the ringtones registered using {@link RingtoneManager}. It will add title as the key and
     * uri of the sound as value in given hashmap.
     *
     * @param context       instance of the caller.
     * @param ringTonesList Hash map in which ringtone will be added.
     */
    static void getRingTone(Context context,
                            @NonNull HashMap<String, Uri> ringTonesList) {
        getTone(context, RingtoneManager.TYPE_RINGTONE, ringTonesList);
    }

    /**
     * Load the list of all the notification tones registered using {@link RingtoneManager}. It will add title as the key and
     * uri of the sound as value in given hashmap.
     *
     * @param ringTonesList Hash map in which notification tones will be added.
     * @param context       instance of the caller.
     */
    static void getNotificationTones(Context context,
                                     @NonNull HashMap<String, Uri> ringTonesList) {
        getTone(context, RingtoneManager.TYPE_NOTIFICATION, ringTonesList);
    }

    /**
     * Load the list of all the alarm tones registered using {@link RingtoneManager}. It will add title as the key and
     * uri of the sound as value in given hashmap.
     *
     * @param ringTonesList Hash map in which alarm tone will be added.
     * @param context       instance of the caller.
     */
    static void getAlarmTones(Context context,
                              @NonNull HashMap<String, Uri> ringTonesList) {
        getTone(context, RingtoneManager.TYPE_ALARM, ringTonesList);
    }

    /**
     * Get the tone from {@link RingtoneManager} for any given type. It will add title as the key and
     * uri of the sound as value in given hashmap.
     *
     * @param context       instance of the caller
     * @param type          type of the ringtone from {@link RingtonePickerBuilder#TYPE_NOTIFICATION},
     *                      {@link RingtonePickerBuilder#TYPE_RINGTONE} or {@link RingtonePickerBuilder#TYPE_ALARM}
     * @param ringTonesList Hash map in which alarm tone will be added.
     */
    private static void getTone(Context context,
                                int type,
                                @NonNull HashMap<String, Uri> ringTonesList) {
        RingtoneManager mRingtoneMgr = new RingtoneManager(context);

        mRingtoneMgr.setType(type);
        Cursor ringsCursor = mRingtoneMgr.getCursor();

        while (ringsCursor.moveToNext()) {
            ringTonesList.put(ringsCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX),
                    Uri.parse(ringsCursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/"
                            + ringsCursor.getString(RingtoneManager.ID_COLUMN_INDEX)));
        }
        ringsCursor.close();
    }

    /**
     * Get the list of the music (sound) files from the phone storage. It will add title as the key and
     * uri of the sound as value in given hashmap.
     *
     * @param context       instance of the caller.
     * @param ringTonesList Hash map in which alarm tone will be added.
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(allOf = {Manifest.permission.READ_EXTERNAL_STORAGE})
    static void getMusic(@NonNull Context context,
                         @NonNull HashMap<String, Uri> ringTonesList) {
        //Prepare query
        Cursor mediaCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                null,
                MediaStore.Audio.Media.TITLE + " ASC");

        if (mediaCursor != null) {
            while (mediaCursor.moveToNext()) {
                ringTonesList.put(mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                                + mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media._ID))));
            }
            mediaCursor.close();
        }
    }

    /**
     * Get the system selected default ringtone.
     *
     * @return Uri of the selected ringtone. You may need android.permission.READ_EXTERNAL_STORAGE
     * permission to read the ringtone selected from the external storage.
     */
    @Nullable
    public static Uri getSystemRingtoneTone() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    }

    /**
     * Get the system selected default alarm tone.
     *
     * @return Uri of the selected alarm tone. You may need android.permission.READ_EXTERNAL_STORAGE
     * permission to read the ringtone selected from the external storage.
     */
    @Nullable
    public static Uri getSystemAlarmTone() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    }

    /**
     * Get the system selected default notification tone.
     *
     * @return Uri of the selected notification tone. You may need android.permission.READ_EXTERNAL_STORAGE
     * permission to read the ringtone selected from the external storage.
     */
    @Nullable
    public static Uri getSystemNotificationTone() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     * Get the title of the ringtone from the uri of ringtone.
     *
     * @param context instance of the caller
     * @param uri     uri of the tone to search
     * @return title of the tone or return null if no tone found.
     */
    @Nullable
    public static String getRingtoneName(@NonNull Context context,
                                         @NonNull Uri uri) {
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        if (ringtone != null) {
            return ringtone.getTitle(context);
        } else {
            Cursor cur = context
                    .getContentResolver()
                    .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.Audio.Media.TITLE},
                            MediaStore.Audio.Media._ID + " =?",
                            new String[]{uri.getLastPathSegment()},
                            null);

            String title = null;
            if (cur != null) {
                title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                cur.close();
            }
            return title;
        }
    }
}

