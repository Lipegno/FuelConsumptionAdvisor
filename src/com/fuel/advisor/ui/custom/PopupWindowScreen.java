package com.fuel.advisor.ui.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

/**
 * An abstract class responsible for bringing up a new PopupWindow into the screen. This class
 * sets up all the necessary measurements for the PopupWindow object and is responsible for inflating
 * the Layout to be used by the PopupWindow. Sub-classes need to implement concrete listeners for
 * widgets present in the Layout, by overriding the abstract {@link PopupWindowScreen#setButtonsClickListeners()} method
 * <br><br>
 * 
 * @author Tiago Camacho
 */
public abstract class PopupWindowScreen {
	
	@SuppressWarnings("unused")
	private static final String MODULE = "PopupWindowScreen";
	private static final int POPUP_WINDOW_WIDHT = 440;	//px
	private static final int POPUP_WINDOW_HEIGHT = LayoutParams.WRAP_CONTENT;
	private static final int ALPHA_VALUE_CENTER_VIEW = 150;
	
	private final View view;
	private final ViewGroup root;
	private final int popupLayoutID;
	private final int centerLayoutID;
	
	private View popupView;
	private PopupWindow pw;
	
	/**
	 * The default constructor. It receives the view object that has been pressed (usually a LinearLayout), as
	 * well as the yet to be inflated layout ID for the PopupWindow, as well as an optional center view layout ID
	 * to perform dimming effect upon PopupWindow inflation
	 * @param view - The View object that has been pressed
	 * @param popupLayoutID - The required layout ID to use to inflate the PopupWindow
	 * @param centerLayoutID - The optional center layout ID to perform dimming upon PopupWindow creation
	 */
	public PopupWindowScreen(View view, int popupLayoutID, int centerLayoutID) {

		this.view = view;
		this.popupLayoutID = popupLayoutID;
		this.centerLayoutID = centerLayoutID;
		this.root = (ViewGroup)view.getRootView();
		initPopupWindow();
	}
	
	/**
	 * Retrieves a widget associated with the PopupWindow
	 * @param widgetID - The widget's ID
	 * @return - The View object that represents the widget
	 */
	protected View findPopupWidget(int widgetID)	{
		
		View v = null;
		
		try {
			v = popupView.findViewById(widgetID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return v;
	}
	
	/**
	 * Get the ViewGroup (e.g. root view) associated with this PopupWindow
	 * @return - The ViewGroup object that represents the root view
	 */
	protected ViewGroup getRootView()	{
		
		return this.root;
	}
	
	/**
	 * This method cancels the current operation being executed in the context of this PopupWindow.
	 * Calling this dismisses the PopupWindow object, returning to the previous screen
	 */
	protected void dismissPopupWindow()	{
		
		try {
			View cView = root.findViewById(centerLayoutID);
			if (cView != null)	{
				Drawable d = cView.getBackground();
				d.setAlpha(255);
				cView.setBackgroundDrawable(d);
				cView.invalidate();
			}
			pw.showAsDropDown(root);
			pw.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the application's current context
	 * @return - The current Context
	 */
	protected Context getApplicationContext()	{
		
		return root.getContext();
	}
	
	/**
	 * Abstract method that sub-classes use to set up the click listeners for the buttons
	 */
	protected abstract void setButtonsClickListeners();
	
	/**
	 * Initializes the popup window screen
	 */
	private void initPopupWindow()	{
		
		try {
			// We first need to inflate the View and create the actual PopupWindow object
			LayoutInflater inflater = (LayoutInflater)
				view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			pw = new PopupWindow(
					inflater.inflate(popupLayoutID, null, false),
					POPUP_WINDOW_WIDHT,
					POPUP_WINDOW_HEIGHT,
					true);
			popupView = pw.getContentView();
			pw.showAtLocation(root, Gravity.CENTER, 0, 0);
			pw.setTouchable(true);
			pw.setOutsideTouchable(false);
			
			// If center layout is defined, reduce alpha of the view so that a dimming
			// effect is achieved on the background when we the PopupWindow comes up
			View cView = root.findViewById(centerLayoutID);
			if (cView != null)	{
				Drawable d = cView.getBackground();
				d.setAlpha(ALPHA_VALUE_CENTER_VIEW);
				cView.setBackgroundDrawable(d);
				cView.invalidate();
			}
			
			// Calls the abstract method to set up the listeners
			setButtonsClickListeners();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}	