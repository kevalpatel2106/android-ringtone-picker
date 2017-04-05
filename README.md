# android-ringtone-picker
[![Download](https://api.bintray.com/packages/kevalpatel2106/maven/android-ringtone-picker/images/download.svg) ](https://bintray.com/kevalpatel2106/maven/android-ringtone-picker/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Ringtone%20Picker-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/5513)

#### Simple Ringtone Picker dialog which allows you to pick different sounds from ringtone, alarm tone, notification tone and music from external storage.

## Gradle dependency: 
- Add below dependency into your build.gradle file.

```compile 'com.kevalpatel2106:ringtonepicker:1.0'```

## How to use?
- User `RingtonePicker.Builder` to build the ringtone picker dialog. 
- Pass all the parameters and call `RingtonePicker.Builder#show()` to display ringtone picker dialog.

```
RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog.Builder(MainActivity.this, getSupportFragmentManager());

//Set title of the dialog.
//If set null, no title will be displayed.
ringtonePickerBuilder.setTitle("Select ringtone");

//Add the desirable ringtone types.
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC);
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_NOTIFICATION);
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE);
ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);

//set the text to display of the positive (ok) button. (Optional)
ringtonePickerBuilder.setPositiveButtonText("SET RINGTONE");

//set text to display as negative button. (Optional)
ringtonePickerBuilder.setCancelButtonText("CANCEL");

//Set flag true if you want to play the com.ringtonepicker.sample of the clicked tone.
ringtonePickerBuilder.setPlaySampleWhileSelection(true);

//Set the callback listener.
ringtonePickerBuilder.setListener(new RingtonePickerListener() {
    @Override
    public void OnRingtoneSelected(String ringtoneName, Uri ringtoneUri) {
        //Do someting with Uri.
    }
});

//set the currently selected uri, to mark that ringtone as checked by default. (Optional)
ringtonePickerBuilder.setCurrentRingtoneUri(mCurrentSelectedUri);

//Display the dialog.
ringtonePickerBuilder.show();
```

## Demo

![sample](/app/demo.gif)

- You can download the sample apk from [here](/app/sample.apk).

## Contribute:
- Still there are many open weather apis to implement. Any pull request are most welcome.
**Simple 3 step to contribute into this repo:**
1. Fork the project.
2. Make required changes and commit.
3. Generate pull request. Mention all the required description regarding changes you made.

## Questions:
Hit me on twitter [![Twitter](https://img.shields.io/badge/Twitter-@kevalpatel2106-blue.svg?style=flat)](https://twitter.com/kevalpatel2106)

## License
Copyright 2017 Keval Patel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
