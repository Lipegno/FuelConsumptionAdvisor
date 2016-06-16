/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Notification Service for Android Aware framework
 * Last updated: 2-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/*
 * Example of usage:
 * 
	Notification_Service notifyService = Notification_Service.getService();
	notifyService.setApplicationContext(getApplicationContext());
		
	Intent notifyIntent = new Intent(this,Notification_Service.class);
	startService(notifyIntent);
	
	//What happens when you click the notification
	PendingIntent onClick = PendingIntent.getActivity(getApplicationContext(), 0, notifyService.getNoticationIntent(), 0);
	notifyService.notify("Family awareness", "Subject", "Put your text here", R.drawable.ic_dialog_alert, true, true, true, Color.BLUE, false, false, onClick);
 */

public class Notification_Service extends Service {

	private static NotificationManager notManager;
	private static Notification notification;
    private static Context context;
    private static Intent notificationIntent;
    private static RemoteViews remoteView;
    private static int notificationNumber = 0;
	
	//Activity binder
	private final IBinder notificationBinder = new NotificationBinder();
		
    //Singleton pattern
    private static final Notification_Service notificationService = new Notification_Service();
    public static Notification_Service getService() {
    	return notificationService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return notificationBinder;
	}
	
	public class NotificationBinder extends Binder {
		Notification_Service getService() {
			return Notification_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Notification_Service.context = mContext;
	}
	
	public void setNotificationIntent(Intent notif) {
		Notification_Service.notificationIntent = notif;
	}
	
	public Intent getNoticationIntent() {
		return notificationIntent;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(notManager == null) 
			notManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		
		if(notificationIntent == null) 
			notificationIntent = new Intent(this, Notification_Service.class);
		
		Log.d("AWARE", "Notification Service running!");
	}

	public void setCustomLayout(RemoteViews customLayout) {
		remoteView = customLayout;
	}
	
	public void setNumber(int number) {
		notificationNumber = number;
	}
	
	//Display a notification on top bar
	public void notify(int ID_notification,String scrolling, String subject, String content, int icon, boolean SoundOn, boolean VibrateOn, boolean LEDOn, int LEDColor, boolean persistent, boolean ongoing, boolean clearOnClick, PendingIntent onClick) {
		long when = System.currentTimeMillis();
		
		notification = new Notification(icon,scrolling,when);
		
		if(remoteView!=null)
		{
			notification.contentView = remoteView;
			notification.contentIntent = onClick;
		}else{
			notification.setLatestEventInfo(context, subject, content, onClick);
		}
		
		if(notificationNumber>0) {
			notification.number = notificationNumber;
		}
		
		if(SoundOn) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if(VibrateOn) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		
		if(LEDOn) {
			notification.ledARGB = LEDColor;
			notification.ledOnMS = 500;
			notification.ledOffMS = 100;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}else{
			notification.flags |= Notification.DEFAULT_LIGHTS;
		}
		
		if(persistent) {
			notification.flags |= Notification.FLAG_INSISTENT;
		}
		
		if(ongoing)
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		if(clearOnClick)
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		else notification.flags |= Notification.FLAG_NO_CLEAR;
		
		if( ID_notification == -1 )
			ID_notification = new Random().nextInt();
		
		notManager.notify(ID_notification, notification); //each notification has a different ID randomly or fixed
	}
	
	public void stopNotification(int notification_id) {
		notManager.cancel(notification_id);
	}
	
	@Override
	public void onDestroy() {
		notManager.cancelAll();
		Log.d("AWARE","Notification Service terminated...");
		
		super.onDestroy();
	}

	public void setNotManager(NotificationManager notManager) {
		Notification_Service.notManager = notManager;
	}
}
