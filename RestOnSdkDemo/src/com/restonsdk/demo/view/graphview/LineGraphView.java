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

import java.util.ArrayList;
import java.util.List;

import com.restonsdk.demo.R;
import com.restonsdk.demo.bean.CvPoint;
import com.restonsdk.demo.util.DensityUtil;
import com.restonsdk.demo.view.graphview.interfs.GraphViewDataInterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Line Graph View. This draws a line chart.
 */
public class LineGraphView extends GraphView {
	private final Paint paintBackground;
	private boolean drawBackground;
	private boolean drawDataPoints;
	private float dataPointsRadius = 10f;
	private final String TAG = "LineGraphView";
	public boolean GraphISOval = false;
	private Path Ovalpath;

	// 背景图的颜色设置
	private final int DEEPSLEEPPOINT = -2;
	private final int WAKEMAX = 2;
	private final int LIGHTSEEPPOINT = 0;
	private final int INSEEPPINT = -1;
	private final int WAKEPOINT = 1;

	private float dataCircleRadius = 4f;// 需要特别指明的点的 园的 直径
	private float dataWakeValue = 1;// 指明wake的时候

	public boolean isDrawYMax() {
		return isDrawYMax;
	}

	public void setDrawYMax(boolean drawYMax) {
		isDrawYMax = drawYMax;
	}

	/**
	 * 是否绘制y的最大坐标点
	 */
	private boolean  isDrawYMax=true;

	/**
	 * 画图中，出现的无效值
	 */
	private double invalid = -100;
	
	public void setInvaid(double invalid)
	{
		this.invalid = invalid;
	}

	/**
	 * 描述：呼吸暂停数据集合
	 */
	private List<GraphViewData> apneaPauseList;

	/**
	 * 描述：心脏停止数据集合
	 */
	private List<GraphViewData> heartPauseList;

	public void setBitmap(Bitmap bitmap, double width) {
	}

	/**
	 * 描述：设置 暂停数据
	 */
	public void setPauseData(List<GraphViewData> apneaList,
			List<GraphViewData> heartList) {
		this.heartPauseList = heartList;
		this.apneaPauseList = apneaList;
	}

	public LineGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LeaveBedFontSize = DensityUtil.sp2px(getContext(), LeaveBedFontSize);
		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
		paintBackground.setAlpha(128);
	}

	public LineGraphView(Context context, String title,boolean withRightLabel) {
		super(context, title,withRightLabel);
		LeaveBedFontSize = DensityUtil.sp2px(getContext(), LeaveBedFontSize);
		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
		paintBackground.setAlpha(128);
	}
	
	public LineGraphView(Context context,String title)
	{
		this(context,title,false);
	}

	private List<BedBean> beans;
	private List<BedBean> SleepUpIn;

	public void setSleepUpIn(List<BedBean> data) {
		this.SleepUpIn = data;
	}

	private float lastBedEndX = -1;// 上一次离床的时间
	private float LeaveBedFontSize = 8f;// 离床时间字体的大小
	private Paint markPaint;
	private float leaveBedDrawableHeight = 0;// 起床那个图的高度

	/**
	 * 描述：获取字体的高度 目前设置的是默认的字体的高度 就是 最大值 和 最小值 需要使用 的高度
	 */
	private float textHight = 0;

	/**
	 * 描述：画出最大值 或 最小值
	 * 
	 * @param isMax
	 *            是否为 最大值
	 * @param canvas
	 *            画布
	 * @param x
	 *            坐标X值
	 * @param y
	 *            坐标Y值
	 * @param value
	 */
	private void drawBorderData(boolean isMax, Canvas canvas, double x,
			double y, int value, int diffX, float graphwidth, float graphheight) {
		int color = paint.getColor();
		if (super.end > 0 && (value > super.end || value < super.begin)) {
			paint.setColor(getResources().getColor(R.color.learn_LOW_color));
		} else {
			paint.setColor(getResources().getColor(R.color.white_50));
		}
		canvas.drawCircle((float) x, (float) y, dataPointsRadius, paint);

		int w = (int) paint.measureText(String.valueOf(value));
		if (isMax) {
			if (x + w / 2 >= super.NomalX && y - textHight * 2.5 < super.NomalY
					&& y + textHight * 2.5 > super.NomalY)
				y = super.NomalY - textHight * 2.5;
			else
				y -= textHight;
		} else {
			y += textHight * 2.5;
		}

		if (diffX < w / 2) {
			x = x - diffX + w / 2;
		} else if (diffX + w / 2 > graphwidth) {
			x = graphwidth - w / 2;
		}
		canvas.drawText(String.valueOf(value), (float) x, (float) y, paint);
		paint.setColor(color);
	}

	private synchronized void drawMark(Canvas canvas, float graphwidth, float graphheight,
			float border, float endX, float endY, GraphViewDataInterface data,
			double minX, double diffX, float horstart) {
		if (markPaint == null) {
			markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			markPaint.setStyle(Style.STROKE);
			markPaint.setColor(getResources().getColor(R.color.black_30));
			markPaint.setStrokeWidth(2f);
			// 虚线图 ，不行
			/*
			 * DashPathEffect markLineeffect = new DashPathEffect(new float[] {
			 * 1, 2 }, 1); markPaint.setPathEffect(markLineeffect);
			 */
		}

		if (beans == null)
			beans = getBedBeans();

		final int sleepInUpSize = SleepUpIn == null ? 0 : SleepUpIn.size();
		/******** 画入睡的醒来的 月亮 和 太阳 *********/
		for (int i = 0; i < sleepInUpSize; i++) {
			if (SleepUpIn.get(i).getData().getX() == data.getX())
				if (i == 0 || i == SleepUpIn.size() - 1) {
					// // 画背景图 此时必定是入睡
					// int [] times
					// =TimeUtill.int2HMInt(beginTimes+(int)data.getX());
					// 判断当前的是sun 还是moon
					Bitmap time_icon;
					if (i == 0) {
						time_icon = BitmapFactory.decodeResource(
								getResources(), R.drawable.moon_c);
					} else {
						time_icon = BitmapFactory.decodeResource(
								getResources(), R.drawable.sun_a);
					}
					canvas.drawBitmap(time_icon, endX - time_icon.getWidth()
							/ 2, border + 10, paint);
					canvas.drawLine(endX, endY, endX,
							border + time_icon.getHeight() + 10, markPaint);
					canvas.drawCircle(endX, endY, dataCircleRadius, paint);
				}
		}

		final int beanSize = beans == null ? 0 : beans.size();
		for (int j = 0; j < beanSize; j++) {
			if (beans.get(j).getData().getX() == data.getX()) {
				{
					Bitmap markBed = BitmapFactory.decodeResource(
							getResources(), R.drawable.leavebed_b);
					leaveBedDrawableHeight = markBed.getHeight();
					canvas.drawLine(endX, endY, endX,
							border + markBed.getHeight() + 10, markPaint);
					canvas.drawCircle(endX, endY, dataCircleRadius, paint);
					if (!beans.get(j).isWake())
					{
						canvas.drawBitmap(markBed, endX - (endX - lastBedEndX)
								/ 2 - markBed.getWidth() / 2, border + 10,
								paint);
						// 画离床时间
						if (endY - 10 - (border + 10 + markBed.getHeight()) > LeaveBedFontSize) {
							Paint paint = new Paint();
							paint.setTextSize(LeaveBedFontSize);
							// String leaveTime
							// =(int)((beans.get(j).getData().getX()
							// - beans.get(j - 1).getData().getX())/60) + "M";
							String leaveTime = (int) (Math.ceil((beans.get(j)
									.getStatusValue()) / 60f)) + "M";
							float fontWidth = paint.measureText(leaveTime);
							if (endX - lastBedEndX > fontWidth) {
								paint.setColor(Color.WHITE);
								canvas.drawText(leaveTime, endX
										- (endX - lastBedEndX) / 2 - fontWidth
										/ 2, border + 10 + markBed.getHeight()
										+ 15, paint);
								// Log.e(VIEW_LOG_TAG, "画起床的时间："+leaveTime);
							}
						}
						lastBedEndX = -1;
					} else {
						lastBedEndX = endX;
					}
				}
			}
		}
	}

	/**
	 * 描述：画 心脏 呼吸暂停的标志，当 呼吸 低于10次的时候，调用
	 */
	private void drawPause(GraphViewDataInterface[] values, float endX,
			Canvas canvas, float graphheight, float border, float endY, int i) {

		if (heartPauseList != null)
			for (GraphViewData d : heartPauseList) {
				if (values[i].getX() == d.getX()) {
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.heart_3);
					canvas.drawBitmap(bitmap, endX - bitmap.getWidth() / 2,
							graphheight + border - bitmap.getHeight() - 10,
							paint);
					paint.setStyle(Style.STROKE);
					canvas.drawCircle(endX, endY, dataCircleRadius, paint);
					canvas.drawLine(endX, endY, endX, graphheight + border
							- bitmap.getHeight() - 10, markPaint);
					BedBean bean = new BedBean();
					bean.setData(values[i]);
					bean.setHeartPause(true);
					bean.setX(endX);
					bean.setY(graphheight + border - bitmap.getHeight() / 2
							- 10);
					bean.setApneaRate(d.getApneaRate());
					bean.setHeartRate(d.getHeartRate());

					bean.setStatus(d.getStatus());
					bean.setStatusValue(d.getStatusValue());
					heartPoint.add(bean);
				}
			}

		if (apneaPauseList != null)
			for (GraphViewData d : apneaPauseList) {

				if (values[i].getX() == d.getX()) {
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.huxi_3);
					canvas.drawBitmap(bitmap, endX - bitmap.getWidth() / 2,
							graphheight + border - bitmap.getHeight() - 10,
							paint);
					paint.setStyle(Style.STROKE);
					canvas.drawCircle(endX, endY, dataCircleRadius, paint);
					canvas.drawLine(endX, endY, endX, graphheight + border
							- bitmap.getHeight() - 10, markPaint);
					BedBean bean = new BedBean();
					bean.setData(values[i]);
					bean.setX(endX);
					bean.setHeartPause(false);
					bean.setY(graphheight + border - bitmap.getHeight() / 2
							- 10);
					bean.setApneaRate(d.getApneaRate());
					bean.setHeartRate(d.getHeartRate());

					bean.setStatus(d.getStatus());
					bean.setStatusValue(d.getStatusValue());
					heartPoint.add(bean);
				}
			}
	}

	/**
	 * 描述：画心脏 呼吸暂停的标志，当呼吸 次数 >= 10次的时候，调用
	 */
	private void drawMuchPause(GraphViewDataInterface[] values, float endX,
			Canvas canvas, float graphheight, float border, float endY, int i) {
		if (heartPauseList != null)
			for (GraphViewData d : heartPauseList) {
				if (values[i].getX() == d.getX()) {
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.heart_3);
					canvas.drawBitmap(bitmap, endX - bitmap.getWidth() / 2,
							graphheight + border - bitmap.getHeight() - 10
									- graphheight / 6, paint);
					paint.setStyle(Style.STROKE);
					canvas.drawCircle(endX, endY, dataCircleRadius, paint);
					canvas.drawLine(endX, endY, endX, graphheight + border
							- bitmap.getHeight() - 10, markPaint);
					BedBean bean = new BedBean();
					bean.setData(values[i]);
					bean.setHeartPause(true);
					bean.setX(endX);
					bean.setY(graphheight + border - bitmap.getHeight() / 2
							- 10);
					heartPoint.add(bean);
				}
			}

		if (apneaPauseList != null)
			for (GraphViewData d : apneaPauseList) {
				if (values[i].getX() == d.getX()) {
					Log.e(TAG, "d.getX()=" + d.getX());
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.huxi_4);
					canvas.drawBitmap(bitmap, endX - bitmap.getWidth() / 2,
							graphheight + border - bitmap.getHeight() - 10,
							paint);
					paint.setStyle(Style.STROKE);
					/*
					 * canvas.drawCircle(endX, endY, dataCircleRadius, paint);
					 * canvas.drawLine(endX, endY, endX, graphheight + border -
					 * bitmap.getHeight() - 10, markPaint);
					 */
					BedBean bean = new BedBean();
					bean.setData(values[i]);
					bean.setX(endX);
					bean.setHeartPause(false);
					bean.setY(graphheight + border - bitmap.getHeight() / 2
							- 10);
					heartPoint.add(bean);
				}
			}
	}

	/**
	 * 描述：得到最大值和 最小值
	 */
	private GraphViewData min = null, max = null;
	

	/**
	 * 描述:设置边界值
	 */
	public void setBorderData(GraphViewData min, GraphViewData max) {
		this.min = min;
		this.max = max;
		FontMetrics fm = paint.getFontMetrics();
		this.textHight = (float) Math.ceil(fm.descent - fm.ascent);
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewDataInterface[] values,
			float graphwidth, float graphheight, float border, double minX,
			double minY, double diffX, double diffY, float horstart,
			GraphViewSeries.GraphViewSeriesStyle style) {
		if (GraphISOval) {
			Ovalpath = new Path();
		}
		double lastValue = 0;
		// 把该心跳放入到
		if (heartPoint == null)
			heartPoint = new ArrayList<BedBean>();
		else
			heartPoint.clear();
		// draw background
		double lastEndY = 0;
		double lastEndX = 0;

		// draw data
		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);

//		LogUtil.showMsg(TAG+" drawSeries color:" + style.color);

		Path bgPath = null;
		// 描述 画背景图
		/*
		 * Path deep_inBG = null; Path in_lightBG = null; Path light_wakeBG =
		 * null;
		 */
		Path wakeBG = null;

		if (drawBackground) {
			bgPath = new Path();
		}

		lastEndY = 0;
		lastEndX = 0;
		float firstX = 0;
		float wakeMax = border + graphheight
				- (float) ((WAKEMAX - minY) / diffY * graphheight);
		float WeekPointY = border + graphheight
				- (float) ((WAKEPOINT - minY) / diffY * graphheight);
		// float lightPointY = border+graphheight-(float) ((LIGHTSEEPPOINT -
		// minY) / diffY * graphheight);
		// float inPontY = border+graphheight-(float) ((INSEEPPINT - minY) /
		// diffY * graphheight);
		float deepPontY = border + graphheight
				- (float) ((DEEPSLEEPPOINT - minY) / diffY * graphheight);
		if (beans == null && isMySelft)
			beans = getBedBeans();
		// if(beans!=null&&beans.size()>2&&beans.size()%2==0){
		// if(values[0].getY()>WAKEPOINT)
		// canDrawBG=true;
		// else
		// canDrawBG=false;
		// }else
		// canDrawBG=false;
		//
		// canDrawBG=false;
		LinearGradient shader = null;
		// 设置back
		for (int i = 0; i < values.length; i++) {
			/*
			 * if(i == 255) {//到了画清醒的值
			 * paint.setColor(Color.BLUE); }
			 */
			double valY = values[i].getY() - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;

			double valX = values[i].getX() - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

			float endX = 0, endY = 0;
			endX = (float) x + (horstart + 1);
			endY = (float) (border - y) + graphheight;

			if (i > 0) {
				float startX = (float) lastEndX + (horstart + 1);
				float startY = (float) (border - lastEndY) + graphheight;
				if (isMySelft)
					drawMark(canvas, graphwidth, graphheight, border, endX,
							endY, values[i], minX, diffX, horstart);
				if (shader == null) {
					shader = new LinearGradient(
							0,
							border + leaveBedDrawableHeight + 10,
							0,
							WeekPointY,
							new int[] { Color.TRANSPARENT,
									getResources().getColor(R.color.white_15) },
							new float[] { 0.4f, 0.8f }, Shader.TileMode.CLAMP);
					paintBackground.setShader(shader);
				}
				// 画醒来的背景图
				/*
				 * if(endY<=WeekPointY && isMySelft) { if(beans==null)
				 * beans=getBedBeans(); for (int j = 0; j < beans.size(); j++) {
				 * if(beans.get(j).getData().getX()==values[i].getX()){
				 * if(!beans.get(j).isWake()){//入睡的节点 if(wakeBG==null&&j!=0){
				 * wakeBG=new Path(); wakeBG.moveTo(0, border +
				 * leaveBedDrawableHeight + 10); wakeBG.lineTo(0, WeekPointY);
				 * // int x=getPoint(); wakeBG.lineTo(endX, WeekPointY);
				 * wakeBG.lineTo(endX, border + leaveBedDrawableHeight + 10);
				 * wakeBG.release(); canvas.drawPath(wakeBG, paintBackground);
				 * wakeBG=null;
				 * 
				 * }else if(wakeBG!=null&&j!=1){ wakeBG.lineTo(endX,
				 * WeekPointY); wakeBG.lineTo(endX, border +
				 * leaveBedDrawableHeight + 10); canvas.drawPath(wakeBG,
				 * paintBackground); wakeBG=null; } }else
				 * if(j!=beans.size()-1){//醒来的节点 wakeBG=null; wakeBG=new Path();
				 * wakeBG.moveTo(endX, border + leaveBedDrawableHeight + 10);
				 * wakeBG.lineTo(endX, WeekPointY); } } } }
				 */
				// draw data point
				if (drawDataPoints) {
					// fix: last value was not drawn. Draw here now the end
					// values
					canvas.drawCircle(endX, endY, dataPointsRadius, paint);
				}
				// 画心跳图
				if (isMySelft) {
					if (verlabels.length != 30) {
						// 当出 肺呼吸暂停的时候，
						drawPause(values, endX, canvas, graphheight, border,
								endY, i);
					} else {
						drawMuchPause(values, endX, canvas, graphheight,
								border, endY, i);
					}
				}
				if (GraphISOval) {
					if (i == 1) {
						Ovalpath.moveTo(startX, startY);
					}
					Ovalpath.quadTo(startX, startY, endX, endY);
				} else 
				{
					//判断 当前的值 是否无效
					if(values[i].getY() == invalid || lastValue == invalid)
					{
						paint.setColor(Color.TRANSPARENT);
					}else
					{
						paint.setColor(style.color);
					}
					lastValue = values[i].getY();
					canvas.drawLine(startX, startY, endX, endY, paint);
				}

				if (bgPath != null) {
					if (i == 1) {
						firstX = startX;
						bgPath.moveTo(startX, startY);
					}
					bgPath.lineTo(endX, endY);
					// }
				}
			} else if (drawDataPoints) {
				// fix: last value not drawn as datapoint. Draw first point
				// here, and then on every step the end values (above)
				float first_X = (float) x + (horstart + 1);
				float first_Y = (float) (border - y) + graphheight;
				canvas.drawCircle(first_X, first_Y, dataPointsRadius, paint);
			} else if (isMySelft) {// 画背景图 当 该点 是第一个点的时候
				float first_X = (float) x + (horstart + 1);
				float first_Y = (float) (border - y) + graphheight;
				// Log.e(VIEW_LOG_TAG,
				// "first_x="+first_X+"****first_Y"+first_Y);

			}
			lastEndY = y;
			lastEndX = x;

			// 画最大值和最小值

			if (min != null) {
				if (min.getX() == values[i].getX()) {
					drawBorderData(false, canvas, endX, endY, (int) min.getY(),
							(int) x, graphwidth, graphheight);
				}
			}

			if (max != null) {
				if (max.getX() == values[i].getX()&&isDrawYMax) {
					drawBorderData(true, canvas, endX, endY, (int) max.getY(), (int) x, graphwidth, graphheight);
				}
			}

		}

		if (wakeBG != null) {
			wakeBG.lineTo((float) lastEndX, WeekPointY);
			wakeBG.lineTo((float) lastEndX, border + leaveBedDrawableHeight
					+ 10);
			canvas.drawPath(wakeBG, paintBackground);
			wakeBG = null;
		}
		if (bgPath != null) {
			// end / release path
			if (isMySelft) {
				bgPath.lineTo((float) lastEndX, wakeMax);
				bgPath.lineTo(firstX, wakeMax);
				bgPath.close();
				int deep = getResources().getColor(R.color.detail_deep_bg);
				int light = getResources().getColor(R.color.detail_light_bg);
				int in = getResources().getColor(R.color.detail_in_bg);
				int wake = getResources().getColor(R.color.detail_wake_bg);
				LinearGradient shader1 = new LinearGradient(0, wakeMax, 0,
						deepPontY, new int[] { Color.TRANSPARENT, wake, light,
								light, in, in, deep, deep }, new float[] {
								0.05f, 0.20f, 0.30f, 0.45f, 0.55f, 0.70f,
								0.80f, 1.0f }, Shader.TileMode.CLAMP);
				paintBackground.setShader(shader1);

			} else {
				bgPath.lineTo((float) lastEndX, graphheight + border);
				bgPath.lineTo(firstX, graphheight + border);
				bgPath.close();
			}
			canvas.drawPath(bgPath, paintBackground);
		}
		if (GraphISOval) {
			/** 设置画笔抗锯齿 **/
			paint.setAntiAlias(true);
			/** 画笔的类型 **/
			paint.setStyle(Style.STROKE);
			/** 设置画笔变为圆滑状 **/
			paint.setStrokeCap(Paint.Cap.ROUND);
			canvas.drawPath(Ovalpath, paint);
		}

	}

	public int getBackgroundColor() {
		return paintBackground.getColor();
	}

	public float getDataPointsRadius() {
		return dataPointsRadius;
	}

	public boolean getDrawBackground() {
		return drawBackground;
	}

	public boolean getDrawDataPoints() {
		return drawDataPoints;
	}

	/**
	 * sets the background color for the series. This is not the background
	 * color of the whole graph.
	 * 
	 * @see #setDrawBackground(boolean)
	 */
	@Override
	public void setBackgroundColor(int color) {
		paintBackground.setColor(color);
	}

	/**
	 * sets the radius of the circles at the data points.
	 * 
	 * @see #setDrawDataPoints(boolean)
	 * @param dataPointsRadius
	 */
	public void setDataPointsRadius(float dataPointsRadius) {
		this.dataPointsRadius = dataPointsRadius;
	}

	/**
	 * @param drawBackground
	 *            true for a light blue background under the graph line
	 * @see #setBackgroundColor(int)
	 */
	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

	/**
	 * You can set the flag to let the GraphView draw circles at the data points
	 * 
	 * @see #setDataPointsRadius(float)
	 * @param drawDataPoints
	 */
	public void setDrawDataPoints(boolean drawDataPoints) {
		this.drawDataPoints = drawDataPoints;
	}

	public List<CvPoint> points;

	/**
	 * 描述：设置画曲线的时候的 关键点
	 * 
	 * @param points
	 */
	public void setMainPoint(List<CvPoint> points) {
		this.points = points;
	}

	private List<BedBean> LeaveBeds;

	/**
	 * 该方法与drawMark均是同步方法，防止绘图过程中修改数据集合LeaveBeds导致app奔溃
	 * @param beans
     */
	public synchronized void setBedBeans(List<BedBean> beans) {
		this.LeaveBeds = beans;
	}

	/**
	 * 描述：先获取 起床 和 入睡点
	 */
	private List<BedBean> getBedBeans() {

		return LeaveBeds;
	}

	public static class BedBean {

		public boolean isWake() {
			return wake;
		}

		public void setWake(boolean wake) {
			this.wake = wake;
		}

		private GraphViewDataInterface data;
		// true表示 醒来，false表示 入睡
		private boolean wake;
		private float x;
		private float y;

		/**
		 * 描述：true表示 心脏停止， false 表示呼吸停止
		 */
		private boolean isHeartPause;

		private int heartRate;

		private int apneaRate;

		private int status;

		/**
		 * 描述：开始睡眠，入睡点
		 */
		public static final int SLEEPIN = -101;

		/**
		 * 描述：醒来点，起床前的一个清醒点
		 */
		public static final int SLEEPUP = -102;

		/**
		 * 描述：睡眠中的，离床的 醒来的点
		 */
		public static final int WAKEUP = -103;

		/**
		 * 描述：睡眠中的，离床的入睡点
		 */
		public static final int WAKEIN = -104;

		private float statusValue;

		public boolean isHeartPause() {
			return isHeartPause;
		}

		public void setHeartPause(boolean isHeartPause) {
			this.isHeartPause = isHeartPause;
		}

		public GraphViewDataInterface getData() {
			return data;
		}

		public void setData(GraphViewDataInterface data) {
			this.data = data;
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
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
	}

}
