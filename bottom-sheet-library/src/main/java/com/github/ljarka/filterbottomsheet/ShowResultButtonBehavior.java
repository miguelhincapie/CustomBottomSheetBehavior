package com.github.ljarka.filterbottomsheet;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

public class ShowResultButtonBehavior extends CoordinatorLayout.Behavior<View> {
    private boolean isButtonVisible = false;
    private int anchorPoint;

    public ShowResultButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShowResultsButtonLayoutBehavior_Params);
        anchorPoint = typedArray.getDimensionPixelSize(R.styleable.ShowResultsButtonLayoutBehavior_Params_behavior_show_result_button_anchor_point, 0);
        typedArray.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, final View child, View dependency) {

        float translationY = dependency.getY() - anchorPoint;

        if (translationY <= 0) {
            translationY = 0;
            isButtonVisible = true;
        }

        if (translationY > child.getHeight()) {
            translationY = child.getHeight();
            isButtonVisible = false;
        }

        child.setTranslationY(translationY);
        return true;
    }

    public boolean isButtonVisible() {
        return isButtonVisible;
    }
}
