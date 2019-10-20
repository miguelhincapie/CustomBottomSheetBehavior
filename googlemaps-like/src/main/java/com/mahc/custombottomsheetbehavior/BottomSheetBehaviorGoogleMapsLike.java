package com.mahc.custombottomsheetbehavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.Vector;


public class BottomSheetBehaviorGoogleMapsLike<V extends View> extends CoordinatorLayout.Behavior<V> {

    /**
     * Callback for monitoring events about bottom sheets.
     */
    public abstract static class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param newState    The new state. This will be one of {@link #STATE_DRAGGING},
         *                    {@link #STATE_SETTLING}, {@link #STATE_ANCHOR_POINT},
         *                    {@link #STATE_EXPANDED},
         *                    {@link #STATE_COLLAPSED}, or {@link #STATE_HIDDEN}.
         */
        public abstract void onStateChanged(@NonNull View bottomSheet, @State int newState);

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within its range, from 0 to 1
         *                    when it is moving upward, and from 0 to -1 when it moving downward.
         */
        public abstract void onSlide(@NonNull View bottomSheet, float slideOffset);
    }

    /**
     * The bottom sheet is dragging.
     */
    public static final int STATE_DRAGGING = 1;

    /**
     * The bottom sheet is settling.
     */
    public static final int STATE_SETTLING = 2;

    /**
     * The bottom sheet is expanded_half_way.
     */
    public static final int STATE_ANCHOR_POINT = 3;

    /**
     * The bottom sheet is expanded.
     */
    public static final int STATE_EXPANDED = 4;

    /**
     * The bottom sheet is collapsed.
     */
    public static final int STATE_COLLAPSED = 5;

    /**
     * The bottom sheet is hidden.
     */
    public static final int STATE_HIDDEN = 6;

    /** @hide */
    @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_ANCHOR_POINT, STATE_SETTLING, STATE_HIDDEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private static final float HIDE_THRESHOLD = 0.5f;
    private static final float HIDE_FRICTION = 0.1f;

    private float mMinimumVelocity;

    private int mPeekHeight;

    private int mMinOffset;
    private int mMaxOffset;

    private static final int DEFAULT_ANCHOR_POINT = 700;
    private int mAnchorPoint;

    private boolean mHideable;

    private boolean mCollapsible;

    @State
    private int mState = STATE_ANCHOR_POINT;
    @State
    private int mLastStableState = STATE_ANCHOR_POINT;

    private ViewDragHelper mViewDragHelper;

    private boolean mIgnoreEvents;

    private boolean mNestedScrolled;

    private int mParentHeight;

    private WeakReference<V> mViewRef;

    private WeakReference<View> mNestedScrollingChildRef;

    private Vector<BottomSheetCallback> mCallback;

    private int mActivePointerId;

    private int mInitialY;

    private boolean mTouchingScrollingChild;

    /**
     * Default constructor for instantiating BottomSheetBehaviors.
     */
    public BottomSheetBehaviorGoogleMapsLike() { }

    /**
     * Default constructor for inflating BottomSheetBehaviors from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public BottomSheetBehaviorGoogleMapsLike( Context context, AttributeSet attrs ) {
        super( context, attrs );
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.google.android.material.R.styleable.BottomSheetBehavior_Layout);
        setPeekHeight(a.getDimensionPixelSize(
                com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, 0));
        setHideable(a.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
        a.recycle();

        /**
         * Getting the anchorPoint...
         */
        mAnchorPoint = DEFAULT_ANCHOR_POINT;
        mCollapsible = true;
        a = context.obtainStyledAttributes(attrs, R.styleable.CustomBottomSheetBehavior);
        if (attrs != null) {
            mAnchorPoint = (int) a.getDimension( R.styleable.CustomBottomSheetBehavior_anchorPoint, 0);
            mState = a.getInt(R.styleable.CustomBottomSheetBehavior_defaultState, STATE_ANCHOR_POINT);
        }
        a.recycle();

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    public Parcelable onSaveInstanceState( CoordinatorLayout parent, V child ) {
        return new SavedState(super.onSaveInstanceState(parent, child), mState);
    }

    @Override
    public void onRestoreInstanceState( CoordinatorLayout parent, V child, Parcelable state ) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            mState = STATE_COLLAPSED;
        } else {
            mState = ss.state;
        }

        mLastStableState = mState;
    }

    @Override
    public boolean onLayoutChild( CoordinatorLayout parent, V child, int layoutDirection ) {
        // First let the parent lay it out
        if (parent.getFitsSystemWindows() &&
                !child.getFitsSystemWindows()) {
            child.setFitsSystemWindows(true);
        }
        int savedTop = child.getTop();
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection);
        // Offset the bottom sheet
        mParentHeight = parent.getHeight();
        mMinOffset = Math.max(0, mParentHeight - child.getHeight());
        mMaxOffset = Math.max(mParentHeight - mPeekHeight, mMinOffset);

        /**
         * New behavior
         */
        if (mState == STATE_ANCHOR_POINT) {
            ViewCompat.offsetTopAndBottom(child, mAnchorPoint);
        } else if (mState == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, mMinOffset);
        } else if (mHideable && mState == STATE_HIDDEN) {
            ViewCompat.offsetTopAndBottom(child, mParentHeight);
        } else if (mState == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, mMaxOffset);
        } else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
            ViewCompat.offsetTopAndBottom(child, savedTop - child.getTop());
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
        }
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent( CoordinatorLayout parent, V child, MotionEvent event ) {
        if ( ! child.isShown() ) {
            mIgnoreEvents = true;
            return false;
        }

        int action = event.getActionMasked();
        if ( action == MotionEvent.ACTION_DOWN ) {
            reset();
        }

        switch ( action ) {
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
                mScrollVelocityTracker.clear();
                int initialX = (int) event.getX();
                mInitialY = (int) event.getY();
                if(mState == STATE_ANCHOR_POINT){
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                }else {
                    View scroll = mNestedScrollingChildRef.get();
                    if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
                        mActivePointerId = event.getPointerId(event.getActionIndex());
                        mTouchingScrollingChild = true;
                    }
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID &&
                        !parent.isPointInChildBounds(child, initialX, mInitialY);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }

        if ( ! mIgnoreEvents  &&  mViewDragHelper.shouldInterceptTouchEvent( event ) ) {
            return true;
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        View scroll = mNestedScrollingChildRef.get();
        boolean ret = action == MotionEvent.ACTION_MOVE && scroll != null &&
                !mIgnoreEvents && mState != STATE_DRAGGING &&
                !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY()) &&
                Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
        return ret;
    }

    @Override
    public boolean onTouchEvent( CoordinatorLayout parent, V child, MotionEvent event ) {
        if ( ! child.isShown() ) {
            return false;
        }

        int action = event.getActionMasked();
        if ( mState == STATE_DRAGGING  &&  action == MotionEvent.ACTION_DOWN ) {
            return true;
        }

        // Detect scroll direction for ignoring collapsible
        if(mLastStableState == STATE_ANCHOR_POINT && action==MotionEvent.ACTION_MOVE) {
            if(event.getY()>mInitialY && !mCollapsible) {
                reset();
                return false;
            }
        }

        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
        }

        mViewDragHelper.processTouchEvent(event);

        if ( action == MotionEvent.ACTION_DOWN ) {
            reset();
        }

        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if ( action == MotionEvent.ACTION_MOVE  &&  ! mIgnoreEvents ) {
            if ( Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop() ) {
                mViewDragHelper.captureChildView( child, event.getPointerId(event.getActionIndex()) );
            }
        }
        return ! mIgnoreEvents;
    }

    @Override
    public boolean onStartNestedScroll( CoordinatorLayout coordinatorLayout, V child,
                                        View directTargetChild, View target, int nestedScrollAxes,
                                        @ViewCompat.NestedScrollType int type) {
        mNestedScrolled = false;
        return ( nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL ) != 0;
    }

    private ScrollVelocityTracker mScrollVelocityTracker = new ScrollVelocityTracker();
    private class ScrollVelocityTracker {
        private long  mPreviousScrollTime = 0;
        private float mScrollVelocity     = 0;

        public void recordScroll( int dy ) {
            long now = System.currentTimeMillis();

            if ( mPreviousScrollTime != 0 ) {
                long elapsed = now - mPreviousScrollTime;
                mScrollVelocity = (float) dy / elapsed * 1000; // pixels per sec
            }

            mPreviousScrollTime = now;
        }

        public void clear() {
            mPreviousScrollTime = 0;
            mScrollVelocity = 0;
        }

        public float getScrollVelocity() {
            return mScrollVelocity;
        }
    }

    @Override
    public void onNestedPreScroll( CoordinatorLayout coordinatorLayout, V child, View target,
                                   int dx, int dy, int[] consumed,
                                   @ViewCompat.NestedScrollType int type) {
        View scrollingChild = mNestedScrollingChildRef.get();
        if ( target != scrollingChild ) {
            return;
        }

        mScrollVelocityTracker.recordScroll( dy );

        int currentTop = child.getTop();
        int newTop     = currentTop - dy;

        // Force stop at the anchor - do not go from collapsed to expanded in one scroll
        if (
                ( mLastStableState == STATE_COLLAPSED  &&  newTop < mAnchorPoint )  ||
                        ( mLastStableState == STATE_EXPANDED   &&  newTop > mAnchorPoint )
                ) {
            consumed[1] = dy;
            ViewCompat.offsetTopAndBottom( child, mAnchorPoint - currentTop );
            dispatchOnSlide( child.getTop() );
            mNestedScrolled = true;
            return;
        }

        if ( dy > 0 ) { // Upward
            if ( newTop < mMinOffset ) {
                consumed[1] = currentTop - mMinOffset;
                ViewCompat.offsetTopAndBottom( child, -consumed[1] );
                setStateInternal( STATE_EXPANDED );
            } else {
                consumed[1] = dy;
                ViewCompat.offsetTopAndBottom( child, -dy );
                setStateInternal( STATE_DRAGGING );
            }
        } else if (dy < 0) { // Downward
            if (!ViewCompat.canScrollVertically(target, -1)) {
                if (newTop <= mMaxOffset || mHideable) {
                    // Restrict STATE_COLLAPSED if restrictedState is set
                    if(mCollapsible==true || (mCollapsible==false && (mAnchorPoint - newTop)>=0)) {
                        consumed[1] = dy;
                        ViewCompat.offsetTopAndBottom(child, -dy);
                        setStateInternal(STATE_DRAGGING);
                    }
                } else {
                    consumed[1] = currentTop - mMaxOffset;
                    ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                    setStateInternal(STATE_COLLAPSED);
                }
            }
        }
        dispatchOnSlide(child.getTop());
        mNestedScrolled = true;
    }

    @Override
    public void onStopNestedScroll( CoordinatorLayout coordinatorLayout, V child, View target,
                                    @ViewCompat.NestedScrollType int type) {
        if ( child.getTop() == mMinOffset ) {
            setStateInternal( STATE_EXPANDED );
            mLastStableState = STATE_EXPANDED;
            return;
        }
        if ( target != mNestedScrollingChildRef.get()  ||  ! mNestedScrolled ) {
            return;
        }
        int top;
        int targetState;

        // Are we flinging up?
        float scrollVelocity = mScrollVelocityTracker.getScrollVelocity();
        if ( scrollVelocity > mMinimumVelocity) {
            if ( mLastStableState == STATE_COLLAPSED ) {
                // Fling from collapsed to anchor
                top = mAnchorPoint;
                targetState = STATE_ANCHOR_POINT;
            }
            else
            if ( mLastStableState == STATE_ANCHOR_POINT ) {
                // Fling from anchor to expanded
                top = mMinOffset;
                targetState = STATE_EXPANDED;
            }
            else {
                // We are already expanded
                top = mMinOffset;
                targetState = STATE_EXPANDED;
            }
        }
        else
            // Are we flinging down?
            if ( scrollVelocity < -mMinimumVelocity ) {
                if ( mLastStableState == STATE_EXPANDED ) {
                    // Fling to from expanded to anchor
                    top = mAnchorPoint;
                    targetState = STATE_ANCHOR_POINT;
                }
                else if(mCollapsible==true) {
                    if (mLastStableState == STATE_ANCHOR_POINT) {
                        // Fling from anchor to collapsed
                        top = mMaxOffset;
                        targetState = STATE_COLLAPSED;
                    } else {
                        // We are already collapsed
                        top = mMaxOffset;
                        targetState = STATE_COLLAPSED;
                    }
                } else {
                    top = mAnchorPoint;
                    targetState = STATE_ANCHOR_POINT;
                }
            }
            // Not flinging, just settle to the nearest state
            else {
                // Collapse?
                int currentTop = child.getTop();
                if ( currentTop > mAnchorPoint * 1.25 && mCollapsible==true) { // Multiply by 1.25 to account for parallax. The currentTop needs to be pulled down 50% of the anchor point before collapsing.
                    top = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
                // Expand?
                else
                if ( currentTop < mAnchorPoint * 0.5 ) {
                    top = mMinOffset;
                    targetState = STATE_EXPANDED;
                }
                // Snap back to the anchor
                else {
                    top = mAnchorPoint;
                    targetState = STATE_ANCHOR_POINT;
                }
            }

        mLastStableState = targetState;
        if ( mViewDragHelper.smoothSlideViewTo( child, child.getLeft(), top ) ) {
            setStateInternal( STATE_SETTLING );
            ViewCompat.postOnAnimation( child, new SettleRunnable( child, targetState ) );
        } else {
            setStateInternal( targetState );
        }
        mNestedScrolled = false;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target,
            float velocityX, float velocityY) {
        return target == mNestedScrollingChildRef.get() &&
                (mState != STATE_EXPANDED ||
                        super.onNestedPreFling(coordinatorLayout, child, target,
                                velocityX, velocityY));
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_peekHeight
     */
    public final void setPeekHeight(int peekHeight) {
        mPeekHeight = Math.max(0, peekHeight);
        mMaxOffset = mParentHeight - peekHeight;
    }

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_peekHeight
     */
    public final int getPeekHeight() {
        return mPeekHeight;
    }

    public void setAnchorPoint(int anchorPoint) {
        mAnchorPoint = anchorPoint;
    }
    public int getAnchorPoint(){
        return mAnchorPoint;
    }

    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    public void setHideable(boolean hideable) {
        mHideable = hideable;
    }

    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return {@code true} if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    public boolean isHideable() {
        return mHideable;
    }

    /**
     * Sets whether some states should be skipped.
     *
     * @param collapsible {@code true} to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    public void setCollapsible(boolean collapsible) {
        mCollapsible = collapsible;
    }

    /**
     * Gets whether some states should be skipped.
     *
     * @return {@code true} if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Params_behavior_hideable
     */
    public boolean isCollapsible() {
        return mCollapsible;
    }

    /**
     * Adds a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    public void addBottomSheetCallback(BottomSheetCallback callback) {
        if (mCallback == null)
            mCallback = new Vector<>();

        mCallback.add(callback);
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_ANCHOR_POINT},
     *              {@link #STATE_EXPANDED} or {@link #STATE_HIDDEN}.
     */
    public final void setState( @State int state ) {
        if ( state == mState ) {
            return;
        }

        /**
         * New behavior (added: state == STATE_ANCHOR_POINT ||)
         */
        if ( state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_ANCHOR_POINT ||
                (mHideable && state == STATE_HIDDEN)) {
            mState = state;
            mLastStableState = state;
        }

        V child = mViewRef == null ? null : mViewRef.get();
        if (child == null) {
            return;
        }
        int top;
        if (state == STATE_COLLAPSED) {
            top = mMaxOffset;
        }
        else
        if (state == STATE_ANCHOR_POINT) {
            top = mAnchorPoint;
        }
        else
        if (state == STATE_EXPANDED) {
            top = mMinOffset;
        }
        else
        if (mHideable && state == STATE_HIDDEN) {
            top = mParentHeight;
        } else {
            throw new IllegalArgumentException("Illegal state argument: " + state);
        }
        setStateInternal(STATE_SETTLING);
        if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
        }
    }

    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of {@link #STATE_EXPANDED}, {@link #STATE_ANCHOR_POINT}, {@link #STATE_COLLAPSED},
     * {@link #STATE_DRAGGING}, and {@link #STATE_SETTLING}.
     */
    @State
    public final int getState() {
        return mState;
    }

    private void setStateInternal(@State int state) {
        if (mState == state) {
            return;
        }
        switch (state) {
            case STATE_ANCHOR_POINT:
            case STATE_COLLAPSED:
            case STATE_EXPANDED:
            case STATE_HIDDEN:
                mLastStableState = state;
        }
        mState = state;
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null && mCallback != null) {
//            mCallback.onStateChanged(bottomSheet, state);
            notifyStateChangedToListeners(bottomSheet, state);
        }
    }

    private void notifyStateChangedToListeners(@NonNull View bottomSheet, @State int newState) {
        for (BottomSheetCallback bottomSheetCallback:mCallback) {
            bottomSheetCallback.onStateChanged(bottomSheet, newState);
        }
    }

    private void notifyOnSlideToListeners(@NonNull View bottomSheet, float slideOffset) {
        for (BottomSheetCallback bottomSheetCallback:mCallback) {
            bottomSheetCallback.onSlide(bottomSheet, slideOffset);
        }
    }

    private void reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER;
    }

    private boolean shouldHide(View child, float yvel) {
        if (child.getTop() < mMaxOffset) {
            // It should not hide, but collapse.
            return false;
        }
        final float newTop = child.getTop() + yvel * HIDE_FRICTION;
        return Math.abs(newTop - mMaxOffset) / (float) mPeekHeight > HIDE_THRESHOLD;
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

    private final ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView( View child, int pointerId ) {
            if ( mState == STATE_DRAGGING ) {
                return false;
            }
            if ( mTouchingScrollingChild ) {
                return false;
            }
            if ( mState == STATE_EXPANDED  &&  mActivePointerId == pointerId ) {
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null && scroll.canScrollVertically(-1)) {
                    // Let the content scroll up
                    return false;
                }
            }
            return mViewRef != null && mViewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged( View changedView, int left, int top, int dx, int dy ) {
            dispatchOnSlide( top );
        }

        @Override
        public void onViewDragStateChanged( int state ) {
            if ( state == ViewDragHelper.STATE_DRAGGING ) {
                setStateInternal( STATE_DRAGGING );
            }
        }

        @Override
        public void onViewReleased( View releasedChild, float xvel, float yvel ) {
            int top;
            @State int targetState;
            if ( yvel < 0 ) { // Moving up
                boolean change = false;
                if (yvel * -1 > mMinimumVelocity) {
                    change = true;
                }
                if (change) {
                    switch (mLastStableState) {
                        case STATE_ANCHOR_POINT:
                            targetState = STATE_EXPANDED;
                            break;
                        case STATE_COLLAPSED:
                            targetState = STATE_ANCHOR_POINT;
                            break;
                        default:
                            targetState = STATE_ANCHOR_POINT;
                    }
                } else {
                    targetState = mLastStableState;
                }
                switch (targetState) {
                    case STATE_ANCHOR_POINT:
                        top = mAnchorPoint;
                        break;
                    case STATE_COLLAPSED:
                        top = mMaxOffset;
                        break;
                    case STATE_EXPANDED:
                        top = mMinOffset;
                        break;
                    default:
                        top = mAnchorPoint;

                }
            }
            else if ( mHideable  &&  shouldHide(releasedChild, yvel) ) {
                top = mParentHeight;
                targetState = STATE_HIDDEN;
            }
            else
            if ( yvel == 0.f ) {
                int currentTop = releasedChild.getTop();
                int distToBottom = Math.abs(currentTop - mMaxOffset);
                int distToAnchor = Math.abs(currentTop - mAnchorPoint);
                int distToTop = Math.abs(currentTop - mMinOffset);
                if (distToTop < distToBottom) {
                    if (distToTop < distToAnchor) {
                        top = mMinOffset;
                        targetState = STATE_EXPANDED;
                        // top
                    } else {
                        top = mAnchorPoint;
                        targetState = STATE_ANCHOR_POINT;
                        // anchor
                    }
                } else {
                    if (distToBottom < distToAnchor) {
                        if (distToBottom == 0) {
                            // handling click on bottom to make it go to anchor point
                            top = mAnchorPoint;
                            targetState = STATE_ANCHOR_POINT;
                        } else {
                            top = mMaxOffset;
                            targetState = STATE_COLLAPSED;
                        }

                        // bottom
                    } else {
                        top = mAnchorPoint;
                        targetState = STATE_ANCHOR_POINT;
                        // anchor
                    }
                }
            } else {
                top = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }

            // Restrict Collapsed view (optional)
            if(!mCollapsible && targetState==STATE_COLLAPSED) {
                top = mAnchorPoint;
                targetState = STATE_ANCHOR_POINT;
            }

            if ( mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top) ) {
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild,
                        new SettleRunnable(releasedChild, targetState));
            } else {
                setStateInternal(targetState);
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return constrain(top, mMinOffset, mHideable ? mParentHeight : mMaxOffset);
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
            if (mHideable) {
                return mParentHeight - mMinOffset;
            } else {
                return mMaxOffset - mMinOffset;
            }
        }
    };

    private void dispatchOnSlide( int top ) {
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null && mCallback != null) {
            if (top > mMaxOffset) {
                notifyOnSlideToListeners(bottomSheet, (float) (mMaxOffset - top) / mPeekHeight);
            } else {
                notifyOnSlideToListeners(bottomSheet, (float) (mMaxOffset - top) / ((mMaxOffset - mMinOffset)));
            }
        }
    }

    private class SettleRunnable implements Runnable {

        private final View mView;

        @State
        private final int mTargetState;

        SettleRunnable( View view, @State int targetState ) {
            mView = view;
            mTargetState = targetState;
        }

        @Override
        public void run() {
            if ( mViewDragHelper != null  &&  mViewDragHelper.continueSettling( true ) ) {
                ViewCompat.postOnAnimation( mView, this );
            } else {
                setStateInternal( mTargetState );
            }
        }
    }

    protected static class SavedState extends View.BaseSavedState {

        @State
        final int state;

        public SavedState( Parcel source ) {
            super( source );
            // noinspection ResourceType
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

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
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

    /**
     * A utility function to get the {@link BottomSheetBehaviorGoogleMapsLike} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link BottomSheetBehaviorGoogleMapsLike}.
     * @param <V> Instance of behavior
     * @return The {@link BottomSheetBehaviorGoogleMapsLike} associated with the {@code view}.
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> BottomSheetBehaviorGoogleMapsLike<V> from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof BottomSheetBehaviorGoogleMapsLike)) {
            throw new IllegalArgumentException(
                    "The view is not associated with BottomSheetBehaviorGoogleMapsLike");
        }
        return (BottomSheetBehaviorGoogleMapsLike<V>) behavior;
    }

}
