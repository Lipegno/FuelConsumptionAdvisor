/**
 * A custom TextView class that supports customized border drawing
 * User can set up color and width of the border through the setter methods
 */
package com.fuel.advisor.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class TextViewBorder extends	TextView	{

	private static final String MODULE = "TextViewBorder";
	
	private final Rect r = new Rect();
	private final Paint p = new Paint();
	
	private int borderColor = Color.GRAY;
	private Canvas canvas;
	
	/**
	 * Default constructors
	 * 
	 */
	public TextViewBorder(Context t) {
		
		super(t);
		initStyle();
	}
	public TextViewBorder(Context t, AttributeSet attrs)	{
		
		super(t, attrs);
		initStyle();
	}
	public TextViewBorder(Context t, AttributeSet attrs, int defStyle)	{
		
		super(t, attrs, defStyle);
		initStyle();
	}
	
	/**
	 * Call this on drawing the object
	 */
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
			p.setStyle(Paint.Style.STROKE);
			p.setColor(this.borderColor);
			p.setStrokeWidth(3);
			p.setFakeBoldText(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
