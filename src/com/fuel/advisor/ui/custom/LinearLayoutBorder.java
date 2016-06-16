package com.fuel.advisor.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class LinearLayoutBorder extends LinearLayout {

	private static final String MODULE = "BorderLinearLayout";
	
	private final Rect r = new Rect();
	private final Paint p = new Paint();
	
	public int borderColor = Color.WHITE;
	private Canvas canvas;
	
	public LinearLayoutBorder(Context context, AttributeSet attrs){
		super(context,attrs);
		initStyle();

	}
	
	public LinearLayoutBorder(Context context) {
		super(context);
		initStyle();

	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		try {
			super.onDraw(canvas);
			getLocalVisibleRect(r);
			canvas.drawRect(r, p);
		} catch (Exception e) {
			Log.e(MODULE, "Failed to draw object: " + e.getMessage());
		}
	}
	
	
	/**
	 * Modify the border color
	 * @param color
	 */
	public void setBorderColor(int color)	{
		
		try {
			this.borderColor = color;
			p.setColor(this.borderColor);
			this.redraw();
		} catch (Exception e) {
			Log.e(MODULE, "Failed to set border color: " + e.getMessage());
		}
	}
	
	/**
	 * Redraw object
	 */
	private void redraw()	{
		
		try {
			getLocalVisibleRect(r);
			this.canvas.drawRect(r, p);
		} catch (Exception e) {
			Log.e(MODULE, "Failed to redraw object: " + e.getMessage());
		}
	}
	
	/**
	 * Initialize the drawing style
	 */
	private void initStyle()	{
		
		try {
			setWillNotDraw(false);
			p.setStyle(Paint.Style.STROKE);
			p.setColor(this.borderColor);
			p.setStrokeWidth(6);
			p.setFakeBoldText(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
