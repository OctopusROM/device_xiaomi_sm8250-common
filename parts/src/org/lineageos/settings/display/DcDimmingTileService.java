/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.display;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

public class DcDimmingTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (DcDimmingUtils.isAvailable()) {
            boolean enabled = !DcDimmingUtils.isEnabled();
            if (DcDimmingUtils.setEnabled(enabled)) {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(DcDimmingUtils.KEY, enabled).apply();
            }
        }
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        tile.setState(!DcDimmingUtils.isAvailable() ? Tile.STATE_UNAVAILABLE
                : DcDimmingUtils.isEnabled() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }
}
