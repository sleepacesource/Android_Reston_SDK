package com.restonsdk.demo.util;


import java.util.ArrayList;
import java.util.List;

import com.restonsdk.demo.bean.CvPoint;
import com.restonsdk.demo.view.graphview.LineGraphView;


public class Bazier {

	public Bazier() {}


	/**
	 * 描述：呼吸波，专用的算法工具
	 * 
	 * @param points
	 * @return
	 */
	public List<CvPoint> breathCurve(CvPoint[] points) {
		List<CvPoint> data = new ArrayList<CvPoint>();
		int kGranularity = 13;
		if (points != null) {
			// 增加两个控制点
			// 已经增加
			data.add(points[0]);
			for (int i = 1; i < points.length - 2; i++) {
				CvPoint p0 = points[i - 1];
				CvPoint p1 = points[i];
				CvPoint p2 = points[i + 1];
				CvPoint p3 = points[i + 2];

				for (int j = 1; j < kGranularity; j++) {
					float t = (float) j * (1.0f / (float) kGranularity);

					float tt = t * t;

					float ttt = tt * t;

					CvPoint pi = new CvPoint(0, 0); // intermediate point
					pi.x = (float) (0.5 * (2 * p1.x + (p2.x - p0.x) * t
							+ (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tt + (3
							* p1.x - p0.x - 3 * p2.x + p3.x)
							* ttt));
					pi.y = (float) (0.5 * (2 * p1.y + (p2.y - p0.y) * t
							+ (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tt + (3
							* p1.y - p0.y - 3 * p2.y + p3.y)
							* ttt));
					if (pi.x > data.get(data.size() - 1).x) {
						data.add(pi);
					}
				}
				if (p2.x > data.get(data.size() - 1).x)
					data.add(p2);

			}

			// finish by adding the last point

			// [allCurvePoints addObject:[NSValue
			// valueWithCGPoint:[points[points.count - 1] CGPointValue]]];
			// 删除先前增加的两个控制点

		}
		return data;
	}

	public List<CvPoint> createCurve1(CvPoint[] points) {
		List<CvPoint> data = new ArrayList<CvPoint>();
		int kGranularity = 15;
		if (points != null) {
			// 增加两个控制点
			// 已经增加
			data.add(points[0]);
			for (int i = 1; i < points.length - 2; i++) {
				CvPoint p0 = points[i - 1];
				CvPoint p1 = points[i];
				CvPoint p2 = points[i + 1];
				CvPoint p3 = points[i + 2];

				for (int j = 1; j < kGranularity; j++) {
					float t = (float) j * (1.0f / (float) kGranularity);

					float tt = t * t;

					float ttt = tt * t;

					CvPoint pi = new CvPoint(0, 0); // intermediate point
					pi.x = (float) (0.5 * (2 * p1.x + (p2.x - p0.x) * t
							+ (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tt + (3
							* p1.x - p0.x - 3 * p2.x + p3.x)
							* ttt));
					pi.y = (float) (0.5 * (2 * p1.y + (p2.y - p0.y) * t
							+ (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tt + (3
							* p1.y - p0.y - 3 * p2.y + p3.y)
							* ttt));
					if (p1.status == LineGraphView.BedBean.WAKEUP
							&& p2.status == LineGraphView.BedBean.WAKEIN)// 判断是否是离床或清醒的区间
					{
						pi.y = 0;
					}
					if (pi.x > data.get(data.size() - 1).x) {
						data.add(pi);
					}
				}
				if (p2.x > data.get(data.size() - 1).x)
					data.add(p2);

			}

			// finish by adding the last point

			// [allCurvePoints addObject:[NSValue
			// valueWithCGPoint:[points[points.count - 1] CGPointValue]]];
			// 删除先前增加的两个控制点

		}
		return data;
	}

	public List<CvPoint> createCurve(CvPoint[] originPoint, int originCount) {
		List<CvPoint> curvePoint = new ArrayList<CvPoint>();
		;
		// 控制点收缩系数 ，经调试0.6较好，CvPoint是opencv的，可自行定义结构体(x,y)
		float scale = 0.6f;
		CvPoint midpoints[] = new CvPoint[originCount];
		for (int i = 0; i < midpoints.length; i++) {
			midpoints[i] = new CvPoint(0, 0);
		}
		// 生成中点
		for (int i = 0; i < originCount - 1; i++) {
			int nexti = (i + 1) % originCount;
			float x = (float) ((originPoint[i].x + originPoint[nexti].x) / 2.0);
			float y = (float) ((originPoint[i].y + originPoint[nexti].y) / 2.0);
			midpoints[i] = new CvPoint(x, y);
		}

		// 平移中点
		CvPoint extrapoints[] = new CvPoint[2 * originCount];
		for (int i = 0; i < extrapoints.length; i++) {
			extrapoints[i] = new CvPoint(0, 0);
		}

		for (int i = 0; i < originCount; i++) {
			//int nexti = (i + 1) % originCount;
			int backi = (i + originCount - 1) % originCount;
			CvPoint midinmid = new CvPoint(0, 0);
			midinmid.x = (float) ((midpoints[i].x + midpoints[backi].x) / 2.0);
			midinmid.y = (float) ((midpoints[i].y + midpoints[backi].y) / 2.0);
			int offsetx = (int) (originPoint[i].x - midinmid.x);
			int offsety = (int) (originPoint[i].y - midinmid.y);
			int extraindex = 2 * i;
			extrapoints[extraindex].x = midpoints[backi].x + offsetx;
			extrapoints[extraindex].y = midpoints[backi].y + offsety;
			// 朝 originPoint[i]方向收缩
			int addx = (int) ((extrapoints[extraindex].x - originPoint[i].x) * scale);
			int addy = (int) ((extrapoints[extraindex].y - originPoint[i].y) * scale);
			extrapoints[extraindex].x = originPoint[i].x + addx;
			extrapoints[extraindex].y = originPoint[i].y + addy;

			int extranexti = (extraindex + 1) % (2 * originCount);
			extrapoints[extranexti].x = midpoints[i].x + offsetx;
			extrapoints[extranexti].y = midpoints[i].y + offsety;
			// 朝 originPoint[i]方向收缩
			addx = (int) ((extrapoints[extranexti].x - originPoint[i].x) * scale);
			addy = (int) ((extrapoints[extranexti].y - originPoint[i].y) * scale);
			extrapoints[extranexti].x = originPoint[i].x + addx;
			extrapoints[extranexti].y = originPoint[i].y + addy;

		}

		CvPoint controlPoint[] = new CvPoint[4];
		// 生成4控制点，产生贝塞尔曲线
		for (int i = 0; i < originCount; i++) {
			controlPoint[0] = originPoint[i];
			int extraindex = 2 * i;
			controlPoint[1] = extrapoints[extraindex + 1];
			int extranexti = (extraindex + 2) % (2 * originCount);
			controlPoint[2] = extrapoints[extranexti];
			int nexti = (i + 1) % originCount;
			controlPoint[3] = originPoint[nexti];
			float u = 1;
			while (u >= 0) {
				int px = (int) bezier3funcX(u, controlPoint);
				int py = (int) bezier3funcY(u, controlPoint);
				// u的步长决定曲线的疏密
				u -= 0.1;
				CvPoint tempP = new CvPoint(px, py);
				// 存入曲线点
				curvePoint.add(tempP);
			}
		}

		return curvePoint;
	}

	// 三次贝塞尔曲线
	public float bezier3funcX(float uu, CvPoint[] controlP) {
		float part0 = controlP[0].x * uu * uu * uu;
		float part1 = 3 * controlP[1].x * uu * uu * (1 - uu);
		float part2 = 3 * controlP[2].x * uu * (1 - uu) * (1 - uu);
		float part3 = controlP[3].x * (1 - uu) * (1 - uu) * (1 - uu);
		return part0 + part1 + part2 + part3;

	}

	float bezier3funcY(float uu, CvPoint[] controlP) {
		float part0 = controlP[0].y * uu * uu * uu;
		float part1 = 3 * controlP[1].y * uu * uu * (1 - uu);
		float part2 = 3 * controlP[2].y * uu * (1 - uu) * (1 - uu);
		float part3 = controlP[3].y * (1 - uu) * (1 - uu) * (1 - uu);
		return part0 + part1 + part2 + part3;
	}

}
