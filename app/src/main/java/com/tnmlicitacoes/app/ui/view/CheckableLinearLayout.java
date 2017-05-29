package com.tnmlicitacoes.app.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.tnmlicitacoes.app.R;

/**
 * CheckableLinearLayout
 * Custom checkable linear layout
 */

public class CheckableLinearLayout extends LinearLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

    private boolean mChecked;

    public CheckableLinearLayout(Context context) {
        this(context, null);
        init();
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (getBackground() == null) {
            setBackgroundResource(R.drawable.list_item_selector);
        }
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onTouchEvent(ev);
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
            setCheckedRecursive(this, checked);
        }
    }

    private void setCheckedRecursive(ViewGroup parent, boolean checked) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = parent.getChildAt(i);
            if (v instanceof Checkable) {
                ((Checkable) v).setChecked(checked);
            }

            if (v instanceof ViewGroup) {
                setCheckedRecursive((ViewGroup) v, checked);
            }
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        Drawable drawable = getBackground();
        if (drawable != null) {
            int[] myDrawableState = getDrawableState();
            drawable.setState(myDrawableState);
            invalidate();
        }
    }
}
