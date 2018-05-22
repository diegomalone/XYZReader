package com.diegomalone.xyzreader.utils;

import android.content.Context;
import android.util.TypedValue;

public class DimenUtils {

    public static int getSizeInDips(Context context, float sizeInPixels) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInPixels, context.getResources().getDisplayMetrics());
    }

    public static float dipToPixels(Context context, float dipValue) {
        return (int) (dipValue / context.getResources().getDisplayMetrics().density);
    }
}
