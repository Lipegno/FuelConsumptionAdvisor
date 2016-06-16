package com.fuel.advisor.ui.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuel.advisor.R;

/**
 * A class that provides an interface for building and showing custom Dialog objects. It
 * implements a parameterized Factory Method so that clients can instantiate the object
 * passing as an argument the type of dialog through {@link CustomDialog#createDialog(Context, DialogType)}.
 * Showing the dialog is achieved through the {@link CustomDialog#show(String, String)}.
 * 
 * @author Tiago Camacho
 */
public class CustomDialog {

	// Defines the allowed custom dialog types
	public static enum DialogType { ERROR, SUCCESS, INFO, OPTION }
	
	@SuppressWarnings("unused")
	private static final String MODULE = "CustomDialog";
	
	private static CustomDialog cd = new CustomDialog();
	private static Dialog d;
	private static OnCancelationListener cancelationListener;
	private static OnConfirmationListener confirmationListener;
	
	private CustomDialog() {}
	
	/**
	 * Show a dialog with a given title and message
	 * @param title - The optional title to show in the dialog
	 * @param message - The message to show in the dialog
	 */
	public void show(String title, String message)	{
		
		try {
			if (title != null)
				d.setTitle(title);
			TextView tv = (TextView)d.findViewById(R.id.custom_dialog_text);
			tv.setText(message);
			tv.invalidate();
			d.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the listener for cancelation
	 * @param listener
	 */
	public void setOnCancelationListener(OnCancelationListener listener)	{
		
		cancelationListener = listener;
	}
	
	/**
	 * Sets the listener for confirmation
	 * @param listener
	 */
	public void setOnConfirmationListener(OnConfirmationListener listener)	{
		
		confirmationListener = listener;
	}
	
	/**
	 * Factory Method that creates a new CustomDialog object
	 * @see {@link DialogType}
	 * @param ctx - The context to show this dialog in
	 * @param type - The type of dialog to create
	 * @return - The newly created CustomDialog object
	 */
	public static CustomDialog createDialog(Context ctx, DialogType type)	{
	
		try {
			// First create the Dialog object
			initDialog(ctx);
			setDismissButtonListener();
			
			// Determine concrete settings according to type
			// of dialog to create. Return null pointer if no
			// known value for type has been received
			switch (type) {
			case ERROR:
				removeCancelButton();
				setImage(R.drawable.error);
				break;
			case SUCCESS:
				removeCancelButton();
				setImage(R.drawable.ok);
				break;
			case INFO:
				removeCancelButton();
				setImage(R.drawable.info);
				break;
			case OPTION:
				setImage(R.drawable.question_mark);
				break;
			default:
				cd = null;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cd;
	}
	
	/**
	 * Initializes the dialog under a given context
	 * @param - The current Context
	 */
	private static void initDialog(Context ctx) throws Exception	{
		
		d = new Dialog(ctx);
		d.setContentView(R.layout.custom_dialog);
	}
	
	/**
	 * Changes the dialog image to present 
	 * @param drawableID - The ID for the drawable resource
	 */
	private static void setImage(int drawableID) throws Exception	{
		
		ImageView iv = (ImageView)d.findViewById(R.id.custom_dialog_image);
		iv.setImageResource(drawableID);
		iv.invalidate();
	}
	
	/**
	 * Sets up the dismiss button click listener so that the dialog
	 * is dismissed upon click
	 */
	private static void setDismissButtonListener() throws Exception	{
		
		Button b = (Button)d.findViewById(R.id.custom_dialog_ok_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				if (confirmationListener != null)
					confirmationListener.onConfirmation();
			}
		});
		b = (Button)d.findViewById(R.id.custom_dialog_cancel_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
				if (cancelationListener != null)
					cancelationListener.onCancelation();
			}
		});
	}

	/**
	 * Removes cancelation button and re-arranges the OK button
	 * @throws Exception
	 */
	private static void removeCancelButton() throws Exception	{
		
		Button b = (Button)d.findViewById(R.id.custom_dialog_cancel_button);
		b.setVisibility(View.GONE);
	}

	/**
	 *	The interface used for listening to cancelation 
	 */
	public interface OnCancelationListener	{
		
		public void onCancelation();
	}
	
	/**
	 *	The interface used for listening to confirmation 
	 */
	public interface OnConfirmationListener	{
		
		public void onConfirmation();
	}
}