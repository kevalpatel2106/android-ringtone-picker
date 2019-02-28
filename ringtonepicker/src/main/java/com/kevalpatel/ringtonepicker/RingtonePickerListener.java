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

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Keval on 29-Mar-17.
 * <p>
 * A listener to notify the class whenever new ringtone is selected from ringtone picker.
 */

public interface RingtonePickerListener extends Serializable {

    /**
     * This callback will invoke whenever the ringtone is selected in the ringtone picker.
     *
     * @param ringtoneName Name of the selected ringtone.
     * @param ringtoneUri  {@link Uri} of the selected ringtone. This may be null if the user selects
     *                     silent ringtone option.
     */
    void OnRingtoneSelected(@NonNull String ringtoneName, @Nullable Uri ringtoneUri);
}
