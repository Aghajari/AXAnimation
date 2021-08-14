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
import android.view.View;
import android.widget.ImageView;

/**
 * A {@link RuleMatrix} to change ImageView's ImageMatrix
 *
 * @author AmirHossein Aghajari
 * @see ImageView#setImageMatrix(Matrix) (float)
 */
public class RuleImageMatrix extends RuleMatrix {

    public RuleImageMatrix(Matrix... matrices) {
        super(null, matrices);
    }

    @Override
    public Matrix getStartMatrix(View view) {
        if (view instanceof ImageView) {
            if (!isReverse()  || tmpData == null)
                tmpData = ((ImageView) view).getImageMatrix();

            return tmpData;
        } else {
            return super.getStartMatrix(view);
        }
    }

    @Override
    public void apply(View view, Matrix matrix) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageMatrix(matrix);
        } else {
            super.apply(view, matrix);
        }
    }
}