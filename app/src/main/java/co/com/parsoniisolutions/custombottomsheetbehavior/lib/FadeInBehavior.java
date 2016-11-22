package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;

public class FadeInBehavior extends CoordinatorLayout.Behavior<FrameLayout> {
    public static final int ANCHOR_SCREEN_POSITION = 400;
    public static final int BOTTOM_SHEET_HEIGHT = 1088;
    private static final float MIN_ALPHA = 0;
    private static final float MAX_ALPHA = 0.5f;

    public FadeInBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FrameLayout child, View dependency) {
        if (dependency.getY() > ANCHOR_SCREEN_POSITION) {
            child.setAlpha(positionToAlpha(dependency.getY()));
        }
        return true;
    }

    private float positionToAlpha(float position) {
        return (position - BOTTOM_SHEET_HEIGHT)
                * ((MAX_ALPHA - MIN_ALPHA) / (ANCHOR_SCREEN_POSITION - BOTTOM_SHEET_HEIGHT))
                + MIN_ALPHA;
    }
}
