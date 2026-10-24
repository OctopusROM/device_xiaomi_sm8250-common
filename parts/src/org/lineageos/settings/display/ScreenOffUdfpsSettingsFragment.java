/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.display;

import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.lineageos.settings.R;

public class ScreenOffUdfpsSettingsFragment extends SettingsBasePreferenceFragment {
    private SwitchPreferenceCompat mScreenOffUdfps;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.screen_off_udfps_settings, rootKey);
        mScreenOffUdfps = findPreference("screen_off_udfps");
        mScreenOffUdfps.setSingleLineTitle(false);
        mScreenOffUdfps.setOnPreferenceChangeListener(this::onScreenOffUdfpsChanged);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean defaultOn = getResources().getBoolean(
                com.android.internal.R.bool.config_screen_off_udfps_default_on);
        mScreenOffUdfps.setChecked(Settings.Secure.getIntForUser(
                getContext().getContentResolver(),
                Settings.Secure.SCREEN_OFF_UNLOCK_UDFPS_ENABLED,
                defaultOn ? 1 : 0, UserHandle.myUserId()) != 0);
    }

    private boolean onScreenOffUdfpsChanged(Preference preference, Object newValue) {
        return Settings.Secure.putIntForUser(getContext().getContentResolver(),
                Settings.Secure.SCREEN_OFF_UNLOCK_UDFPS_ENABLED,
                (Boolean) newValue ? 1 : 0, UserHandle.myUserId());
    }
}
