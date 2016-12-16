package com.github.ljarka.filterbottomsheet;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.Vector;

public class BottomSheetBehavior extends CoordinatorLayout.Behavior<BottomSheetView> {

    private static final float DEFAULT_ALPHA_ANIMATION_START_TOP_OFFSET = 150f;
    private static final float DEFAULT_ALPHA_ANIMATION_END_TOP_OFFSET = 25f;
    private static final float DEFAULT_COLOR_CHANGE_INTERVAL = 100f;

    private static final float MAX_PERCENT = 100f;
    private final float alphaAnimationStartTopOffset;
    private final float alphaAnimationEndTopOffset;
    private final float colorChangeInterval;

    public abstract static class BottomSheetCallback {

        public abstract void onStateChanged(@NonNull View bottomSheet, @State int newState);

        public abstract void onSlide(@NonNull View bottomSheet, float slideOffset);

    }

    public static final int STATE_DRAGGING = 1;
    public static final int STATE_SETTLING = 2;
    public static final int STATE_ANCHOR_POINT = 3;
    public static final int STATE_EXPANDED = 4;
    public static final int STATE_COLLAPSED = 5;

    @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_ANCHOR_POINT, STATE_SETTLING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {

    }

    private int mPeekHeight;

    private int mMinOffset;

    private int mMaxOffset;

    private static final int DEFAULT_ANCHOR_POINT = 700;

    private int mAnchorPoint;

    @State
    private int mState = STATE_COLLAPSED;

    private ViewDragHelper mViewDragHelper;

    private boolean mIgnoreEvents;

    private int mLastNestedScrollDy;

    private boolean mNestedScrolled;

    private int mParentHeight;

    private WeakReference<BottomSheetView> mViewRef;

    private WeakReference<View> mNestedScrollingChildRef;

    private Vector<BottomSheetCallback> mCallback;

    private VelocityTracker mVelocityTracker;

    private int mActivePointerId;

    private int mInitialY;

    private boolean mTouchingScrollingChild;

    private float lastTouchY = Float.MAX_VALUE;
    private boolean isMovingDown = false;
    private boolean isTouchingButton = false;
    private boolean enableInternalScrollingInAnchorPointState;

    public BottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, android.support.design.R.styleable.BottomSheetBehavior_Layout);
        setPeekHeight(a.getDimensionPixelSize(android.support.design.R.styleable
                .BottomSheetBehavior_Layout_behavior_peekHeight, 0));
        a.recycle();

        float density = context.getResources().getDisplayMetrics().density;
        alphaAnimationStartTopOffset = DEFAULT_ALPHA_ANIMATION_START_TOP_OFFSET * density;
        alphaAnimationEndTopOffset = DEFAULT_ALPHA_ANIMATION_END_TOP_OFFSET * density;
        colorChangeInterval = DEFAULT_COLOR_CHANGE_INTERVAL * density;

        mAnchorPoint = DEFAULT_ANCHOR_POINT;
        a = context.obtainStyledAttributes(attrs, R.styleable.CustomBottomSheetBehavior);
        if (attrs != null) {
            mAnchorPoint = (int) a.getDimension(R.styleable.CustomBottomSheetBehavior_anchorPoint, 0);
            enableInternalScrollingInAnchorPointState = a.getBoolean(R.styleable
                    .CustomBottomSheetBehavior_enableInternalScrollingInAnchorPointState, false);
        }

        a.recycle();
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, BottomSheetView child) {
        return new SavedState(super.onSaveInstanceState(parent, child), mState);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, BottomSheetView child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            mState = STATE_COLLAPSED;
        } else {
            mState = ss.state;
        }

        notifyStateChangedToListeners(child, mState);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, BottomSheetView child, int layoutDirection) {
        // First let the parent lay it out
        if (mState != STATE_DRAGGING && mState != STATE_SETTLING) {
            if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
                ViewCompat.setFitsSystemWindows(child, true);
            }
            parent.onLayoutChild(child, layoutDirection);
        }
        // Offset the bottom sheet
        mParentHeight = parent.getHeight();
        mMinOffset = Math.max(0, mParentHeight - child.getHeight());
        mMaxOffset = Math.max(mParentHeight - mPeekHeight, mMinOffset);

        if (mState == STATE_ANCHOR_POINT) {
            ViewCompat.offsetTopAndBottom(child, mAnchorPoint);
        } else if (mState == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, mMinOffset);
        } else if (mState == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, mMaxOffset);
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, new CustomDragHelperCallback(child));
        }
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, BottomSheetView child, MotionEvent event) {
        isTouchingButton = event.getY() > child.getTop() && event.getY() < child.getTop() + child.getTitleContainer().getHeight();
        int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_MOVE) {
            isMovingDown = lastTouchY < event.getY();
        }

        if (action == MotionEvent.ACTION_DOWN) {
            lastTouchY = event.getY();
        }

        if (!child.isShown()) {
            return false;
        }
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchingScrollingChild = false;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                // Reset the ignore flag
                if (mIgnoreEvents) {
                    mIgnoreEvents = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                int initialX = (int) event.getX();
                mInitialY = (int) event.getY();
                if (mState == STATE_ANCHOR_POINT) {
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                } else {
                    View scroll = mNestedScrollingChildRef.get();
                    if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
                        mActivePointerId = event.getPointerId(event.getActionIndex());
                        mTouchingScrollingChild = true;
                    }
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID
                        && !parent.isPointInChildBounds(child, initialX, mInitialY);
                break;
        }
        if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event)) {
            return true;
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        View scroll = mNestedScrollingChildRef.get();
        return action == MotionEvent.ACTION_MOVE && scroll != null &&
                !mIgnoreEvents && mState != STATE_DRAGGING &&
                !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) &&
                Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, BottomSheetView child, MotionEvent event) {
        if (!child.isShown()) {
            return false;
        }
        int action = MotionEventCompat.getActionMasked(event);
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true;
        }
        mViewDragHelper.processTouchEvent(event);
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop()) {
                mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
            }
        }
        return !mIgnoreEvents;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, BottomSheetView child, View directTargetChild,
                                       View target, int nestedScrollAxes) {
        mLastNestedScrollDy = 0;
        mNestedScrolled = false;
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, BottomSheetView child, View target, int dx, int dy,
                                  int[] consumed) {
        if (enableInternalScrollingInAnchorPointState) {
            if (shouldScrollInternally(child)) {
                super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
                return;
            }
        }
        View scrollingChild = mNestedScrollingChildRef.get();
        if (target != scrollingChild) {
            return;
        }
        int currentTop = child.getTop();
        int newTop = currentTop - dy;
        if (dy > 0) { // Upward
            if (newTop < mMinOffset) {
                consumed[1] = currentTop - mMinOffset;
                ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                setStateInternal(STATE_EXPANDED);
            } else {
                consumed[1] = dy;
                ViewCompat.offsetTopAndBottom(child, -dy);
                setStateInternal(STATE_DRAGGING);
            }
        } else if (dy < 0) { // Downward
            if (!ViewCompat.canScrollVertically(target, -1)) {
                if (newTop <= mMaxOffset) {
                    consumed[1] = dy;
                    ViewCompat.offsetTopAndBottom(child, -dy);
                    setStateInternal(STATE_DRAGGING);
                } else {
                    consumed[1] = currentTop - mMaxOffset;
                    ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                    setStateInternal(STATE_COLLAPSED);
                }
            }
        }
        dispatchOnSlide(child.getTop(), child);
        mLastNestedScrollDy = dy;
        mNestedScrolled = true;
    }

    private void animateTitle(BottomSheetView child, int bottomSheetTop) {
        float translationInPercent = adjustPercentValue(
                (((float) mAnchorPoint - bottomSheetTop) / (float) mAnchorPoint) * MAX_PERCENT);
        float alphaInPercent = adjustPercentValue(((bottomSheetTop - alphaAnimationEndTopOffset) / (alphaAnimationStartTopOffset - alphaAnimationEndTopOffset)) * MAX_PERCENT);
        float colorChangePercent = adjustPercentValue((bottomSheetTop / colorChangeInterval) * MAX_PERCENT);

        child.translateTextLeft(translationInPercent);
        child.translateTextBottom(translationInPercent);
        child.transformTextSize(translationInPercent);
        child.animateTextColor(colorChangePercent);
        child.animateBackgroundColor(alphaInPercent);
    }

    private float adjustPercentValue(float percentValue) {
        if (percentValue > MAX_PERCENT) {
            return MAX_PERCENT;
        } else if (percentValue < 0) {
            return 0;
        }
        return percentValue;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, BottomSheetView child, View target) {
        if (child.getTop() == mMinOffset) {
            setStateInternal(STATE_EXPANDED);
            return;
        }
        if (target != mNestedScrollingChildRef.get() || !mNestedScrolled) {
            return;
        }
        int top;
        int targetState;
        if (mLastNestedScrollDy > 0) {
            int currentTop = child.getTop();
            if (currentTop > mAnchorPoint) {
                top = mAnchorPoint;
                targetState = STATE_ANCHOR_POINT;
            } else {
                top = mMinOffset;
                targetState = STATE_EXPANDED;
            }
        } else if (mLastNestedScrollDy == 0) {
            int currentTop = child.getTop();
            if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
                top = mMinOffset;
                targetState = STATE_EXPANDED;
            } else {
                top = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }
        } else {
            int currentTop = child.getTop();
            if (currentTop > mAnchorPoint) {
                top = mMaxOffset;
                targetState = STATE_COLLAPSED;
            } else {
                top = mAnchorPoint;
                targetState = STATE_ANCHOR_POINT;
            }
        }
        if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
        } else {
            setStateInternal(targetState);
        }
        mNestedScrolled = false;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, BottomSheetView child, View target, float velocityX,
                                    float velocityY) {
        if (enableInternalScrollingInAnchorPointState) {
            if (shouldScrollInternally(child)) {
                return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
            }
        }

        return target == mNestedScrollingChildRef.get() && (mState != STATE_EXPANDED
                || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    }

    private boolean shouldScrollInternally(BottomSheetView child) {
        return !isTouchingButton && child.getScrollY() != child.getScrollRange() && !(child.getScrollY() == 0 && isMovingDown);
    }

    private final void setPeekHeight(int peekHeight) {
        mPeekHeight = Math.max(0, peekHeight);
        mMaxOffset = mParentHeight - peekHeight;
    }

    public int getInitialPosition() {
        return mParentHeight - mPeekHeight;
    }

    public void addBottomSheetCallback(BottomSheetCallback callback) {
        if (mCallback == null) {
            mCallback = new Vector<>();
        }

        mCallback.add(callback);
    }

    public final void setState(@State int state) {
        if (state == mState) {
            return;
        }
        if (mViewRef == null) {
            return;
        }
        BottomSheetView child = mViewRef.get();
        if (child == null) {
            return;
        }
        int top;
        if (state == STATE_COLLAPSED) {
            top = mMaxOffset;
        } else if (state == STATE_ANCHOR_POINT) {
            top = mAnchorPoint;
        } else if (state == STATE_EXPANDED) {
            top = mMinOffset;
        } else {
            throw new IllegalArgumentException("Illegal state argument: " + state);
        }
        setStateInternal(STATE_SETTLING);
        if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
        }
    }

    @State
    public final int getState() {
        return mState;
    }

    private void setStateInternal(@State int state) {
        if (mState == state) {
            return;
        }
        mState = state;
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null && mCallback != null) {
            notifyStateChangedToListeners(bottomSheet, state);
        }
    }

    private void notifyStateChangedToListeners(@NonNull View bottomSheet, @State int newState) {
        for (BottomSheetCallback bottomSheetCallback : mCallback) {
            bottomSheetCallback.onStateChanged(bottomSheet, newState);
        }
    }

    private void notifyOnSlideToListeners(@NonNull View bottomSheet, float slideOffset) {
        for (BottomSheetCallback bottomSheetCallback : mCallback) {
            bottomSheetCallback.onSlide(bottomSheet, slideOffset);
        }
    }

    private void reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private View findScrollingChild(View view) {
        if (view instanceof NestedScrollingChild) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
                View scrollingChild = findScrollingChild(group.getChildAt(i));
                if (scrollingChild != null) {
                    return scrollingChild;
                }
            }
        }
        return null;
    }

    private class CustomDragHelperCallback extends ViewDragHelper.Callback {

        private BottomSheetView bottomSheetView;

        public CustomDragHelperCallback(BottomSheetView bottomSheetView) {
            this.bottomSheetView = bottomSheetView;
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (mState == STATE_DRAGGING) {
                return false;
            }
            if (mTouchingScrollingChild) {
                return false;
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null && ViewCompat.canScrollVertically(scroll, -1)) {
                    // Let the content scroll up
                    return false;
                }
            }
            return mViewRef != null && mViewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            dispatchOnSlide(top, bottomSheetView);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int top;
            @State
            int targetState;
            if (yvel < 0) {
                top = mMinOffset;
                targetState = STATE_EXPANDED;
            } else if (yvel == 0.f) {
                int currentTop = releasedChild.getTop();
                if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
                    top = mMinOffset;
                    targetState = STATE_EXPANDED;
                } else {
                    top = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
            } else {
                top = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }
            if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top)) {
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild, new SettleRunnable(releasedChild, targetState));
            } else {
                setStateInternal(targetState);
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return constrain(top, mMinOffset, mMaxOffset);
        }

        int constrain(int amount, int low, int high) {
            return amount < low ? low : (amount > high ? high : amount);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mMaxOffset - mMinOffset;
        }

    }

    private void dispatchOnSlide(int top, BottomSheetView bottomSheetView) {
        animateTitle(bottomSheetView, top);
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null && mCallback != null) {
            if (top > mMaxOffset) {
                notifyOnSlideToListeners(bottomSheet, (float) (mMaxOffset - top) / mPeekHeight);
            } else {
                notifyOnSlideToListeners(bottomSheet, (float) (mMaxOffset - top) / ((mMaxOffset - mMinOffset)));
            }
        }
    }

    public void onBottomSheetConnectionValuesReady(BottomSheetView bottomSheetView) {
        animateTitle(bottomSheetView, bottomSheetView.getTop());
    }

    private class SettleRunnable implements Runnable {

        private final View mView;

        @State
        private final int mTargetState;

        SettleRunnable(View view, @State int targetState) {
            mView = view;
            mTargetState = targetState;
        }

        @Override
        public void run() {
            if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this);
            } else {
                setStateInternal(mTargetState);
            }
        }
    }

    protected static class SavedState extends View.BaseSavedState {

        @State
        final int state;

        public SavedState(Parcel source) {
            super(source);
            //noinspection ResourceType
            state = source.readInt();
        }

        public SavedState(Parcelable superState, @State int state) {
            super(superState);
            this.state = state;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(state);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

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
}