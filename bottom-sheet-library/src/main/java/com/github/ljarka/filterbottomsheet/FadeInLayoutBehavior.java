package com.github.ljarka.filterbottomsheet;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class FadeInLayoutBehavior extends CoordinatorLayout.Behavior<FrameLayout> {
    private static final float MIN_ALPHA = 0;
    private static final float MAX_ALPHA = 0.5f;
    private boolean isInit = false;
    private float bottomSheetInitialPosition;
    private float anchorPoint;

    public FadeInLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FadeInBehavior_Params);
        anchorPoint = typedArray.getDimensionPixelSize(R.styleable.FadeInBehavior_Params_fade_in_behavior_anchor_point, 0);
        typedArray.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        return dependency instanceof BottomSheetView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FrameLayout child, View dependency) {
        if (!isInit) {
            BottomSheetView bottomSheetView = (BottomSheetView) dependency;
            bottomSheetInitialPosition = bottomSheetView.getInitialPosition();
            isInit = true;
        }

        if (dependency.getY() >= anchorPoint && isInit) {
            child.setAlpha(positionToAlpha(dependency.getY()));
        }
        return true;
    }

    private float positionToAlpha(float position) {
        return (position - bottomSheetInitialPosition)
                * ((MAX_ALPHA - MIN_ALPHA) / (anchorPoint - bottomSheetInitialPosition))
                + MIN_ALPHA;
    }
}
