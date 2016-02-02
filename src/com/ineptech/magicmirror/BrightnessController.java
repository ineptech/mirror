package com.ineptech.magicmirror;

import java.util.ArrayList;
import java.util.Calendar;

import com.ineptech.magicmirror.modules.Module;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BrightnessController {

	String description = "This section is for setting the brightnness of the display.  Getting the brightness "
			+ "right is tricky, but it will make the mirror look much sharper.  Values should range from 0 to 1."
			+ "  Start by setting a default value (about .5 works for me) and then if desired, add dimmer or "
			+ "brighter settings for certain times of day.  \n Fair warning, there is no error checking on this - "
			+ "if you set overlapping time ranges it'll just pick one or the other.\n Why not use the brightness "
			+ "sensor on the tablet, you may ask? In my testing, it's near useless, even on good tablets - the "
			+ "reading it reports just isn't reliable through the mirror.  If it works well on yours, feel free to "
			+ "rewrite all this stuff, but it will probably only work for you (i.e. don't push it to git).";
	
	public LinearLayout configLayout;
    MirrorPrefs prefs;
    private static ArrayList<BrightnessSchedule > scheds;
    Activity activity;
    float defaultBrightness = 0.5f;
    private EditText globalBrightness;
    
    public BrightnessController(Activity a) {
    	activity = a;
    	prefs = new MirrorPrefs(MainApplication.getContext());
    	scheds = new ArrayList<BrightnessSchedule>();
    	loadConfig();
    }
    
    public void makeConfigLayout() {
    	Context context = MainApplication.getContext();
    	if (configLayout == null)
    		configLayout = new LinearLayout(context);
    	else
    		configLayout.removeAllViews();
    	configLayout.setOrientation(LinearLayout.VERTICAL);
    	TextView name = new TextView(context);
    	name.setText("Brightness Controls");
    	name.setTextSize(MainActivity.textSizeGuessFactor*32);
    	TextView desc = new TextView(context);
    	desc.setText(description);
    	configLayout.addView(name);
    	configLayout.addView(desc);
    	
    	// widget for setting default text size
    	LinearLayout defbrholder = new LinearLayout(context);
    	TextView globalTv = new TextView(context);
    	globalTv.setText("Default brightness: ");
    	globalTv.setTextSize(MainActivity.textSizeGuessFactor*26);
    	defbrholder.addView(globalTv);
    	globalBrightness = new EditText(context);
    	globalBrightness.setText("0.5");
    	globalBrightness.setRawInputType(InputType.TYPE_CLASS_NUMBER);
    	globalBrightness.setTextSize(MainActivity.textSizeGuessFactor*26);
    	defbrholder.addView(globalBrightness);
    	Button test = new Button(context);
    	test.setText("Set now");
    	test.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Float f = 0.5f;
				try {f = Float.parseFloat(globalBrightness.getText().toString()); }catch (Exception e) { }
				defaultBrightness = f;
				setBrightness(f);
				globalBrightness.setText(""+f);
			}
		});
    	defbrholder.addView(test);
    	configLayout.addView(defbrholder);
    	
    	// add existing brightness settings
    	for (final BrightnessSchedule sched : scheds) {
    		Button remove = new Button(context);
    		remove.setText("X");
    		LinearLayout removeholder = new LinearLayout(context);
    		
    		TextView removetv = new TextView(context);
    		removetv.setText(sched.toString());
    		removetv.setTextSize(MainActivity.textSizeGuessFactor*26);
    		removeholder.addView(removetv);
    		remove.setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) {
        			scheds.remove(sched);
        			saveConfig();
        			makeConfigLayout();
        		}
        	});
    		removeholder.addView(remove);
    		configLayout.addView(removeholder);
    	}
    	
    	// add widget for setting new scheduled brightness setting
    	TextView addtv = new TextView(context);
    	addtv.setText("Add a new scheduled brightness value: ");
    	addtv.setTextSize(MainActivity.textSizeGuessFactor*16);
    	configLayout.addView(addtv);
    	LinearLayout holder = new LinearLayout(context);
    	holder.setOrientation(LinearLayout.HORIZONTAL);
    	Button plus = new Button(context);
    	plus.setText("+");
    	holder.addView(plus);
    	final EditText addBrightness = new EditText(context);
    	addBrightness.setText("0.1");
    	addBrightness.setRawInputType(Configuration.KEYBOARD_12KEY);
    	addBrightness.setTextSize(MainActivity.textSizeGuessFactor*24);
    	holder.addView(addBrightness);
    	final TimeSelectionWidget tsw = new TimeSelectionWidget();
    	holder.addView(tsw.getLayout());    	
    	plus.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			String begin=tsw.getStartHour()+":"+tsw.getStartMinute();
    			String end=tsw.getEndHour()+":"+tsw.getEndMinute();
    			String prefstr = begin+"-"+end+"="+addBrightness.getText().toString();
    			try {
        			scheds.add(new BrightnessSchedule(prefstr));
        		} catch (Exception e) {}
    			makeConfigLayout();
    		}
    	});
    	Module.addBorder(configLayout);
    	configLayout.addView(holder);
    }
    
    public void loadConfig() {
    	String s = prefs.get("Brightness_settings", "");
    	String[] ss = s.split("\\|");
    	scheds.clear();
    	for (int i = 0; i < ss.length; i++) {
    		try {
    			scheds.add(new BrightnessSchedule(ss[i]));
    		} catch (Exception e) {}
    	}
    	defaultBrightness = 0.5f;
    	try { 
    		defaultBrightness = Float.parseFloat(prefs.get("Brightness_default", ""));
    	} catch (Exception e) { }
    }
    
    public void saveConfig() {
    	String s = "";
    	for (int i = 0; i < scheds.size(); i++) {
    		if (i > 0) 
    			s+= "|";
    		s += scheds.get(i).toString();
    	}
    	prefs.set("Brightness_settings", s);
    	float f = defaultBrightness;
    	try {
    		f = Float.parseFloat(globalBrightness.getText().toString());
    	} catch (Exception e) { }
    	prefs.set("Brightness_default", ""+f);
    }
    
    public void setBrightness() {
    	Calendar now = Calendar.getInstance();
    	int hour = now.get(Calendar.HOUR_OF_DAY);
    	int min = now.get(Calendar.MINUTE);
    	BrightnessSchedule setit = null; 
    	for (BrightnessSchedule sched : scheds) {
    		if (sched.contains(hour, min)) {
    			setit = sched;
    		}
    	}
    	Float f = defaultBrightness;
    	if (setit != null)
    		f = setit.b;
    	setBrightness(f);
    }
    
    
    public void setBrightness(Float f) {
    	WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
    	lp.screenBrightness = f;
    	activity.getWindow().setAttributes(lp);
    }
}

class BrightnessSchedule {
	float b;
	int begin, end;
	public BrightnessSchedule(String s) {
		String[] s1 = s.split("=");
		b = Float.parseFloat(s1[1]);
		if (b < 0.01f)
			b = 0.01f;
		if (b > 1f)
			b = 1f;
		String[] s2 = s1[0].split("-");
		String[] beginstr = s2[0].split(":"), endstr = s2[1].split(":");
		int beginh = Integer.parseInt(beginstr[0]);
		int beginm = Integer.parseInt(beginstr[1]);
		int endh = Integer.parseInt(endstr[0]);
		int endm = Integer.parseInt(endstr[1]);
		begin = 60*beginh + beginm;
		end = 60*endh + endm;
	}
	public Boolean contains(int h, int m) { 
		if (begin > end) {
			// this time range wraps around midnight
			int n = 60*h + m;
			if (n >= begin || n <= end) 
				return true;
		} else {
			// this time range does not wrap around midnight
			int n = 60*h + m;
			if (n >= begin && n <= end) 
				return true;
		}
		return false;
	}
	public String toString() {
		String s = "";
		s += begin/60 + ":" + begin%60;
		s += "-" + end/60 + ":" + end%60;
		s += "=" + b;
		return s;
	}
}