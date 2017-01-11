package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

/**
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 */

/**
 * This class only cares about hide or unhide the FAB because the anchor behavior is something
 * already in FAB.
 */
public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {

    /**
     * One of the point used to set hide() or show() in FAB
     */
    private float offset;
    /**
     * The FAB should be hidden when it reach {@link #offset} or when {@link BottomSheetBehaviorGoogleMapsLike}
     * is visually lower than {@link BottomSheetBehaviorGoogleMapsLike#getPeekHeight()}.
     * We got a reference to the object to allow change dynamically PeekHeight in BottomSheet and
     * got updated here.
     */
    private BottomSheetBehaviorGoogleMapsLike mBottomSheetBehavior;

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
        offset = 0;
        mBottomSheetBehavior = null;
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionButton child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        /**
         * Because we are not moving it, we always return false in this method.
         */

        if (offset == 0)
            setOffsetValue(parent);

        if (mBottomSheetBehavior == null)
            getBottomSheetBehavior(parent);

        if (dependency.getY() <=0) {
            if (child.getVisibility() == View.VISIBLE)
                child.hide();
            return false;
        }

        if (child.getY() <= (offset + child.getHeight()) && child.getVisibility() == View.VISIBLE)
            child.hide();
        else if (child.getY() > offset) {

            /**
             * We are calculating every time point in Y where BottomSheet get {@link BottomSheetBehaviorGoogleMapsLike#STATE_COLLAPSED}.
             * If PeekHeight change dynamically we can reflect the behavior asap.
             */
            int collapsedY = dependency.getHeight() - mBottomSheetBehavior.getPeekHeight();
            if ((dependency.getY() > collapsedY))
                child.hide();
            else
                child.show();
        }

        return false;
    }

    /**
     * Define one of the point in where the FAB should be hide when it reachs that point.
     * @param coordinatorLayout container of BottomSheet and AppBarLayout
     */
    private void setOffsetValue(CoordinatorLayout coordinatorLayout) {

        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
            View child = coordinatorLayout.getChildAt(i);

            if (child instanceof AppBarLayout) {

                if (child.getTag() != null &&
                        child.getTag().toString().contentEquals("modal-appbar") ) {
                    offset = child.getY()+child.getHeight();
                    break;
                }
            }
        }
    }

    /**
     * Look into the CoordiantorLayout for the {@link BottomSheetBehaviorGoogleMapsLike}
     * @param coordinatorLayout with app:layout_behavior= {@link BottomSheetBehaviorGoogleMapsLike}
     */
    private void getBottomSheetBehavior(CoordinatorLayout coordinatorLayout) {

        for (int i = 0; i < coordinatorLayout.getChildCount(); i++) {
            View child = coordinatorLayout.getChildAt(i);

            if (child instanceof NestedScrollView) {

                try {
                    mBottomSheetBehavior = BottomSheetBehaviorGoogleMapsLike.from(child);
                    break;
                }
                catch (IllegalArgumentException e){}
            }
        }
    }
}