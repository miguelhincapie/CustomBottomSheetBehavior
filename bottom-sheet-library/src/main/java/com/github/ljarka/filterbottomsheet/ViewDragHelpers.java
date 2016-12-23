package com.github.ljarka.filterbottomsheet;

import android.support.annotation.NonNull;
import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;

import static com.github.ljarka.filterbottomsheet.Preconditions.checkNotNull;

final class ViewDragHelpers {

    private static final ViewDragHelperDelegate EMPTY = new EmptyViewDragHelperDelegate();

    @NonNull
    static ViewDragHelperDelegate wrap(@NonNull ViewDragHelper viewDragHelper) {
        return new ViewDragHelperWrapper(viewDragHelper);
    }

    @NonNull
    static ViewDragHelperDelegate empty() {
        return EMPTY;
    }

    private static class EmptyViewDragHelperDelegate implements ViewDragHelperDelegate {

        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent event) {
            return false;
        }

        @Override
        public void processTouchEvent(MotionEvent event) {
        }

        @Override
        public float getTouchSlop() {
            return 0;
        }

        @Override
        public void captureChildView(BottomSheetView child, int pointerId) {
        }

        @Override
        public boolean smoothSlideViewTo(BottomSheetView child, int left, int top) {
            return false;
        }

        @Override
        public boolean settleCapturedViewAt(int left, int top) {
            return false;
        }

        @Override
        public boolean continueSettling(boolean deferCallbacks) {
            return false;
        }
    }

    private static class ViewDragHelperWrapper implements ViewDragHelperDelegate {

        private final ViewDragHelper viewDragHelper;

        private ViewDragHelperWrapper(@NonNull ViewDragHelper viewDragHelper) {
            this.viewDragHelper = checkNotNull(viewDragHelper);
        }

        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent event) {
            return viewDragHelper.shouldInterceptTouchEvent(event);
        }

        @Override
        public void processTouchEvent(MotionEvent event) {
            viewDragHelper.processTouchEvent(event);
        }

        @Override
        public float getTouchSlop() {
            return viewDragHelper.getTouchSlop();
        }

        @Override
        public void captureChildView(BottomSheetView child, int pointerId) {
            viewDragHelper.captureChildView(child, pointerId);
        }

        @Override
        public boolean smoothSlideViewTo(BottomSheetView child, int left, int top) {
            return viewDragHelper.smoothSlideViewTo(child, left, top);
        }

        @Override
        public boolean settleCapturedViewAt(int left, int top) {
            return viewDragHelper.settleCapturedViewAt(left, top);
        }

        @Override
        public boolean continueSettling(boolean deferCallbacks) {
            return viewDragHelper.continueSettling(deferCallbacks);
        }
    }
}
