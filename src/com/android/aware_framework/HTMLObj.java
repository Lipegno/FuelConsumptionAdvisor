package com.android.aware_framework;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HTMLObj {
	
	public HTMLObj(){}
	
	public static boolean dataPOST(String url, ArrayList<NameValuePair> data) {
		try {
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(data));		
			HttpResponse httpRequest = httpClient.execute(httpPost);
			
			if(httpRequest.getStatusLine().getStatusCode()==200) { //make sure the server replyed OK
				return true;
			}
			
		}catch (UnsupportedEncodingException e) {
			return false;
		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		
		return false;
	}
	
	public static String dataGET(String url) {
		String output = "";
		
		try {
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
			output = httpClient.execute(httpGet,responseHandler);
			
		} catch (ClientProtocolException e) {
			Log.w("AWARE",e.toString());
		} catch (IOException e) {
			Log.w("AWARE",e.toString());
		}
		
		return output;
	}
}
