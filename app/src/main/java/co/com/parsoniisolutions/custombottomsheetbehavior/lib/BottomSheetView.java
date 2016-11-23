package co.com.parsoniisolutions.custombottomsheetbehavior.lib;

import android.content.Context;
import android.graphics.Color;
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
    private LinearLayout bottomSheetContainer;
    private TextView bottomSheetTitle;
    private ViewGroup titleContainer;
    private int bottomSheetTitleBackgroundColor;
    private int maxTextTranslation = -1;
    private int appBarTextLeftDistance;

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
        if (maxTextTranslation == -1) {
            maxTextTranslation = bottomSheetTitle.getLeft() - appBarTextLeftDistance;
        }
        bottomSheetTitle.setTranslationX(-maxTextTranslation * (translationInPercent / 100));
    }


    public void animateBackgroundColor(float alphaInPercent) {
        titleContainer.setBackgroundColor(ColorUtils
                .setAlphaComponent(bottomSheetTitleBackgroundColor,
                        (int) (255 * (alphaInPercent / 100))));
    }

    public void animateTextColor(float colorChangePercent) {
        bottomSheetTitle.setTextColor(Color.rgb((int) (255 * (colorChangePercent / 100)),
                (int) (255 * (colorChangePercent / 100)),
                (int) (255 * (colorChangePercent / 100))));
    }

    public void setAppBarTextLeftDistance(int appBarTextLeftDistance) {
        this.appBarTextLeftDistance = appBarTextLeftDistance;
    }
}
