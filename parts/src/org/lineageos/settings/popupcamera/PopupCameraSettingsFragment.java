/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.popupcamera;

import android.os.Bundle;

import androidx.preference.Preference;

import com.android.colorpicker.ColorPickerDialog;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.lineageos.settings.R;

public class PopupCameraSettingsFragment
        extends SettingsBasePreferenceFragment {

    private static final String COLOR_DIALOG_TAG = "popup_led_color_dialog";

    private Preference mLedColor;
    private PopupCameraPreferences mPreferences;
    private int[] mColors;
    private String[] mColorNames;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.popup_settings, rootKey);
        mPreferences = new PopupCameraPreferences(requireContext());
        mColors = getResources().getIntArray(R.array.popupcamera_led_colors);
        mColorNames = getResources().getStringArray(R.array.popupcamera_led_color_names);
        mLedColor = findPreference(PopupCameraPreferences.LED_COLOR_KEY);
        mLedColor.setOnPreferenceClickListener(preference -> {
            ColorPickerDialog dialog = ColorPickerDialog.newInstance(
                    R.string.popup_led_color_title, mColors, mPreferences.getLedColor(),
                    4, ColorPickerDialog.SIZE_SMALL);
            dialog.setColorContentDescriptions(mColorNames);
            dialog.setOnColorSelectedListener(this::onColorSelected);
            dialog.show(requireActivity().getFragmentManager(), COLOR_DIALOG_TAG);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateColorSummary();
        ColorPickerDialog dialog = (ColorPickerDialog) requireActivity().getFragmentManager()
                .findFragmentByTag(COLOR_DIALOG_TAG);
        if (dialog != null) {
            dialog.setOnColorSelectedListener(this::onColorSelected);
        }
    }

    private void onColorSelected(int color) {
        mLedColor.getSharedPreferences().edit()
                .putInt(PopupCameraPreferences.LED_COLOR_KEY, color).apply();
        updateColorSummary();
    }

    private void updateColorSummary() {
        int color = mPreferences.getLedColor();
        for (int i = 0; i < mColors.length; i++) {
            if (mColors[i] == color) {
                mLedColor.setSummary(mColorNames[i]);
                return;
            }
        }
    }
}
