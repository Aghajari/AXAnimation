package com.aghajari.sample.animator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

public class TestEditText extends AppCompatEditText {

    final Paint line, bg, text;

    public TestEditText(@NonNull Context context) {
        super(context);
        setPadding(160, 0, 0, 0);
        setGravity(Gravity.TOP);
        setBackground(null);

        line = new Paint();
        line.setColor(Color.BLACK);
        line.setStrokeWidth(6);

        bg = new Paint();
        bg.setColor(Color.LTGRAY);
        bg.setAlpha(150);

        text = new Paint();
        text.setColor(Color.BLACK);
        text.setTextSize(40);
    }

    final Rect r = new Rect();

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawLine(getPaddingLeft() - 20
                , 0, getPaddingLeft() - 20,
                getMeasuredHeight(), line);

        int selected = getLayout().getLineForOffset(getSelectionStart());

        for (int i = 0; i < getLineCount(); i++) {
            int y = getLineBounds(i, r);

            if (i == selected) {
                r.left = 0;
                canvas.drawRect(r, bg);
            }
            canvas.drawText(String.valueOf(i + 1), getPaddingLeft() / 2f, y, text);
        }

        super.dispatchDraw(canvas);
    }
}
