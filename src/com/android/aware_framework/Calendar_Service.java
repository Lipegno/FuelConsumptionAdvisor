/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Calendar service for Android Aware framework
 * Last updated: 2-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.util.Date;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

// 	Example of usage:
//	Calendar_Service calService = Calendar_Service.getService();
//	
//	calService.setApplicationContext(getApplicationContext());
//	calService.setContentResolver(getContentResolver());
//	
//	//Override what happens everytime the calendar changes
//	calService.setCalendarChangedListener(new OnCalendarChangedListener() {
//		@Override
//		public void onCalendarChanged(Cursor calendar) {
//			
//		}
//	});
//	
//	//Get the calendars available on the device
//	Cursor calendars = calService.getCalendars();
//	
//	//Get events in interval, all if howLong = 0
//	long howLong = 30 * 24 * 60 * 1000; //30 days before and after today
//	Cursor events = calService.getCalendarEvents(howLong);
//	
//	//Get last changed synched events (20 events)
//	Cursor lastEvents = calService.getLatestChanges(20);
//	
//	//How far ahead do you want to see
//	calService.setPremonition(10*60*1000);
//	
//	//Get next event
//	Cursor nextEvent = calService.getNextEvent();
//		
//	Intent calIntent = new Intent(this,Calendar_Service.class);
//	startService(calIntent);
	


public class Calendar_Service extends Service {
	
	private static BroadcastReceiver calendarReceiver = new CalendarReceiver();
	public static class CalendarReceiver extends BroadcastReceiver {
		public static final String ACTION_CALENDAR_UPDATE = "ACTION_CALENDAR_UPDATE";
		@Override
		public void onReceive(Context context, Intent intent) {
			if(calendarListener!=null)
				calendarListener.onCalendarNextEvent(calendarService.getNextEvent());
		}		
	}
	
	public static boolean isFroyo = false;
	
	private static OnCalendarChangedListener calendarListener;
	public interface OnCalendarChangedListener {
		public abstract void onCalendarChanged(Cursor calendar);
		public abstract void onCalendarNextEvent(Cursor event);
	}
	public void setOnCalendarChangedListener(OnCalendarChangedListener listener) {
		calendarListener = listener;
	}
	
	public OnCalendarChangedListener getOnCalendarChangedListener() {
		return calendarListener;
	}
	
	public class CalendarBinder extends Binder {
		Calendar_Service getService() {
			return Calendar_Service.getService();
		}
	}
	
	private static CalendarObserver calendarObs = new CalendarObserver(null);
	public static class CalendarObserver extends ContentObserver {
		public CalendarObserver(Handler handler) {
			super(handler);
		}
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if(calendarListener!=null) 
				calendarListener.onCalendarChanged(getLatestChanges(1));
		}	
	}
	public CalendarObserver getCalendarObserver() {
		return calendarObs;
	}

	public static void setCalendarObserver(CalendarObserver calendarObserver) {
		calendarObs = calendarObserver;
	}
	
	private static long premonition = 0; //how far ahead to see the future?
	private static String calendarFilter = "";

	//Activity binder
	private final IBinder calendarBinder = new CalendarBinder();
	
    private static Context context;
    private static ContentResolver contentResolver;

	//Singleton pattern
    private static final Calendar_Service calendarService = new Calendar_Service();
    public static Calendar_Service getService() {
    	return calendarService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return calendarBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		 
		if(context == null)
			context = this.getApplicationContext();
		
		if(contentResolver == null)
			contentResolver = this.getContentResolver();
		
		if(calendarObs == null)
			calendarObs = new CalendarObserver(null);
		
		if(contentResolver == null)
			contentResolver = this.getContentResolver();
		
		contentResolver.registerContentObserver(Uri.parse("content://calendar/events"), true, calendarObs); //donut + eclair
		contentResolver.registerContentObserver(Uri.parse("content://com.android.calendar/events"), true, calendarObs); //froyo
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(CalendarReceiver.ACTION_CALENDAR_UPDATE);
		if(calendarReceiver!=null)
			registerReceiver(calendarReceiver, filter);
		
		Log.d("AWARE", "Calendar Service running!");
	}

	//Get the Calendars selected as active by the user on the phone
	public Cursor getCalendars() {
		if(contentResolver!=null) 
		{
			Cursor calendars = contentResolver.query(Uri.parse("content://calendar/calendars"), null, null, null, null);
			if(calendars == null) 
			{
				calendars = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"), null, null, null, null);
				setFroyo(true);
			}
			return calendars;
		}
		return null;
	}
	
	//Get calendar events, sorted by time for a day, week and month
	public Cursor getCalendarEvents(long howLongMillis) {
		
		String selection = "";
		String sortStr = "";
		
		long now = new Date().getTime();
		
		if(premonition>0) {
			now-=premonition; //we want to see ahead
		}
		
		if(howLongMillis>0) {
			selection = "dtstart BETWEEN " + (now - howLongMillis) + " AND "+ (now + howLongMillis) ;
			sortStr = " dtstart DESC ";
		}
		
		Cursor events = contentResolver.query(Uri.parse("content://calendar/events"),
				null, selection, null, sortStr);
		
		//If we are working at Froyo
		if(events == null)
		{
			events = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
					null, selection, null, sortStr);
			setFroyo(true);
		}
		return events;
	}
	
	public static Cursor getLatestChanges(int limit) {
		String selection = "";
		if(getCalendarFilter().length()>0) {
			selection = "calendar_id in ("+getCalendarFilter()+")";
		}
		
		String sortStr = "_sync_time DESC LIMIT "+limit;
		
		Cursor events = contentResolver.query(Uri.parse("content://calendar/events"), null, selection, null, sortStr);
		if(events == null)
		{
			events = contentResolver.query(Uri.parse("content://com.android.calendar/events"), null, selection, null, sortStr);
			setFroyo(true);
		}
		if(events.getCount()>0) {
			return events;
		}
		
		return null;
	}
	
	public Cursor getNextEvent() {
		
		long now = new Date().getTime();
		
		String selection = "dtstart >= " + (now-premonition);
		String sortStr = " dtstart ASC LIMIT 1";
		
		Cursor events = contentResolver.query(Uri.parse("content://calendar/events"),
				null, selection, null, sortStr);
		
		//If we are working at Froyo
		if(events == null)
		{
			events = contentResolver.query(Uri.parse("content://com.android.calendar/events"),
					null, selection, null, sortStr);
			setFroyo(true);
		}
		return events;
	}
	
	public static long getPremonition() {
		return premonition;
	}

	public void setPremonition(long premonition) {
		Calendar_Service.premonition = premonition;
	}
	
	public void setApplicationContext(Context mContext) {
		Calendar_Service.context = mContext;
	}

	public void setContentResolver(ContentResolver contentResolver) {
		Calendar_Service.contentResolver = contentResolver;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(calendarReceiver);
		Log.d("AWARE","Calendar Service terminated...");
		contentResolver.unregisterContentObserver(calendarObs);
	}

	public boolean isFroyo() {
		return isFroyo;
	}

	public static void setFroyo(boolean isFroyo) {
		Calendar_Service.isFroyo = isFroyo;
	}

	public static String getCalendarFilter() {
		return calendarFilter;
	}

	public void setCalendarFilter(String calendarFilter) {
		Calendar_Service.calendarFilter = calendarFilter;
	}
}
