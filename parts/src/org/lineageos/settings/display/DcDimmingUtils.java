/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.display;

import org.lineageos.settings.utils.FileUtils;

public final class DcDimmingUtils {
    public static final String KEY = "dc_dimming_enable";
    public static final String NODE =
            "/sys/devices/platform/soc/soc:qcom,dsi-display-primary/msm_fb_ea_enable";

    private DcDimmingUtils() {
    }

    public static boolean isAvailable() {
        return FileUtils.isFileWritable(NODE);
    }

    public static boolean isEnabled() {
        return "1".equals(FileUtils.readOneLine(NODE));
    }

    public static boolean setEnabled(boolean enabled) {
        return FileUtils.writeLine(NODE, enabled ? "1" : "0") && isEnabled() == enabled;
    }
}
