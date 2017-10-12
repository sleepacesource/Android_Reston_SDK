package com.restonsdk.demo.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.restonsdk.demo.util.DensityUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class RealTimeView extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = getClass().getSimpleName();

    public RealTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private SurfaceHolder holder;

    private List<PointF> data;

    private BlockingQueue<PointF> addData;

    private Paint mPaint;

    private MyThread thread;

    private int width, height;

    public RealTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private float minY, maxY;

    public void setBondValue(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }


    private float getRealY(float y) {
        float realY = height - height / (maxY - minY) * (y + 1);
        return realY;
    }

    public void add(PointF p) {
        p.y = getRealY(p.y);
        addData.offer(p);
    }

    public RealTimeView(Context context) {
        super(context);
        init();
    }

    private void init() {
        minY = -1;
        maxY = 1;
        holder = this.getHolder();
        holder.addCallback(this);
        data = new ArrayList<PointF>();
        thread = new MyThread(holder);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(DensityUtil.dip2px(getContext(), 1));
        mPaint.setAntiAlias(true);
        addData = new LinkedBlockingQueue<PointF>(121);

        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public void setGraphLineColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        this.width = width;
        this.height = height;

        if (!thread.isRun) {
            thread.isRun = true;
            thread.start();
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        thread.isRun = false;
    }

    // 线程内部类
    class MyThread extends Thread {
        //		private SurfaceHolder holder;
        public boolean isRun;


        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "Run:" + isRun;
        }

        public MyThread(SurfaceHolder holder) {
//			this.holder = holder;
//			isRun = true;
        }

        @Override
        public void run() {
//			int count = 0;
            Canvas c = null;
            while (isRun) {
                try {
                    PointF p = addData.take();
                    data.add(p);

                    if (addData.size() < 20) {
                        myDraw(c);
                        Thread.sleep(10);
                    } else {
                        PointF p1 = addData.take();
                        data.add(p1);
                        myDraw(c);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    int count;

    protected void myDraw(Canvas c) {

        synchronized (holder) {
            Rect rect = new Rect(0, 0,
                    (int) (data.get(data.size() - 1).x - data.get(0).x + 50), height);
            c = holder.lockCanvas(rect);// 锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
            if (c == null) return;
            mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            c.drawPaint(mPaint);
            mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));

//			c.drawColor(Color.TRANSPARENT);// 设置画布背景颜色
            if (data.size() > 1)
                for (int i = 1; i < data.size(); i++) {
                    PointF a = data.get(i - 1);
                    PointF b = data.get(i);
                    c.drawLine(a.x - data.get(0).x, data.get(i - 1).y,
                            b.x - data.get(0).x, data.get(i).y, mPaint);
                }

            PointF pointFirst = data.get(0);
            PointF PointEnd = data.get(data.size() - 1);
            if (PointEnd.x - pointFirst.x > width)
                data.clear();

        }
        if (c != null) {
            holder.unlockCanvasAndPost(c);// 结束锁定画图，并提交改变。
        }
    }

}
