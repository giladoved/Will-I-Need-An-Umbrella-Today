package com.gilad.oved.willineedanumbrellatoday;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity implements OnSeekBarChangeListener {

    Handler handler;
    TextView topTextView;
    TextView timeTextView;
    ImageView weatherIcon;
    TextView chancePercentageTextView;
    SeekBar timeSeekBar;
    TextView zipCodeTextView;
    
    GoogleApiClient googleApiClient;
    Location location;
    JSONArray hourlyData;
    HourData[] hourDatas;
    int startIndex;
    int endIndex;
    int minChance = 50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("Will I Need An Umbrella Today?");
		
		topTextView = (TextView) findViewById(R.id.topTextView);
		timeTextView = (TextView) findViewById(R.id.timeTextView);
		weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
		chancePercentageTextView = (TextView) findViewById(R.id.chancePercentageTextView);
		timeSeekBar = (SeekBar) findViewById(R.id.timeSeekBar);
		zipCodeTextView = (TextView) findViewById(R.id.zipCodeTextView);
		
		timeSeekBar.setMax(23);
		timeSeekBar.setOnSeekBarChangeListener(this);
		
		startIndex = -1;
		endIndex = -1;
		
		handler = new Handler();
		hourlyData = new JSONArray();
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria crta = new Criteria();
		crta.setAccuracy(Criteria.ACCURACY_FINE);
		crta.setAltitudeRequired(true);
		crta.setBearingRequired(true);
		crta.setCostAllowed(true);
		crta.setPowerRequirement(Criteria.POWER_LOW);
		String provider = locationManager.getBestProvider(crta, true);
		Log.d("", "provider : " + provider);   

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
				Log.d("Gilad", "lastLocation" + location.getLatitude() + ", "
						+ location.getLongitude());
				updateWeatherData(location);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
		String dateStr = timeFormat.format(hourDatas[progress].time); 
		chancePercentageTextView.setText(hourDatas[progress].chance
				+ "% chance of rain at "
				+ dateStr);
		seekBar.setProgress(progress);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//initing the location services
	/*
	 * protected synchronized void buildGoogleApiClient() { Log.d("Gilad",
	 * "buidling"); googleApiClient = new GoogleApiClient.Builder(this)
	 * .addConnectionCallbacks(this) .addOnConnectionFailedListener(this)
	 * .addApi(LocationServices.API) .build(); }
	 */

	private void updateWeatherData(final Location loc) {
		new Thread() {
			public void run() {
				final JSONObject json = RemoteFetch.getJSON(MainActivity.this,
						loc);
				try {
					JSONObject obj = json.getJSONObject("hourly");
					hourlyData = obj.getJSONArray("data");
					if (hourlyData != null) {
						hourDatas = new HourData[24];
						JSONObject currentHourData = null;
						int percentChance = 0;
						Date time = null;
						for (int i = 0; i < hourDatas.length; i++){
							hourDatas[i] = new HourData();
							try {
								currentHourData = hourlyData.getJSONObject(i);
								percentChance = (int)(currentHourData.getDouble("precipProbability") * 100.0);
								//Log.d("Gilad", i + ") " + percentChance + " > " + minChance);
								if (startIndex != -1 && percentChance < minChance && i-startIndex > 1 && endIndex == -1) {
									endIndex = i;
								}
								if (percentChance >= minChance && startIndex == -1) {
									startIndex = i;
								}
								//Log.d("Gilad", "start: " + startIndex + " and end: " + endIndex);
								time = new Date((long) currentHourData.getLong("time") * 1000);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							hourDatas[i].chance = percentChance;
							hourDatas[i].time = time;
							hourDatas[i].jsonObj = currentHourData;
						}
												
						handler.post(new Runnable() {
							public void run() {
								SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
								String dateStr = timeFormat.format(hourDatas[0].time); 
								chancePercentageTextView.setText(hourDatas[0].chance
										+ "% chance of rain at "
										+ dateStr);
								
								if (startIndex != -1 && endIndex != -1) {
									topTextView.setText("You will need an umbrella today from"); 
									String startTime = timeFormat.format(hourDatas[startIndex].time);
									String endTime = timeFormat.format(hourDatas[endIndex].time);
									timeTextView.setText(startTime + "-" + endTime);
									weatherIcon.setImageResource(R.drawable.rain);
								} else {
									topTextView.setText("You will NOT need an umbrella today :)");
									timeTextView.setText("Little chance of rain");
									weatherIcon.setImageResource(R.drawable.sun);
								}
							}
						});
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	    
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
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