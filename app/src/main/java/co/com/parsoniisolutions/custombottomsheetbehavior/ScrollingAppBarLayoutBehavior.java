package co.com.parsoniisolutions.custombottomsheetbehavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 *
 */
public class ScrollingAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    boolean mInit = false;

    private Context mContext;
    private boolean mVisible = true;

    private int mPeekHeight;
    private View mStatusBarBackground;

    private ValueAnimator mAppBarYValueAnimator;

    public ScrollingAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.ScrollingAppBarLayoutBehavior_Params);
        setPeekHeight(a.getDimensionPixelSize(R.styleable.ScrollingAppBarLayoutBehavior_Params_behavior_scrolling_appbar_peek_height, 0));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if (!mInit) {
            init();
            return false;
        }
        setAppBarVisible((AppBarLayout)child,dependency.getY() >= dependency.getHeight() - mPeekHeight);
        return true;
    }

    private void init() {
        setStatusBarBackgroundVisible(true);
        mInit = true;
    }

    public void setPeekHeight(int peekHeight) {
        this.mPeekHeight = peekHeight;
    }

    public void setAppBarVisible(final AppBarLayout appBarLayout, final boolean visible){

        if(visible == mVisible)
            return;

        if(mAppBarYValueAnimator == null || !mAppBarYValueAnimator.isRunning()){

            mAppBarYValueAnimator = ValueAnimator.ofFloat(
                    (int) appBarLayout.getY(),
                    visible ? (int) appBarLayout.getY() + appBarLayout.getHeight() + getStatusBarHeight() :
                              (int) appBarLayout.getY() - appBarLayout.getHeight() - getStatusBarHeight());
            mAppBarYValueAnimator.setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            mAppBarYValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    appBarLayout.setY((Float) animation.getAnimatedValue());

                }
            });
            mAppBarYValueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if(visible)
                        setStatusBarBackgroundVisible(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(!visible)
                        setStatusBarBackgroundVisible(false);
                    mVisible = visible;
                    super.onAnimationEnd(animation);
                }
            });
            mAppBarYValueAnimator.start();
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
}
