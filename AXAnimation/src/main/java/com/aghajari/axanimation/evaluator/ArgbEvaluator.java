package com.aghajari.axanimation.evaluator;

public class ArgbEvaluator extends android.animation.ArgbEvaluator {

    private static final ArgbEvaluator sInstance = new ArgbEvaluator();

    public static ArgbEvaluator getInstance() {
        return sInstance;
    }
}
