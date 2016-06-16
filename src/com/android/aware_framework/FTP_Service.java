/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * FTP service for Android Aware framework
 * Last updated: 27-Jul-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.net.ftp.FTPClient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.aware_framework.DevicesInfo_Service.DeviceInfo;

//Example
//ATTENTION: you need to add a reference to the aware_framework/assets/commons-net-ftp-2.0.jar
//
//		FTP_Service ftpSrv = FTP_Service.getService();
//		ftpSrv.setApplicationContext(getApplicationContext());
//		

//		ftpSrv.setOnFtpTransferListener(new FTP_Service.OnFtpTransferListener() {
//			@Override
//			public void onFtpTransfer(String file, long fileSize, long timeMillis) {
//				
//			}
//		});

//		Intent ftpIntent = new Intent(this, FTP_Service.class);
//		startService(ftpIntent);
//			
//		ftpSrv.setHost("ftp.dymsoft.com");
//		ftpSrv.setHostPort(21);
//		ftpSrv.setUsername("dymsoft");
//		ftpSrv.setPassword("liznedarierref");
//		ftpSrv.setFolder("/public_html/");
//		
//		File sdcard = Environment.getExternalStorageDirectory();
//		
//		if(sdcard.exists()) //just in case the user has mounted the SDCard 
//		{
//			ArrayList<File> filesToUpload = new ArrayList<File>();
//			
//			filesToUpload.add(new File(sdcard.getPath()+"/mobility_battery.db"));
//			filesToUpload.add(new File(sdcard.getPath()+"/mobility_calls.db"));
//			filesToUpload.add(new File(sdcard.getPath()+"/mobility_sms.db"));
//			filesToUpload.add(new File(sdcard.getPath()+"/mobility_screen.db"));
//			filesToUpload.add(new File(sdcard.getPath()+"/mobility_locations.db"));
//			filesToUpload.add(new File(sdcard.getPath()+"/mobility_activity.db"));
//			filesToUpload.add(new File(sdcard.getPath()+"/mobility_apps.db"));
//			
//			ftpSrv.setFiles(filesToUpload);
//			
//			ftpSrv.uploadFiles();
//		}

public class FTP_Service extends Service {

	private static OnFtpTransferListener ftpListener;
	public interface OnFtpTransferListener {
		public abstract void onFtpTransfer(String file, long fileSize, long timeMillis);
	}
	public void setOnFtpTransferListener(OnFtpTransferListener listener) {
		ftpListener = listener;
	}
	
	public OnFtpTransferListener getOnFtpTransferListener() {
		return ftpListener;
	}
	
	private static FTPClient ftpClient = new FTPClient();
	
	private static Hashtable<String,File> files;
	
	private static String SERVER;
	private static int SERVER_PORT;
	private static String USERNAME;
	private static String PASSWORD;
	private static String FOLDER;
	
	public void setFiles(Hashtable<String,File> filesToUpload) {
		files = filesToUpload;
	}
	
	public static FTPClient getFTPClient() {
		return ftpClient;
	}
	
	public static Hashtable<String,File> getFiles() {
		return files;
	}
	
	public void setHost(String host) {
		SERVER = host;
	}
	
	public void setHostPort(int port) {
		SERVER_PORT = port;
	}
	public void setUsername(String user) {
		USERNAME = user;
	}
	public void setPassword(String password) {
		PASSWORD = password;
	}
	public void setFolder(String folder) {
		FOLDER = folder;
	}
	
	public static String getHost() {
		return SERVER;
	}
	public static int getHostPort() {
		return SERVER_PORT;
	}
	public static String getUsername() {
		return USERNAME;
	}
	public static String getPassword() {
		return PASSWORD;
	}
	public static String getFolder() {
		return FOLDER;
	}
	
	public void uploadFiles() {
		
		if(DeviceInfo.getDeviceInfo().getIpAddress()!="") {
			
			Runnable onUpload = new Runnable() {
				@Override
				public void run() {
					Log.d("AWARE","FTP connection started...");
					
					FTPClient ftp = getFTPClient();
					
					try 
					{
						ftp.setDataTimeout(5000);
						ftp.connect(getHost(), getHostPort());
						ftp.login(getUsername(), getPassword());
						if(!ftp.changeWorkingDirectory(getFolder())) {
							ftp.makeDirectory(getFolder());
							ftp.changeWorkingDirectory(getFolder());
						}
						ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
						ftp.enterLocalActiveMode();
						
					} catch (SocketException e2) {
						Log.e("AWARE","Error connecting: "+e2.toString());
					} catch (IOException e2) {
						Log.e("AWARE","Error login: "+e2.toString());
					} catch ( NullPointerException e) {
						Log.e("AWARE",e.toString());
					}
					
					Enumeration<String> keys = getFiles().keys();
					String key = keys.nextElement();
					do {
						
						//connection could get cut out...
						if(!ftp.isConnected()) {
							
							try 
							{
								ftp.setDataTimeout(5000);
								ftp.connect(getHost(), getHostPort());
								ftp.login(getUsername(), getPassword());
								if(!ftp.changeWorkingDirectory(getFolder())) {
									ftp.makeDirectory(getFolder());
									ftp.changeWorkingDirectory(getFolder());
								}
								
								ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
								ftp.enterLocalActiveMode();
								
							} catch (SocketException e2) {
								Log.e("AWARE","Error connecting: "+e2.toString());
							} catch (IOException e2) {
								Log.e("AWARE","Error login: "+e2.toString());
							} catch ( NullPointerException e) {
								Log.e("AWARE",e.toString());
							}
						}
						
						File file = (File) getFiles().get(key);
						
						BufferedInputStream input = null;
						try 
						{	
							input = new BufferedInputStream(new FileInputStream(file.getPath()));
						} catch (FileNotFoundException e1) {
							Log.e("AWARE","File not found: "+e1.toString());
						} catch (NullPointerException e) {
							Log.w("AWARE","Input null:"+e.toString());
						}
						
						if(input!=null) 
						{
							try 
							{
								long started = new Date().getTime(); 
								Log.w("AWARE","Uploading:"+key+" Size:"+file.length()+" started:"+started);
								
								ftp.storeFile(key, input);
								
								long ended = new Date().getTime();
								
								if(ftpListener!=null)
									ftpListener.onFtpTransfer(file.getName(), file.length(), (ended-started));
									
								Log.w("AWARE","Uploaded:"+key+" Elapsed:"+(ended-started)+" ms");
								
								input.close();
								
							} catch (IOException e) {
								Log.e("AWARE","Error InputStream:"+e.toString());
							} catch (NullPointerException e) {
								Log.e("AWARE",e.toString());
							}
							
						}
						
						key = keys.nextElement();
					
					} while(keys.hasMoreElements()); //end while
					
					try {
						ftp.logout();
						ftp.disconnect();
					} catch (IOException e) {
						Log.e("AWARE","Error logout/disconnect: "+e.toString());
					} catch (NullPointerException e) {
						Log.e("AWARE",e.toString());
					}
					
					Log.d("AWARE","FTP connection finished!");
				}
			};
			
			Thread uploader = new Thread(null, onUpload,"ftp");
			uploader.start();
			
		}
		
	}
	
	//Activity binder
	private final IBinder ftpBinder = new FTPBinder();
	
    private static Context context;
	
    //Singleton pattern
    private static FTP_Service ftpService = new FTP_Service();
    public static FTP_Service getService() {
    	return ftpService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return ftpBinder;
	}
	
	public class FTPBinder extends Binder {
		FTP_Service getService() {
			return FTP_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		FTP_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		Log.d("AWARE", "FTP Service running!");
	}

	@Override
	public void onDestroy() {
		
		Log.d("AWARE","FTP Service terminated...");
		
		super.onDestroy();
	}
}
