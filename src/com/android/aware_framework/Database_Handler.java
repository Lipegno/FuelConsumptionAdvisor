/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Database handler for Android Aware framework
 * Last updated: 30-Jun-2010
 * Modified by: Denzil Ferreira
 */

package com.android.aware_framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
import android.util.Log;


//  Example of usage:
//  
//  //Database_Handler that can handle several databases simultaneously.
//  Database_Handler db = new Database_Hander(context,"omniaware.db", 0,"locations",
//  				"_id integer primary key autoincrement," + //0
// 				"longitude real default 0," + //1
// 				"latitude real default 0," + //2
// 				"speed real default 0,"+ //3
// 				"device_id text default ''," + //4
// 				"timestamp text default (CURRENT_TIMESTAMP),"+ //5
// 				"bearing real default 0,"+ //6
// 				"accuracy real default 0" //7
// 				);
//  
//  //Insert data
//  db.open();
//  ContentValues rowData = new ContentValues();
//  rowData.put("tablefieldName","value");
//  db.insertData(rowData);
//  db.close();
//  
//  //Get all entries
//  Cursor locations = db.getData(null, null, null, null, null, null, null);
//  
//  //Delete data
//  int[] idsToDelete = new int[10];
//  idsToDelete[] = location.getInt(0); //0 holds the _id field of the table
//  db.delete(idsToDelete);
//  
//  //Clear all data
//  db.clearData();
//  
//  //Update a row
//  ContentValues newRowData = new ContentValues();
//  newRowData.put("tablefielName","newvalue");
//  db.open();
//  db.updateData(newRowData,rowID);
//  db.close();

// 	//Dump to SDCard
//  db.open();
//	db.dumpDatabaseToSD();
//	db.close();
 

public class Database_Handler {

	private SQLiteDatabase database;
	private DatabaseHelper dbHelp;
	
	private String DATABASE_NAME = "";
	private String DATABASE_TABLE = "";
	private String DATABASE_FIELDS = "";
	private int DATABASE_VERSION = 0;
	
	private Context context;
	
	//Constructor
	public Database_Handler(Context appContext, String databaseName, int databaseVersion, String tableName, String tableFields) {
		context = appContext;
		DATABASE_NAME = databaseName;
		DATABASE_TABLE = tableName;
		DATABASE_FIELDS = tableFields;
		DATABASE_VERSION = databaseVersion;
		
		dbHelp = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public Context getApplicationContext() {
		return context;
	}
	
	public String getDatabaseFields() {
		return DATABASE_FIELDS;
	}
	
	public String getDatabaseTable() {
		return DATABASE_TABLE;
	}
	
	public String getDatabaseName() {
		return DATABASE_NAME;
	}
	
	private class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE IF NOT EXISTS "+DATABASE_TABLE +" ("+DATABASE_FIELDS+");");
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
			onCreate(db);
		}
	}

	public Database_Handler open() throws SQLException {
		try {
			database = dbHelp.getWritableDatabase();
		}catch(SQLException xption) {
			database = dbHelp.getReadableDatabase();
		}
		return this;
	}
	
	public void close() {
		database.close();
	}
	
	public void deleteData(int[] rowIds) {
		if(database!=null){
			for(int rowId : rowIds) {
				database.delete(DATABASE_TABLE, "_id="+rowId, null);
			}
		}
	}
	
	public Cursor getData(String[] colsName, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		if(database!=null) {
			return database.query(DATABASE_TABLE, colsName, selection, selectionArgs, groupBy, having, orderBy,limit);
		}return null;
	}
	
	public void cleanData() {
		database.execSQL("DELETE FROM "+DATABASE_TABLE);
	}
	
	public long insertData(ContentValues rowData) {
		long rowId = -1;
		if(database!=null) {
			rowId = database.insert(DATABASE_TABLE, null, rowData);
		}
		return rowId;
	}
	
	public void updateData(ContentValues rowData, int rowId) {
		if(database!=null) {
			database.update(DATABASE_TABLE, rowData, "_id="+rowId, null);
		}
	}
	
	public void dumpDatabaseToSD(final long timestamp) {
		
		Runnable onDump = new Runnable() {
			@Override
			public void run() {
				
				File sdcard = Environment.getExternalStorageDirectory();
				
				//Is the SDCard writable
				if(sdcard.canWrite()) {
					
					String dbPath = database.getPath();
					String dbBackup = timestamp +"_"+ DATABASE_NAME;
					
					//Get the database file
					File currentDB = new File(dbPath);
					
					//Set the backup
					File backupDB = new File(sdcard,dbBackup);
					
					if(currentDB.exists()) {
						
						try {
							
							Log.d("AWARE","Backing up: "+currentDB.toString());
							
							//Create file transfer channel
							FileChannel src = new FileInputStream(currentDB).getChannel();
							FileChannel dst = new FileOutputStream(backupDB).getChannel();
						
							dst.transferFrom(src, 0, src.size());
							
							src.close();
							dst.close();
							
						} catch (FileNotFoundException e) {
							Log.e("AWARE","Couldn't find the database file: "+e.getMessage().toString());
						} catch (IOException e) {
							Log.e("AWARE","Couldn't transfer to file: "+e.getMessage().toString());
						}
						
					}
				}
				
			}
		};
		
		Thread run = new Thread(null, onDump,"dumper");
		run.start();
		
	}
}
