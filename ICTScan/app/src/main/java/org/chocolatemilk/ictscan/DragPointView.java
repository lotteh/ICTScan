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
import android.graphics.drawable.Drawable;
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
    private Matrix matrix;
    private Bitmap mBitmap;
    private BitmapShader mShader;
    private Paint mCirclePaint;
    private Paint mCrossPaint;
    public ImageView viewImage;

    private DragPointView.OnUpCallback mCallback = null;


    private int intrinsicHeight = 0;
    private int intrinsicWidth = 0;
    private int scaledHeight = 0;
    private int scaledWidth = 0;
    private float heightRatio = 0;
    private float widthRatio = 0;
    private int scaledImageOffsetX = 0;
    private int scaledImageOffsetY = 0;
    private int originalImageOffsetX = 0;
    private int originalImageOffsetY = 0;

    private int [] coordinates = new int [2];

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
        matrix = new Matrix();
        mCirclePaint = new Paint();

        mCrossPaint = new Paint();
        mCrossPaint.setColor(getContext().getResources().getColor(R.color.gray_cross));
        mCrossPaint.setStyle(Paint.Style.STROKE);
        mCrossPaint.setStrokeWidth(5);


}

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!readyForTouch) return true;
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
            coordinates = transformCoordinates(mTempX,mTempY);

            mBitmap = ((BitmapDrawable)viewImage.getDrawable()).getBitmap(); // get Image
            mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mCirclePaint.setShader(mShader);
            matrix.reset();
            matrix.postScale(2f, 2f, 2*coordinates[0], 2*coordinates[1]);
            mCirclePaint.getShader().setLocalMatrix(matrix);

            canvas.drawCircle(mTempX, mTempY, 90, mCirclePaint);//Fixed position: 100, 115

            canvas.drawLine(mTempX-90, mTempY, mTempX+90, mTempY, mCrossPaint);//Fadenkreuz
            canvas.drawLine(mTempX, mTempY-90, mTempX, mTempY+90, mCrossPaint);
        }
    }

    public void fix_coordinates()
    {
        coordinates = transformCoordinates(mTempX, mTempY);
        mEndX = coordinates[0];
        mEndY = coordinates[1];
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

    public int[] transformCoordinates(int X, int Y) {
        /*int[] location = new int[2];
        viewImage.getLocationOnScreen(location);*/

        Drawable drawable = viewImage.getDrawable();
        Rect imageBounds = drawable.getBounds();

        //original height and width of the bitmap
        intrinsicHeight = drawable.getIntrinsicHeight();
        intrinsicWidth = drawable.getIntrinsicWidth();

        //height and width of the visible (scaled) image
        scaledHeight = imageBounds.height();
        scaledWidth = imageBounds.width();

        //Find the ratio of the original image to the scaled image
        //Should normally be equal unless a disproportionate scaling
        //(e.g. fitXY) is used.
        heightRatio = intrinsicHeight / scaledHeight;
        widthRatio = intrinsicWidth / scaledWidth;

        //get the distance from the left and top of the image bounds
        scaledImageOffsetX = X - imageBounds.left;//location[0];
        scaledImageOffsetY = Y - imageBounds.top; //location[1]

        //scale these distances according to the ratio of your scaling
        //For example, if the original image is 1.5x the size of the scaled
        //image, and your offset is (10, 20), your original image offset
        //values should be (15, 30).
        originalImageOffsetX = (int) (scaledImageOffsetX * widthRatio);
        originalImageOffsetY = (int) (scaledImageOffsetY * heightRatio);

        int[] coordinates = new int[] {originalImageOffsetX, originalImageOffsetY};
        return coordinates;
    }

    public int getCurX()
    {
        return mTempX;
    }

    public int getCurY()
    {
        return mTempY;
    }

    public int getFinalX()
    {
        return mEndX;
    }

    public int getFinalY()
    {
        return mEndY;
    }

}