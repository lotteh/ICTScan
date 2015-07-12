package org.chocolatemilk.ictscan;

import android.content.Context;
import android.content.res.TypedArray;
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
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Lotti on 14.05.2015.
 */

public class DragPointView extends View {

    private int offset = 0;
    private Paint mPointPaint;
    private int mTempX = 0;
    private int mTempY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    public boolean readyForTouch = false;
    private boolean mDrawPoint = false;
    private TextPaint mTextPaint = null;


    private boolean zooming = false;
    /*
    private Matrix matrix;
    private Bitmap mBitmap;
    private BitmapShader mShader;
    private Paint mCirclePaint;
    private Paint mCrossPaint;*/
    public TouchImageView viewImage;

    private DragPointView.OnUpCallback mCallback = null;

    /*
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

    private int [] fixedPos = new int[2];*/
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
        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int accentcolor = a.getColor(0, 0);
        a.recycle();
        mPointPaint.setColor(accentcolor);
        //mPointPaint.setColor(getContext().getResources().getColor(R.color.cornercolor));
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setStrokeWidth(10);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(accentcolor);
        //mTextPaint.setColor(getContext().getResources().getColor(R.color.cornercolor));
        mTextPaint.setTextSize(20);


        zooming = false;
        /*
        matrix = new Matrix();
        mCirclePaint = new Paint();


        mCrossPaint = new Paint();
        mCrossPaint.setColor(getContext().getResources().getColor(R.color.gray_cross));
        mCrossPaint.setStyle(Paint.Style.STROKE);
        mCrossPaint.setStrokeWidth(5);*/

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        offset = -width/20;

}

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!readyForTouch) return true;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mTempX = (int) event.getX() +offset;
                mTempY = (int) event.getY() +offset;
                if (mTempX<0) mTempX = 0;
                if (mTempY<0) mTempY = 0;
                mDrawPoint = false;
                //zooming = true;
                coordinates = transformCoordinates(mTempX,mTempY);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX()+offset;
                int y = (int) event.getY()+offset;
                if (x<0) mTempX = 0;
                if (x<0) mTempY = 0;

                if (!mDrawPoint || Math.abs(x - mTempX) > 2 || Math.abs(y - mTempY) > 2) {
                    mTempX = x;
                    mTempY = y;
                    coordinates = transformCoordinates(mTempX,mTempY);
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
                mDrawPoint = true;
                coordinates = transformCoordinates(mTempX,mTempY);
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                zooming = false;
                coordinates = transformCoordinates(mTempX,mTempY);
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
        /*scaledWidth=this.getWidth();
        scaledHeight=this.getHeight();

        if (mTempX<scaledWidth/2) fixedPos[0] = (int)(scaledWidth*0.75);
        else fixedPos[0] = (int)(scaledWidth*0.25);
        if (mTempY<scaledHeight/2) fixedPos[1] = (int)(scaledHeight*0.75);
        else fixedPos[1] = (int)(scaledHeight*0.25);*/

        if (mDrawPoint) {
            canvas.drawPoint(mTempX, mTempY, mPointPaint);
            canvas.drawText("  (" + coordinates[0] + ", " + coordinates[1] + ")",
                    mTempX, mTempY, mTextPaint);
        }

        /*if (zooming && null!=viewImage) {
            mBitmap = ((BitmapDrawable)viewImage.getDrawable()).getBitmap(); // get Image
            mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mCirclePaint.setShader(mShader);
            matrix.reset();
            matrix.postScale(2f, 2f, 2*coordinates[0], 2*coordinates[1]);
            mCirclePaint.getShader().setLocalMatrix(matrix);

            canvas.drawCircle(fixedPos[0], fixedPos[1], 90, mCirclePaint);//Fixed position: 100, 115

            canvas.drawLine(fixedPos[0]-90, fixedPos[1], fixedPos[0]+90, fixedPos[1], mCrossPaint);//Fadenkreuz
            canvas.drawLine(fixedPos[0], fixedPos[1]-90, fixedPos[0], fixedPos[1]+90, mCrossPaint);
        }*/
    }

    public void fix_coordinates()
    {
        int [] new_coordinates = transformCoordinates(mTempX, mTempY);
        mEndX = new_coordinates[0];
        mEndY = new_coordinates[1];
    }

    public void reset()
    {
        mTempX = 0;
        mTempY = 0;
        resetBitmapCoordinates();
        readyForTouch = false;
        invalidate();
    }

    public int[] transformCoordinates(int X, int Y) {
        /*

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
        return coordinates;*/

        PointF point_touched = viewImage.transformCoordTouchToBitmap(X, Y, true);
        int[] coordinates = new int[] {(int)point_touched.x, (int)point_touched.y};
        return coordinates;

    }

    public int getScreenX()
    {
        return mTempX;
    }

    public int getScreenY()
    {
        return mTempY;
    }

    public int getBitmapX()
    {
        return mEndX;
    }

    public int getBitmapY()
    {
        return mEndY;
    }

    public void resetBitmapCoordinates () {mEndY = 0; mEndX = 0;}

}