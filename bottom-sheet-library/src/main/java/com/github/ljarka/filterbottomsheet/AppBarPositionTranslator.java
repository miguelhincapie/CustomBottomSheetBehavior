package com.github.ljarka.filterbottomsheet;

import android.animation.ValueAnimator;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class AppBarPositionTranslator extends RecyclerView.OnScrollListener {
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final int VALUE_ANIMATOR_DURATION = 100;
    private AppBarLayout appBarLayout;
    private ValueAnimator currentAnimator;

    public AppBarPositionTranslator(AppBarLayout appBarLayout) {
        this.appBarLayout = appBarLayout;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        float translationY = appBarLayout.getTranslationY() - (float) dy;

        if (translationY < -appBarLayout.getHeight()) {
            translationY = -appBarLayout.getHeight();
        }

        if (translationY >= 0) {
            translationY = 0;
        }

        appBarLayout.setTranslationY(translationY);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (currentAnimator == null || !currentAnimator.isRunning()) {
                if (appBarLayout.getTranslationY() <= -(appBarLayout.getHeight() / 2f)) {
                    currentAnimator = ValueAnimator.ofFloat(appBarLayout.getTranslationY(),
                            -appBarLayout.getHeight());
                    currentAnimator.addUpdateListener(animator -> {
                        appBarLayout.setTranslationY((float) animator.getAnimatedValue());
                    });
                    currentAnimator.setInterpolator(ACCELERATE_INTERPOLATOR);
                } else {
                    currentAnimator = ValueAnimator.ofFloat(appBarLayout.getTranslationY(), 0);
                    currentAnimator.addUpdateListener(animator -> {
                        appBarLayout.setTranslationY((float) animator.getAnimatedValue());
                    });
                    currentAnimator.setInterpolator(DECELERATE_INTERPOLATOR);
                }
                currentAnimator.setDuration(VALUE_ANIMATOR_DURATION);
                currentAnimator.start();
            }
        }
    }
}
