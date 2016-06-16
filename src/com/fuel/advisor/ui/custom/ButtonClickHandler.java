package com.fuel.advisor.ui.custom;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * This class implements the Template Method pattern for allowing
 * sub-classes to override the handle() method so that specific
 * click behavior is set up accordingly to the Activity. Sub-classes
 * must call the super constructor with the Activity and an array of
 * IDs of the View objects. The animation of the selectable icons is made by this class.
 * 
 * @author Tiago Camacho
 */
public abstract class ButtonClickHandler {

	// Set the rounded "value" for the square shape
	private static final float OUTER_SQUARE_RADIUS = 9.0F;
	
	// Colors to show on DOWN and UP events
	private static final int ON_SELECTION_COLOR = Color.argb(160, 255, 150, 0);	// Orange
	
	// The Activity object
	private final Activity a;
	
	// The current background background of the layout
	private Drawable background;
	
	/**
	 * Default constructor
	 * @param a - The Activity which holds the objects
	 * @param view_ids - The array of object IDs to listen for "clicks"
	 */
	public ButtonClickHandler(Activity a, int[] view_ids) {
		
		this.a = a;
		initHandler(view_ids);
	}

	/**
	 * Get the Activity object
	 * @return - The Activity object
	 */
	protected Activity getActivity()	{
	
		return this.a;
	}
	
	/**
	 * Animates the item upon selection. Override for further personalized behavior
	 * @param v - The ViewGroup object that has been selected (usually a LinearLayout)
	 */
	protected void onItemSelection(ViewGroup v)	{

		// Save background
		this.background = v.getBackground();
		
		// Build the ShapeDrawble object
		ShapeDrawable sd = new ShapeDrawable(
					new RoundRectShape(new float[]{
							OUTER_SQUARE_RADIUS,
							OUTER_SQUARE_RADIUS,
							OUTER_SQUARE_RADIUS,
							OUTER_SQUARE_RADIUS,
							OUTER_SQUARE_RADIUS,
							OUTER_SQUARE_RADIUS, 
							OUTER_SQUARE_RADIUS,
							OUTER_SQUARE_RADIUS }, null, null));

		// Iterate over the View child objects and apply the
		// specified style
		sd.getPaint().setColor(ON_SELECTION_COLOR);
		v.setBackgroundDrawable(sd);
		v.invalidate();
	}
	
	/**
	 * Called after the user releases the item. Override for further personalized behavior
	 * @param v - The ViewGroup object that has been released (usually a LinearLayout)
	 */
	protected void onItemRelease(ViewGroup v)	{
		
		v.setBackgroundDrawable(background);
	}
	
	/**
	 * Override for implementing click specific behavior. Sub-classes must implement this method
	 * The method is called after execution of {@link ButtonClickHandler#onItemRelease(ViewGroup)}
	 * @param v - The View object that has been touched
	 */
	protected abstract void handle(View v); 
	
	/**
	 * Initialize the touch listeners and set up general algorithm (template method)
	 * @param view_ids
	 */
	private void initHandler(int[] view_ids)	{
		
		View v = null;
		
		try {
			for (int i : view_ids) {
				if ((v = a.findViewById(i)) == null)
					continue;
				v.setOnTouchListener(new View.OnTouchListener() {
					
					ViewGroup vg = null;
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						
						// Ensure the downcast can be performed. If not
						// just return false
						if (v instanceof ViewGroup)
							vg = (ViewGroup) v;
						else
							return false;
						
						// Determine action to perform
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							onItemSelection(vg);
							break;
						case MotionEvent.ACTION_UP:
							onItemRelease(vg);
							// Call the abstract method
							handle(v);
							break;
						default:
							break;
						}
						
						return true;
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}