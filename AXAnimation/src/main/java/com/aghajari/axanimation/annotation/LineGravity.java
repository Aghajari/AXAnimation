package com.aghajari.axanimation.annotation;

import android.view.Gravity;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Gravity.START, Gravity.END, Gravity.CENTER})
@Retention(RetentionPolicy.SOURCE)
public @interface LineGravity {
}