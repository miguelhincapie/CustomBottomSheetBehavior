package co.com.parsoniisolutions.custombottomsheetbehavior;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
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
public class SecondAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private Context mContext;
    private float mPreviousY;
    private float mAnchorPoint;
    private float mInitialY;
    private boolean hidden;
    private View mChild;
    private Toolbar mToolbar;
    private boolean isTransparent;
    private int [] finalLocation = new int[2];
    private ViewPropertyAnimator viewPropertyAnimator;
    private TextView textViewFromToolbar;
    private View statusBarBackground;
    private BottomSheetBehaviorGoogleMapsLike bottomSheetBehavior;

    private ViewPropertyAnimator mAppBarLayoutAnimation;

    public SecondAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        hidden = true;
        isTransparent = true;
    }


    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                                          View dependency) {

        if (mChild == null) {
            initValues(child, dependency);
            return false;
        }

        float dVerticalScroll = dependency.getY() - mPreviousY;
        mPreviousY = dependency.getY();

        //scrolling down
        if (dVerticalScroll <= 0) {
            if (dependency.getY() <= mAnchorPoint && hidden) {
                showToolBar(child);
                return true;
            }
            else if (!hidden) {
                if (dependency.getY() <= child.getY() && isTransparent) {
                    changeAppBarBackground(false, child);
                    showTitleInToolBar();
                    showBackgroundInStatusBar();
                }
            }
        }

        //scrolling up
        if (dVerticalScroll >= 0) {
            if (dependency.getY() > mAnchorPoint && !hidden) {
                dismissToolBar(child);
                return true;
            }
            else if (!hidden) {
                if (dependency.getY() > child.getY() && !isTransparent) {
                    changeAppBarBackground(true, child);
                    dismissTitleInToolBar();
                    dismissBackgroundInStatusBar();
                }
            }
        }

        return false;
    }

    private void initValues(final View child, View dependency) {

        mChild = child;
        mInitialY = child.getY();
        hidden = true;

        bottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(dependency);
        mAnchorPoint = bottomSheetBehavior.mAnchorPoint;

        AppBarLayout appBarLayout = (AppBarLayout)child;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
        mToolbar = (Toolbar) appBarLayout.getChildAt(0);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });

        textViewFromToolbar = null;
        for (int i = 0; i < mToolbar.getChildCount(); i++) {
            View toolBarChild = mToolbar.getChildAt(i);
            if (toolBarChild instanceof TextView &&
                    ((TextView)toolBarChild).getText() != null &&
                    ((TextView)toolBarChild).getText().toString().contentEquals(mContext.getResources().getString(R.string.key_binding_default_toolbar_name))) {
                textViewFromToolbar = (TextView) toolBarChild;
                break;
            }
        }

        child.setVisibility(View.INVISIBLE);

        if (textViewFromToolbar == null)
            return;
        textViewFromToolbar.getLocationOnScreen(finalLocation);
        textViewFromToolbar.setAlpha(0);
    }

    private void dismissToolBar(View child){
        hidden = true;

        mAppBarLayoutAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        mAppBarLayoutAnimation.alpha(0).start();
        child.setVisibility(View.INVISIBLE);
    }

    private void showToolBar(View child) {
        hidden = false;

        child.setY(-child.getHeight()/3);
        mAppBarLayoutAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        mAppBarLayoutAnimation.alpha(1).y(mInitialY).start();
        child.setVisibility(View.VISIBLE);
    }

    private void changeAppBarBackground(boolean transparent, View child) {

        int colorId;
        if (transparent)
            colorId = android.R.color.transparent;
        else
            colorId = R.color.colorPrimary;

        child.setBackgroundResource(colorId);

        isTransparent = transparent;
    }

    private void showTitleInToolBar() {

        CharSequence title = ((MainActivity)mContext).bottomSheetTextView.getText();

        int [] initialLocation = new int[2];
        ((MainActivity)mContext).bottomSheetTextView.getLocationOnScreen(initialLocation);

        mToolbar.setTitle(title);

        if (textViewFromToolbar == null)
            return;

        textViewFromToolbar.setAlpha(0);
        textViewFromToolbar.setX(initialLocation[0]);

        viewPropertyAnimator = textViewFromToolbar.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
        viewPropertyAnimator.alpha(1).x(finalLocation[0]).start();
    }

    private void dismissTitleInToolBar() {

        if (textViewFromToolbar != null) {
            textViewFromToolbar.setAlpha(1);
            viewPropertyAnimator = textViewFromToolbar.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
            viewPropertyAnimator.alpha(0).start();
        }
    }

    private void showBackgroundInStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Window w = ((MainActivity)mContext).getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //status bar height
            int statusBarHeight = getStatusBarHeight();

            statusBarBackground = new View(mContext);
            statusBarBackground.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            statusBarBackground.getLayoutParams().height = statusBarHeight;
            ((ViewGroup) w.getDecorView()).addView(statusBarBackground);
            statusBarBackground.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
        }
    }

    private void dismissBackgroundInStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            Window w = ((MainActivity)mContext).getWindow();
            ((ViewGroup) w.getDecorView()).removeView(statusBarBackground);
        }
    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
