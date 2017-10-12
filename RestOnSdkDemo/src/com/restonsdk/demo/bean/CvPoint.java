package com.restonsdk.demo.bean;

public class CvPoint {
    public float x;
    public float y;
    public int status;// 状态，5表示离床
    public int statusValues;

    public CvPoint(float x, float y, int status, int values) {
        this.x = x;
        this.y = y;
        this.status = status;
        this.statusValues = values;
    }

    public CvPoint(float x, float y) {
        this(x, y, 0, 0);
    }
}