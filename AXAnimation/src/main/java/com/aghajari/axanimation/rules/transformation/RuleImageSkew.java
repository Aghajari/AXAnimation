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
package com.aghajari.axanimation.rules.transformation;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;

import com.aghajari.axanimation.rules.Debugger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link RuleImageMatrix} to set ImageView's skew using {@link Matrix}
 *
 * @author AmirHossein Aghajari
 */
public class RuleImageSkew extends RuleImageMatrix implements Debugger {

    private final PointF[] values;

    public RuleImageSkew(PointF... skew) {
        super(new Matrix[skew.length + 1]);
        this.values = skew;
    }

    @Override
    public Object[] getMatrices(View view) {
        Object[] matrices = super.getMatrices(view);

        Matrix matrix = getStartMatrix(view);
        final float[] tmp = new float[9];
        matrix.getValues(tmp);
        boolean hasStartMatrix = matrices.length > data.length;

        for (int i = 0; i < values.length; i++) {
            Matrix m = new Matrix();
            float[] matrixValues = tmp.clone();
            matrixValues[Matrix.MSKEW_X] = values[i].x;
            matrixValues[Matrix.MSKEW_Y] = values[i].y;
            m.setValues(matrixValues);
            if (hasStartMatrix)
                matrices[i + 1] = m;
            else
                matrices[i] = m;
        }
        return matrices;
    }

    @Override
    public Map<String, String> debugValues(@NonNull View view) {
        Map<String, String> map = new HashMap<>();
        map.put("Skew", Arrays.toString(values));
        return map;
    }
}
