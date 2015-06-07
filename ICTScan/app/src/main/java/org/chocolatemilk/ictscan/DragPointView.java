package org.chocolatemilk.ictscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Lotti on 14.05.2015.
 */

public class DragPointView extends View {

    private Paint mPointPaint;
    private int mTempX = 0;
    private int mTempY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    public boolean readyForTouch = false;
    private boolean mDrawPoint = false;
    private TextPaint mTextPaint = null;


    private boolean zooming = false;
    private PointF zoomPos;
    private Matrix matrix;
    private Bitmap mBitmap;
    private BitmapShader mShader;
    private Paint mPaint;
    public ImageView viewImage;

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

        zooming = false;
        zoomPos = new PointF(0,0);
        matrix = new Matrix();
        mPaint = new Paint();
}

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!readyForTouch) return true;

        zoomPos.x = event.getX();
        zoomPos.y = event.getY();
        // TODO: be aware of multi-touches
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawPoint = false;
                mTempX = (int) event.getX();
                mTempY = (int) event.getY();
                zooming = true;
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if (!mDrawPoint || Math.abs(x - mTempX) > 5 || Math.abs(y - mTempY) > 5) {
                    mTempX = x;
                    mTempY = y;
                    invalidate();
                }
                mDrawPoint = true;
                zooming = true;
                break;

            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onPointFinished(new Point(mTempX, mTempY));
                }
                zooming = false;
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                zooming = false;
                this.invalidate();
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
            canvas.drawPoint(mTempX, mTempY, mPointPaint);
            canvas.drawText("  (" + mTempX + ", " + mTempY + ")",
                    mTempX, mTempY, mTextPaint);
        }

        if (zooming && null!=viewImage) {
            float [] coordinates = transformCoordinates(mTempX,mTempY);
            mBitmap = ((BitmapDrawable)viewImage.getDrawable()).getBitmap(); // get Image
            mShader = new BitmapShader(mBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
            mPaint.setShader(mShader);
            matrix.reset();
            matrix.postScale(2f, 2f, coordinates[0], coordinates[1]);
            mPaint.getShader().setLocalMatrix(matrix);

            canvas.drawCircle(100, 115, 90, mPaint); //Fixed position
        }
    }

    public void fix_coordinates()
    {
        mEndX = mTempX;
        mEndY = mTempY;
    }

    public int getCurX()
    {
        return mTempX;
    }

    public int getCurY()
    {
        return mTempY;
    }

    public void reset()
    {
        mTempX = 0;
        mTempY = 0;
        mEndX = 0;
        mEndY = 0;
        readyForTouch = false;
        invalidate();
    }

    public float[] transformCoordinates(float x, float y) {
        float [] coordinates = new float [] {x, y};
        Matrix matrix = new Matrix();
        viewImage.getImageMatrix().invert(matrix); // Inside a class that extends ImageView. Hence this->ImageView
        matrix.postTranslate(viewImage.getScrollX(), viewImage.getScrollY());
        matrix.mapPoints(coordinates);
        return coordinates;
    }

}