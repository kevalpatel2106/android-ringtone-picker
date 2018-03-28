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
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;

import java.util.LinkedHashMap;

/**
 * Created by Keval on 20-Feb-17.
 * This class contains utility classes for the ringtone.
 *
 * @author {@link 'https://github.com/kevalpatel2106'}
 */
public final class RingtoneUtils {

    /**
     * Load the list of all the ringtone registered using {@link RingtoneManager}. It will add title
     * as the key and uri of the sound as value in given {@link LinkedHashMap}.
     *
     * @param context instance of the caller.
     * @return {@link LinkedHashMap} of the title-{@link Uri} pair of all the ringtone.
     */
    @NonNull
    @CheckResult
    static LinkedHashMap<String, Uri> getRingTone(@NonNull final Context context) {
        return getTone(context, RingtoneManager.TYPE_RINGTONE);
    }

    /**
     * Load the list of all the notification tones registered using {@link RingtoneManager}. It will add title as the key and
     * uri of the sound as value in given {@link LinkedHashMap}.
     *
     * @param context instance of the caller.
     * @return {@link LinkedHashMap} of the title-{@link Uri} pair of all the notification tone.
     */
    @NonNull
    @CheckResult
    static LinkedHashMap<String, Uri> getNotificationTones(@NonNull final Context context) {
        return getTone(context, RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     * Load the list of all the alarm tones registered using {@link RingtoneManager}. It will add
     * title as the key and uri of the sound as value in given {@link LinkedHashMap}.
     *
     * @param context instance of the caller.
     * @return {@link LinkedHashMap} of the title-{@link Uri} pair of all the alarm tone.
     */
    @NonNull
    @CheckResult
    static LinkedHashMap<String, Uri> getAlarmTones(@NonNull final Context context) {
        return getTone(context, RingtoneManager.TYPE_ALARM);
    }

    /**
     * Get the tone from {@link RingtoneManager} for any given type. It will add title as the key and
     * uri of the sound as value in given {@link LinkedHashMap}.
     *
     * @param context instance of the caller
     * @param type    type of the ringtone from {@link RingtonePickerDialog.Builder#TYPE_NOTIFICATION},
     *                {@link RingtonePickerDialog.Builder#TYPE_RINGTONE} or
     *                {@link RingtonePickerDialog.Builder#TYPE_ALARM}.
     * @return {@link LinkedHashMap} of the title-{@link Uri} pair of all the ringtone of given type.
     */
    @CheckResult
    @NonNull
    private static LinkedHashMap<String, Uri> getTone(@NonNull final Context context, final int type) {
        final LinkedHashMap<String, Uri> ringToneList = new LinkedHashMap<>();

        final RingtoneManager ringtoneManager = new RingtoneManager(context);
        ringtoneManager.setType(type);

        final Cursor ringsCursor = ringtoneManager.getCursor();
        while (ringsCursor.moveToNext()) {
            ringToneList.put(ringsCursor.getString(RingtoneManager.TITLE_COLUMN_INDEX),
                    Uri.parse(ringsCursor.getString(RingtoneManager.URI_COLUMN_INDEX) + "/"
                            + ringsCursor.getString(RingtoneManager.ID_COLUMN_INDEX)));
        }
        ringsCursor.close();
        return ringToneList;
    }

    /**
     * Get the list of the music (sound) files from the phone storage. It will add title as the key and
     * uri of the sound as value in given {@link LinkedHashMap}.
     *
     * @param context instance of the caller.
     * @return {@link LinkedHashMap} of the title-{@link Uri} pair of all the music tracks.
     * @throws IllegalStateException If storage read permission is not available.
     */
    @NonNull
    @CheckResult
    @SuppressLint("InlinedApi")
    @RequiresPermission(anyOf = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    static LinkedHashMap<String, Uri> getMusic(@NonNull final Context context) {
        final LinkedHashMap<String, Uri> ringToneList = new LinkedHashMap<>();

        //Check for the read permission
        if (!RingtoneUtils.checkForStorageReadPermission(context)) {
            throw new IllegalStateException("Storage permission is not available.");
        }

        //Prepare query
        final Cursor mediaCursor = context.getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID},
                        MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                        null,
                        MediaStore.Audio.Media.TITLE + " ASC");

        if (mediaCursor != null) {
            while (mediaCursor.moveToNext()) {
                ringToneList.put(mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                                + mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media._ID))));
            }
            mediaCursor.close();
        }

        return ringToneList;
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
    public static String getRingtoneName(@NonNull final Context context,
                                         @NonNull final Uri uri) {
        final Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
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

    /**
     * Check if the {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} permission is granted?
     *
     * @return True if the read permission granted or else false.
     */
    static boolean checkForStorageReadPermission(@NonNull final Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN ?
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                        : Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }
}

