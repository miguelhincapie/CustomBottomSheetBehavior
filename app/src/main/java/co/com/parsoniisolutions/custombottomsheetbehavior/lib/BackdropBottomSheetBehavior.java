package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;

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
 * This class will link the Backdrop element (that can be anything extending View) with a
 * NestedScrollView (the dependency). Whenever dependecy is moved, the backdrop will be moved too
 * behaving like parallax effect.
 *
 * The backdrop need to be <bold>into</bold> a CoordinatorLayout and <bold>before</bold>
 * {@link BottomSheetBehaviorGoogleMapsLike} in the XML file to get same behavior like Google Maps.
 * It doesn't matter where the backdrop element start in XML, it will be moved following
 * Google Maps's parallax behavior.
 * @param <V>
 */
public class BackdropBottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    /**
     * Defines the point in where the backdrop will be consider being in collapsed state.
     * It <bold>should</bold> be the same used in {@link BottomSheetBehaviorGoogleMapsLike} if you
     * want the image got hidden behind the BottomSheet when it reaches the collapsed state.
     */
    private int mPeekHeight;
    /**
     * Following {@link #onDependentViewChanged}'s docs mCurrentChildY just save the child Y
     * position.
     */
    private int mCurrentChildY;

    public BackdropBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackdropBottomSheetBehavior_Params);
        setPeekHeight(a.getDimensionPixelSize(R.styleable.BackdropBottomSheetBehavior_Params_behavior_backdrop_peekHeight, 0));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        /**
         * mCollapsedY and mAnchorPointY are calculated every time looking for
         * flexibility, in case that dependency's height, child's height or {@link #mPeekHeight}'s
         * value changes throught the time, I mean, you can have a {@link android.widget.ImageView}
         * using images with different sizes and you don't want to resize them or so
         */

        /**
         * mCollapsedY: Y position in where backdrop get hidden behind dependency.
         * mPeekHeight and mCollapsed are the same point on screen.
         */
        int mCollapsedY = dependency.getHeight() - mPeekHeight;
        /**
         * mAnchorPointY: with top being Y=0, mAnchorPointY defines the point in Y where could
         * happen 2 things:
         * The backdrop should be moved behind dependency view (when {@link #mCurrentChildY} got
         * positive values) or the dependency view overlaps the backdrop (when
         * {@link #mCurrentChildY} got negative values)
         */
        int mAnchorPointY = child.getHeight();
        /**
         * lastCurrentChildY: Just to know if we need to return true or false at the end of this
         * method.
         */
        int lastCurrentChildY = mCurrentChildY;

        if((mCurrentChildY = (int) ((dependency.getY()-mAnchorPointY) * mCollapsedY / (mCollapsedY-mAnchorPointY))) <= 0)
            child.setY(mCurrentChildY = 0);
        else
            child.setY(mCurrentChildY);
        return (lastCurrentChildY == mCurrentChildY);
    }

    /**
     * Set the PeekHeight like you do in {@link BottomSheetBehaviorGoogleMapsLike}
     * @param peakHeight It <bold>should</bold> be the same used in {@link BottomSheetBehaviorGoogleMapsLike}
     *                   if you want the image got hidden behind the BottomSheet when it reaches
     *                   the collapsed state.
     */
    public void setPeekHeight(int peakHeight) {
        this.mPeekHeight = peakHeight;
    }
}
