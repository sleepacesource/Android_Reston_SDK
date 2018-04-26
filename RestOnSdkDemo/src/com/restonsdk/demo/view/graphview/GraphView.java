/**
 * This file is part of GraphView.
 *
 * GraphView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GraphView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GraphView.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 *
 * Copyright Jonas Gehring
 */

package com.restonsdk.demo.view.graphview;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.restonsdk.demo.DemoApp;
import com.restonsdk.demo.R;
import com.restonsdk.demo.util.DensityUtil;
import com.restonsdk.demo.view.graphview.compatible.ScaleGestureDetector;
import com.restonsdk.demo.view.graphview.interfs.CustomLabelFormatter;
import com.restonsdk.demo.view.graphview.interfs.GraphViewDataInterface;
import com.sleepace.sdk.util.TimeUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs. This
 * is the abstract base class for all graphs. Extend this class and implement
 * {@link #drawSeries(Canvas, GraphViewDataInterface[], float, float, float, double, double, double, double, float, GraphViewSeries.GraphViewSeriesStyle)}
 * to display a custom graph. Use {@link LineGraphView} for creating a line
 * chart.
 *
 * @author jjoe64 - jonas gehring - http://www.jjoe64.com
 *
 *         Copyright (C) 2011 Jonas Gehring Licensed under the GNU Lesser
 *         General Public License (LGPL) http://www.gnu.org/licenses/lgpl.html
 *         //
 */
abstract public class GraphView extends LinearLayout {
	protected final String TAG = GraphView.class.getSimpleName();
	static final private class GraphViewConfig {
		static final float BORDER = DensityUtil.dip2px(DemoApp.getInstance(),12);;
	}

	private DashPathEffect graphEffect;

	private boolean rightVerLabel;

	/**
	 * <h3>设置竖直的水平Label</h3>
	 * <ul>
	 *   <li></li>
	 * </ul>
	 * @param rightVerLabel
	 *//*
	public void setRightVerLabel(boolean rightVerLabel)
	{
		this.rightVerLabel = rightVerLabel;
	}*/

	private String[] rightVerLabels;

	public void setRightVerLabels(int min ,int max ,int spaceNum, boolean isTime)
	{
		rightVerLabel = true;
		rightVerLabels = new String[spaceNum + 1];
		if(max - min < spaceNum)
		{
			max =  min + spaceNum;
		}
		int step = ( max - min )/spaceNum;
		for (int i = 0; i < spaceNum + 1; i++)
		{
			int a = min + step*i;
			if(isTime)
				a = a%24;
			if(a < 10)
				rightVerLabels[spaceNum -i] ="0" +String.valueOf(a);
			else
				rightVerLabels[spaceNum -i] = String.valueOf(a);
		}

	}

	public boolean isLearnMore = false;
	public String testVLabel = null;

	private float disLastX, disLastY;
	private MotionEvent event;

	/**
	 * 描述；是否禁止用NumberFormat的方法 由于后面还需要使用所以
	 */
	private boolean noFarmatData = true;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (isLearnMore) {
			if (event.getPointerCount() == 1)
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						disLastX = event.getX();
						disLastY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						if (Math.abs(event.getX() - disLastX) > Math.abs(event
								.getY() - disLastY)) {
							disLastX = event.getX();
							disLastY = event.getY();
							getParent().requestDisallowInterceptTouchEvent(true);
							return false;
						} else {
							disLastX = event.getX();
							disLastY = event.getY();
							return false;
						}
					default:
						break;
				}
		}
		return super.onInterceptTouchEvent(event);
	}

	public boolean noLine = false;

	public boolean vLabel2Time = false;// 就是把竖直方向的数字 换成时间 如 12--12pm

	private boolean parentIsDisallowTouch = false;

	/**
	 * 描述：detailFragment的 需要另一组参数，就是 每个点的时间值，起始时间戳
	 */
	protected int beginTimes;

	private float timezone = -100;

	private int dst_off = 0;

	public void setTimeZone(int timezone, int dst_off) {
		this.timezone = timezone;
		this.dst_off = dst_off;
	}

	public void setBeginAndOffset(int beginTimes, float timezone, int dst) {
		this.beginTimes = beginTimes;
		this.timezone = timezone;
		this.dst_off = dst;
//		LogUtil.log(TAG+" setBeginAndOffset starttime:" +beginTimes+",timezone:" + timezone+",dst:" + dst);
	}

	public void setTouchDisallowByParent(boolean isDisallw) {
		parentIsDisallowTouch = isDisallw;
	}

	/**
	 * 监听 graphView的滑动距离
	 *
	 * @author Administrator
	 *
	 */
	public interface OnGraphViewScrollListener {

		public void onTouchEvent(MotionEvent event, GraphView v);
	}

	private OnGraphViewScrollListener onGraphViewScroll = null;

	public void setOnGraphViewScrollListener(
			OnGraphViewScrollListener onGraphViewScroll) {
		this.onGraphViewScroll = onGraphViewScroll;
	}

	/**
	 * 描述：true表示Touch事件是自身的事件，可以传递 false表示Touch事件不是自身的事件，不可以传递
	 */
	private boolean touchMoble = true;

	public void onMyTouchEvent(MotionEvent event) {
		touchMoble = false;
		graphViewContentView.onTouchEvent(event);
	}

	protected float begin = 0, end = 0;
	private int rectColor;

	public void setRect(float begin, float end, int bgColor) {
		this.begin = begin;
		this.end = end;
		this.rectColor = bgColor;
	}

	private boolean setY = false;
	private double myMinY, myMaxY;

	// 设置最大，最小的Y值
	public void setMinMaxY(double miny, double maxY) {
		this.myMaxY = maxY;
		this.myMinY = miny;
		setY = true;
	}

	public boolean isMySelft = false;// 我们效果图的 第一个图
	private int myLabelsSize = 12;
	private int myPaintWidth = 2;
	private Paint mydeshPaint;// 画虚线的paint

	public void setmydeshPaint() {
		mydeshPaint = new Paint();
		graphEffect = new DashPathEffect(new float[] { 2, 2, 2, 2 }, 1);
		// mydeshPaint.setPathEffect(graphEffect);
		/** 设置画笔抗锯齿 **/
		mydeshPaint.setAntiAlias(true);
		/** 画笔的类型 **/
		mydeshPaint.setStyle(Paint.Style.STROKE);
		/** 设置画笔变为圆滑状 **/
		mydeshPaint.setStrokeCap(Paint.Cap.ROUND);
		mydeshPaint.setStrokeWidth(0);
		mydeshPaint.setColor(Color.WHITE);
	}

	protected float NomalX, NomalY;

	class GraphViewContentView extends View {
		private float lastTouchEventX;
		private float graphwidth;
		private boolean scrollingStarted;
		private Context context;

		/**
		 * @param context
		 */
		public GraphViewContentView(Context context) {
			super(context);
			this.context = context;
			setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
			myLabelsSize = DensityUtil.sp2px(context, myLabelsSize);
		}

		private float LastX, LastY;

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			if (parentIsDisallowTouch)
				getParent().requestDisallowInterceptTouchEvent(true);
			/*
			 * if(isLearnMore){ switch (event.getAction()) { case
			 * MotionEvent.ACTION_DOWN: LastX=event.getX(); LastY=event.getY();
			 * break; case MotionEvent.ACTION_MOVE: if(event.getX() -
			 * LastX>event.getY() - LastY) { LastX=event.getX();
			 * LastY=event.getY();
			 * getParent().requestDisallowInterceptTouchEvent(true); SleepLog.e(
			 * " 拦截事件：dispatchTouchEvent " ); }else { LastX=event.getX();
			 * LastY=event.getY(); }
			 *
			 * default: break; } }
			 */
			if (event.getPointerCount() > 1) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}

			return super.dispatchTouchEvent(event);
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {
			// if(linesIsDash)
			// setmydeshPaint();

			paint.setAntiAlias(true);

			// normal
			paint.setStrokeWidth(0);

			float border = GraphViewConfig.BORDER;
			float horstart = 0;
			float height = getHeight();
			float width = getWidth() - 1;
			double maxY = getMaxY();
			double minY = getMinY();
			double maxX = getMaxX(false);
			double minX = getMinX(false);
			double diffX = maxX - minX;

			// measure bottom text
			if (labelTextHeight == null || horLabelTextWidth == null)
			{
				paint.setTextSize(getGraphViewStyle().getTextSize());
				double testX = 0.12;
				try {
					testX = ((getMaxX(true) - getMinX(true)) * 0.783)
							+ getMinX(true);
				} catch (NullPointerException e) {
					testX = 0.12;
				}
				String testLabel = formatLabel(testX, true);
				paint.getTextBounds(testLabel, 0, testLabel.length(),
						textBounds);
				labelTextHeight = (textBounds.height());
				horLabelTextWidth = (textBounds.width());
			}
			border += labelTextHeight;

			float graphheight = height - (2 * border);
			graphwidth = width;

//			LogUtil.log(TAG+" onDraw horlabels:" + Arrays.toString(horlabels)+",graphwidth:" + graphwidth);
			if (horlabels == null) {
				horlabels = generateHorlabels(graphwidth);
			}
			if (verlabels == null)
			{
				if(!rightVerLabel)
					verlabels = generateVerlabels(graphheight);
				else
				{
					verlabels = rightVerLabels;
				}
			}

			// vertical lines
			if (graphViewStyle.getGridStyle() != GraphViewStyle.GridStyle.HORIZONTAL) {
				paint.setTextAlign(Align.LEFT);
				int vers = verlabels.length - 1;
				for (int i = 0; i < verlabels.length; i++) {
					paint.setColor(graphViewStyle.getGridColor());
					float y = ((graphheight / vers) * i) + border;

					canvas.drawLine(horstart, y, width, y, paint);
					// Log.e(VIEW_LOG_TAG, "水平划线"+y+"|||border"+border);
				}

				double unit = graphheight / (maxY - minY);
				if (end - begin != 0) {
					paint.setColor(rectColor);
					float top = graphheight - (float) ((end - minY) * unit)
							+ border;
					// Log.e(VIEW_LOG_TAG,
					// minY+"||||"+(begin-minY)+"||||"+(end-minY)+"|||"+unit);
					canvas.drawRect(horstart, top, width, graphheight
							- (float) ((begin - minY) * unit) + border, paint);
					paint.setColor(getResources().getColor(
							R.color.learn_normal_color));
					paint.setTextAlign(Align.RIGHT);
					int w = (int) paint.measureText(context
							.getString(R.string.normal));
					NomalX = graphwidth + horstart - w;
					NomalY = top - 4;
					canvas.drawText(context.getString(R.string.normal), graphwidth + horstart, top - 4, paint);
				}
			}

			// 绘制水平的labels
			if (!noLine)
				drawHorizontalLabels(context, canvas, border, horstart, height,
						horlabels, graphwidth);

			paint.setColor(graphViewStyle.getHorizontalLabelsColor());
			paint.setTextAlign(Align.CENTER);
			canvas.drawText(title, (graphwidth / 2) + horstart, border - 4,
					paint);

			if (maxY == minY) {
				// if min/max is the same, fake it so that we can render a line
				if (maxY == 0) {
					// if both are zero, change the values to prevent division
					// by zero
					maxY = 1.0d;
					minY = 0.0d;
				} else {
					maxY = maxY * 1.05d;
					minY = minY * 0.95d;
				}
			}

			double diffY = maxY - minY;
			paint.setStrokeCap(Paint.Cap.ROUND);

			for (int i = 0; i < graphSeries.size(); i++) {
				drawSeries(canvas, _values(i), graphwidth, graphheight, border,
						minX, minY, diffX, diffY, horstart,
						graphSeries.get(i).style);
			}

			if (showLegend)
				drawLegend(canvas, height, width);
		}

		/**
		 * 单手机滑动
		 *
		 * @param f
		 */
		private void onMoveGesture(float f) {
//			LogUtil.log(TAG+" onMoveGesture staticHorizontalLabels:" + staticHorizontalLabels);
			// view port update
			if (viewportSize != 0) {
				viewportStart -= f * viewportSize / graphwidth;

				// minimal and maximal view limit
				double minX = getMinX(true);
				double maxX = getMaxX(true);
				/*
				 * if (viewportStart < minX) { viewportStart = minX; } else if
				 * (viewportStart+viewportSize > maxX) { //控制滑动 viewportStart =
				 * maxX - viewportSize; }
				 */

				if (viewportStart < minX) {
					viewportStart = minX;
				} else if (isMySelft) { // 控制滑动
					if (viewportStart + viewportSize > maxX) {
						viewportStart = maxX - viewportSize;
					}
				} else if (viewportStart + viewportSize > maxX) { // 控制滑动
					viewportStart = maxX - viewportSize;
				}

				// labels have to be regenerated
				if (!staticHorizontalLabels)
					horlabels = null;
				if (!staticVerticalLabels)
					verlabels = null;
				viewVerLabels.invalidate();
			}
			invalidate();
		}

		private Point touchPoint;

		private int touchWidth = DensityUtil.dip2px(getContext(), 15);

		/**
		 * @param event
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			if (!isScrollable() || isDisableTouch()) {
				touchMoble = true;
				return true;
				// return super.onTouchEvent(event);
			}

			if (onGraphViewScroll != null && touchMoble)
				onGraphViewScroll.onTouchEvent(event, GraphView.this);

			boolean handled = false;//是否是双手操作
			// Log.e(VIEW_LOG_TAG, handled+""+scalable+(scaleDetector==null));
			// first scale
			if (scalable && scaleDetector != null) {
				scaleDetector.onTouchEvent(event);
				handled = scaleDetector.isInProgress();
			}


			// 表示的是单手滑动
			if (!handled) {
				// Log.d("GraphView",
				// "on touch event scale not handled+"+lastTouchEventX);
				// if not scaled, scroll
				if ((event.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN
						&& (event.getAction() & MotionEvent.ACTION_MOVE) == 0) {
					scrollingStarted = true;
					handled = true;
					// 记录下按下的Point
					touchPoint = new Point((int) event.getX(),
							(int) event.getY());
				}
				if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
					scrollingStarted = false;
					lastTouchEventX = 0;
					handled = true;
					touchMoble = true;
					// Log.e(VIEW_LOG_TAG,
					// "*evnetX*"+event.getX()+"***"+event.getY());
					if (onHeartListener != null && isMySelft){

//						LogUtil.logE("GraphView onHeartListener x:" + event.getX()+",y:"+event.getY()+",tx:"+touchPoint.x+",ty:"+touchPoint.y+",w:"+touchWidth+",size:"+heartPoint.size());
//						BedBean b = heartPoint.get(5);
//						LogUtil.logE("GraphView onHeartListener bx:" + b.getX()+",by:"+b.getY());

						LineGraphView.BedBean item = null;
						int size = heartPoint == null ? 0 : heartPoint.size();
						if (event.getX() > touchPoint.x - touchWidth
								&& event.getX() < touchPoint.x + touchWidth
								&& event.getY() > touchPoint.y - touchWidth
								&& event.getY() < touchPoint.y + touchWidth) {
							for (int i = 0; i < size; i++) {
								LineGraphView.BedBean bean = heartPoint.get(i);
								if (event.getX() > bean.getX() - touchWidth
										&& event.getX() < bean.getX()
										+ touchWidth
										&& event.getY() > bean.getY()
										- touchWidth
										&& event.getY() < bean.getY()
										+ touchWidth) {
									item = bean;
								}
							}

							if(item != null){//如果有2个呼吸暂停图标重叠，调用最后一个
								onHeartListener.onHeartClick(item, event);
							}
						}
					}
				}
				if ((event.getAction() & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE) {
					if (scrollingStarted) {
						if (lastTouchEventX != 0) {
							onMoveGesture(event.getX() - lastTouchEventX);
						}
						lastTouchEventX = event.getX();
						handled = true;
					}
				}
				if (handled)
					invalidate();
			} else {
				// currently scaling
				scrollingStarted = false;
				lastTouchEventX = 0;
				touchMoble = true;
			}

			return true;
		}
	}

	/**
	 * one data set for a graph series
	 */
	public static class GraphViewData implements GraphViewDataInterface {
		public final double valueX;
		public double valueY;
		public final int level;

		private int heartRate;

		private int apneaRate;

		private int status;

		private float statusValue;

		public GraphViewData(double valueX, double valueY, int level) {
			super();
			this.valueX = valueX;
			this.valueY = valueY;
			this.level = level;
		}

		public GraphViewData(double valueX, double valueY) {
			this(valueX, valueY, -1);
		}

		@Override
		public double getX() {
			return valueX;
		}

		@Override
		public double getY() {
			return valueY;
		}

		@Override
		public int getLevel() {
			// TODO Auto-generated method stub
			return level;
		}

		public int getHeartRate() {
			return heartRate;
		}

		public void setHeartRate(int heartRate) {
			this.heartRate = heartRate;
		}

		public int getApneaRate() {
			return apneaRate;
		}

		public void setApneaRate(int apneaRate) {
			this.apneaRate = apneaRate;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public int getStatus() {
			return this.status;
		}

		public void setStatusValue(float value) {
			this.statusValue = value;
		}

		public float getStatusValue() {
			return this.statusValue;
		}

		@Override
		public String toString() {
			return "GraphViewData{" +
					"valueX=" + valueX +
					", valueY=" + valueY +
					", level=" + level +
					", heartRate=" + heartRate +
					", apneaRate=" + apneaRate +
					", status=" + status +
					", statusValue=" + statusValue +
					'}';
		}
	}

	public enum LegendAlign {
		TOP, MIDDLE, BOTTOM
	}

	public interface OnHeartClickListener {
		public void onHeartClick(LineGraphView.BedBean bean, MotionEvent event);
	}

	private OnHeartClickListener onHeartListener;

	public void setOnHeartClickListener(OnHeartClickListener onHeartListener) {
		this.onHeartListener = onHeartListener;
	}

	private class VerLabelsView extends View {
		private Context context;
		private String sWake, sLight, sIn, sDeep;

		private boolean RightVerLabel = false;

		/**
		 * @param context
		 */
		public VerLabelsView(Context context) {
			super(context);
			this.context = context;
			sWake = context.getString(R.string.wake_);
			sLight = context.getString(R.string.light_);
			sIn = context.getString(R.string.mid_);
			sDeep = context.getString(R.string.deep_);

			setLayoutParams(new LayoutParams(getGraphViewStyle()
					.getVerticalLabelsWidth() == 0 ? 100 : getGraphViewStyle()
					.getVerticalLabelsWidth(), LayoutParams.FILL_PARENT));
		}

		/**
		 *
		 * @param context
		 * @param isRightAlign 是否是 右边的竖直Label
		 */
		public VerLabelsView(Context context,boolean isRightAlign)
		{
			this(context);
			this.RightVerLabel = isRightAlign;
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {
			// normal
			paint.setStrokeWidth(0);

			// measure bottom text
			if (labelTextHeight == null || verLabelTextWidth == null) {
				paint.setTextSize(getGraphViewStyle().getTextSize());
				double testY = ((getMaxY() - getMinY()) * 0.783) + getMinY();
				String testLabel = formatLabel(testY, false);
				if (testVLabel != null)
					testLabel = testVLabel;
				paint.getTextBounds(testLabel, 0, testLabel.length(),
						textBounds);
				labelTextHeight = (textBounds.height());
				verLabelTextWidth = (textBounds.width());
			}
			if (getGraphViewStyle().getVerticalLabelsWidth() == 0
					&& getLayoutParams().width != verLabelTextWidth
					+ GraphViewConfig.BORDER) {
				setLayoutParams(new LayoutParams(
						(int) (verLabelTextWidth + GraphViewConfig.BORDER),
						LayoutParams.FILL_PARENT));
			} else if (getGraphViewStyle().getVerticalLabelsWidth() != 0
					&& getGraphViewStyle().getVerticalLabelsWidth() != getLayoutParams().width) {
				setLayoutParams(new LayoutParams(getGraphViewStyle()
						.getVerticalLabelsWidth(), LayoutParams.FILL_PARENT));
			}

			float border = GraphViewConfig.BORDER;
			border += labelTextHeight;
			float height = getHeight();
			float graphheight = height - (2 * border);

			if(!RightVerLabel)
				if (verlabels == null)
				{
					verlabels = generateVerlabels(graphheight);
				}

			// vertical labels
			paint.setTextAlign(getGraphViewStyle().getVerticalLabelsAlign());
			int labelsWidth = getWidth();
			int labelsOffset = 0;
			if (getGraphViewStyle().getVerticalLabelsAlign() == Align.RIGHT) {
				labelsOffset = labelsWidth;
			} else if (getGraphViewStyle().getVerticalLabelsAlign() == Align.CENTER) {
				labelsOffset = labelsWidth / 2;
			}
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				float y = ((graphheight / vers) * i) + border;
				paint.setColor(graphViewStyle.getVerticalLabelsColor());
				if (isMySelft) {
					paint.setTextSize(myLabelsSize);
					paint.setPathEffect(new PathEffect());
					/*if (sLight.equals(verlabels[i])) {
						paint.setColor(getResources().getColor(R.color.detail_light));
					} else if (sDeep.equals(verlabels[i])) {
						paint.setColor(getResources().getColor(R.color.detail_deep));
					} else if (sIn.endsWith(verlabels[i])) {
						paint.setColor(getResources().getColor(R.color.detail_in));
					} else if (sWake.endsWith(verlabels[i])) {
						paint.setColor(getResources().getColor(R.color.detail_wake));
					} else {
						paint.setColor(getResources().getColor(R.color.detail_deep));
					}*/
					float original_size=paint.getTextSize();
					float  size=6*original_size/verlabels[i].length();
					if (verlabels[i].length()<=6){
						size=original_size;
					}
					paint.setTextSize(size);
					canvas.drawText(verlabels[i], labelsOffset, y, paint);
					paint.setTextSize(original_size);
				} else if (vLabel2Time) {// 为了画 12pm
					if (i != 0)
					{
						String txt = "";
						float l = 0;
						int x = (int) Double.parseDouble(verlabels[i]);
						x = x % 24;


						if (x < 10)
							txt = "0" + x;
						else
							txt = String.valueOf(x);


						//先判断 当前手机的设定是12小时制 还是 24小时制
						/*if (TimeUtill.HourIs24())
						{
							if (x < 10)
								txt = "0" + x;
							else{
								txt = String.valueOf(x);
							}
						}else
						{

							 l = paint.measureText("PM");// 测量pm字体的长度

							String am = "";

							if (x >= 12)
								am = "PM";
							else
								am = "AM";

							x = x%12;
							txt = String.valueOf(x);
							if (x < 10)
								txt = "0" + x;
							else
							{
								txt = String.valueOf(x);
							}

							paint.setTextSize((float) (paint.getTextSize() * 0.8));
							paint.getTextBounds(am, 0, am.length(), textBounds);
							canvas.drawText(am, labelsOffset - 5,
									(float) (y + textBounds.height() / 2), paint);
						}*/
						txt = txt+":00";
						paint.setTextSize(getGraphViewStyle().getTextSize());
						canvas.drawText(txt, labelsOffset - l, y
										+ labelTextHeight - textBounds.height() / 2,
								paint);

					}
				} else
				{
					if(RightVerLabel)
					{
						paint.setColor(context.getResources().getColor(R.color.graph_right_label_color));
						canvas.drawText(rightVerLabels[i], labelsOffset, y, paint);
					}
					else{
						float original_size=paint.getTextSize();
						float  size=6*original_size/verlabels[i].length();
						if (verlabels[i].length()<=6){
							size=original_size;
						}
						paint.setTextSize(size);
						canvas.drawText(verlabels[i], labelsOffset, y, paint);
						paint.setTextSize(original_size);
					}
				}
			}

			// reset
			paint.setTextAlign(Align.LEFT);
		}
	}

	protected final Paint paint;
	private String[] horlabels;
	protected String[] verlabels;
	private String title;
	private boolean scrollable;
	private boolean disableTouch;
	private double viewportStart;
	private double viewportSize;


	public void setStartAndSize(double start, double size) {
		this.viewportStart = start;
		this.viewportSize = size;
		redrawAll();
	}

	private  View viewVerLabels,rightViewVerLabels;
	private ScaleGestureDetector scaleDetector;
	private boolean scalable;
	private final NumberFormat[] numberformatter = new NumberFormat[2];
	protected final List<GraphViewSeries> graphSeries;
	private boolean showLegend = false;
	private LegendAlign legendAlign = LegendAlign.MIDDLE;
	private boolean manualYAxis;
	private boolean manualMaxY;
	private boolean manualMinY;
	private double manualMaxYValue;
	private double manualMinYValue;
	protected GraphViewStyle graphViewStyle;
	public final GraphViewContentView graphViewContentView;
	private CustomLabelFormatter customLabelFormatter;
	private Integer labelTextHeight;
	private Integer horLabelTextWidth;
	private Integer verLabelTextWidth;
	private final Rect textBounds = new Rect();
	private boolean staticHorizontalLabels;
	private boolean staticVerticalLabels;
	private boolean showHorizontalLabels = true;
	private boolean showVerticalLabels = true;

	public boolean HorizontalLableShowTop = false;

	public List<LineGraphView.BedBean> heartPoint;// 心跳停止的地方

	public GraphView(Context context, AttributeSet attrs) {
		this(context, attrs.getAttributeValue(null, "title"));

		int width = attrs.getAttributeIntValue("android", "layout_width",
				LayoutParams.MATCH_PARENT);
		int height = attrs.getAttributeIntValue("android", "layout_height",
				LayoutParams.MATCH_PARENT);
		setLayoutParams(new LayoutParams(width, height));
	}

	/**
	 * @param context
	 * @param title
	 *            [optional]
	 */
	public GraphView(Context context, String title,boolean rightVerLabel) {
		super(context);
		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		this.rightVerLabel = rightVerLabel;
		if (title == null)
			this.title = "";
		else
			this.title = title;

		graphViewStyle = new GraphViewStyle();
		graphViewStyle.useTextColorFromTheme(context);

		paint = new Paint();
		graphSeries = new ArrayList<GraphViewSeries>();

		viewVerLabels = new VerLabelsView(context,false);

		addView(viewVerLabels);
		graphViewContentView = new GraphViewContentView(context);
		addView(graphViewContentView, new LayoutParams(
				200, LayoutParams.FILL_PARENT, 1));
		if(rightVerLabel)
		{
			rightViewVerLabels = new VerLabelsView(context,true);
			addView(rightViewVerLabels);
		}

	}

	public GraphView(Context context, String title)
	{
		this(context,title,false);
	}

	private GraphViewDataInterface[] _values(int idxSeries) {
		GraphViewDataInterface[] values = graphSeries.get(idxSeries).values;
		synchronized (values) {
			if (viewportStart == 0 && viewportSize == 0) {
				// all data
				return values;
			} else {
				// viewport
				List<GraphViewDataInterface> listData = new ArrayList<GraphViewDataInterface>();
				for (int i = 0; i < values.length; i++) {
					if (values[i].getX() >= viewportStart) {
						if (values[i].getX() > viewportStart + viewportSize) {
							listData.add(values[i]); // one more for nice
							// scrolling
							break;
						} else {
							listData.add(values[i]);
						}
					} else {
						if (listData.isEmpty()) {
							listData.add(values[i]);
						}
						listData.set(0, values[i]); // one before, for nice
						// scrolling
					}
				}
				return listData.toArray(new GraphViewDataInterface[listData
						.size()]);
			}
		}
	}

	/**
	 * add a series of data to the graph
	 *
	 * @param series
	 */
	public void addSeries(GraphViewSeries series) {
		series.addGraphView(this);
		graphSeries.add(series);
		redrawAll();
	}

	/**
	 * 把 str指 转化为时间的显示
	 *
	 * @param str
	 * @return
	 */
	private String str2timeStr(String str) {
		if (!"".equals(str)) {
			int index = str.indexOf('.');
			if (index != -1) {
				int h = Integer.parseInt(str.substring(0, index));
				h = h % 24;
				String m = "0." + str.substring(index + 1);
				m = String.valueOf((int) (Double.parseDouble(m) * 60));
				return h + ":" + m;
			} else {
				int h = Integer.parseInt(str);
				return String.valueOf(h % 24);
			}
		} else
			return "";
	}

	protected void drawHorizontalLabels(Context context, Canvas canvas,
										float border, float horstart, float height, String[] horlabels,
										float graphwidth) {
//		LogUtil.log(TAG+" drawHorizontalLabels:" + Arrays.toString(horlabels));
		// horizontal labels + lines
		int hors = horlabels.length - 1;
		for (int i = 0; i < horlabels.length; i++) {
			paint.setColor(graphViewStyle.getGridColor());
			float x = ((graphwidth / hors) * i) + horstart;
			if (graphViewStyle.getGridStyle() != GraphViewStyle.GridStyle.VERTICAL) {
				// 绘制竖直的
				canvas.drawLine(x, height - border, x, border, paint);
			}
			if (showHorizontalLabels) {
				paint.setTextAlign(Align.CENTER);
				if (i == horlabels.length - 1)
					paint.setTextAlign(Align.RIGHT);
				if (i == 0)
					paint.setTextAlign(Align.LEFT);
				paint.setColor(graphViewStyle.getHorizontalLabelsColor());
				if (!isMySelft) {
					if (!isLearnMore) {
						if (HorizontalLableShowTop) {
							//TODO:border用来设置当前文字的上下位置，主要是用来调整top文字的垂直高度
//							canvas.drawText(horlabels[i], x, border - 24, paint);
							canvas.drawText(horlabels[i], x, border - 14, paint);//这是为了适配OPPO做出的更改，原始代码为上一条
						} else
							canvas.drawText(horlabels[i], x, height - 4, paint);
					} else if (i == horlabels.length - 1 && begin >= 0) {// detail中
						// 查看更多的是Hight
						// normal
						// low
						paint.setColor(getResources().getColor(
								R.color.learn_LOW_color));
						canvas.drawText(context.getString(R.string.low),
								graphwidth + horstart,
								height - labelTextHeight, paint);
						canvas.drawText(context.getString(R.string.high),
								graphwidth + horstart, border , paint);
					}
				} else // if(i!=0&&i!=horlabels.length-1)
				{// /绘制到top上
					Paint paint = new Paint();
					paint.setTextSize(myLabelsSize);
					paint.setColor(getResources().getColor(R.color.COLOR_3));
					String strT = horlabels[i];
					// String str="";
					if (beginTimes > 0) {
						// SleepLog.e("得到的结果1"+strT);
						// strT = strT.replaceAll(",","");
						// strT = strT.replaceAll(" ","");
						// SleepLog.e("得到的结果2"+strT);
						int tempTime = (int) Double.parseDouble(strT);
						// int [] times=TimeUtill.int2HMInt(beginTimes+tempTime
						// , timezone);
						strT = TimeUtil.int2TimeHM(beginTimes + tempTime, timezone, dst_off);
						/*
						 * if(times.length==2) strT=times[0]+":"+times[1];
						 */
						// canvas.drawText(strT, x-width/2, border-4, paint);
						// Log.e("isMySlef", "graphViewLabel"+horlabels[i]);
						/*
						 * if(!"".equals(horlabels[i])) { double time=times[0];
						 * time=time%24; if(time>=12) str="PM"; else str="AM"; }
						 */
					}
					
					
//					LogUtil.log(TAG+" drawHorizontalLabels i:"+ i+",label:"+ horlabels[i] +",beginTimes:" +beginTimes+",strT:" + strT);
					/*
					 * else { str = "AM"; }
					 */
					float width = paint.measureText(strT);
					float strX = x;
					if (i == 0) {
						strX = x;
					} else if (i == horlabels.length - 1) {
						strX = x - width;
					} else {
						strX = x - width / 2;
					}
					//canvas.drawText(strT, strX, border -24, paint);
					canvas.drawText(strT, strX, border -14, paint);////这是为了适配OPPO做出的更改，原始代码为上一条
					paint.setTextSize(myLabelsSize / 2);
					// canvas.drawText(str,
					// strX+width+2,border-4-myLabelsSize/2, paint);
				}
			}
		}

	}

	protected void drawLegend(Canvas canvas, float height, float width) {
		float textSize = paint.getTextSize();
		int spacing = getGraphViewStyle().getLegendSpacing();
		int border = getGraphViewStyle().getLegendBorder();
		int legendWidth = getGraphViewStyle().getLegendWidth();

		int shapeSize = (int) (textSize * 0.8d);
		// Log.d("GraphView", "draw legend size: "+paint.getTextSize());

		// rect
		paint.setARGB(180, 100, 100, 100);
		float legendHeight = (shapeSize + spacing) * graphSeries.size() + 2
				* border - spacing;
		float lLeft = width - legendWidth - border * 2;
		float lTop;
		switch (legendAlign) {
			case TOP:
				lTop = 0;
				break;
			case MIDDLE:
				lTop = height / 2 - legendHeight / 2;
				break;
			default:
				lTop = height - GraphViewConfig.BORDER - legendHeight
						- getGraphViewStyle().getLegendMarginBottom();
		}
		float lRight = lLeft + legendWidth;
		float lBottom = lTop + legendHeight;
		canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8,
				paint);

		for (int i = 0; i < graphSeries.size(); i++) {
			paint.setColor(graphSeries.get(i).style.color);
			canvas.drawRect(new RectF(lLeft + border, lTop + border
							+ (i * (shapeSize + spacing)), lLeft + border + shapeSize,
							lTop + border + (i * (shapeSize + spacing)) + shapeSize),
					paint);
			if (graphSeries.get(i).description != null) {
				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(graphSeries.get(i).description, lLeft + border
						+ shapeSize + spacing, lTop + border + shapeSize
						+ (i * (shapeSize + spacing)), paint);
			}
		}
	}

	abstract protected void drawSeries(Canvas canvas,
									   GraphViewDataInterface[] values, float graphwidth,
									   float graphheight, float border, double minX, double minY,
									   double diffX, double diffY, float horstart,
									   GraphViewSeries.GraphViewSeriesStyle style);

	/**
	 * formats the label use #setCustomLabelFormatter or static labels if you
	 * want custom labels
	 *
	 * @param value
	 *            x and y values
	 * @param isValueX
	 *            if false, value y wants to be formatted
	 * @deprecated use {@link #setCustomLabelFormatter(CustomLabelFormatter)}
	 * @return value to display
	 */
	@Deprecated
	protected String formatLabel(double value, boolean isValueX) {
		if (customLabelFormatter != null) {
			String label = customLabelFormatter.formatLabel(value, isValueX);
			if (label != null) {
				return label;
			}
		}
		int i = isValueX ? 1 : 0;
		if (numberformatter[i] == null) {
			numberformatter[i] = NumberFormat.getNumberInstance();
			double highestvalue = isValueX ? getMaxX(false) : getMaxY();
			double lowestvalue = isValueX ? getMinX(false) : getMinY();
			if (highestvalue - lowestvalue < 0.1) {
				numberformatter[i].setMaximumFractionDigits(6);
			} else if (highestvalue - lowestvalue < 1) {
				numberformatter[i].setMaximumFractionDigits(4);
			} else if (highestvalue - lowestvalue < 20) {
				numberformatter[i].setMaximumFractionDigits(3);
			} else if (highestvalue - lowestvalue < 100) {
				numberformatter[i].setMaximumFractionDigits(1);
			} else {
				numberformatter[i].setMaximumFractionDigits(0);
			}
		}
		return numberformatter[i].format(value);
	}

	private String[] generateHorlabels(float graphwidth) {
		int numLabels = getGraphViewStyle().getNumHorizontalLabels() - 1;
		if (numLabels < 0) {
			if (graphwidth <= 0)
				graphwidth = 1f;
			numLabels = (int) (graphwidth / (horLabelTextWidth * 2));
		}

		String[] labels = new String[numLabels + 1];
		double min = getMinX(false);
		double max = getMaxX(false);
		for (int i = 0; i <= numLabels; i++) {
			if (noFarmatData) {
				labels[i] = String
						.valueOf((int) (min + ((max - min) * i / numLabels)));
			} else {
				labels[i] = formatLabel(min + ((max - min) * i / numLabels),
						true);
			}
		}
		
//		LogUtil.log(TAG+" generateHorlabels graphwidth:"+ graphwidth+",horlabels:" + Arrays.toString(labels));
		return labels;
	}

	synchronized private String[] generateVerlabels(float graphheight) {
		int numLabels = getGraphViewStyle().getNumVerticalLabels() - 1;
		if (numLabels < 0) {
			if (graphheight <= 0)
				graphheight = 1f;
			numLabels = (int) (graphheight / (labelTextHeight * 3));
			if (numLabels == 0) {
				// Log.w("GraphView",
				// "Height of Graph is smaller than the label text height, so no vertical labels were shown!");
			}
		}
		String[] labels = new String[numLabels + 1];
		double min = getMinY();
		double max = getMaxY();
		if (max == min) {
			// if min/max is the same, fake it so that we can render a line
			if (max == 0) {
				// if both are zero, change the values to prevent division by
				// zero
				max = 1.0d;
				min = 0.0d;
			} else {
				max = max * 1.05d;
				min = min * 0.95d;
			}
		}

		for (int i = 0; i <= numLabels; i++) {
			labels[numLabels - i] = formatLabel(min
					+ ((max - min) * i / numLabels), false);
		}
		return labels;
	}

	/**
	 * @return the custom label formatter, if there is one. otherwise null
	 */
	public CustomLabelFormatter getCustomLabelFormatter() {
		return customLabelFormatter;
	}

	/**
	 * @return the graphview style. it will never be null.
	 */
	public GraphViewStyle getGraphViewStyle() {
		return graphViewStyle;
	}

	/**
	 * get the position of the legend
	 *
	 * @return
	 */
	public LegendAlign getLegendAlign() {
		return legendAlign;
	}

	/**
	 * @return legend width
	 * @deprecated use {@link GraphViewStyle#getLegendWidth()}
	 */
	@Deprecated
	public float getLegendWidth() {
		return getGraphViewStyle().getLegendWidth();
	}

	/**
	 * returns the maximal X value of the current viewport (if viewport is set)
	 * otherwise maximal X value of all data.
	 *
	 * @param ignoreViewport
	 *
	 *            warning: only override this, if you really know want you're
	 *            doing!
	 */
	protected double getMaxX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart + viewportSize;
		} else {
			// otherwise use the max x value
			// values must be sorted by x, so the last value has the largest X
			// value
			double highest = 0;
			if (graphSeries.size() > 0) {
				GraphViewDataInterface[] values = graphSeries.get(0).values;
				if (values.length == 0) {
					highest = 0;
				} else {
					highest = values[values.length - 1].getX();
				}
				for (int i = 1; i < graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					if (values.length > 0) {
						highest = Math.max(highest,
								values[values.length - 1].getX());
					}
				}
			}
			return highest;
		}
	}

	/**
	 * returns the maximal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxY() {
		if (setY) {
			return myMaxY;
		}
		double largest;
		if (manualYAxis || manualMaxY) {
			largest = manualMaxYValue;
		} else {
			largest = Integer.MIN_VALUE;
			for (int i = 0; i < graphSeries.size(); i++) {
				GraphViewDataInterface[] values = _values(i);
				for (int ii = 0; ii < values.length; ii++)
					if (values[ii].getY() > largest)
						largest = values[ii].getY();
			}
		}
		return largest;
	}

	/**
	 * returns the minimal X value of the current viewport (if viewport is set)
	 * otherwise minimal X value of all data.
	 *
	 * @param ignoreViewport
	 *
	 *            warning: only override this, if you really know want you're
	 *            doing!
	 */
	protected double getMinX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart;
		} else {
			// otherwise use the min x value
			// values must be sorted by x, so the first value has the smallest X
			// value
			double lowest = 0;
			if (graphSeries.size() > 0) {
				GraphViewDataInterface[] values = graphSeries.get(0).values;
				if (values.length == 0) {
					lowest = 0;
				} else {
					lowest = values[0].getX();
				}
				for (int i = 1; i < graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					if (values.length > 0) {
						lowest = Math.min(lowest, values[0].getX());
					}
				}
			}
			return lowest;
		}
	}

	/**
	 * returns the minimal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinY() {
		if (setY) {
			return myMinY;
		}
		double smallest;
		if (manualYAxis || manualMinY) {
			smallest = manualMinYValue;
		} else {
			smallest = Integer.MAX_VALUE;
			for (int i = 0; i < graphSeries.size(); i++) {
				GraphViewDataInterface[] values = _values(i);
				for (int ii = 0; ii < values.length; ii++)
					if (values[ii].getY() < smallest)
						smallest = values[ii].getY();
			}
		}
		return smallest;
	}

	/**
	 * returns the size of the Viewport
	 *
	 */
	public double getViewportSize() {
		return viewportSize;
	}

	public boolean isDisableTouch() {
		return disableTouch;
	}

	public boolean isScrollable() {
		return scrollable;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * forces graphview to invalide all views and caches. Normally there is no
	 * need to call this manually.
	 */
	public void redrawAll() {
//		LogUtil.log(TAG+" redrawAll staticHorizontalLabels:" + staticHorizontalLabels);
		if (!staticVerticalLabels)
			verlabels = null;
		if (!staticHorizontalLabels)
			horlabels = null;
		numberformatter[0] = null;
		numberformatter[1] = null;
		labelTextHeight = null;
		horLabelTextWidth = null;
		verLabelTextWidth = null;

		invalidate();
		viewVerLabels.invalidate();
		graphViewContentView.invalidate();
	}

	/**
	 * removes all series
	 */
	public void removeAllSeries() {
		for (GraphViewSeries s : graphSeries) {
			s.removeGraphView(this);
		}
		while (!graphSeries.isEmpty()) {
			graphSeries.remove(0);
		}
		redrawAll();
	}

	/**
	 * removes a series
	 *
	 * @param series
	 *            series to remove
	 */
	public void removeSeries(GraphViewSeries series) {
		series.removeGraphView(this);
		graphSeries.remove(series);
		redrawAll();
	}

	/**
	 * removes series
	 *
	 * @param index
	 */
	public void removeSeries(int index) {
		if (index < 0 || index >= graphSeries.size()) {
			throw new IndexOutOfBoundsException("No series at index " + index);
		}

		removeSeries(graphSeries.get(index));
	}

	/**
	 * scrolls to the last x-value
	 *
	 * @throws IllegalStateException
	 *             if scrollable == false
	 */
	public void scrollToEnd() {
//		LogUtil.log(TAG+" scrollToEnd staticHorizontalLabels:" + staticHorizontalLabels);
		if (!scrollable)
			throw new IllegalStateException("This GraphView is not scrollable.");
		double max = getMaxX(true);
		viewportStart = max - viewportSize;

		// don't clear labels width/height cache
		// so that the display is not flickering
		if (!staticVerticalLabels)
			verlabels = null;
		if (!staticHorizontalLabels)
			horlabels = null;

		invalidate();
		viewVerLabels.invalidate();
		graphViewContentView.invalidate();
	}

	/**
	 * set a custom label formatter
	 *
	 * @param customLabelFormatter
	 */
	public void setCustomLabelFormatter(
			CustomLabelFormatter customLabelFormatter) {
		this.customLabelFormatter = customLabelFormatter;
	}

	/**
	 * The user can disable any touch gestures, this is useful if you are using
	 * a real time graph, but don't want the user to interact
	 *
	 * @param disableTouch
	 */
	public void setDisableTouch(boolean disableTouch) {
		this.disableTouch = disableTouch;
	}

	/**
	 * set custom graphview style
	 *
	 * @param style
	 */
	public void setGraphViewStyle(GraphViewStyle style) {
		graphViewStyle = style;
		labelTextHeight = null;
	}

	/**
	 * set's static horizontal labels (from left to right)
	 *
	 * @param horlabels
	 *            if null, labels were generated automatically
	 */
	public void setHorizontalLabels(String[] horlabels) {
//		LogUtil.log(TAG+" setHorizontalLabels:" + Arrays.toString(horlabels));
		staticHorizontalLabels = horlabels != null;
		this.horlabels = horlabels;
	}

	/**
	 * legend position
	 *
	 * @param legendAlign
	 */
	public void setLegendAlign(LegendAlign legendAlign) {
		this.legendAlign = legendAlign;
	}

	/**
	 * legend width
	 *
	 * @param legendWidth
	 * @deprecated use {@link GraphViewStyle#setLegendWidth(int)}
	 */
	@Deprecated
	public void setLegendWidth(float legendWidth) {
		getGraphViewStyle().setLegendWidth((int) legendWidth);
	}

	/**
	 * you have to set the bounds {@link #setManualYAxisBounds(double, double)}.
	 * That automatically enables manualYAxis-flag. if you want to disable the
	 * menual y axis, call this method with false.
	 *
	 * @param manualYAxis
	 */
	public void setManualYAxis(boolean manualYAxis) {
		this.manualYAxis = manualYAxis;
	}

	/**
	 * if you want to disable the menual y axis maximum bound, call this method
	 * with false.
	 */
	public void setManualMaxY(boolean manualMaxY) {
		this.manualMaxY = manualMaxY;
	}

	/**
	 * if you want to disable the menual y axis minimum bound, call this method
	 * with false.
	 */
	public void setManualMinY(boolean manualMinY) {
		this.manualMinY = manualMinY;
	}

	/**
	 * set manual Y axis limit
	 *
	 * @param max
	 * @param min
	 */
	public void setManualYAxisBounds(double max, double min) {
		manualMaxYValue = max;
		manualMinYValue = min;
		manualYAxis = true;
	}

	/*
	 * set manual Y axis max limit
	 *
	 * @param max
	 */
	public void setManualYMaxBound(double max) {
		manualMaxYValue = max;
		manualMaxY = true;
	}

	/*
	 * set manual Y axis min limit
	 *
	 * @param min
	 */
	public void setManualYMinBound(double min) {
		manualMinYValue = min;
		manualMinY = true;
	}

	/**
	 * this forces scrollable = true
	 *
	 * @param scalable
	 */
	synchronized public void setScalable(boolean scalable) {
		this.scalable = scalable;
		if (scalable == true && scaleDetector == null) {
			scrollable = true; // automatically forces this
			scaleDetector = new ScaleGestureDetector(getContext(),
					new ScaleGestureDetector.SimpleOnScaleGestureListener() {
						@Override
						public boolean onScale(ScaleGestureDetector detector) {
							// Log.e("GraphView","viewportStart="+viewportStart+",viewportSize="+viewportSize+",detector.getScaleFactor()="+detector.getScaleFactor());
							double center = viewportStart + viewportSize / 2;
							viewportSize /= detector.getScaleFactor();
							viewportStart = center - viewportSize / 2;

							// viewportStart must not be < minX
							double minX = getMinX(true);
							if (viewportStart < minX) {
								viewportStart = minX;
							}

							// viewportStart + viewportSize must not be > maxX
							double maxX = getMaxX(true);
							if (viewportSize == 0) {
								viewportSize = maxX;
							}
							double overlap = viewportStart + viewportSize
									- maxX;
							if (overlap > 0) {
								// scroll left
								if (viewportStart - overlap > minX) {
									viewportStart -= overlap;
								} else {
									// maximal scale
									viewportStart = minX;
									viewportSize = maxX - viewportStart;
								}
							}
							redrawAll();
							return true;
						}
					});
		}
	}

	/**
	 * the user can scroll (horizontal) the graph. This is only useful if you
	 * use a viewport {@link #setViewPort(double, double)} which doesn't
	 * displays all data.
	 *
	 * @param scrollable
	 */
	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * sets the title of graphview
	 *
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * set's static vertical labels (from top to bottom)
	 *
	 * @param verlabels
	 *            if null, labels were generated automatically
	 */
	public void setVerticalLabels(String[] verlabels) {
		staticVerticalLabels = verlabels != null;
		this.verlabels = verlabels;
//		LogUtil.showMsg(TAG+" setVerticalLabels labels:"+ Arrays.toString(verlabels));
	}

	/**
	 * set's the viewport for the graph.
	 *
	 * @see #setManualYAxisBounds(double, double) to limit the y-viewport
	 * @param start
	 *            x-value
	 * @param size
	 */
	public void setViewPort(double start, double size) {
		if (size < 0) {
			throw new IllegalArgumentException(
					"Viewport size must be greater than 0!");
		}
		viewportStart = start;
		if (isMySelft) {
			viewportStart = getMinX(true);
			// viewportSize = getMaxX(true);
		}
		/*
		 * else
		 */
		viewportSize = size;
	}

	/**
	 * Sets whether horizontal labels are drawn or not.
	 *
	 * @param showHorizontalLabels
	 */
	public void setShowHorizontalLabels(boolean showHorizontalLabels) {
		this.showHorizontalLabels = showHorizontalLabels;
		redrawAll();
	}

	/**
	 * Gets are horizontal labels drawn.
	 *
	 * @return {@code True} if horizontal labels are drawn
	 */
	public boolean getShowHorizontalLabels() {
		return showHorizontalLabels;
	}

	/**
	 * Sets whether vertical labels are drawn or not.
	 *
	 * @param showVerticalLabels
	 */
	public void setShowVerticalLabels(boolean showVerticalLabels) {
		this.showVerticalLabels = showVerticalLabels;
		if (this.showVerticalLabels)
		{
			addView(viewVerLabels, 0);
		} else {
			removeView(viewVerLabels);
		}
	}

	/**
	 * Gets are vertical labels are drawn.
	 *
	 * @return {@code True} if vertical labels are drawn
	 */
	public boolean getShowVerticalLabels() {
		return showVerticalLabels;
	}

	public double getViewportStart() {
		return viewportStart;
	}

	public void setViewportStart(double viewportStart) {
		this.viewportStart = viewportStart;
	}

	public void setViewportSize(double viewportSize) {
		this.viewportSize = viewportSize;
	}

}
