package com.ineptech.magicmirror.modules;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainApplication;

public class HolidayModule extends Module {

	private static HashMap<String, String> mHolidayMap;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/d", Locale.US);
    final String prefsHolidays = "HolidayListString";
    final String defaultHolidays = "1/1,Happy New Year!|2/14,Happy Valentine's Day!|3/17,Happy St. Patrick's Day!|5/25,Happy Memorial Day!|6/5,Happy Donut Day!|7/4,Happy Independence Day!|10/31,Happy Halloween!|12/24,Santa Claus is coming...|12/25,Merry Christmas!";
    Boolean usePresetHolidaysThanksgiving = true;
    Boolean usePresetHolidaysLaborday = true;
    Boolean usePresetHolidaysMothersAndFathersDay = true;
    CheckBox cbPresetHolidaysThanksgiving;
    CheckBox cbPresetHolidaysLaborday;
    CheckBox cbPresetHolidaysMothersAndFathersDay;
    
    public HolidayModule() {
    	super("Holidays");
    	desc = "Display any message you like on specific dates.  Use the buttons below to remove existing holidays "
    			+ "or add new ones.\n Note: currently, four holidays  (Thanksgiving, Labor Day (US), "
    			+ "Mother's Day and Father's Day) are hard-coded.  This is because they fall on varying dates (e.g. "
    			+ "3rd Thursday in November) and I am too lazy to make the UI widgets necessary to configure stuff "
    			+ "like that.  If this is a problem, there are checkboxes to disable them.";
    	defaultTextSize = 64;
    	sampleString = "Merry Festivus!";
    	mHolidayMap = new HashMap<>();
    	loadConfig();
    }
    
    private void loadConfig() {
    	String s = prefs.get(prefsHolidays, defaultHolidays);
    	String[] days = s.split("\\|");
    	mHolidayMap.clear();
    	for (String d : days) {
    		int i = d.indexOf(",");
    		if (i > 0) 
    			mHolidayMap.put(d.substring(0, i), d.substring(i+1));
    	}
    	usePresetHolidaysThanksgiving = prefs.get(name+"_usePresetHolidaysThanksgiving", true);
    	usePresetHolidaysLaborday = prefs.get(name+"_usePresetHolidaysLaborday", true);
    	usePresetHolidaysMothersAndFathersDay = prefs.get(name+"_usePresetHolidaysMothersAndFathersDay", true);
    }
    
    @Override
    public void saveConfig() {
    	super.saveConfig();
    	String days = "";
    	for (String day : mHolidayMap.keySet()) {
    		if (days.length() > 0) 
    			days += "|";
    		days += day + "," + mHolidayMap.get(day);
    	}
    	prefs.set(prefsHolidays, days);
    	usePresetHolidaysThanksgiving = cbPresetHolidaysThanksgiving.isChecked();
    	usePresetHolidaysLaborday = cbPresetHolidaysLaborday.isChecked();
    	usePresetHolidaysMothersAndFathersDay = cbPresetHolidaysMothersAndFathersDay.isChecked();
    	prefs.set(name+"_usePresetHolidaysThanksgiving", usePresetHolidaysThanksgiving);
    	prefs.set(name+"_usePresetHolidaysLaborday", usePresetHolidaysLaborday);
    	prefs.set(name+"_usePresetHolidaysMothersAndFathersDay", usePresetHolidaysMothersAndFathersDay);
    }

    @Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();
    	
    	// Options to disable hardcoded holidays
    	cbPresetHolidaysThanksgiving = new CheckBox(MainApplication.getContext());
    	cbPresetHolidaysLaborday = new CheckBox(MainApplication.getContext());
    	cbPresetHolidaysMothersAndFathersDay = new CheckBox(MainApplication.getContext());
    	cbPresetHolidaysThanksgiving.setText("Include Thanksgiving? " );
    	cbPresetHolidaysLaborday.setText("Include US Labor Day? " );
    	cbPresetHolidaysMothersAndFathersDay.setText("Include Mothers' Day and Fathers' Day? " );
    	cbPresetHolidaysThanksgiving.setChecked(usePresetHolidaysThanksgiving);
    	cbPresetHolidaysLaborday.setChecked(usePresetHolidaysLaborday);
    	cbPresetHolidaysMothersAndFathersDay.setChecked(usePresetHolidaysMothersAndFathersDay);
    	configLayout.addView(cbPresetHolidaysThanksgiving);
		configLayout.addView(cbPresetHolidaysLaborday);
		configLayout.addView(cbPresetHolidaysMothersAndFathersDay);
		
    	// add a display of each item in the map
    	for (final String date : mHolidayMap.keySet()) {
    		String text = mHolidayMap.get(date);
    		Button remove = new Button(MainApplication.getContext());
    		remove.setText("X");
    		remove.setOnClickListener
    		(new View.OnClickListener() {
    			public void onClick(View v) {
    				mHolidayMap.remove(date);
    				saveConfig();
    				makeConfigLayout();
    			}
    		});
    		LinearLayout holder = new LinearLayout(MainApplication.getContext());
    		holder.setOrientation(LinearLayout.HORIZONTAL);
    		TextView hdtv = new TextView(MainApplication.getContext());
    		hdtv.setText(date +", "+ text);
    		holder.addView(hdtv);
    		holder.addView(remove);
    		configLayout.addView(holder);
    	}
    	// widgets for adding a new Holiday
    	LinearLayout add = new LinearLayout(MainApplication.getContext());
    	add.setOrientation(LinearLayout.HORIZONTAL);
    	final EditText day = new EditText(MainApplication.getContext());
    	final EditText name = new EditText(MainApplication.getContext());
    	day.setText("7/15");
    	name.setText("Happy St. Swithin's Day");
    	Button plus = new Button(MainApplication.getContext());
    	plus.setText("+");
    	plus.setOnClickListener
		(new View.OnClickListener() {
			public void onClick(View v) {
				String d = day.getText().toString();
				mHolidayMap.put(d, name.getText().toString());
				saveConfig();
				makeConfigLayout();
			}
		});
    	LinearLayout addholder = new LinearLayout(MainApplication.getContext());
    	addholder.addView(plus);
    	addholder.addView(day);
    	addholder.addView(name);
    	
    	configLayout.addView(addholder);
    	
    	
    }
    

    /*
    
    CheckBox cbPresetHolidaysThanksgiving;
    CheckBox cbPresetHolidaysLaborday;
    CheckBox cbPresetHolidaysMothersAndFathersDay;
    
     */
    public void update() {
    	
        String hday = "";
        Calendar cal = Calendar.getInstance();
        if (usePresetHolidaysMothersAndFathersDay) {
        	if (cal.get(Calendar.MONTH)== Calendar.MAY && cal.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY 
    				&& cal.get(Calendar.DAY_OF_MONTH) >7 && cal.get(Calendar.DAY_OF_MONTH) <15)
        		hday = "Happy Mother's Day!";
        	if (cal.get(Calendar.MONTH)== Calendar.JUNE && cal.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY 
    				&& cal.get(Calendar.DAY_OF_MONTH) >14 && cal.get(Calendar.DAY_OF_MONTH) <22)
        		hday = "Happy Father's Day!";
        }
        if (usePresetHolidaysLaborday) {
        	if (cal.get(Calendar.MONTH)== Calendar.SEPTEMBER && cal.get(Calendar.DAY_OF_WEEK)== Calendar.MONDAY
    				&& cal.get(Calendar.DAY_OF_MONTH) <8)
        		hday = "Happy Labor Day!";
        }
        if (usePresetHolidaysThanksgiving) {
        	if (cal.get(Calendar.MONTH)== Calendar.NOVEMBER && cal.get(Calendar.DAY_OF_WEEK)== Calendar.THURSDAY
        			&& cal.get(Calendar.DAY_OF_MONTH) >21 && cal.get(Calendar.DAY_OF_MONTH) <29)
        		hday = "Happy Thanksgiving!";
        }
        // Override those if necessary with the user defined holidays
        hday = mHolidayMap.get(simpleDateFormat.format(new Date()));
        
        if (hday != null) {
        	tv.setText(hday);
        	tv.setVisibility(TextView.VISIBLE);
        } else {
        	tv.setText("");
        	tv.setVisibility(TextView.GONE);
        }
    }
}
