package com.gilad.oved.willineedanumbrellatoday;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnSeekBarChangeListener {

	TextView topTextView;
	TextView timeTextView;
	ImageView weatherIcon;
	TextView chancePercentageTextView;
	SeekBar timeSeekBar;
	TextView zipCodeTextView;

	HourData[] hourDatas;
	int startIndex;
	int endIndex;
	int minChance = 50;

	JSONObject urlRequestJson;

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

		try {
			urlRequestJson = new JSONObject(getIntent().getStringExtra("json"));
			JSONObject obj = urlRequestJson.getJSONObject("hourly");
			JSONArray allHourlyData = obj.getJSONArray("data");
			if (allHourlyData != null) {
				hourDatas = new HourData[24];
				JSONObject currentHourData = null;
				int percentChance = 0;
				Date time = null;
				for (int i = 0; i < hourDatas.length; i++) {
					hourDatas[i] = new HourData();
					try {
						currentHourData = allHourlyData.getJSONObject(i);
						percentChance = (int) (currentHourData
								.getDouble("precipProbability") * 100.0);
						time = new Date(
								(long) currentHourData.getLong("time") * 1000);
					} catch (JSONException e) {
						e.printStackTrace();
					}

					if (startIndex != -1 && percentChance * 100 < minChance
							&& i - startIndex > 1 && endIndex == -1) {
						endIndex = i;
					}
					if (percentChance * 100 >= minChance && startIndex == -1) {
						startIndex = i;
					}

					hourDatas[i].chance = percentChance;
					hourDatas[i].time = time;
					hourDatas[i].jsonObj = currentHourData;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
		String dateStr = timeFormat.format(hourDatas[0].time);
		chancePercentageTextView.setText(hourDatas[0].chance
				+ "% chance of rain at " + dateStr);

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
		String dateStr = timeFormat.format(hourDatas[progress].time);
		chancePercentageTextView.setText(hourDatas[progress].chance
				+ "% chance of rain at " + dateStr);
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

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}