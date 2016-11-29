package com.github.ljarka.filterbottomsheet;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class ViewInstanceStateKeeper<T extends Parcelable> {
    private static final String VIEW_STATE = "view_state";
    private static final String SUPER_STATE = "super_state";

    public Bundle saveInstanceState(@NonNull ViewInstanceState viewInstanceState) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(VIEW_STATE, viewInstanceState.getViewState());
        bundle.putParcelable(SUPER_STATE, viewInstanceState.getSuperState());
        return bundle;
    }

    public ViewInstanceState<T> restoreInstanceState(@NonNull Parcelable state) {
        if (!(state instanceof Bundle)) {
            throw new IllegalArgumentException("You should pass state from onRestoreInstanceState(Parcelable state) method");
        }
        return restoreInstanceState((Bundle) state);
    }

    public ViewInstanceState<T> restoreInstanceState(@NonNull Bundle bundle) {
        T viewState = bundle.getParcelable(VIEW_STATE);
        Parcelable superState = bundle.getParcelable(SUPER_STATE);
        return new ViewInstanceState<>(viewState, superState);
    }
}
