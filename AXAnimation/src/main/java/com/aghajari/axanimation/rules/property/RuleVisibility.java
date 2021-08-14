/*
 * Copyright (C) 2021 - Amir Hossein Aghajari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.aghajari.axanimation.rules.property;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.livevar.LayoutSize;
import com.aghajari.axanimation.rules.Debugger;
import com.aghajari.axanimation.rules.Rule;
import com.aghajari.axanimation.rules.RuleWithTmpData;

import java.util.HashMap;
import java.util.Map;


/**
 * A {@link Rule} to change View's visibility using alpha
 *
 * @author AmirHossein Aghajari
 * @see View#setVisibility(int)
 */
public class RuleVisibility extends RuleWithTmpData<Integer, Object[]> implements Debugger {

    public RuleVisibility(int data) {
        super(data);
    }

    @Override
    public Map<String, String> debugValues(@NonNull View view) {
        Map<String, String> map = new HashMap<>();
        map.put("Visibility", data == View.VISIBLE ? "Visible" : (data == View.GONE ? "Gone" : "Invisible"));
        return map;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Animator onCreateAnimator(@NonNull final View view, final LayoutSize target, final LayoutSize original, final LayoutSize parentSize) {
        if (!isReverse() || tmpData == null) {
            boolean isViewVisible = view.getVisibility() == View.VISIBLE;
            boolean isNewVisible = data == View.VISIBLE;
            if (isViewVisible == isNewVisible) {
                view.setVisibility(data);
                return null;
            }

            final float oldAlpha = view.getAlpha();

            tmpData = new Object[3];
            tmpData[0] = isViewVisible;
            tmpData[1] = oldAlpha;
            tmpData[2] = view.getVisibility();
        }

        final ObjectAnimator a;
        view.setVisibility(View.VISIBLE);

        if ((boolean) tmpData[0]) {
            a = ObjectAnimator.ofFloat(view, "alpha", (float) tmpData[1], 0);
        } else {
            a = ObjectAnimator.ofFloat(view, "alpha", 0, (float) tmpData[1]);
        }
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isReverse()) { // reverseMode
                    if (data == View.VISIBLE)
                        view.setVisibility((Integer) tmpData[2]);
                    else
                        view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(data);
                }
                view.setAlpha((float) tmpData[1]);
            }
        });
        return initEvaluator(a);
    }

}
