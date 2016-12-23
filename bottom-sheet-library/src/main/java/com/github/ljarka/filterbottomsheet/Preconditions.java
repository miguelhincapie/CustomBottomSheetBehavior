package com.github.ljarka.filterbottomsheet;

import android.support.annotation.NonNull;

public final class Preconditions {

    @NonNull
    public static <T> T checkNotNull(@NonNull T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    private Preconditions() {
    }
}
