/**
 * Copyright (c) 2015-2021 TomTom N.V. All rights reserved.
 * <p>
 * This software is the proprietary copyright of TomTom N.V. and its subsidiaries and may be used
 * for internal evaluation purposes or commercial use strictly subject to separate licensee
 * agreement between you and TomTom. If you are the licensee, you are only permitted to use
 * this Software in accordance with the terms of your license agreement. If you are not the
 * licensee then you are not authorised to use this software in any manner and should
 * immediately return it to TomTom N.V.
 */
package com.truckroute.ecoway.adapaters;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.truckroute.ecoway.R;
import com.truckroute.ecoway.models.LeaderBoardModel;

//Dummy implementation, just to get the example
public class LeaderBoardIconProvider {
    public static final String PROFILE_PREFIX = "profile";

    public static final String ANDROID_DRAWABLE_DIR_NAME = "drawable";

    public Drawable getProfileIcon(Context context, LeaderBoardModel instruction) {
        String iconName;
        iconName = getIconName(PROFILE_PREFIX, instruction.getPosition());
        int resId = context.getResources().getIdentifier(iconName, ANDROID_DRAWABLE_DIR_NAME, context.getPackageName());
        if (resId == 0) {
            resId = R.drawable.maneuver_straight;
        }
        return ContextCompat.getDrawable(context, resId);
    }

    @NonNull
    private String getIconName(String prefix, int position) {
        return prefix + position;
    }


}
