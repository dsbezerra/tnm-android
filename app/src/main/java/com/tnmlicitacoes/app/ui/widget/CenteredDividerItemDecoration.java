package com.tnmlicitacoes.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

/**
 * Created by diegobezerra on 6/4/17.
 */

public class CenteredDividerItemDecoration extends RecyclerView.ItemDecoration {


    private static final String TAG = "CenteredDividerItemDeco";

    private Drawable mDivider;

    private int mFactor;

    public CenteredDividerItemDecoration(Context context, int drawableResId, int factor) {
        mDivider = context.getResources().getDrawable(drawableResId);
        mFactor = factor;
    }

    public CenteredDividerItemDecoration(Context context, int drawableResId) {
        mDivider = context.getResources().getDrawable(drawableResId);
        mFactor = 2;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        // Parent width
        int parentWidth = parent.getWidth();

        // Center position relative to the parent
        int parentCenterX = parentWidth / 2;

        // How much we need to shrink in both sides
        int shrinkSize = parentCenterX / mFactor;

        // Get the left position
        int left = parent.getPaddingLeft() + parentCenterX - shrinkSize;
        // Get the right position
        int right = parentWidth - parent.getPaddingRight() - parentCenterX + shrinkSize;

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

}
