/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * SMS service for Android Aware framework
 * Last updated: 14-Jul-2010
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
import android.provider.ContactsContract;
import android.util.Log;

/*
 * Example of usage:
 * 
 * 		SMS_Service smsSrv = SMS_Service.getService();
 * 
 * 		smsSrv.setApplicationContext(getApplicationContext());
		smsSrv.setActivityHandler(new Handler());
		smsSrv.setContentResolver(getContentResolver());
		smsSrv.setOnSMSListener(new SMS_Service.OnSMSListener() {
			@Override
			public void onSMSSent(SMSObj smsobj) {
				Log.d(TAG,"SMS SENT to "+smsobj.getSmsPerson()+"-"+smsobj.getSmsNumber()+" with "+smsobj.getSmsBody());
			}
			@Override
			public void onSMSReceived(SMSObj smsobj) {
				Log.d(TAG,"SMS RECEIVED from "+smsobj.getSmsPerson()+"-"+smsobj.getSmsNumber()+" with "+smsobj.getSmsBody());
			}
//		});
//		
//		Intent startSms = new Intent(this,SMS_Service.class);
//		startService(startSms);
*/

public class SMS_Service extends Service {

	public class SMSObj {
		private int smsID = 0; //id on device database
		private int smsThreadID = 0; //>0 if part of thread
		private String smsNumber = ""; //number
		private String smsPerson = ""; //person's name if we have it on the contact list, otherwise returns number
		private long smsDate = 0; //date of message in timestamp
		private String smsType = ""; //Inbox or Sent
		private boolean smsRead = false; //user has read the message?
		private String smsSubject = ""; //only in MMS, otherwise empty
		private String smsBody = ""; //text message
		
		public SMSObj(Cursor sms) {
//			_id
//			 * thread_id
//			 * address
//			 * person
//			 * date
//			 * protocol
//			 * read
//			 * status
//			 * type
//			 * reply_path_present
//			 * subject
//			 * body
//			 * service_center
//			 * locked
			smsID = sms.getInt(sms.getColumnIndex("_id"));
			smsThreadID = sms.getInt(sms.getColumnIndex("thread_id"));
			smsNumber = sms.getString(sms.getColumnIndex("address"));
			smsPerson = getPerson(sms);
			smsDate = sms.getLong(sms.getColumnIndex("date"));
			smsType = (sms.getInt(sms.getColumnIndex("type"))==1?"Inbox":"Sent");
			smsRead = (sms.getInt(sms.getColumnIndex("read"))==0?false:true);
			smsSubject = sms.getString(sms.getColumnIndex("subject"));
			smsBody = sms.getString(sms.getColumnIndex("body"));
		}
		
		//This needs READ_CONTACTS permission
		private String getPerson(Cursor sms) {
			Uri phoneUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, sms.getString(sms.getColumnIndex("address")));
			if(phoneUri != null) {
				String[] projection = new String[] {ContactsContract.Contacts.DISPLAY_NAME}; //I want the person's name/number/email
				Cursor phoneCursor = smsResolver.query(phoneUri, projection, null, null, null);
				if(phoneCursor.moveToFirst()) {
					return phoneCursor.getString(0);
				}
			}
			return sms.getString(sms.getColumnIndex("address"));
		}
		
		public int getSmsID() {
			return smsID;
		}

		public int getSmsThreadID() {
			return smsThreadID;
		}

		public String getSmsNumber() {
			return smsNumber;
		}

		public String getSmsPerson() {
			return smsPerson;
		}

		public long getSmsDate() {
			return smsDate;
		}

		public String getSmsType() {
			return smsType;
		}

		public boolean isSmsRead() {
			return smsRead;
		}

		public String getSmsSubject() {
			return smsSubject;
		}

		public String getSmsBody() {
			return smsBody;
		}
	}
	
	public static final int INBOX = 1;
	public static final int SENT = 2;
	
	private static OnSMSListener smsListener;
	public interface OnSMSListener {
		public abstract void onSMSReceived(SMSObj sms);
		public abstract void onSMSSent(SMSObj sms);
	}
	
	public void setOnSMSListener(OnSMSListener smslistener) {
		SMS_Service.smsListener = smslistener;
	}
	
	public OnSMSListener getSMSListener() {
		return SMS_Service.smsListener;
	}
	
	private static ContentResolver smsResolver;
	private SMSObserver smsObserver = new SMSObserver(getActivityHandler());
	
	private static Handler activityHandler = new Handler();
	
	public Handler getActivityHandler() {
		return SMS_Service.activityHandler;
	}
	
	public void setActivityHandler(Handler activity) {
		SMS_Service.activityHandler = activity;
	}
	
	public void setContentResolver(ContentResolver cr) {
		SMS_Service.smsResolver = cr;
	}
	
	public static ContentResolver getSMSContentResolver() {
		return SMS_Service.smsResolver;
	}
	
	public static class SMSObserver extends ContentObserver {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			Uri SMSURI = Uri.parse("content://sms");
			Cursor sms = getSMSContentResolver().query(SMSURI, null, null, null, null);
			
			sms.moveToFirst();
			
			int box = sms.getInt(sms.getColumnIndex("type"));
			
			if(smsListener!=null) 
			{
				if(box == INBOX) 
					smsListener.onSMSReceived(smsService.new SMSObj(sms));
				else if(box == SENT)
					smsListener.onSMSSent(smsService.new SMSObj(sms));
			}
		}
		
		public SMSObserver(Handler handler) {
			super(handler);
		}
	}
	
	//Activity binder
	private final IBinder smsBinder = new SMSBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static final SMS_Service smsService = new SMS_Service();
    public static SMS_Service getService() {
    	return smsService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return smsBinder;
	}
	
	public class SMSBinder extends Binder {
		SMS_Service getService() {
			return SMS_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		SMS_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		if(smsObserver==null) 
			smsObserver = new SMSObserver(this.getActivityHandler());
		
		if(smsResolver==null)
			smsResolver = this.getContentResolver();
		
		smsResolver.registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
		
		Log.d("AWARE", "SMS Service running!");
	}

	 
	
	@Override
	public void onDestroy() {
		
		Log.d("AWARE","SMS Service terminated...");
		
		super.onDestroy();
	}
}
