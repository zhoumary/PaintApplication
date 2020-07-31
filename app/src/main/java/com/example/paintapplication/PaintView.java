package com.example.paintapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class Line {
    float startX, startY, stopX, stopY;
    float joinX, joinY = 0;
    public Line(float startX, float startY, float stopX, float stopY) {
        this.startX = startX;
        this.startY = startY;
        this.stopX = stopX;
        this.stopY = stopY;
    }
    public Line(float startX, float startY) { // for convenience
        this(startX, startY, startX, startY);
    }
}

class Point
{
    float x, y;
}

public class PaintView extends View {

    public static int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private float oX, oY;
    private ArrayList<Line> lines = new ArrayList<Line>();
    List<Point> pointsDown = new ArrayList<Point>();
    List<Point> pointsUp = new ArrayList<Point>();
    int currentContact = -1;
    private Resources mResources;
    private Picture mPicture = new Picture();
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private boolean circle;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        mEmboss = new EmbossMaskFilter(new float[] {1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
        mResources = getResources();
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void normal() {
        emboss = false;
        blur = false;
        circle = false;
    }

    public void emboss() {
        emboss = true;
        blur = false;
        circle = false;
    }

    public void blur() {
        emboss = false;
        blur = true;
        circle = false;
    }

    public void circle() {
        emboss = false;
        blur = false;
        circle = true;
    }

    public void clear() {
        mCanvas.drawColor(backgroundColor, PorterDuff.Mode.CLEAR);
        paths.clear();
        normal();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        if (circle) {
            for (int i = 0; i <= currentContact; i++)
            {
                // draw footer
                Bitmap imageBitmap = ((BitmapDrawable) mResources.getDrawable(R.drawable.footer)).getBitmap();
                canvas.drawBitmap(imageBitmap, pointsUp.get(i).x, pointsUp.get(i).y, mPaint);
                // draw line
//                canvas.drawLine(pointsUp.get(i).x, pointsUp.get(i).y, pointsDown.get(i).x, pointsDown.get(i).y, mPaint);
//              // draw circle
//                float distance = (float) Math.sqrt(Math.pow(pointsUp.get(i).x - pointsDown.get(i).x, 2) + Math.pow(pointsUp.get(i).y - pointsDown.get(i).y, 2));
//                canvas.drawCircle(pointsUp.get(i).x, pointsUp.get(i).y, distance, mPaint);
//              // draw rectangle
//                canvas.drawRect(pointsUp.get(i).x, pointsUp.get(i).y, pointsDown.get(i).x, pointsDown.get(i).y, mPaint);
            }

        } else {
            mCanvas.drawColor(backgroundColor);
        }


        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath(fp.path, mPaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, circle, strokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) /2);
            mX = x;
            mX = y;
    }

    private void touchUp(float x, float y) {
        mX = x;
        mX = y;
        mPath.lineTo(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mPath = new Path();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                touchStart(x, y);
                currentContact++;
                pointsDown.add(new Point());
                pointsDown.get(currentContact).x = x;
                pointsDown.get(currentContact).y = y;
//                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
//                touchMove(x, y);
                pointsUp.add(new Point());
                pointsUp.get(currentContact).x = x;
                pointsUp.get(currentContact).y = y;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
//                touchUp(x, y);
                mX = x;
                mY = y;
                pointsUp.add(new Point());
                pointsUp.get(currentContact).x = x;
                pointsUp.get(currentContact).y = y;
                invalidate();
                break;
        }

        return true;
    }
}
