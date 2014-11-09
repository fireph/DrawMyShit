package com.fmeyer.drawmyshit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmeyer on 11/8/14.
 */
public class MainDrawingView extends View {
    int mColor = Color.BLACK;

    public class Stroke {
        public Path path;
        public Paint paint;
    }

    List<Stroke> allStrokes = new ArrayList<Stroke>();

    private void newStroke(int color) {
        Stroke s = new Stroke();
        s.paint = new Paint();
        s.path = new Path();
        s.paint.setAntiAlias(true);
        s.paint.setStrokeWidth(5f);
        s.paint.setColor(color);
        s.paint.setStyle(Paint.Style.STROKE);
        s.paint.setStrokeJoin(Paint.Join.ROUND);

        allStrokes.add(s);
    }

    public MainDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        newStroke(mColor);
    }

    public void setColor(int color) {
        mColor = color;
        newStroke(mColor);
        invalidate();
    }

    public void erase() {
        allStrokes.clear();
        newStroke(mColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Stroke s : allStrokes) {
            canvas.drawPath(s.path, s.paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the coordinates of the touch event.
        float eventX = event.getX();
        float eventY = event.getY();

        Stroke s = allStrokes.get(allStrokes.size()-1);

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Set a new starting point
                s.path.moveTo(eventX, eventY);
            case MotionEvent.ACTION_MOVE:
                // Connect the points
                s.path.lineTo(eventX, eventY);
//            default:
//                return false;
        }

        // Makes our view repaint and call onDraw
        invalidate();
        return true;
    }
}
