package com.gilad.oved.willineedanumbrellatoday;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class SplashScreen extends Activity {

    Location location;
    JSONArray hourlyData;
    HourData[] hourDatas;
    Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		setTitle("Will I Need An Umbrella Today?");
	
		handler = new Handler();
		hourlyData = new JSONArray();
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crta = new Criteria();
		crta.setAccuracy(Criteria.ACCURACY_COARSE);
		crta.setPowerRequirement(Criteria.POWER_LOW);
		String provider = locationManager.getBestProvider(crta, true);

		if (provider != null) {
			locationManager.requestLocationUpdates(provider, 1000, 0,
					new LocationListener() {

						@Override
						public void onStatusChanged(String provider,
								int status, Bundle extras) {

						}

						@Override
						public void onProviderEnabled(String provider) {

						}

						@Override
						public void onProviderDisabled(String provider) {

						}

						@Override
						public void onLocationChanged(Location location) {

						}
					});
			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				updateWeatherData(location);
			}
		}
	}
	
	private void updateWeatherData(final Location loc) {
		new Thread() {
			public void run() {
				final JSONObject json = RemoteFetch.getJSON(SplashScreen.this, loc);
				if (json != null) {
					handler.post(new Runnable() {
						public void run() {
							Intent intent = new Intent(SplashScreen.this, MainActivity.class);
							intent.putExtra("json", json.toString());
							startActivity(intent);
							finish();
						}
					});
				}
			}
		}.start();
	    
	}

}

class RemoteFetch {
		 
	    private static final String WEATHER_URL_API = "https://api.forecast.io/forecast/16aeaee30417ff6e26c30421b8e0bbe9/";
	     
	    public static JSONObject getJSON(Context context, Location loc){
	        try {
	        	double lat = loc.getLatitude();
	        	double lng = loc.getLongitude();
	        	//lat = 47.614848;
	        	//lng = -122.3359059;
	        	String urlStr = WEATHER_URL_API + lat + ","  + lng;
	        	Log.d("Gilad", urlStr);
	            URL url = new URL(urlStr);           
	            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	             
	            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	             
	            StringBuffer json = new StringBuffer(1024);
	            String tmp="";
	            while((tmp=reader.readLine())!=null)
	                json.append(tmp).append("\n");
	            reader.close();
	             
	            JSONObject data = new JSONObject(json.toString());

	            return data;
	        }catch(Exception e){
	            return null;
	        }
	    }   
	}
