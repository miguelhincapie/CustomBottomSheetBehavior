package pl.lukjar.bottom_sheet_library;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

public class MergedAppBarLayout extends AppBarLayout {
    public MergedAppBarLayout(Context context) {
        super(context);
        init(context);
    }

    public MergedAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.merged_appbar_layout, this);
    }
}
