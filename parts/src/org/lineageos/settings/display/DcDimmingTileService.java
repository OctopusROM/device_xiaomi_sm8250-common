/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.display;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

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
            DcDimmingUtils.setEnabled(!DcDimmingUtils.isEnabled());
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
