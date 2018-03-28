# Android Ringtone Picker
[![Download](https://api.bintray.com/packages/kevalpatel2106/maven/android-ringtone-picker/images/download.svg) ](https://bintray.com/kevalpatel2106/maven/android-ringtone-picker/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Ringtone%20Picker-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5513) [![Build Status](https://travis-ci.org/kevalpatel2106/android-ringtone-picker.svg?branch=master)](https://travis-ci.org/kevalpatel2106/android-ringtone-picker) [![Javadoc](https://img.shields.io/badge/JavaDoc-master-brightgreen.svg?style=orange)](http://kevalpatel2106.com/android-ringtone-picker/)

#### Simple Ringtone Picker dialog which allows you to pick different sounds from ringtone, alarm tone, notification tone and music from external storage.

## Gradle dependency: 
- Add below dependency into your build.gradle file.

```compile 'com.kevalpatel2106:ringtonepicker:1.2'```

## How to use?
- User `RingtonePicker.Builder` to build the ringtone picker dialog. 
- Pass all the parameters and call `RingtonePicker.Builder#show()` to display ringtone picker dialog.

```
RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog
        .Builder(MainActivity.this, getSupportFragmentManager())

        //Set title of the dialog.
        //If set null, no title will be displayed.
        .setTitle("Select ringtone")

        //set the currently selected uri, to mark that ringtone as checked by default.
        //If no ringtone is currently selected, pass null.
        .setCurrentRingtoneUri(/* Prevously selected ringtone Uri */)

        //Set true to allow allow user to select default ringtone set in phone settings.
        .displayDefaultRingtone(true)

        //Set true to allow user to select silent (i.e. No ringtone.).
        .displaySilentRingtone(true)

        //set the text to display of the positive (ok) button.
        //If not set OK will be the default text.
        .setPositiveButtonText("SET RINGTONE")

        //set text to display as negative button.
        //If set null, negative button will not be displayed.
        .setCancelButtonText("CANCEL")

        //Set flag true if you want to play the sample of the clicked tone.
        .setPlaySampleWhileSelection(true)

        //Set the callback listener.
        .setListener(new RingtonePickerListener() {
            @Override
            public void OnRingtoneSelected(@NonNull String ringtoneName, Uri ringtoneUri) {
                //Do someting with selected uri...
            }
        });

//Add the desirable ringtone types.
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC);
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_NOTIFICATION);
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE);
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);

//Display the dialog.
ringtonePickerBuilder.show();
```

## Demo

![sample](/app/demo.gif)

- You can download the sample apk from [here](/app/sample.apk).

## Contribute:
- Any pull request is most welcome.
**Simple 3 step to contribute into this repo:**
1. Fork the project.
2. Make required changes and commit.
3. Generate pull request. Mention all the required description regarding changes you made.

## Questions:
Hit me on twitter [![Twitter](https://img.shields.io/badge/Twitter-@kevalpatel2106-blue.svg?style=flat)](https://twitter.com/kevalpatel2106)

## License
Copyright 2018 Keval Patel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
