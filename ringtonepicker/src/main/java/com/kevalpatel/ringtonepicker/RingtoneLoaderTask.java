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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kevalpatel2106 on 30-Mar-18.
 *
 * @author <a href="https://github.com/kevalpatel2106">kevalpatel2106</a>
 */
class RingtoneLoaderTask extends AsyncTask<ArrayList<Integer>, Void, HashMap<String, Uri>> {

    @NonNull
    private final LoadCompleteListener mListener;

    @SuppressLint("StaticFieldLeak")
    @NonNull
    private final Context mApplication;

    RingtoneLoaderTask(@NonNull final Context application,
                       @NonNull final LoadCompleteListener loadCompleteListener) {
        mListener = loadCompleteListener;
        mApplication = application;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SafeVarargs
    @SuppressLint("MissingPermission")
    @Override
    @NonNull
    protected final HashMap<String, Uri> doInBackground(ArrayList<Integer>... voids) {
        HashMap<String, Uri> ringTones = new HashMap<>();

        for (int type : voids[0]) {
            switch (type) {
                case RingtonePickerDialog.Builder.TYPE_RINGTONE:
                    ringTones.putAll(RingtoneUtils.getRingTone(mApplication));
                    break;
                case RingtonePickerDialog.Builder.TYPE_ALARM:
                    ringTones.putAll(RingtoneUtils.getAlarmTones(mApplication));
                    break;
                case RingtonePickerDialog.Builder.TYPE_MUSIC:
                    ringTones.putAll(RingtoneUtils.getMusic(mApplication));
                    break;
                case RingtonePickerDialog.Builder.TYPE_NOTIFICATION:
                    ringTones.putAll(RingtoneUtils.getNotificationTones(mApplication));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid ringtone type.");
            }
        }
        return ringTones;
    }

    @Override
    protected void onPostExecute(HashMap<String, Uri> ringtone) {
        super.onPostExecute(ringtone);
        mListener.onLoadComplete(ringtone);
    }

    interface LoadCompleteListener {
        void onLoadComplete(@NonNull final HashMap<String, Uri> ringtone);
    }
}
