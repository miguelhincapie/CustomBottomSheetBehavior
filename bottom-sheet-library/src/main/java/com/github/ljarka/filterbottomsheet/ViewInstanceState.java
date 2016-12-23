package com.github.ljarka.filterbottomsheet;

import android.os.Parcelable;

public class ViewInstanceState<T extends Parcelable> {
    private T viewState;
    private Parcelable superState;

    public ViewInstanceState(T viewState, Parcelable superState) {
        this.viewState = viewState;
        this.superState = superState;
    }

    public T getViewState() {
        return viewState;
    }

    public Parcelable getSuperState() {
        return superState;
    }
}
