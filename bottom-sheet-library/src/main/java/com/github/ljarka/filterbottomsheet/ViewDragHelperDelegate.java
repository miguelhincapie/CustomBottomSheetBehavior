package com.github.ljarka.filterbottomsheet;

import android.view.MotionEvent;

interface ViewDragHelperDelegate {

    boolean shouldInterceptTouchEvent(MotionEvent event);

    void processTouchEvent(MotionEvent event);

    float getTouchSlop();

    void captureChildView(BottomSheetView child, int pointerId);

    boolean smoothSlideViewTo(BottomSheetView child, int left, int top);

    boolean settleCapturedViewAt(int left, int top);

    boolean continueSettling(boolean deferCallbacks);
}
