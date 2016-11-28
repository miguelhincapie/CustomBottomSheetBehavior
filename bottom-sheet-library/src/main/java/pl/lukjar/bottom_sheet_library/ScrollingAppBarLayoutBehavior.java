package pl.lukjar.bottom_sheet_library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ScrollingAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private boolean isInit = false;
    private Context context;
    private boolean isVisible = true;

    private int peekHeight;
    private View dependentView;

    private ValueAnimator appBarYValueAnimator;

    public ScrollingAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs,
                pl.lukjar.bottom_sheet_library.R.styleable.ScrollingAppBarLayoutBehavior_Params);
        setPeekHeight(a.getDimensionPixelSize(
                pl.lukjar.bottom_sheet_library.R.styleable
                        .ScrollingAppBarLayoutBehavior_Params_behavior_scrolling_appbar_peek_height, 0));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof BottomSheetView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (!isInit) {
            init(child);
            return false;
        }

        setAppBarVisible((AppBarLayout) child, dependentView, dependency.getY() >= dependency.getHeight() - peekHeight);
        return true;
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, View child) {
        return new SavedState(super.onSaveInstanceState(parent, child), isVisible);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, View child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        this.isVisible = ss.isVisible;
    }

    private void init(View child) {
        if (!isVisible) {
            child.setY((int) child.getY() - child.getHeight() - getStatusBarHeight());
        }
        isInit = true;
    }

    public void setPeekHeight(int peekHeight) {
        this.peekHeight = peekHeight;
    }

    public void setScrollingDependentView(View dependentView) {
        this.dependentView = dependentView;
    }

    public void setAppBarVisible(final AppBarLayout appBarLayout, View dependentView, final boolean visible) {
        if (visible == this.isVisible) {
            return;
        }

        if (appBarYValueAnimator == null || !appBarYValueAnimator.isRunning()) {

            appBarYValueAnimator = ValueAnimator.ofFloat(
                    (int) appBarLayout.getY(),
                    visible ? (int) appBarLayout.getY() + appBarLayout.getHeight() + getStatusBarHeight()
                            : (int) appBarLayout.getY() - appBarLayout.getHeight() - getStatusBarHeight());
            appBarYValueAnimator.setDuration(context.getResources().getInteger(android.R.integer.config_shortAnimTime));
            appBarYValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    appBarLayout.setY((Float) animation.getAnimatedValue());
                    if (dependentView != null) {
                        dependentView.setTranslationY((Float) animation.getAnimatedValue());
                    }
                }
            });
            appBarYValueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {

                    ScrollingAppBarLayoutBehavior.this.isVisible = visible;
                }
            });
            appBarYValueAnimator.start();
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <V extends View> ScrollingAppBarLayoutBehavior from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof ScrollingAppBarLayoutBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with ScrollingAppBarLayoutBehavior");
        }
        return (ScrollingAppBarLayoutBehavior) behavior;
    }

    protected static class SavedState extends View.BaseSavedState {

        final boolean isVisible;

        public SavedState(Parcel source) {
            super(source);
            isVisible = source.readByte() != 0;
        }

        public SavedState(Parcelable superState, boolean visible) {
            super(superState);
            this.isVisible = visible;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (isVisible ? 1 : 0));
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
