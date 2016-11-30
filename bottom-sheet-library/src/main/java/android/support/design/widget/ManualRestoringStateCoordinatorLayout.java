package android.support.design.widget;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;


public class ManualRestoringStateCoordinatorLayout extends CoordinatorLayout {
    private SparseArray<Parcelable> childStates;

    public ManualRestoringStateCoordinatorLayout(Context context) {
        super(context);
    }

    public ManualRestoringStateCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
        childStates = container;
    }

    public void restoreState() {
        if (childStates != null) {
            super.dispatchRestoreInstanceState(childStates);
        }
    }
}