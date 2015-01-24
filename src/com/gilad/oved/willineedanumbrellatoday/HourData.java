package com.gilad.oved.willineedanumbrellatoday;

import java.util.Date;

import org.json.JSONObject;

public class HourData {
	public int chance;
	public Date time;
	public String icon;
	public JSONObject jsonObj;
	
	public HourData() {
		chance = 0;
		time = new Date();
		icon = "";
		jsonObj = new JSONObject();
	}
}
