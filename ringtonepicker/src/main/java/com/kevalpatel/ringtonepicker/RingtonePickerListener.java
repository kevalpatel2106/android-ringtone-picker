package com.kevalpatel.ringtonepicker;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Keval on 29-Mar-17.
 */

public interface RingtonePickerListener extends Serializable {
    void OnRingtoneSelected(String ringtoneName, Uri ringtoneUri);
}
