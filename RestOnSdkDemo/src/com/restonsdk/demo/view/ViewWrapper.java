package com.restonsdk.demo.view;

import android.view.View;

public class ViewWrapper 
{
	private View mTarget;
	
	public ViewWrapper(View mTarget)
	{
		this.mTarget = mTarget;
	}
	
	public void setHeight(int height)
	{
		mTarget.getLayoutParams().height = height;  
        mTarget.requestLayout(); 
	}
	
	public int getHeight()
	{
		return mTarget.getLayoutParams().height;
	}
}
