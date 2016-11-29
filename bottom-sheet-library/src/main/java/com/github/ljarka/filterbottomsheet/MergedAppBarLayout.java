package com.github.ljarka.filterbottomsheet;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MergedAppBarLayout extends AppBarLayout {
    private MergedAppBarLayoutBehavior layoutBehavior;

    public MergedAppBarLayout(Context context) {
        super(context);
        init(context);
    }

    public MergedAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.merged_appbar_layout, this);
    }

    public void setToolbarTitle(String toolbarTitle) {
        lazyInitLayoutBehavior();
        layoutBehavior.setToolbarTitle(toolbarTitle);
    }

    public void setTitleTextViewReadyListener(OnTitleTextViewReadyListener listener) {
        lazyInitLayoutBehavior();
        layoutBehavior.setOnTitleTextViewReadyListener(listener);
    }

    public void setNavigationOnClickListener(View.OnClickListener onClickListener) {
        lazyInitLayoutBehavior();
        layoutBehavior.setNavigationOnClickListener(onClickListener);
    }

    public void lazyInitLayoutBehavior() {
        if (layoutBehavior == null) {
            layoutBehavior = findLayoutBehavior(this);
        }
    }

    private static <V extends View> MergedAppBarLayoutBehavior findLayoutBehavior(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof MergedAppBarLayoutBehavior)) {
            throw new IllegalArgumentException("The view is not associated with " +
                    "MergedAppBarLayoutBehavior");
        }
        return (MergedAppBarLayoutBehavior) behavior;
    }
}
