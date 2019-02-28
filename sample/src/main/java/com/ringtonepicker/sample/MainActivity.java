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

package com.ringtonepicker.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtonePickerListener;

public class MainActivity extends AppCompatActivity {

    private Uri mCurrentSelectedUri;

    private View.OnClickListener mCheckBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof CheckedTextView) {
                CheckedTextView checkedTextView = (CheckedTextView) v;
                checkedTextView.setChecked(!checkedTextView.isChecked());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CheckedTextView musicCb = findViewById(R.id.cb_music);
        musicCb.setOnClickListener(mCheckBoxClickListener);

        final CheckedTextView notificationCb = findViewById(R.id.cb_notification);
        notificationCb.setOnClickListener(mCheckBoxClickListener);

        final CheckedTextView ringtoneCb = findViewById(R.id.cb_ringtone);
        ringtoneCb.setOnClickListener(mCheckBoxClickListener);

        final CheckedTextView alarmCb = findViewById(R.id.cb_alarm);
        alarmCb.setOnClickListener(mCheckBoxClickListener);

        final SwitchCompat playRingtoneSwitch = findViewById(R.id.switch_play_ringtone);
        final SwitchCompat defaultSwitch = findViewById(R.id.switch_default_ringtone);
        final SwitchCompat silentSwitch = findViewById(R.id.switch_silent_ringtone);

        final TextView ringtoneTv = findViewById(R.id.tv_ringtone_info);

        findViewById(R.id.btn_pick_ringtone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Validate if at least one ringtone type is selected.
                if (!musicCb.isChecked()
                        && !notificationCb.isChecked()
                        && alarmCb.isChecked()
                        && musicCb.isChecked()) {

                    Toast.makeText(MainActivity.this, R.string.error_no_ringtone_type,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //Application needs read storage permission for Builder.TYPE_MUSIC .
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog
                            .Builder(MainActivity.this, getSupportFragmentManager())

                            //Set title of the dialog.
                            //If set null, no title will be displayed.
                            .setTitle("Select ringtone")

                            //set the currently selected uri, to mark that ringtone as checked by default.
                            //If no ringtone is currently selected, pass null.
                            .setCurrentRingtoneUri(mCurrentSelectedUri)

                            //Allow user to select default ringtone set in phone settings.
                            .displayDefaultRingtone(defaultSwitch.isChecked())

                            //Allow user to select silent (i.e. No ringtone.).
                            .displaySilentRingtone(silentSwitch.isChecked())

                            //set the text to display of the positive (ok) button.
                            //If not set OK will be the default text.
                            .setPositiveButtonText("SET RINGTONE")

                            //set text to display as negative button.
                            //If set null, negative button will not be displayed.
                            .setCancelButtonText("CANCEL")

                            //Set flag true if you want to play the sample of the clicked tone.
                            .setPlaySampleWhileSelection(playRingtoneSwitch.isChecked())

                            //Set the callback listener.
                            .setListener(new RingtonePickerListener() {
                                @Override
                                public void OnRingtoneSelected(@NonNull String ringtoneName, Uri ringtoneUri) {
                                    mCurrentSelectedUri = ringtoneUri;
                                    ringtoneTv.setText(String.format("Name : %s\nUri : %s", ringtoneName, ringtoneUri));
                                }
                            });


                    //Add the desirable ringtone types.
                    if (musicCb.isChecked())
                        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC);
                    if (notificationCb.isChecked())
                        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_NOTIFICATION);
                    if (ringtoneCb.isChecked())
                        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE);
                    if (alarmCb.isChecked())
                        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);

                    //Display the dialog.
                    ringtonePickerBuilder.show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            123);
                }
            }
        });
    }
}
