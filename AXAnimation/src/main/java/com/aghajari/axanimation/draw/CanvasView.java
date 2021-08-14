package com.aghajari.axanimation.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class CanvasView extends View implements DrawableLayout {

    private final DrawHandler drawHandler = new DrawHandler();

    public CanvasView(Context context) {
        super(context);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CanvasView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        drawHandler.draw(this, canvas, false);
        super.dispatchDraw(canvas);
        drawHandler.draw(this, canvas, true);
        canvas.restore();
    }

    @Override
    public DrawHandler getDrawHandler() {
        return drawHandler;
    }

    @Override
    public boolean canDraw(String key) {
        return true;
    }
}
