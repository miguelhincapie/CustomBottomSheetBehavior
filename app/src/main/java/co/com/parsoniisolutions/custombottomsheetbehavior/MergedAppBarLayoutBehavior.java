package co.com.parsoniisolutions.custombottomsheetbehavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 *
 */
public class MergedAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    boolean mInit = false;

    private FrameLayout.LayoutParams mBackGroundLayoutParams;

    private Context mContext;
    private float mAnchorPoint;
    private float mInitialY;
    private boolean mHidden = true;

    private String mToolbarTitle;

    private Toolbar mToolbar;
    private TextView mTitleTextView;
    private View mStatusBarBackground;
    private View mBackground;
    private View.OnClickListener mOnNavigationClickListener;

    private ValueAnimator mTitleAlphaValueAnimator;

    public MergedAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.MergedAppBarLayoutBehavior_Params);
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

        if(isDependencyYBelowAnchorPoint(dependency)){

            setToolbarVisible(false,child);

        }else if(isDependencyYBetweenAnchorPointAndToolbar(child,dependency)){

            setToolbarVisible(true,child);
            setPartialBackGroundHeight(0);

        } else if(isDependencyYBelowToolbar(child, dependency) && ! isDependencyYReachTop(dependency)){

            if(isStatusBarVisible())
                setStatusBarBackgroundVisible(false);
            if(isTitleVisible())
                setTitleVisible(false);
            setFullBackGroundColor(android.R.color.transparent);
            setPartialBackGroundHeight((int)((child.getHeight() + child.getY()) - dependency.getY()));

        } else if(isDependencyYBelowStatusToolbar(child, dependency) || isDependencyYReachTop(dependency)){

            if(!isStatusBarVisible())
                setStatusBarBackgroundVisible(true);
            if(!isTitleVisible())
                setTitleVisible(true);
            setFullBackGroundColor(R.color.colorPrimary);
            setPartialBackGroundHeight(0);
        }
        return false;
    }

    private void init(@NonNull View child){
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
        mTitleTextView.setAlpha(0);
        mInitialY = child.getY();
        child.setVisibility(View.INVISIBLE);
        mInit = true;
    }

    private boolean isDependencyYBelowAnchorPoint(@NonNull View dependency){
        return dependency.getY() > mAnchorPoint;
    }

    private boolean isDependencyYBetweenAnchorPointAndToolbar(@NonNull View child, @NonNull View dependency){
        return dependency.getY() <= mAnchorPoint && dependency.getY() > child.getY() + child.getHeight();
    }

    private boolean isDependencyYBelowToolbar(@NonNull View child, @NonNull View dependency){
        return dependency.getY() <= child.getY() + child.getHeight() && dependency.getY() > child.getY();
    }

    private boolean isDependencyYBelowStatusToolbar(@NonNull View child, @NonNull View dependency){
        return dependency.getY() <= child.getY();
    }

    private boolean isDependencyYReachTop(@NonNull View dependency){
        return dependency.getY() == 0;
    }

    private void setPartialBackGroundHeight(int height){
        mBackGroundLayoutParams.height = height;
        mBackground.setLayoutParams(mBackGroundLayoutParams);
    }

    private void setFullBackGroundColor(@ColorRes int colorRes){
        mToolbar.setBackgroundColor(ContextCompat.getColor(mContext,colorRes));
    }

    private TextView findTitleTextView(Toolbar toolbar){
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View toolBarChild = toolbar.getChildAt(i);
            if (toolBarChild instanceof TextView &&
                    ((TextView)toolBarChild).getText() != null &&
                    ((TextView)toolBarChild).getText().toString().contentEquals(mContext.getResources().getString(R.string.key_binding_default_toolbar_name))) {
                return (TextView) toolBarChild;
            }
        }
        return null;
    }

    private void setToolbarVisible(boolean visible, final View child){
        ViewPropertyAnimator mAppBarLayoutAnimation;
        if(visible && mHidden){
            child.setY(-child.getHeight()/3);
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
                    ((AppCompatActivity)mContext).setSupportActionBar(mToolbar);
                    mToolbar.setNavigationOnClickListener(mOnNavigationClickListener);
                    ActionBar actionBar = ((AppCompatActivity)mContext).getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    }
                    mHidden = false;
                }
            });
            mAppBarLayoutAnimation.alpha(1).y(mInitialY).start();
        }else if(!visible && !mHidden){
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
                    ((AppCompatActivity)mContext).setSupportActionBar(null);
                    mHidden = true;
                }
            });
            mAppBarLayoutAnimation.alpha(0).start();
        }
    }

    private boolean isTitleVisible(){
        return mTitleTextView.getAlpha() == 1;
    }

    private void setTitleVisible(boolean visible){

        if((visible && mTitleTextView.getAlpha() == 1)||
          (!visible && mTitleTextView.getAlpha() == 0))
            return;

        if(mTitleAlphaValueAnimator == null || !mTitleAlphaValueAnimator.isRunning()){
            mToolbar.setTitle(mToolbarTitle);
            int startAlpha = visible ? 0 : 1;
            int endAlpha = visible ? 1 : 0;

            mTitleAlphaValueAnimator = ValueAnimator.ofFloat(startAlpha,endAlpha);
            mTitleAlphaValueAnimator.setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mTitleAlphaValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mTitleTextView.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            mTitleAlphaValueAnimator.start();
        }
    }

    private boolean isStatusBarVisible(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return ((Activity)mContext).getWindow().getStatusBarColor() == ContextCompat.getColor(mContext,R.color.colorPrimaryDark);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return mStatusBarBackground != null;
        }
        return false;
    }

    private void setStatusBarBackgroundVisible(boolean visible){
        if(visible){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

                Window window = ((Activity)mContext).getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                mStatusBarBackground = new View(mContext);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(mStatusBarBackground == null){
                    Window w = ((Activity)mContext).getWindow();
                    w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    //status bar height
                    int statusBarHeight = getStatusBarHeight();
                    mStatusBarBackground = new View(mContext);
                    mStatusBarBackground.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    mStatusBarBackground.getLayoutParams().height = statusBarHeight;
                    ((ViewGroup) w.getDecorView()).addView(mStatusBarBackground);
                    mStatusBarBackground.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                }
            }
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Window window = ((Activity)mContext).getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(ContextCompat.getColor(mContext,android.R.color.transparent));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                if(mStatusBarBackground != null){
                    Window w = ((Activity)mContext).getWindow();
                    ((ViewGroup) w.getDecorView()).removeView(mStatusBarBackground);
                    mStatusBarBackground = null;
                }
            }
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setNavigationOnClickListener(View.OnClickListener listener){
        this.mOnNavigationClickListener = listener;
    }

    public void setToolbarTitle(String title) {
        this.mToolbarTitle = title;
        if(this.mToolbar!=null)
            this.mToolbar.setTitle(title);
    }

    public void setAnchorPoint(float anchorPoint) {
        this.mAnchorPoint = anchorPoint;
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
