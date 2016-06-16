/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Call Manager service for Android Aware framework
 * Last updated: 15-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.util.Log;

//	Example of usage
//	@Permissions: android.permission.READ_CONTACTS
//	CallManager_Service callSrv = CallManager_Service.getService();
//	callSrv.setApplicationContext(getApplicationContext());
//	callSrv.setActivityHandler(new Handler());
//	callSrv.setContentResolver(getContentResolver());
//	callSrv.setOnCallListener(new CallManager_Service.OnCallListener() {
//		@Override
//		public void onIncoming(CallObj call) {
//			Log.d("AWARE","Incoming: "+call.getCallName()+" ("+call.getCallNumberType()+") "+call.getCallNumber());
//		}
//	
//		@Override
//		public void onMissed(CallObj call) {
//			Log.d("AWARE","Missed: "+call.getCallName()+" ("+call.getCallNumberType()+") "+call.getCallNumber());
//		}
//	
//		@Override
//		public void onOutgoing(CallObj call) {
//			Log.d("AWARE","Outgoing: "+call.getCallName()+" ("+call.getCallNumberType()+") "+call.getCallNumber());
//		}
//					
//	});
//	
//	Intent callIntent = new Intent(this,CallManager_Service.class);
//	startService(callIntent);


public class CallManager_Service extends Service {

	public class CallObj {
		private String callNumberType="";
		private int callAcknowledged=0;
		private long callDuration=0;
		private String callNumberLabel = "";
		private String callName = "";
		private String callNumber = "";
		private int callType=0;
		private long callTimestamp = 0;
		
		public CallObj(Cursor call) {
			callNumberType = call.getString(call.getColumnIndex(Calls.CACHED_NUMBER_TYPE));
			callAcknowledged = call.getInt(call.getColumnIndex(Calls.NEW));
			callDuration = call.getLong(call.getColumnIndex(Calls.DURATION));
			callNumberLabel = call.getString(call.getColumnIndex(Calls.CACHED_NUMBER_LABEL));
			callName = call.getString(call.getColumnIndex(Calls.CACHED_NAME));
			callNumber = call.getString(call.getColumnIndex(Calls.NUMBER));
			callType = call.getInt(call.getColumnIndex(Calls.TYPE));
			callTimestamp = call.getLong(call.getColumnIndex(Calls.DURATION));
		}
		
		public String getCallNumberType() {
			return callNumberType;
		}

		public void setCallNumberType(String callNumberType) {
			this.callNumberType = callNumberType;
		}

		public int getCallAcknowledged() {
			return callAcknowledged;
		}

		public void setCallAcknowledged(int callAcknowledged) {
			this.callAcknowledged = callAcknowledged;
		}

		public long getCallDuration() {
			return callDuration;
		}

		public void setCallDuration(long callDuration) {
			this.callDuration = callDuration;
		}

		public String getCallNumberLabel() {
			return callNumberLabel;
		}

		public void setCallNumberLabel(String callNumberLabel) {
			this.callNumberLabel = callNumberLabel;
		}

		public String getCallName() {
			return callName;
		}

		public void setCallName(String callName) {
			this.callName = callName;
		}

		public String getCallNumber() {
			return callNumber;
		}

		public void setCallNumber(String callNumber) {
			this.callNumber = callNumber;
		}

		public int getCallType() {
			return callType;
		}

		public void setCallType(int callType) {
			this.callType = callType;
		}

		public long getCallTimestamp() {
			return callTimestamp;
		}

		public void setCallTimestamp(long callTimestamp) {
			this.callTimestamp = callTimestamp;
		}
	}
	
	//Push CallManager listener
	private static OnCallListener callListener;
	public interface OnCallListener {
		public abstract void onIncoming(CallObj call);
		public abstract void onOutgoing(CallObj call);
		public abstract void onMissed(CallObj call);
	}
	
	public void setOnCallListener(OnCallListener call) {
		CallManager_Service.callListener = call;
	}
	
	public OnCallListener getOnCallListener() {
		return CallManager_Service.callListener;
	}
	
	//ContentResolver, as we are going to use a contentobserver
	private static ContentResolver callsResolver;
	public void setContentResolver(ContentResolver cr) {
		CallManager_Service.callsResolver = cr;
	}
	
	public static ContentResolver getCallContentResolver() {
		return CallManager_Service.callsResolver;
	}
	
	//A contentobserver runs on a handler thread, so that it doesn't bottleneck the system
	private static Handler activityHandler = new Handler();
	public Handler getActivityHandler() {
		return CallManager_Service.activityHandler;
	}
	
	public void setActivityHandler(Handler activity) {
		CallManager_Service.activityHandler = activity;
	}
	
	private CallObserver callObserver = new CallObserver(getActivityHandler());
	public static class CallObserver extends ContentObserver {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			//Send latest events to the listener
			Uri callUri = Calls.CONTENT_URI;
			Cursor calls = getCallContentResolver().query(callUri, null, null, null, "date DESC LIMIT 2");
			
			/* Calls content
			 * numbertype
			 * new
			 * duration
			 * _id
			 * numberlabel
			 * name
			 * number
			 * type
			 * date
			 */
			
			calls.moveToFirst();
			
			switch(calls.getInt(calls.getColumnIndex("type"))) {
				case android.provider.CallLog.Calls.INCOMING_TYPE:
					callListener.onIncoming(callManagerService.new CallObj(calls));
				break;
				case android.provider.CallLog.Calls.MISSED_TYPE:
					callListener.onMissed(callManagerService.new CallObj(calls));
				break;
				case android.provider.CallLog.Calls.OUTGOING_TYPE:
					callListener.onOutgoing(callManagerService.new CallObj(calls));
				break;
			}
		}
		
		public CallObserver(Handler handler) {
			super(handler);
		}
		
	}
	
	//Activity binder
	private final IBinder callmanagerBinder = new CallManagerBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final CallManager_Service callManagerService = new CallManager_Service();
    public static CallManager_Service getService() {
    	return callManagerService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return callmanagerBinder;
	}
	
	public class CallManagerBinder extends Binder {
		CallManager_Service getService() {
			return CallManager_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		CallManager_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(callObserver==null) { 
			callObserver = new CallObserver(getActivityHandler());
		}
		
		if(callsResolver!=null)
			callsResolver.registerContentObserver(Calls.CONTENT_URI, true, callObserver);
		
		Log.d("AWARE", "Call Manager Service running!");
	}

	 
	
	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Call Manager Service terminated...");
		
		super.onDestroy();
	}
}
