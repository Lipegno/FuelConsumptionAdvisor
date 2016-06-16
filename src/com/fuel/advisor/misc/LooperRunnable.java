/**
 * An interface for implementing a Looper inside a new thread
 */
package com.fuel.advisor.misc;

import android.os.Handler;

public interface LooperRunnable extends Runnable {

	// Define constants to control the message loop
	public final static int STOP_SERVICE = 0;
	public final static int START_SERVICE = 1;
	
	public Handler getHandler();
}
