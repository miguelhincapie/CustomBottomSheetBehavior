package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.TextView;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;

/**
 * This behavior should be applied on an AppBarLayout... More Explanations coming soon
 */
public class MergedAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private static final String TAG = MergedAppBarLayoutBehavior.class.getSimpleName();

    boolean mInit = false;

    private FrameLayout.LayoutParams mBackGroundLayoutParams;

    private Context mContext;
    private float mAnchorPoint;
    private float mInitialY;
    private boolean mVisible = false;

    private String mToolbarTitle;

    private Toolbar mToolbar;
    private TextView mTitleTextView;
    private View mBackground;
    private View.OnClickListener mOnNavigationClickListener;

    private ValueAnimator mTitleAlphaValueAnimator;
    private int mCurrentTitleAlpha = 0;

    public MergedAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MergedAppBarLayoutBehavior_Params);
        setAnchorPoint(a.getDimensionPixelSize(R.styleable.MergedAppBarLayoutBehavior_Params_behavior_merged_appbar_anchor_point, 0));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (!mInit) {
            init(child);
            return false;
        }

        if (isDependencyYBelowAnchorPoint(dependency)) {

            setToolbarVisible(false, child);

        } else if (isDependencyYBetweenAnchorPointAndToolbar(child, dependency)) {
            setToolbarVisible(true, child);
            setFullBackGroundColor(android.R.color.transparent);
        } else if (isDependencyYBelowToolbar(child, dependency) && !isDependencyYReachTop(dependency)) {
            if (isTitleVisible())
                setTitleVisible(false);
            setFullBackGroundColor(android.R.color.transparent);
        } else if (isDependencyYBelowStatusToolbar(child, dependency) || isDependencyYReachTop(dependency)) {
            if (!isTitleVisible())
                setTitleVisible(true);
            setFullBackGroundColor(R.color.colorPrimary);
        }
        return true;
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, View child) {
        return new SavedState(super.onSaveInstanceState(parent, child),
                mVisible,
                mToolbarTitle,
                mCurrentTitleAlpha);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, View child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        this.mVisible = ss.mVisible;
        this.mToolbarTitle = ss.mToolbarTitle;
        this.mCurrentTitleAlpha = ss.mTitleAlpha;
    }

    private void init(@NonNull View child) {

        AppBarLayout appBarLayout = (AppBarLayout) child;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }

        mToolbar = (Toolbar) appBarLayout.findViewById(R.id.expanded_toolbar);
        mBackground = appBarLayout.findViewById(R.id.background);
        mBackGroundLayoutParams = (FrameLayout.LayoutParams) mBackground.getLayoutParams();

        mTitleTextView = findTitleTextView(mToolbar);
        if (mTitleTextView == null)
            return;

        mInitialY = child.getY();

        child.setVisibility(mVisible ? View.VISIBLE : View.INVISIBLE);

        setFullBackGroundColor(mVisible && mCurrentTitleAlpha == 1 ? R.color.colorPrimary : android.R.color.transparent);
        setPartialBackGroundHeight(0);
        mTitleTextView.setText(mToolbarTitle);
        mTitleTextView.setAlpha(mCurrentTitleAlpha);
        mInit = true;
    }

    private boolean isDependencyYBelowAnchorPoint(@NonNull View dependency) {
        return dependency.getY() > mAnchorPoint;
    }

    private boolean isDependencyYBetweenAnchorPointAndToolbar(@NonNull View child, @NonNull View dependency) {
        return dependency.getY() <= mAnchorPoint && dependency.getY() > child.getY() + child.getHeight();
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

    private void setPartialBackGroundHeight(int height) {
        mBackGroundLayoutParams.height = height;
        mBackground.setLayoutParams(mBackGroundLayoutParams);
    }

    private void setFullBackGroundColor(@ColorRes int colorRes) {
        mToolbar.setBackgroundColor(ContextCompat.getColor(mContext, colorRes));
    }

    private TextView findTitleTextView(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View toolBarChild = toolbar.getChildAt(i);
            if (toolBarChild instanceof TextView &&
                    ((TextView) toolBarChild).getText() != null &&
                    ((TextView) toolBarChild).getText().toString().contentEquals(mContext.getResources().getString(R.string.key_binding_default_toolbar_name))) {
                return (TextView) toolBarChild;
            }
        }
        return null;
    }

    private void setToolbarVisible(boolean visible, final View child) {
        ViewPropertyAnimator mAppBarLayoutAnimation;
        if (visible && !mVisible) {
            child.setY(-child.getHeight() / 3);
            mAppBarLayoutAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mAppBarLayoutAnimation.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    child.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((AppCompatActivity) mContext).setSupportActionBar(mToolbar);
                    mToolbar.setNavigationOnClickListener(mOnNavigationClickListener);
                    ActionBar actionBar = ((AppCompatActivity) mContext).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    }
                    mVisible = true;
                }
            });
            mAppBarLayoutAnimation.alpha(1).y(mInitialY).start();
        } else if (!visible && mVisible) {
            mAppBarLayoutAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mAppBarLayoutAnimation.setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    child.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((AppCompatActivity) mContext).setSupportActionBar(null);
                    mVisible = false;
                }
            });
            mAppBarLayoutAnimation.alpha(0).start();
        }
    }

    private boolean isTitleVisible() {
        return mTitleTextView.getAlpha() == 1;
    }

    private void setTitleVisible(boolean visible) {

        if ((visible && mTitleTextView.getAlpha() == 1) ||
                (!visible && mTitleTextView.getAlpha() == 0))
            return;

        mTitleTextView.setAlpha(visible ? 1 : 0);
    }

    public void setNavigationOnClickListener(View.OnClickListener listener) {
        this.mOnNavigationClickListener = listener;
    }

    public void setToolbarTitle(String title) {
        this.mToolbarTitle = title;
        if (this.mToolbar != null)
            this.mToolbar.setTitle(title);
    }

    public void setAnchorPoint(float anchorPoint) {
        this.mAnchorPoint = anchorPoint;
    }

    protected static class SavedState extends View.BaseSavedState {

        final boolean mVisible;
        final String mToolbarTitle;
        final int mTitleAlpha;

        public SavedState(Parcel source) {
            super(source);
            mVisible = source.readByte() != 0;
            mToolbarTitle = source.readString();
            mTitleAlpha = source.readInt();
        }

        public SavedState(Parcelable superState, boolean visible, String toolBarTitle, int titleAlpha) {
            super(superState);
            this.mVisible = visible;
            this.mToolbarTitle = toolBarTitle;
            this.mTitleAlpha = titleAlpha;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mVisible ? 1 : 0));
            out.writeString(mToolbarTitle);
            out.writeInt(mTitleAlpha);
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

    public static <V extends View> MergedAppBarLayoutBehavior from(V view) {
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
