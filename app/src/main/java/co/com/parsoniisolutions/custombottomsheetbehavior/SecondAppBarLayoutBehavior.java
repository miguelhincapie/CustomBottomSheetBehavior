package co.com.parsoniisolutions.custombottomsheetbehavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;

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
    private BottomSheetBehaviorGoogleMapsLike bottomSheetBehavior;

    private ViewPropertyAnimator mToolbarAnimation;

    public SecondAppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        hidden = true;
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
        if (dVerticalScroll <= 0 && dependency.getY() <= mAnchorPoint && hidden) {
            showToolBar(child);
            return true;
        }

        //going down
        if (dVerticalScroll >= 0 && dependency.getY() > mAnchorPoint && !hidden) {
            dismissToolBar(child);
            return true;
        }

        return false;
    }

    private void initValues(final View child, View dependency) {

        mChild = child;
        mInitialY = child.getY();
        hidden = true;
        child.setVisibility(View.INVISIBLE);

        bottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(dependency);
        mAnchorPoint = bottomSheetBehavior.mAnchorPoint;

        AppBarLayout appBarLayout = (AppBarLayout)child;
        Toolbar newtoolbar = (Toolbar) appBarLayout.getChildAt(0);
        newtoolbar.setNavigationIcon(mContext.getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
        newtoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });
    }

    private void dismissToolBar(View child){
        hidden = true;

        mToolbarAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
        mToolbarAnimation.alpha(0).start();
        child.setVisibility(View.INVISIBLE);
    }

    private void showToolBar(View child) {
        hidden = false;

        child.setY(-child.getHeight()/3);
        mToolbarAnimation = child.animate().setDuration(mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));
        mToolbarAnimation.alpha(1).y(mInitialY).start();
        child.setVisibility(View.VISIBLE);
    }
}
