package com.ineptech.magicmirror.modules;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ineptech.magicmirror.MainApplication;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeModule extends Module {

	SimpleDateFormat mTimeSDF = new SimpleDateFormat("h:mm a");
	SimpleDateFormat mTimeSDF24 = new SimpleDateFormat("H:mm");
	Boolean use24hr = false;
	CheckBox cbTime;
	
	public TimeModule() {
		super("Time");
		desc = "Display of the current time.";
		defaultTextSize = 144;
		sampleString = "12:34 PM";
		loadConfig();
	}
	
	private void loadConfig() {
    	use24hr = prefs.get("Use24HourTime", false);
    }
	
	@Override
    public void saveConfig() {
    	super.saveConfig();
    	use24hr = cbTime.isChecked();
    	prefs.set("Use24HourTime", use24hr);
    }
	
	@Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();
    	if (cbTime == null)
    		cbTime = new CheckBox(MainApplication.getContext());
    	cbTime.setText("Use military (24 hour) time?");
    	cbTime.setChecked(use24hr);
		configLayout.addView(cbTime);
    }
	
	public void update() {
		if (use24hr)
			tv.setText(mTimeSDF24.format(new Date()));
		else
			tv.setText(mTimeSDF.format(new Date()));
	}
}
