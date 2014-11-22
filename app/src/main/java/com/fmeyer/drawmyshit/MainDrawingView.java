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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainDrawingView extends View {
    int mColor = Color.BLACK;

    int mViewWidth = 1;
    int mViewHeight = 1;

    OnLineListener mLineListener = null;

    public class Stroke {
        public Path path;
        public Paint paint;
    }

    List<Stroke> myStrokes = new ArrayList<Stroke>();

    HashMap<String, List<Stroke>> otherStrokes = new HashMap<String, List<Stroke>>();

    private Stroke newStroke(int color) {
        return newStroke(color, null);
    }

    private Stroke newStroke(int color, String id) {
        Stroke s = new Stroke();
        s.paint = new Paint();
        s.path = new Path();
        s.paint.setAntiAlias(true);
        s.paint.setStrokeWidth(5f);
        s.paint.setColor(color);
        s.paint.setStyle(Paint.Style.STROKE);
        s.paint.setStrokeJoin(Paint.Join.ROUND);

        if (id == null) {
            myStrokes.add(s);
        } else {
            if (otherStrokes.containsKey(id)) {
                otherStrokes.get(id).add(s);
            } else {
                List<Stroke> otherStrokeList = new ArrayList<Stroke>();
                otherStrokeList.add(s);
                otherStrokes.put(id, otherStrokeList);
            }
        }

        return s;
    }

    public MainDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        newStroke(mColor);
    }

    public interface OnLineListener{
        public void onLineTo(float x, float y, Paint paint);
        public void onMoveTo(float x, float y, Paint paint);
        public void onErase();
    }

    public void onLine(OnLineListener listener) {
        mLineListener = listener;
    }

    public void setColor(int color) {
        setColor(color, null);
    }

    public void setColor(int color, String id) {
        mColor = color;
        newStroke(mColor, id);
        invalidate();
    }

    public void erase(boolean emit) {
        myStrokes.clear();
        otherStrokes.clear();
        newStroke(mColor);
        this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
        if (emit) {
            mLineListener.onErase();
        }
    }

    public void lineTo(float x, float y, int color, String id) {
        float realX = x*mViewWidth;
        float realY = y*mViewHeight;
        Stroke s;
        if (id == null) {
            s = myStrokes.get(myStrokes.size()-1);
        } else {
            if (otherStrokes.containsKey(id)) {
                List<Stroke> otherStrokeList = otherStrokes.get(id);
                s = otherStrokeList.get(otherStrokeList.size()-1);
            } else {
                List<Stroke> otherStrokeList = new ArrayList<Stroke>();
                otherStrokes.put(id, otherStrokeList);
                newStroke(color, id);
                s = otherStrokeList.get(otherStrokeList.size()-1);
            }
        }
        int oldColor = s.paint.getColor();
        if (oldColor != color) {
            s = newStroke(color, id);
        }
        s.path.lineTo(realX, realY);
        this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public void moveTo(float x, float y, int color, String id) {
        float realX = x*mViewWidth;
        float realY = y*mViewHeight;
        Stroke s;
        if (id == null) {
            s = myStrokes.get(myStrokes.size()-1);
        } else {
            if (otherStrokes.containsKey(id)) {
                List<Stroke> otherStrokeList = otherStrokes.get(id);
                s = otherStrokeList.get(otherStrokeList.size()-1);
            } else {
                List<Stroke> otherStrokeList = new ArrayList<Stroke>();
                otherStrokes.put(id, otherStrokeList);
                newStroke(color, id);
                s = otherStrokeList.get(otherStrokeList.size()-1);
            }
        }
        int oldColor = s.paint.getColor();
        if (oldColor != color) {
            s = newStroke(color, id);
        }
        s.path.moveTo(realX, realY);
        this.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        mViewWidth = xNew;
        mViewHeight = yNew;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Stroke s : myStrokes) {
            canvas.drawPath(s.path, s.paint);
        }
        for (List<Stroke> sList : otherStrokes.values()) {
            for (Stroke s : sList) {
                canvas.drawPath(s.path, s.paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the coordinates of the touch event.
        float eventX = event.getX();
        float eventY = event.getY();

        Stroke s = myStrokes.get(myStrokes.size()-1);

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Set a new starting point
//                s.path.moveTo(eventX-2, eventY);
//                s.path.lineTo(eventX+2, eventY);
                s.path.moveTo(eventX, eventY);
                if (mLineListener != null) {
                    mLineListener.onMoveTo(eventX/mViewWidth, eventY/mViewHeight, s.paint);
                }
            case MotionEvent.ACTION_MOVE:
                // Connect the points
                s.path.lineTo(eventX, eventY);
                if (mLineListener != null) {
                    mLineListener.onLineTo(eventX/mViewWidth, eventY/mViewHeight, s.paint);
                }

//            default:
//                return false;
        }

        // Makes our view repaint and call onDraw
        invalidate();
        return true;
    }
}
