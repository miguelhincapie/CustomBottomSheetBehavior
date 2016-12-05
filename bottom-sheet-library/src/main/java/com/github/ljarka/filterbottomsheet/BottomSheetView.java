package com.github.ljarka.filterbottomsheet;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.ljarka.filterbottomsheet.BottomSheetBehavior.STATE_ANCHOR_POINT;
import static com.github.ljarka.filterbottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.github.ljarka.filterbottomsheet.BottomSheetBehavior.STATE_EXPANDED;

public class BottomSheetView extends NestedScrollView {

    public interface OnBottomSheetStateChangedListener {

        void onBottomSheetStateChanged(@BottomSheetBehavior.State int state);

    }

    private static final String BOTTOM_SHEET_STATE = "bottom_sheet_state";

    private static final int MAX_PERCENT = 100;
    private static final int MAX_HEX = 255;
    private RelativeLayout bottomSheetContainer;
    private TextView bottomSheetTitle;
    private ViewGroup titleContainer;
    private View titleBackground;
    private BottomSheetBehavior layoutBehavior;
    private int bottomSheetTitleBackgroundColor;
    private int maxHorizontalTextTranslation = -1;
    private int maxVerticalTranslation = -1;
    private int appBarTextLeftDistance;
    private int appBarTextTopDistance;
    private float appBarTextSize;
    private float textScaleValue;
    private ViewInstanceStateKeeper<Bundle> viewInstanceStateKeeper = new ViewInstanceStateKeeper<>();

    public BottomSheetView(Context context) {
        super(context);
        init(context);
    }

    public BottomSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.bottom_sheet_layout, this);
        bottomSheetContainer = (RelativeLayout) findViewById(R.id.bottom_sheet_container);
        bottomSheetTitle = (TextView) findViewById(R.id.bottom_sheet_title);
        titleContainer = (ViewGroup) findViewById(R.id.title_container);
        titleBackground = findViewById(R.id.title_background);
        bottomSheetTitleBackgroundColor = ContextCompat.getColor(context,
                R.color.bottomSheetTitleBackground);
        titleBackground.setBackgroundColor(bottomSheetTitleBackgroundColor);
        bottomSheetTitle.setTextColor(Color.WHITE);
        titleContainer.setOnClickListener(v -> onTitleContainerClick());
    }

    private void onTitleContainerClick() {
        lazyInitLayoutBehavior();
        if (BottomSheetBehavior.STATE_COLLAPSED == layoutBehavior.getState()) {
            layoutBehavior.setState(STATE_ANCHOR_POINT);
        } else if (STATE_ANCHOR_POINT == layoutBehavior.getState()) {
            layoutBehavior.setState(STATE_EXPANDED);
        } else if (STATE_EXPANDED == layoutBehavior.getState()) {
            layoutBehavior.setState(STATE_ANCHOR_POINT);
        }
    }

    @Override
    public void addView(View child) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT,
                WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.title_container);
        bottomSheetContainer.addView(child, layoutParams);
        titleContainer.bringToFront();
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (bottomSheetContainer == null) {
            super.addView(child, params);
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params.width,
                    params.height);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.title_container);
            bottomSheetContainer.addView(child, layoutParams);
        }
    }

    public void translateTextLeft(float translationInPercent) {
        if (maxHorizontalTextTranslation == -1) {
            maxHorizontalTextTranslation = bottomSheetTitle.getLeft() - appBarTextLeftDistance;
        }
        bottomSheetTitle.setTranslationX(-maxHorizontalTextTranslation * (translationInPercent / MAX_PERCENT));
    }

    public void translateTextBottom(float translationInPercent) {
        if (maxVerticalTranslation == -1) {
            maxVerticalTranslation = bottomSheetTitle.getTop() - appBarTextTopDistance;
        }

        bottomSheetTitle.setTranslationY(-maxVerticalTranslation * (translationInPercent / MAX_PERCENT));
    }

    public void transformTextSize(float transformInPercent) {
        if (appBarTextSize != 0) {
            textScaleValue = appBarTextSize / bottomSheetTitle.getTextSize();
        }
        if (appBarTextSize > 0) {
            bottomSheetTitle.setPivotX(5);
            bottomSheetTitle.setPivotY(0);
            bottomSheetTitle.setScaleX(1 - ((1 - textScaleValue) * transformInPercent / MAX_PERCENT));
            bottomSheetTitle.setScaleY(1 - ((1 - textScaleValue) * transformInPercent / MAX_PERCENT));
        }
    }

    public void defaultBehaviorConnectionWith(MergedAppBarLayout mergedAppBarLayout) {
        lazyInitLayoutBehavior();
        mergedAppBarLayout.setToolbarTitle(bottomSheetTitle.getText().toString());
        mergedAppBarLayout.setTitleTextViewReadyListener(titleTextView -> {
            setAppBarTextLeftDistance(titleTextView.getLeft());
            setAppBarTextTopDistance(titleTextView.getTop());
            setAppBarTextSize(titleTextView.getTextSize());
            layoutBehavior.onBottomSheetConnectionValuesReady(BottomSheetView.this);
        });

        mergedAppBarLayout.setNavigationOnClickListener(v -> close());
    }

    public void close() {
        lazyInitLayoutBehavior();
        layoutBehavior.setState(STATE_COLLAPSED);
        scrollTo(0, 0);
    }

    public void open() {
        lazyInitLayoutBehavior();
        layoutBehavior.setState(STATE_EXPANDED);
    }

    public void openToAnchorPosition() {
        lazyInitLayoutBehavior();
        layoutBehavior.setState(STATE_ANCHOR_POINT);
    }

    @BottomSheetBehavior.State
    public int getCurrentState() {
        lazyInitLayoutBehavior();
        return layoutBehavior.getState();
    }

    public boolean isInAnchorPosition() {
        lazyInitLayoutBehavior();
        return STATE_ANCHOR_POINT == layoutBehavior.getState();
    }

    private void lazyInitLayoutBehavior() {
        layoutBehavior = findLayoutBehavior(this);
    }

    public void addOnBottomSheetStateChangedListener(OnBottomSheetStateChangedListener listener) {
        lazyInitLayoutBehavior();
        layoutBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
                if (listener != null) {
                    listener.onBottomSheetStateChanged(newState);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //nop
            }
        });
    }

    public void animateBackgroundColor(float alphaInPercent) {
        titleBackground.setBackgroundColor(ColorUtils
                .setAlphaComponent(bottomSheetTitleBackgroundColor,
                        convertPercentToHex(alphaInPercent)));
    }

    public boolean isExpanded() {
        lazyInitLayoutBehavior();
        return layoutBehavior.getState() == STATE_EXPANDED || layoutBehavior.getState() == STATE_ANCHOR_POINT;
    }

    public void animateTextColor(float colorChangePercent) {
        int hexColorValue = convertPercentToHex(colorChangePercent);
        bottomSheetTitle.setTextColor(Color.rgb(hexColorValue, hexColorValue, hexColorValue));
    }

    @IntRange(from = 0x0, to = 0xFF)
    private int convertPercentToHex(@FloatRange(from = 0f, to = 100f) float percentValue) {
        return (int) (MAX_HEX * (percentValue / MAX_PERCENT));
    }

    public void setAppBarTextLeftDistance(int appBarTextLeftDistance) {
        this.appBarTextLeftDistance = appBarTextLeftDistance;
    }

    public void setAppBarTextTopDistance(int appBarTextTopDistance) {
        this.appBarTextTopDistance = appBarTextTopDistance;
    }

    public void setAppBarTextSize(float appBarTextSize) {
        this.appBarTextSize = appBarTextSize;
    }

    public int getInitialPosition() {
        lazyInitLayoutBehavior();
        return layoutBehavior.getInitialPosition();
    }

    @Override
    protected void onScrollChanged(int l, int verticalScroll, int oldl, int oldt) {
        super.onScrollChanged(l, verticalScroll, oldl, oldt);
        controlButton(verticalScroll);
    }

    private void controlButton(int scrolLY) {
        int scrollRange = getScrollRange();

        if (scrolLY <= 0) {
            titleContainer.setY(0);
        } else {
            titleContainer.setY(scrolLY >= scrollRange ? scrollRange : scrolLY);
        }
    }

    public int getScrollRange() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0,
                    child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        lazyInitLayoutBehavior();
        Bundle state = new Bundle();
        state.putInt(BOTTOM_SHEET_STATE, layoutBehavior.getState());
        ViewInstanceState<Bundle> viewInstanceState = new ViewInstanceState<>(state, super.onSaveInstanceState());
        return viewInstanceStateKeeper.saveInstanceState(viewInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        lazyInitLayoutBehavior();
        ViewInstanceState<Bundle> viewInstanceState = viewInstanceStateKeeper.restoreInstanceState(state);
        super.onRestoreInstanceState(viewInstanceState.getSuperState());

        @BottomSheetBehavior.State
        int bottomSheetState = viewInstanceState.getViewState().getInt(BOTTOM_SHEET_STATE);
        layoutBehavior.setState(bottomSheetState);
    }

    public View getTitleContainer() {
        return titleContainer;
    }

    @SuppressWarnings("unchecked")
    private static <V extends View> BottomSheetBehavior findLayoutBehavior(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof BottomSheetBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with BottomSheetBehavior");
        }
        return (BottomSheetBehavior) behavior;
    }
}
