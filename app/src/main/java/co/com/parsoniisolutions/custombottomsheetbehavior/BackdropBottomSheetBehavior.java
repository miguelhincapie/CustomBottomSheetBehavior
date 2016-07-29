package co.com.parsoniisolutions.custombottomsheetbehavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

public class BackdropBottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    boolean mInit = false;
    int mCollapsedY;
    int mPeakHeight;
    int mAnchorPointY;
    int mCurrentChildY;

    public BackdropBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.BackdropBottomSheetBehavior_Params);
        setPeekHeight(a.getDimensionPixelSize(R.styleable.BackdropBottomSheetBehavior_Params_behavior_backdrop_peekHeight, 0));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        if(!mInit){
            init(child, dependency);
            return false;
        }
        if((mCurrentChildY = (int) ((dependency.getY()-mAnchorPointY) * mCollapsedY / (mCollapsedY-mAnchorPointY))) <= 0)
            child.setY(mCurrentChildY = 0);
        else
            child.setY(mCurrentChildY);
        return true;
    }

    private void init(@NonNull View child, @NonNull View dependency){
        mCollapsedY = dependency.getHeight() - (2 * mPeakHeight);
        mAnchorPointY = child.getHeight();
        mCurrentChildY = (int) dependency.getY();
        mInit = true;
    }

    public void setPeekHeight(int peakHeight) {
        this.mPeakHeight = peakHeight;
    }
}
