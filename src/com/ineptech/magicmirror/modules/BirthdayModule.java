package com.ineptech.magicmirror.modules;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainActivity;
import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.Utils;

public class BirthdayModule extends Module {
	
	private static HashMap<String, String> mBirthdayMap;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/d", Locale.US);
    final String prefsBirthdays = "BirthdayListString";
    final String defaultBirthdays = "12/11,Nick";
    EditText bdayGreeting;
    String bdayGreetingText = "";
    
    public BirthdayModule() {
    	super("Birthdays");
    	desc = "This module shows a birthday greeting for whoever's birthday it happens to be. "
    			+ "This is separate from the Holiday module in case someone in your household was born on a holiday."
    			+ "Use the buttons below to add or remove holidays for whoever you like - hit the X to remove an "
    			+ "existing holiday, or hit the Plus sign to add a new entry for the specified date and name.";
    	mBirthdayMap = new HashMap<>();
    	defaultTextSize = 56;
    	sampleString = "Happy Birthday Buddy!";
    	loadConfig();
    }
    
    private void loadConfig() {
    	String s = prefs.get(prefsBirthdays, defaultBirthdays);
    	String[] bdays = s.split("\\|");
    	mBirthdayMap.clear();
    	for (String b : bdays) {
    		int i = b.indexOf(",");
    		if (i > 0) 
    			mBirthdayMap.put(b.substring(0, i), b.substring(i+1));
    	}
    	bdayGreetingText = prefs.get(name+"_greeting", "Happy Birthday ");
    	
    }
    
    @Override
    public void saveConfig() {
    	super.saveConfig();
    	String bdays = "";
    	for (String bday : mBirthdayMap.keySet()) {
    		if (bdays.length() > 0) 
    			bdays += "|";
    		bdays += bday + "," + mBirthdayMap.get(bday);
    	}
    	prefs.set(prefsBirthdays, bdays);
    	bdayGreetingText = bdayGreeting.getText().toString();
    	prefs.set(name+"_greeting", bdayGreetingText);
    }

    @Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();
    	
    	// add a display of each item in the map
    	for (final String bdate : mBirthdayMap.keySet()) {
    		String btext = mBirthdayMap.get(bdate);
    		Button remove = new Button(MainApplication.getContext());
    		remove.setText("X");
    		remove.setOnClickListener
    		(new View.OnClickListener() {
    			public void onClick(View v) {
    				mBirthdayMap.remove(bdate);
    				saveConfig();
    				makeConfigLayout();
    			}
    		});
    		LinearLayout holder = new LinearLayout(MainApplication.getContext());
    		holder.setOrientation(LinearLayout.HORIZONTAL);
    		TextView bdtv = new TextView(MainApplication.getContext());
    		bdtv.setText(bdate +", "+ btext);
    		holder.addView(bdtv);
    		holder.addView(remove);
    		configLayout.addView(holder);
    	}
    	// widgets for adding a new birthday
    	LinearLayout add = new LinearLayout(MainApplication.getContext());
    	add.setOrientation(LinearLayout.HORIZONTAL);
    	final EditText day = new EditText(MainApplication.getContext());
    	final EditText name = new EditText(MainApplication.getContext());
    	day.setText("12/25");
    	name.setText("Jesus");
    	Button plus = new Button(MainApplication.getContext());
    	plus.setText("+");
    	plus.setOnClickListener
		(new View.OnClickListener() {
			public void onClick(View v) {
				String d = day.getText().toString();
				mBirthdayMap.put(d, name.getText().toString());
				saveConfig();
				makeConfigLayout();
			}
		});
    	LinearLayout addholder = new LinearLayout(MainApplication.getContext());
    	addholder.addView(plus);
    	addholder.addView(day);
    	addholder.addView(name);
    	configLayout.addView(addholder);
    	
    	LinearLayout greetingholder = new LinearLayout(MainApplication.getContext());
    	TextView greettv = new TextView(MainApplication.getContext());
    	greettv.setText("Message prefix: ");
    	bdayGreeting = new EditText(MainApplication.getContext());
    	bdayGreeting.setText(bdayGreetingText);
    	greetingholder.addView(greettv);
    	greetingholder.addView(bdayGreeting);
    	configLayout.addView(greetingholder);
    	
    }
    
    public void update() {
    	String bday = mBirthdayMap.get(simpleDateFormat.format(new Date()));
        if (bday != null) {
        	if (bday.compareTo("Nick & Laura") == 0) {
        		int num = Calendar.getInstance().get(Calendar.YEAR) - 2012;
        		bday = "Happy " +num+ Utils.getDayOfMonthSuffix(num) + " Anniversary,\n"+bday+"!";
        	} else {
        		bday = bdayGreetingText + bday;
        	}
        	tv.setText(bday);
        	tv.setVisibility(TextView.VISIBLE);
        } else {
        	tv.setText("");
        	tv.setVisibility(TextView.GONE);
        }
    }
}
