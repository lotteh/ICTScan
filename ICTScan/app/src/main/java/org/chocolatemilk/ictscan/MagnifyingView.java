package org.chocolatemilk.ictscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Lotti on 02.06.2015.
 */
public class MagnifyingView extends ImageView {

    private boolean zooming = false;
    private PointF zoomPos;
    private Matrix matrix;
    private Bitmap mBitmap;
    private BitmapShader mShader;
    private Paint mPaint;

    public MagnifyingView(Context ctx) {
        super(ctx);
        init();
    }

    public MagnifyingView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init();
    }

    public MagnifyingView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        zooming = false;
        zoomPos = new PointF(0,0);
        matrix = new Matrix();

        mBitmap = ((BitmapDrawable)this.getDrawable()).getBitmap(); // get Image
        mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mPaint = new Paint();
        mPaint.setShader(mShader);
    }

    public boolean touchAction (View view, MotionEvent event)
    //@Override
    //public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();

        zoomPos.x = event.getX();
        zoomPos.y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                zooming = true;
                this.invalidate();
                break;
            case MotionEvent.ACTION_UP:
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
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (zooming) {
            matrix.reset();
            matrix.postScale(2f, 2f, zoomPos.x, zoomPos.y);
            mPaint.getShader().setLocalMatrix(matrix);

            canvas.drawCircle(zoomPos.x, zoomPos.y, 80, mPaint); //Un-Fixed position
        }
    }
}
