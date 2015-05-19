package org.chocolatemilk.ictscan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Lotti on 14.05.2015.
 */

public class DragPointView extends View {

    private Paint mPointPaint;
    private int mStartX = 0;
    private int mStartY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    private boolean mDrawPoint = false;
    private TextPaint mTextPaint = null;

    private DragPointView.OnUpCallback mCallback = null;

    public interface OnUpCallback {
        void onPointFinished(Point point);
    }


    public DragPointView(final Context context) {
        super(context);
        init();
    }

    public DragPointView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragPointView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Sets callback for up
     *
     * @param callback {@link OnUpCallback}
     */
    public void setOnUpCallback(OnUpCallback callback) {
        mCallback = callback;
    }

    /**
     * Inits internal data
     */
    private void init() {
        mPointPaint = new Paint();
        mPointPaint.setColor(getContext().getResources().getColor(R.color.holo_green_light));
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setStrokeWidth(15); // TODO: should take from resources

        mTextPaint = new TextPaint();
        mTextPaint.setColor(getContext().getResources().getColor(R.color.holo_green_light));
        mTextPaint.setTextSize(20);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        // TODO: be aware of multi-touches
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawPoint = false;
                mEndX = (int) event.getX();
                mEndY = (int) event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if (!mDrawPoint || Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                    mEndX = x;
                    mEndY = y;
                    invalidate();
                }

                mDrawPoint = true;
                break;

            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onPointFinished(new Point(mEndX, mEndY));
                }
                invalidate();
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawPoint) {
            canvas.drawPoint(mEndX, mEndY, mPointPaint);
            canvas.drawText("  (" + mEndX + ", " + mEndY + ")",
                    mEndX, mEndY, mTextPaint);
        }
    }
}