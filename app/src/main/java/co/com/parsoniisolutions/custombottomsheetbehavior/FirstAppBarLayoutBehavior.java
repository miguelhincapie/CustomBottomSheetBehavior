package co.com.parsoniisolutions.custombottomsheetbehavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 *
 */
public class FirstAppBarLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private Context mContext;
    private float mPreviousY;
    private float mInitialY;
    private boolean hidden;
    private View mChild;

    private ViewPropertyAnimator mToolbarAnimation;

    public FirstAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        hidden = false;
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

        //going up
        if (dVerticalScroll <= 0 && !hidden) {
            dismissAppBar(child);
            return true;
        }

        return false;
    }

    private void initValues(final View child, View dependency) {

        mChild = child;
        mInitialY = child.getY();

        BottomSheetBehaviorGoogleMapsLike bottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(dependency);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehaviorGoogleMapsLike.State int newState) {
                if (newState == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED ||
                        newState == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN)
                    showAppBar(child);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void dismissAppBar(View child){
        hidden = true;
        AppBarLayout appBarLayout = (AppBarLayout)child;
        mToolbarAnimation = appBarLayout.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
        mToolbarAnimation.y(-(mChild.getHeight()+25)).start();
    }

    private void showAppBar(View child) {
        hidden = false;
        AppBarLayout appBarLayout = (AppBarLayout)child;
        mToolbarAnimation = appBarLayout.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        mToolbarAnimation.y(mInitialY).start();
    }
}
