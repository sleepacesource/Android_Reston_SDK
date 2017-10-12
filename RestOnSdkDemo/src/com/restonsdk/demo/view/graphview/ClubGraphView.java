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

import java.util.List;

import com.restonsdk.demo.R;
import com.restonsdk.demo.util.DensityUtil;
import com.restonsdk.demo.view.graphview.interfs.GraphViewDataInterface;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

/**
 * Line Graph View. This draws a line chart.
 */
public class ClubGraphView extends GraphView {
	private final Paint paintBackground;
	private Paint myPaint, cirPaint;
	private boolean drawBackground;
	private boolean drawDataPoints;
	private float dataPointsRadius = 10f;
	private float clubWidth = 10f;
	private Resources res;
	private int graphViewIndex;

	public ClubGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
		shadeWidth = DensityUtil.dip2px(context, shadeWidth);
		res = getResources();
	}

	public ClubGraphView(Context context, String title, int graphViewIndex) {
		super(context, title);
		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
		paintBackground.setAlpha(128);
		shadeWidth = DensityUtil.dip2px(context, shadeWidth);
		clubWidth = DensityUtil.dip2px(context, clubWidth);
		res = getResources();

		myPaint = new Paint();
		myPaint.setAntiAlias(true);
		myPaint.setDither(true);
		myPaint.setStyle(Paint.Style.STROKE);
		// myPaint.setStrokeJoin(Paint.Join.ROUND);
		myPaint.setStrokeCap(Paint.Cap.ROUND);

		HorizontalLableShowTop = true;
		this.testVLabel = "24pm";
		this.vLabel2Time = true;
		this.graphViewIndex = graphViewIndex;
	}

	private int getColor(int level) {
		int color = 0;
		switch (level) {
		case 3:
			color = res.getColor(R.color.detail_deep);
			break;
		case 2:
			color = res.getColor(R.color.detail_in);
			break;
		case 1:
			color = res.getColor(R.color.detail_light);
			break;
		case 0:
			color = res.getColor(R.color.detail_wake);
			break;
		default:
			color = res.getColor(R.color.wake);
			break;
		}
		return color;
	}

	private void setShader(int lastColor, int color, float x, float y1, float y2) {
		LinearGradient lg = new LinearGradient(x, y1, x, y2, new int[] {
				lastColor, color }, null, Shader.TileMode.CLAMP);
		myPaint.setShader(lg);
	}

	private int shadeWidth = 10;// 渐变的宽度

	/**
	 * 描述：画棒棒
	 */
	public void drawClub(Canvas canvas, GraphViewDataInterface[] values,
			int scale, float graphwidth, float graphheight, float border,
			double minX, double minY, double diffX, double diffY, float horstart) {
		if (values == null)
			return;
		double valX = values[0].getX() - minX;
		double ratX = valX / diffX;
		double x = graphwidth * ratX;
		float endX = (float) x + (horstart + 1);
		double lastEndY = 0;
		double lastEndX = 0;
		int lastColor = 0;
		int nowColor = 0;
		// 得到最大值,为了 让他 从中间开始 变化
		float mendY = 0;
		float lastGraph = graphheight;
		graphheight = graphheight * scale / animCount;
		border += (lastGraph - graphheight) / 2;
		myPaint.setStrokeWidth(clubWidth);
		for (int i = 0; i < values.length; i++) {
			double valY = values[i].getY() - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;
			if (i > 0) {
				float endY = (float) (border - y) + graphheight;
				float startY = (float) (border - lastEndY) + graphheight;
				if (i == 1) {
					lastColor = getColor(values[0].getLevel());
					nowColor = getColor(values[1].getLevel());
					setShader(lastColor, lastColor, endX, endY, startY);
					// canvas.drawCircle(endX, startY, clubWidth/2,paint);
					canvas.drawLine(endX, startY, endX, endY + shadeWidth / 2,
							myPaint);
				} else if (i == values.length - 1) {
					nowColor = getColor(values[i].getLevel());
					setShader(lastColor, nowColor, endX, startY + shadeWidth
							/ 2, startY - shadeWidth / 2);
					// canvas.drawCircle(endX, endY+clubWidth/2,
					// clubWidth/2,paint);
					canvas.drawLine(endX, startY + shadeWidth / 2, endX, endY
							+ clubWidth / 2, myPaint);
				} else {
					if (startY - endY < shadeWidth)
						continue;
					nowColor = getColor(values[i].getLevel());
					setShader(lastColor, nowColor, endX, startY + shadeWidth
							/ 2, startY - shadeWidth / 2);
					canvas.drawLine(endX, startY + shadeWidth / 2, endX, endY
							+ shadeWidth / 2, myPaint);
					lastColor = nowColor;
				}
				lastEndY = y;
			} else {
				lastEndY = y;
			}
		}

	}

	private List<GraphViewData[]> list;

	public void setData(List<GraphViewData[]> list2) {
		this.list = list2;
	}

	private int animCount = 15;
	private int animCount1 = 15 * 6 / 10;

	@Override
	public void drawSeries(Canvas canvas, GraphViewDataInterface[] values,
			float graphwidth, float graphheight, float border, double minX,
			double minY, double diffX, double diffY, float horstart,
			GraphViewSeries.GraphViewSeriesStyle style) {
		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);
		if (list != null && anima) {
			for (int i = 0; i < index1 && i < list.size(); i++) {
				if (list.get(i) != null && list.get(i).length > 0)
					drawClub(canvas, list.get(i), animCount, graphwidth,
							graphheight, border, minX, minY, diffX, diffY,
							horstart);
			}

			if (index1 < list.size()) {
				if (count1 >= animCount) {
					if (list.get(index1) != null && list.get(index1).length > 0)
						drawClub(canvas, list.get(index1), animCount,
								graphwidth, graphheight, border, minX, minY,
								diffX, diffY, horstart);
				} else {
					if (list.get(index1) != null && list.get(index1).length > 0)
						drawClub(canvas, list.get(index1), count1, graphwidth,
								graphheight, border, minX, minY, diffX, diffY,
								horstart);
				}
			}

			if (count2 > 0 && index2 < list.size()) {
				if (count2 >= animCount) {
					if (list.get(index2) != null && list.get(index2).length > 0)
						drawClub(canvas, list.get(index2), animCount,
								graphwidth, graphheight, border, minX, minY,
								diffX, diffY, horstart);
				} else {
					if (list.get(index2) != null && list.get(index2).length > 0)
						drawClub(canvas, list.get(index2), count2, graphwidth,
								graphheight, border, minX, minY, diffX, diffY,
								horstart);
				}
			}

			/*
			 * for (int i = index; i <list.size(); i++) { if(anima) { if (index
			 * < list.size()) { if(count1 >= animCount) {
			 * if(list.get(index)!=null && list.get(index).length >0)
			 * drawClub(canvas,list.get(index),animCount,graphwidth,
			 * graphheight, border, minX, minY, diffX, diffY, horstart); }else {
			 * if(list.get(index)!=null && list.get(index).length >0)
			 * drawClub(canvas,list.get(index),count1,graphwidth, graphheight,
			 * border, minX, minY, diffX, diffY, horstart); } }
			 * 
			 * if (index+1 < list.size()) { if(count2 >= animCount) {
			 * if(list.get(index)!=null && list.get(index).length >0)
			 * drawClub(canvas,list.get(index),animCount,graphwidth,
			 * graphheight, border, minX, minY, diffX, diffY, horstart); }else {
			 * if(list.get(index)!=null && list.get(index).length >0)
			 * drawClub(canvas,list.get(index),count1,graphwidth, graphheight,
			 * border, minX, minY, diffX, diffY, horstart); } } } else {
			 * if(list.get(i)!=null && list.get(i).length >0)
			 * drawClub(canvas,list.get(i),animCount,graphwidth, graphheight,
			 * border, minX, minY, diffX, diffY, horstart); } }
			 */
			if (animListener != null && stopAnima)
				animListener.onOver(true, graphViewIndex);
		} else {
			stopAnima = true;
			if (animListener != null)
				animListener.onOver(true, graphViewIndex);
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

	private Handler clubHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
//				Log.e(VIEW_LOG_TAG, "clubHanlder: " + index1 + "," + index2 + "," + count1 + "," + count2);
				count1++;
				if (count1 >= animCount1) {
					count2++;
				} else {
					count2 = 0;
				}

				ClubGraphView.this.graphViewContentView.invalidate();
				if (count1 <= animCount) {
					this.sendEmptyMessageDelayed(1, 10);
				} else if (count1 == animCount + 1) {
					count1 = count2;
					count2 = 0;
					index1 = index2;
					if (index1 < list.size()) {
						this.sendEmptyMessageDelayed(1, 10);
						index2 = index1 + 1;
						while (index2 < list.size() && list.get(index2) == null)
							index2++;
					} else {
						if (animListener != null)
							animListener.onOver(true, graphViewIndex);
					}
				}
				break;

			default:
				break;
			}
		}

	};

	private boolean stopAnima = false;
	private int count1 = 10, count2 = 10;
	private int index1 = 0, index2 = 0;

	private boolean anima = false;

	public void setIsAnima(boolean anima) {
		this.anima = anima;
	}

	public void startAnima() {
		if (!anima) {
			count1 = count2 = animCount;
			index1 = index2 = list.size();
			if (animListener != null) {
				animListener.onStart(graphViewIndex);
				animListener.onOver(false, graphViewIndex);
			}
		} else {
			clubHandler.sendEmptyMessageDelayed(1, 50);
			count1 = count2 = index1 = index2 = 0;
			if (animListener != null)
				animListener.onStart(graphViewIndex);

			while (index1 < list.size() && list.get(index1) == null)
				index1++;

			index2 = index1 + 1;
			while (index2 < list.size() && list.get(index2) == null)
				index2++;

			if (index1 >= list.size()) {
				if (animListener != null) {
					animListener.onStart(graphViewIndex);
					animListener.onOver(false, graphViewIndex);
				}
			}
		}
	}

	/**
	 * 描述：动画的监听事件
	 */
	private OnClubAnimListener animListener;

	public void setOnAnimListener(OnClubAnimListener animListener) {
		this.animListener = animListener;
	}

	public static interface OnClubAnimListener {
		public void onStart(int index);

		public void onOver(boolean isAnima, int index);
	}
}
