/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.popupcamera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.lineageos.settings.R;

public final class PopupCameraAnimation {
    private static final String TAG = "PopupCameraAnimation";
    private final Context mContext;
    private final WindowManager mWindowManager;
    private View mView;

    public PopupCameraAnimation(Context context) {
        mContext = context;
        mWindowManager = context.getSystemService(WindowManager.class);
    }

    public void start(boolean opening, int color) {
        stop();
        if (mContext.getResources().getConfiguration().orientation
                != Configuration.ORIENTATION_PORTRAIT || Settings.Global.getFloat(
                        mContext.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
                        == 0.0f) {
            return;
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_camera_animation, null);
        ImageView lowWave = view.findViewById(R.id.popup_camera_low_wave);
        ImageView highWave = view.findViewById(R.id.popup_camera_high_wave);
        if (color != Color.BLUE) {
            float red = Color.red(color) > 0 ? 1.0f : 0.0f;
            float green = Color.green(color) > 0 ? 1.0f : 0.0f;
            float blue = Color.blue(color) > 0 ? 1.0f : 0.0f;
            // Route the blue wave into active LED channels, retaining highlights and alpha.
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(new float[] {
                    1 - red, 0, red, 0, 0,
                    1 - green, 0, green, 0, 0,
                    1 - blue, 0, blue, 0, 0,
                    0, 0, 0, 1, 0
            });
            lowWave.setColorFilter(filter);
            highWave.setColorFilter(filter);
        }
        // These pixel coordinates are from MIUI's UpAndDownAnimationView on LMI.
        lowWave.setTranslationX(opening ? -133.5f : -229.5f);
        highWave.setTranslationX(opening ? 123.0f : 27.0f);
        lowWave.setPivotX(533.5f);
        lowWave.setPivotY(0.0f);
        lowWave.setScaleX(opening ? 2.0f : 0.5f);
        lowWave.setScaleY(opening ? 0.4f : 0.5f);

        Animation lowAnimation = AnimationUtils.loadAnimation(mContext, opening
                ? R.anim.popup_camera_open_low : R.anim.popup_camera_close_low);
        Animation highAnimation = AnimationUtils.loadAnimation(mContext, opening
                ? R.anim.popup_camera_open_high : R.anim.popup_camera_close_high);
        lowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // View animations notify during drawing; remove the window after that traversal.
                view.post(() -> {
                    if (mView == view) {
                        stop();
                    }
                });
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.setFitInsetsTypes(0);
        params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        params.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY;
        params.setTitle("PopupCameraAnimation");
        try {
            mWindowManager.addView(view, params);
        } catch (WindowManager.BadTokenException | SecurityException e) {
            Log.w(TAG, "Unable to show camera animation", e);
            return;
        }
        mView = view;
        lowWave.startAnimation(lowAnimation);
        highWave.startAnimation(highAnimation);
    }

    public void stop() {
        if (mView == null) {
            return;
        }
        View view = mView;
        mView = null;
        view.findViewById(R.id.popup_camera_low_wave).clearAnimation();
        view.findViewById(R.id.popup_camera_high_wave).clearAnimation();
        mWindowManager.removeViewImmediate(view);
    }
}
