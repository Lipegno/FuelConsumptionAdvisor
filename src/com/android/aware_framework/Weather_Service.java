/*
 * Created by Denzil Ferreira
 * dferreir@andrew.cmu.edu
 * 
 * Weather Service for Android Aware framework
 * Last updated: 6-Aug-2010
 * Modified by: Denzil Ferreira
 */
package com.android.aware_framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.android.aware_framework.DevicesInfo_Service.DeviceInfo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class Weather_Service extends Service {

	private static OnWeatherListener onWeatherListener;
	public interface OnWeatherListener {
		public abstract void onWeatherUpdate(WeatherInfo weatherInfo, long elapsedTime);
	}
	public void setOnWeatherListener(OnWeatherListener weatherListener) {
		onWeatherListener = weatherListener;
	}
	public OnWeatherListener getOnWeatherListener() {
		return onWeatherListener;
	}
	
	private static BroadcastReceiver weatherReceiver = new WeatherReceiver();
	public static class WeatherReceiver extends BroadcastReceiver {
		public static final String ACTION_WEATHERMANAGER = "ACTION_WEATHERMANAGER";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(onWeatherListener!=null) 
			{
				if(DeviceInfo.getDeviceInfo().getIpAddress()!="") 
				{
					try 
					{
						
						SAXParserFactory spf = SAXParserFactory.newInstance();
						SAXParser sp = spf.newSAXParser();
						
						XMLReader xr = sp.getXMLReader();
	
						WeatherHandler handler = new WeatherHandler();
						xr.setContentHandler(handler);
						
						long start = System.currentTimeMillis();
							
							String weatherInfoXML = HTMLObj.dataGET(weatherURL);
							ByteArrayInputStream is = new ByteArrayInputStream(weatherInfoXML.getBytes());
							
						long end = System.currentTimeMillis();
						
						xr.parse(new InputSource(is));
						
						WeatherInfo weatherInfo = handler.getParsedData();
						
						onWeatherListener.onWeatherUpdate(weatherInfo,(end-start));
						
					} catch (ParserConfigurationException e) {
						Log.e("AWARE",e.toString());
					} catch (SAXException e) {
						Log.e("AWARE",e.toString());
					} catch (IOException e) {
						Log.e("AWARE",e.toString());
					}
					
				}
			}
		}		
	}
	
	public static class WeatherHandler extends DefaultHandler {
		
		 private boolean in_current_observation = false;
		 private boolean in_weather = false;
		 private boolean in_temperature_string = false;
		 private boolean in_location = false;
		 
		 private WeatherInfo myParsedDataSet = WeatherInfo.getWeatherInfo();
		 
		 public WeatherInfo getParsedData() {
		     return this.myParsedDataSet;
		 } //getParsedData
		 
		 @Override
		 public void startDocument() throws SAXException {
		      this.myParsedDataSet = new WeatherInfo();
		 } //startDocument
		
		 @Override
		 public void endDocument() throws SAXException {
		 } //endDocument
		
		
		 public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
	    
	    	if(localName.equals("current_observation"))
			{
				this.in_current_observation = true;
			}
			if(localName.equals("weather")) 
			{
				this.in_weather = true;
			}
			if(localName.equals("temperature_string"))
			{
				this.in_temperature_string = true;
			}
			if(localName.equals("location"))
	    	{
	    		this.in_location = true;
	    	}
		 } //startElement
		 
		
		 public void endElement(String namespaceURI, String localName, String qName)
		           throws SAXException {
		 	if(localName.equals("current_observation"))
			{
				this.in_current_observation = false;
			}
			if(localName.equals("weather"))
			{
				this.in_weather = false;
			}
			if(localName.equals("temperature_string")) 
			{
				this.in_temperature_string = false;
			}
			if(localName.equals("location"))
			{
				this.in_location = false;
			}
		 } // endElement
		
		public void characters(char ch[], int start, int length) {
			if(this.in_weather)
			{
				myParsedDataSet.setExtractedCondition(new String(ch, start, length));
		    }
			if(this.in_temperature_string)
			{
				myParsedDataSet.setExtractedTemp(new String(ch, start, length));
		    }
			if(this.in_location)
			{
				myParsedDataSet.setLocation(new String(ch, start, length));
			}
		}
	}
	
	public static class WeatherInfo {
		
		private String temperature = null;
	    private String condition = null;
	    private String location = null;
	 
	    public void setExtractedTemp(String extractedString) 
	    {
	    	temperature = extractedString;
	    }
	    
	    public void setExtractedCondition(String extractedString)
	    {
	    	condition = extractedString;
	    }
	        
	    public void setLocation(String extractedString)
	    {
	    	location = extractedString;
	    }
		
		private WeatherInfo() {} //populated by the parser
		
		private static WeatherInfo weatherInfo = new WeatherInfo();
		public static WeatherInfo getWeatherInfo() {
			return weatherInfo;
		}

		public String getTemperature() {
			return temperature;
		}

		public String getCondition() {
			return condition;
		}

		public String getLocation() {
			return location;
		}

		public void setTemperature(String temperature) {
			this.temperature = temperature;
		}

		public void setCondition(String condition) {
			this.condition = condition;
		}
		
		public int fahrenheitToCelsius(int tFahrenheit) {
			return (int) ((5.0f / 9.0f) * (tFahrenheit - 32));
		}

		public int celsiusToFahrenheit(int tCelsius) {
			return (int) ((9.0f / 5.0f) * tCelsius + 32);
		}
	}
	
	//Activity binder
	private final IBinder weatherBinder = new WeatherBinder();
	
    private static Context context;
    
    private static String weatherURL;
	
    public void setWeatherURL(String weatherURL) {
		weatherURL = weatherURL.replace(" ", "%20");
		Weather_Service.weatherURL = weatherURL;
	}
    
    public String getWeatherURL() {
		return Weather_Service.weatherURL;
	}
    
    //Singleton pattern
    private static final Weather_Service weatherService = new Weather_Service();
    public static Weather_Service getService() {
    	return weatherService;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return weatherBinder;
	}
	
	public class WeatherBinder extends Binder {
		Weather_Service getService() {
			return Weather_Service.getService();
		}
	}
	
	public void setApplicationContext(Context mContext) {
		Weather_Service.context = mContext;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(context == null) 
			context = this.getApplicationContext();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WeatherReceiver.ACTION_WEATHERMANAGER);
		
		if(weatherReceiver!=null)
			registerReceiver(weatherReceiver, filter);
		
		Log.d("AWARE", "Weather Service running!");
	}

	@Override
	public void onDestroy() {
		
		Log.d("AWARE","Weather Service terminated...");
		
		super.onDestroy();
	}
}
