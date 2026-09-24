/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.display;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.lineageos.settings.R;

public class DcDimmingSettingsFragment extends SettingsBasePreferenceFragment {
    private SwitchPreferenceCompat mDcDimming;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.dc_dimming_settings, rootKey);
        mDcDimming = findPreference(DcDimmingUtils.KEY);
        mDcDimming.setOnPreferenceChangeListener(this::onDcDimmingChanged);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDcDimming.setEnabled(DcDimmingUtils.isAvailable());
        mDcDimming.setChecked(DcDimmingUtils.isAvailable() && DcDimmingUtils.isEnabled());
        mDcDimming.setSummary(DcDimmingUtils.isAvailable()
                ? R.string.dc_dimming_summary : R.string.dc_dimming_unavailable);
    }

    private boolean onDcDimmingChanged(Preference preference, Object newValue) {
        return DcDimmingUtils.setEnabled((Boolean) newValue);
    }
}
