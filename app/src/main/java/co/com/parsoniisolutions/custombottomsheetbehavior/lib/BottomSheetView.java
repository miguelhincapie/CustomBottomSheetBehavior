package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.com.parsoniisolutions.custombottomsheetbehavior.R;

public class BottomSheetView extends NestedScrollView {
    private static final int MAX_PERCENT = 100;
    private static final int MAX_HEX = 255;
    private LinearLayout bottomSheetContainer;
    private TextView bottomSheetTitle;
    private ViewGroup titleContainer;
    private int bottomSheetTitleBackgroundColor;
    private int maxHorizontalTextTranslation = -1;
    private int maxVerticalTranslation = -1;
    private int appBarTextLeftDistance;
    private int appBarTextTopDistance;

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
        bottomSheetContainer = (LinearLayout) findViewById(R.id.bottom_sheet_container);
        bottomSheetTitle = (TextView) findViewById(R.id.bottom_sheet_title);
        titleContainer = (ViewGroup) findViewById(R.id.title_container);
        bottomSheetTitleBackgroundColor = ContextCompat.getColor(context,
                R.color.bottomSheetTitleBackground);
        titleContainer.setBackgroundColor(bottomSheetTitleBackgroundColor);
        bottomSheetTitle.setTextColor(Color.WHITE);
    }

    @Override
    public void addView(View child) {
        bottomSheetContainer.addView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (bottomSheetContainer == null) {
            super.addView(child, params);
        } else {
            bottomSheetContainer.addView(child, params);
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


    public void animateBackgroundColor(float alphaInPercent) {
        titleContainer.setBackgroundColor(ColorUtils
                .setAlphaComponent(bottomSheetTitleBackgroundColor,
                        convertPercentToHex(alphaInPercent)));
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
}
