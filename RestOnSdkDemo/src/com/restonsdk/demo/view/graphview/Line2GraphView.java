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
import com.restonsdk.demo.util.DensityUtil;
import com.restonsdk.demo.view.graphview.interfs.GraphViewDataInterface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

/**
 * Line Graph View. This draws a line chart.
 */
public class Line2GraphView extends GraphView {
	private static final String TAG = Line2GraphView.class.getSimpleName();
	private final Paint paintBackground;
	private boolean drawBackground;
	private boolean drawDataPoints;
	private boolean drawText = true;
	private float dataPointsRadius = 5f;
	private String avgBelowTXT = "MAN AGE 56";

	private List<GraphViewData> pointDataList;

	private int graphViewIndex;

	/**
	 * 描述：画数据圆点的Point
	 */
	public Paint dataPointP;

	/**
	 * 描述：设置Text
	 */
	public void setAvgBelowTxt(String text) {
		this.avgBelowTXT = text;
	}

	public Line2GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		paintBackground = new Paint();
		paintBackground.setColor(getResources().getColor(R.color.white_15));
		paintBackground.setStrokeWidth(4);
		// paintBackground.setAlpha(128);
		HorizontalLableShowTop = true;
	}

	public Line2GraphView(Context context, String title, int index) {
		super(context, title);
		this.graphViewIndex = index;
		paintBackground = new Paint();
		paintBackground.setColor(getResources().getColor(R.color.white_15));
		paintBackground.setStrokeWidth(4);
		// paintBackground.setAlpha(128);
		HorizontalLableShowTop = true;
		paint.setStyle(Paint.Style.STROKE);

		dataPointP = new Paint();
		dataPointP.setColor(Color.parseColor("#CA8260"));
		pointDataList = new ArrayList<GraphViewData>();

	}

	private int animIndex = 100;
	private boolean animAble = false;
	private final  int animCount = 8;
	
	private int borderInt = -18;

	public void setAnimaAble(boolean animAble) {
		this.animAble = animAble;
	}

	public void startAnim(int animCount) 
	{
	
		if (!animAble)
			return;
		handler.sendEmptyMessageDelayed(1, 100);
		/*if(animCount > 8)
			animCount = -((animCount -8)*2);*/
		animIndex =  8;
		borderInt = -(animCount + 2);
		if (animListener != null)
			animListener.onStart(graphViewIndex);
	}

	/**
	 * 描述：启动动画
	 */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				animIndex--;
				if (animIndex > borderInt) {
					Line2GraphView.this.graphViewContentView.invalidate();
					this.sendEmptyMessageDelayed(1, 50);
				} else {
					if (animListener != null)
						animListener.onOver(true, graphViewIndex);
				}
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 描述：画点， 原因：由于画点 要盖住线
	 * 
	 * @param canvas
	 */
	public void drawDataPoint(Canvas canvas) {
		final int pointCount = pointDataList == null ? 0 : pointDataList.size();
//		SleepLog.e(this.getClass(),(TAG +" drawDataPoint pointCount:"+pointCount) + "," + animIndex);
		for (int i = 0; i < pointCount; i++) 
		{
			GraphViewData data = pointDataList.get(i);
			if (i < -animIndex && animAble)
			{
				canvas.drawCircle((float) data.valueX, (float) data.valueY,
						dataPointsRadius, dataPointP);
			} else if (!animAble) 
			{
				canvas.drawCircle((float) data.valueX, (float) data.valueY,
						dataPointsRadius, dataPointP);
			}
		}
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewDataInterface[] values,
			float graphwidth, float graphheight, float border, double minX,
			double minY, double diffX, double diffY, float horstart,
			GraphViewSeries.GraphViewSeriesStyle style) {
		if (animAble && animIndex == 100) {
			return;
		}

		// draw background
		double lastEndY = 0;
		double lastEndX = 0;

		// 用来画背景的渐变用的
		double maxYH = 0;
		double minYH = 0;

		Paint textPaint = new Paint();
		textPaint.setColor(getResources().getColor(R.color.white));
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize((float) (getGraphViewStyle().getTextSize() * 1.2));

		// draw data
		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);

		Path bgPath = null;
		if (drawBackground) {
			bgPath = new Path();
		}

		float all = 0;
		lastEndY = 0;
		lastEndX = 0;
		float firstX = 0;
		float firstY = 0;
		pointDataList.clear();
		for (int i = 0; i < values.length; i++) {
			all += values[i].getY();
			double valY = values[i].getY() - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;

			double valX = values[i].getX() - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

			if (i > 0) {
				float startX = (float) lastEndX + (horstart + 1);
				float startY;
				if (animAble && animIndex >= 0)
					startY = (float) (border - lastEndY) + graphheight
							+ graphheight * animIndex / animCount;
				else
					startY = (float) (border - lastEndY) + graphheight;
				float endX = (float) x + (horstart + 1);
				float endY;
				if (animAble && animIndex >= 0)
					endY = (float) (border - y) + graphheight + graphheight
							* animIndex / animCount;
				else
					endY = (float) (border - y) + graphheight;
				// draw data point
				if (drawDataPoints) {
					canvas.drawLine(startX + dataPointsRadius / 2, startY, endX, endY, paint);
					pointDataList.add(new GraphViewData(endX, endY));
					// //fix: last value was not drawn. Draw here now the end
					// values
					if (i < -animIndex && animAble) {
						if(drawText)
						canvas.drawText((int) values[i].getY() + "", endX, endY
								- DensityUtil.dip2px(getContext(), 10),
								textPaint);
						// canvas.drawCircle(endX, endY, dataPointsRadius,
						// dataPointP);
					} else if (!animAble) {
						pointDataList.add(new GraphViewData(endX, endY));
						if(drawText)
						canvas.drawText((int) values[i].getY() + "", endX, endY
								- DensityUtil.dip2px(getContext(), 10),
								textPaint);
						// canvas.drawCircle(endX, endY, dataPointsRadius,
						// dataPointP);
					}
				}

				if (bgPath != null) {
					if (i == 1) {
						firstX = startX;
						firstY = startY;
						bgPath.moveTo(startX, startY);
						minYH = firstY;
						maxYH = firstY;
					}
					if (minYH > endY)
						minYH = endY;
					if (maxYH < endY)
						maxYH = endY;
					bgPath.lineTo(endX, endY);
				}
			} else if (drawDataPoints) {
				// fix: last value not drawn as datapoint. Draw first point
				// here, and then on every step the end values (above)
				float first_X = (float) x + (horstart + 1);
				float first_Y = (float) (border - y) + graphheight;
				pointDataList.add(new GraphViewData(first_X, first_Y));
				if (i < -animIndex && animAble) {
					pointDataList.add(new GraphViewData(first_X, first_Y));
					// canvas.drawCircle(first_X, first_Y, dataPointsRadius,
					// dataPointP);
					if(drawText)
					canvas.drawText((int) values[i].getY() + "", first_X,
							first_Y - DensityUtil.dip2px(getContext(), 10),
							textPaint);
				} else if (!animAble) {
					pointDataList.add(new GraphViewData(first_X, first_Y));
					// canvas.drawCircle(first_X, first_Y, dataPointsRadius,
					// dataPointP);
					if(drawText)
					canvas.drawText((int) values[i].getY() + "", first_X,
							first_Y - DensityUtil.dip2px(getContext(), 10),
							textPaint);
				}
			}
			lastEndY = y;
			lastEndX = x;
		}

		drawDataPoint(canvas);

		double valY = all / values.length - minY;
		double ratY = valY / diffY;
		double y = graphheight * ratY;
		float endY = (float) (border - y) + graphheight;
		paint.setColor(Color.parseColor("#0D7389"));
		textPaint.setTextAlign(Align.RIGHT);
		textPaint.setColor(Color.parseColor("#0D7389"));
		if (animAble && animIndex < -7) {
			int a = 17 + animIndex >= 0 ? 17 + animIndex : 0;
			endY = endY + graphheight * a / 10;
			// canvas.drawLine(0, endY, graphwidth, endY, paint);
			// canvas.drawText("AVERAGE",graphwidth,
			// endY+DensityUtil.dip2px(getContext(), 20), textPaint);
			// canvas.drawText(avgBelowTXT,graphwidth,
			// endY+DensityUtil.dip2px(getContext(), 40), textPaint);
		} else {

		}
		if (bgPath != null) {
			float y_end = (float) (maxYH + (maxYH - minYH) / 2);
			if (y_end > graphheight) {
				y_end = graphheight;
			}
			// end / release path
			bgPath.lineTo((float) lastEndX, y_end);
			bgPath.lineTo(firstX, y_end);
			bgPath.lineTo(firstX, firstY);
			bgPath.close();
			LinearGradient lg = new LinearGradient(0, (int) minYH, 0, y_end,
					new int[] { Color.WHITE, Color.TRANSPARENT }, null,
					Shader.TileMode.CLAMP);
			paintBackground.setShader(lg);
			canvas.drawPath(bgPath, paintBackground);
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
	
	public void setDrawText(boolean drawText){
		this.drawText = drawText;
	}

	private ClubGraphView.OnClubAnimListener animListener;

	public void setOnAnimListener(ClubGraphView.OnClubAnimListener animListener) {
		this.animListener = animListener;
	}

}
