package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;

public class ScrollingAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private boolean isInit = false;
    private Context context;
    private boolean isVisible = true;

    private int mPeekHeight;

    private ValueAnimator mAppBarYValueAnimator;

    public ScrollingAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollingAppBarLayoutBehavior_Params);
        setPeekHeight(a.getDimensionPixelSize(R.styleable.ScrollingAppBarLayoutBehavior_Params_behavior_scrolling_appbar_peek_height, 0));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (!isInit) {
            init(child);
            return false;
        }

        setAppBarVisible((AppBarLayout) child, dependency.getY() >= dependency.getHeight() - mPeekHeight);
        return false;
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
        if (!isVisible) child.setY((int) child.getY() - child.getHeight() - getStatusBarHeight());
        isInit = true;
    }

    public void setPeekHeight(int peekHeight) {
        this.mPeekHeight = peekHeight;
    }

    public void setAppBarVisible(final AppBarLayout appBarLayout, final boolean visible) {
        if (visible == this.isVisible)
            return;

        if (mAppBarYValueAnimator == null || !mAppBarYValueAnimator.isRunning()) {

            mAppBarYValueAnimator = ValueAnimator.ofFloat(
                    (int) appBarLayout.getY(),
                    visible ? (int) appBarLayout.getY() + appBarLayout.getHeight() + getStatusBarHeight() :
                            (int) appBarLayout.getY() - appBarLayout.getHeight() - getStatusBarHeight());
            mAppBarYValueAnimator.setDuration(context.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mAppBarYValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    appBarLayout.setY((Float) animation.getAnimatedValue());

                }
            });
            mAppBarYValueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    ScrollingAppBarLayoutBehavior.this.isVisible = visible;
                }
            });
            mAppBarYValueAnimator.start();
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

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
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
