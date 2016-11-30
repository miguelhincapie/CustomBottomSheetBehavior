package com.github.ljarka.filterbottomsheet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

public class MergedAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private boolean isInit = false;
    private float anchorPoint;
    private boolean isVisible = false;
    private boolean isTitleVisible = false;
    private boolean isFullBackground = false;
    private String toolbarTitle;
    private Toolbar toolbar;
    private TextView titleTextView;
    private View.OnClickListener onNavigationClickListener;
    private float currentTitleAlpha = 0;
    private OnTitleTextViewReadyListener onTitleTextViewReadyListener;
    private ObjectAnimator currentCloseIconAnimator;

    public MergedAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MergedAppBarLayoutBehavior_Params);
        setAnchorPoint(typedArray.getDimensionPixelSize(R.styleable.MergedAppBarLayoutBehavior_Params_behavior_merged_appbar_anchor_point, 0));
        typedArray.recycle();
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

        if (isDependencyYBelowAnchorPoint(dependency)) {
            setToolbarVisible(false, child);
        } else if (isDependencyYBetweenAnchorPointAndToolbar(child, dependency)) {
            showOnlyCloseIconWithTransparentBackground(child);
        } else if (isDependencyYBelowToolbar(child, dependency) && !isDependencyYReachTop(dependency)) {
            setTitleVisible(false);
            setFullBackGroundColor(android.R.color.transparent);
        } else if (isDependencyYBelowStatusToolbar(child, dependency) || isDependencyYReachTop(dependency)) {
            showToolbarWithTitleAndColor();
        }
        return true;
    }

    private void showToolbarWithTitleAndColor() {
        if (currentCloseIconAnimator != null && currentCloseIconAnimator.isRunning()) {
            waitForFinishingCloseIconAnimationAndShowTitle();
        } else {
            setTitleVisible(true);
            setFullBackGroundColor(R.color.colorPrimary);
        }
    }

    private void waitForFinishingCloseIconAnimationAndShowTitle() {
        currentCloseIconAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setTitleVisible(true);
                setFullBackGroundColor(R.color.colorPrimary);
                currentCloseIconAnimator.removeListener(this);
            }
        });
    }

    private void showOnlyCloseIconWithTransparentBackground(View child) {
        setTitleVisible(false);
        if (!isVisible) {
            showCloseButtonWithTranslateAnimation(child);
        }
        setToolbarVisible(true, child);
        setFullBackGroundColor(android.R.color.transparent);
    }

    private void showCloseButtonWithTranslateAnimation(View child) {
        currentCloseIconAnimator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, -child.getHeight(), 0);
        currentCloseIconAnimator.setDuration(200);
        currentCloseIconAnimator.setInterpolator(new DecelerateInterpolator());
        currentCloseIconAnimator.start();
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, View child) {
        return new SavedState(super.onSaveInstanceState(parent, child),
                isVisible,
                toolbarTitle,
                currentTitleAlpha,
                isTitleVisible,
                isFullBackground);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, View child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        this.isVisible = ss.mVisible;
        this.toolbarTitle = ss.mToolbarTitle;
        this.currentTitleAlpha = ss.mTitleAlpha;
        this.isTitleVisible = ss.isTitleVisible;
        this.isFullBackground = ss.isFullBackground;
        init(child);
    }

    private void init(@NonNull View child) {
        AppBarLayout appBarLayout = (AppBarLayout) child;
        hideShadowForTransparentAppBarLayout(appBarLayout);

        if (toolbar == null) {
            toolbar = (Toolbar) appBarLayout.findViewById(R.id.expanded_toolbar);
        }

        if (onNavigationClickListener != null) {
            toolbar.setNavigationOnClickListener(onNavigationClickListener);
        }

        if (titleTextView == null) {
            titleTextView = findTitleTextView(toolbar);
            if (onTitleTextViewReadyListener != null) {
                if (titleTextView.getHeight() > 0) {
                    onTitleTextViewReadyListener.onTitleTextViewReady(titleTextView);
                } else {
                    titleTextView.post(() -> onTitleTextViewReadyListener.onTitleTextViewReady(titleTextView));
                }
            }
        }
        child.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        setFullBackGroundColor(isFullBackground ? R.color.colorPrimary : android.R.color.transparent);
        setTitleVisible(isTitleVisible);
        titleTextView.setText(toolbarTitle);
        titleTextView.setAlpha(currentTitleAlpha);
        isInit = true;
    }

    private void hideShadowForTransparentAppBarLayout(AppBarLayout appBarLayout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
    }

    private boolean isDependencyYBelowAnchorPoint(@NonNull View dependency) {
        return dependency.getY() > anchorPoint;
    }

    private boolean isDependencyYBetweenAnchorPointAndToolbar(@NonNull View child, @NonNull View dependency) {
        return dependency.getY() <= anchorPoint && dependency.getY() > child.getY() + child.getHeight();
    }

    private boolean isDependencyYBelowToolbar(@NonNull View child, @NonNull View dependency) {
        return dependency.getY() <= child.getY() + child.getHeight() && dependency.getY() > child.getY();
    }

    private boolean isDependencyYBelowStatusToolbar(@NonNull View child, @NonNull View dependency) {
        return dependency.getY() <= child.getY();
    }

    private boolean isDependencyYReachTop(@NonNull View dependency) {
        return dependency.getY() == 0;
    }

    private void setFullBackGroundColor(@ColorRes int colorRes) {
        toolbar.setBackgroundColor(ContextCompat.getColor(toolbar.getContext(), colorRes));
        isFullBackground = colorRes != android.R.color.transparent;
    }

    private TextView findTitleTextView(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View toolBarChild = toolbar.getChildAt(i);
            if (toolBarChild instanceof TextView &&
                    ((TextView) toolBarChild).getText() != null &&
                    ((TextView) toolBarChild).getText().toString().contentEquals(toolbar.getContext().getResources()
                            .getString(R.string.key_binding_default_toolbar_name))) {
                return (TextView) toolBarChild;
            }
        }
        throw new IllegalStateException("Toolbar title view not found");
    }

    private void setToolbarVisible(boolean visible, final View child) {
        Context context = child.getContext();
        if (visible && !isVisible) {
            child.setVisibility(View.VISIBLE);
            ((AppCompatActivity) context).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(onNavigationClickListener);
            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            isVisible = true;
        } else if (!visible && isVisible) {
            child.setVisibility(View.INVISIBLE);
            ((AppCompatActivity) context).setSupportActionBar(null);
            isVisible = false;
        }
    }

    private void setTitleVisible(boolean visible) {

        if ((visible && titleTextView.getAlpha() == 1) ||
                (!visible && titleTextView.getAlpha() == 0))
            return;

        titleTextView.setAlpha(visible ? 1 : 0);
        currentTitleAlpha = titleTextView.getAlpha();
        isTitleVisible = visible;
    }

    public void setNavigationOnClickListener(View.OnClickListener listener) {
        this.onNavigationClickListener = listener;
    }

    public void setToolbarTitle(String title) {
        this.toolbarTitle = title;
        if (this.toolbar != null)
            this.toolbar.setTitle(title);
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    public void setAnchorPoint(float anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    public void setOnTitleTextViewReadyListener(OnTitleTextViewReadyListener onTitleTextViewReadyListener) {
        this.onTitleTextViewReadyListener = onTitleTextViewReadyListener;
    }

    protected static class SavedState extends View.BaseSavedState {

        final boolean mVisible;
        final String mToolbarTitle;
        final float mTitleAlpha;
        final boolean isTitleVisible;
        final boolean isFullBackground;

        public SavedState(Parcel source) {
            super(source);
            mVisible = source.readByte() != 0;
            mToolbarTitle = source.readString();
            mTitleAlpha = source.readFloat();
            isTitleVisible = source.readByte() == 1;
            isFullBackground = source.readByte() == 1;
        }

        public SavedState(Parcelable superState, boolean visible, String toolBarTitle,
                          float titleAlpha, boolean isTitleVisible, boolean isFullBackground) {
            super(superState);
            this.mVisible = visible;
            this.mToolbarTitle = toolBarTitle;
            this.mTitleAlpha = titleAlpha;
            this.isFullBackground = isFullBackground;
            this.isTitleVisible = isTitleVisible;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mVisible ? 1 : 0));
            out.writeString(mToolbarTitle);
            out.writeFloat(mTitleAlpha);
            out.writeByte((byte) (isTitleVisible ? 1 : 0));
            out.writeByte((byte) (isFullBackground ? 1 : 0));
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
