package com.diegomalone.xyzreader.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacingItemDecorator extends RecyclerView.ItemDecoration {

    private boolean isGrid;
    private int space;

    public SpacingItemDecorator(boolean isGrid, int space) {
        this.isGrid = isGrid;
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (isGrid) {
            outRect.left = space;
        }

        outRect.top = space;
    }
}