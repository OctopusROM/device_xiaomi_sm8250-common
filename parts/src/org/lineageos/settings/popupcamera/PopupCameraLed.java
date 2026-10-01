/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.popupcamera;

import android.graphics.Color;

import org.lineageos.settings.utils.FileUtils;

public final class PopupCameraLed {
    private PopupCameraLed() {}

    public static void setColor(int color) {
        String red = Color.red(color) > 0 ? "1" : "0";
        String green = Color.green(color) > 0 ? "1" : "0";
        String blue = Color.blue(color) > 0 ? "1" : "0";
        FileUtils.writeLine(Constants.RED_LED_PATH, red);
        FileUtils.writeLine(Constants.RED_RIGHT_LED_PATH, red);
        FileUtils.writeLine(Constants.GREEN_LED_PATH, green);
        FileUtils.writeLine(Constants.GREEN_RIGHT_LED_PATH, green);
        FileUtils.writeLine(Constants.BLUE_LED_PATH, blue);
        FileUtils.writeLine(Constants.BLUE_RIGHT_LED_PATH, blue);
    }
}
