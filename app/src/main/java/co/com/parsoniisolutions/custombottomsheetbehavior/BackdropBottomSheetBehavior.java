package co.com.parsoniisolutions.custombottomsheetbehavior;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

/**
 *
 */
public class BackdropBottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private float mImageHeight;
    private float mYmultiplier;
    private float mPreviousY;
    private View mChild;

    public BackdropBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                                          View dependency) {

        if (mYmultiplier == 0) {
            initValues(child, dependency);
            return true;
        }

        float dVerticalScroll = dependency.getY() - mPreviousY;
        mPreviousY = dependency.getY();

        //going up
        if (dVerticalScroll <= 0 && child.getY() <= 0) {
            child.setY(0);
            return true;
        }

        //going down
        if (dVerticalScroll >= 0 && dependency.getY() <= mImageHeight)
            return false;

        child.setY( (int)(child.getY() + (dVerticalScroll * mYmultiplier) ) );

        return true;
    }


    private void initValues(View child, View dependency) {

        BottomSheetBehaviorGoogleMapsLike bottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(dependency);
        int peekHeight = bottomSheetBehavior.getPeekHeight();

        ViewParent viewParent = dependency.getParent();
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) viewParent;
        float heightParent = coordinatorLayout.getHeight();

        mImageHeight = child.getHeight();

        float bottomSheetScroll_vertical_height = heightParent - mImageHeight - peekHeight;
        mYmultiplier = (mImageHeight / bottomSheetScroll_vertical_height) + 1;

        mPreviousY = dependency.getY();
        child.setY((int) mPreviousY);


        mChild = child;
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehaviorGoogleMapsLike.State int newState) {
                if (newState == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED ||
                        newState == BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN)
                    mChild.setY(bottomSheet.getY());
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }
}
