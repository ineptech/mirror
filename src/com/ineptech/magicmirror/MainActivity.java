package com.ineptech.magicmirror;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ineptech.magicmirror.modules.BirthdayModule;
import com.ineptech.magicmirror.modules.CalendarModule;
import com.ineptech.magicmirror.modules.DayModule;
import com.ineptech.magicmirror.modules.FinanceModule;
import com.ineptech.magicmirror.modules.ForecastModule;
import com.ineptech.magicmirror.modules.HolidayModule;
import com.ineptech.magicmirror.modules.Module;
import com.ineptech.magicmirror.modules.TimeModule;
import com.ineptech.magicmirror.modules.TransitModule;
import com.ineptech.magicmirror.modules.WebModule;

public class MainActivity extends Activity {

	private TimeModule mTime;
	private BirthdayModule mBirthday;
	private DayModule mDay;
	private ForecastModule mForecast;
	private TransitModule mTransit;
	private HolidayModule mHoliday;
	private FinanceModule mFinance;
	private CalendarModule mCalendar;
	private CalendarModule.CalendarListener mCalendarListener;
	private WebModule mWeb;
	private List<Module> modules;
	private BrightnessController brightness;
	BroadcastReceiver mTimeBroadcastReceiver;
	public static float textSizeGuessFactor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// resetDefaults();  // uncomment this to clear all settings back to defaults 
		
		// set the flags that make the display full screen and hide the menu bar
		setupMainWindowDisplayMode();

        // create the modules so their config info can be displayed
	    mTime = new TimeModule();
        mBirthday = new BirthdayModule();
        mDay = new DayModule();
        mForecast = new ForecastModule(this.getApplicationContext());
        mHoliday = new HolidayModule();
        mFinance = new FinanceModule();
        mTransit = new TransitModule();    
        mCalendar = new CalendarModule();
        mCalendarListener = new CalendarModule.CalendarListener() {
        	public void onCalendarUpdate(List<String> eventDetails) {
        		if (eventDetails.size() > 0) {    			
        			String newdetails = "";
        			for (int i = 0; i < eventDetails.size() && i < mCalendar.maxItems; i++) {
        				if (i > 0) newdetails += "\n";
        				newdetails += eventDetails.get(i);
        			}
        			mCalendar.tv.setText(newdetails);
        			mCalendar.tv.setVisibility(View.VISIBLE);
        		} else {
        			mCalendar.tv.setVisibility(View.GONE);
        		}
        	}
        };
        mWeb = new WebModule();
        
        // Make a list of the modules
        modules = new ArrayList<Module>();
        modules.add(mTime);
        modules.add(mBirthday);
        modules.add(mDay);
        modules.add(mForecast);
        modules.add(mHoliday);
        modules.add(mFinance);
        modules.add(mCalendar);
        modules.add(mTransit);
        modules.add(mWeb);
        
        
        // Set up the brightness control
        brightness = new BrightnessController(this);
        brightness.makeConfigLayout();
        
        // show the module options panel first thing on load
        showOptions();
        
	}
	
	public void showOptions() {
		// make and display the configuration panel
		LinearLayout optionsPanel = new LinearLayout(this);
		optionsPanel.setOrientation(LinearLayout.VERTICAL);
		TextView title = new TextView(this);
		title.setBackgroundColor(Color.WHITE);
		title.setTextColor(Color.BLACK);
		title.setText("Magic Mirror Setup");
		title.setTextSize(textSizeGuessFactor*36);
		optionsPanel.addView(title);
		optionsPanel.addView(spacer(80));
		optionsPanel.addView(spacer(80));
		for (int i = 0; i < modules.size(); i++) {
			modules.get(i).makeConfigLayout();
			optionsPanel.addView(modules.get(i).configLayout);
			optionsPanel.addView(spacer(80));
		}
		optionsPanel.addView(brightness.configLayout);
		
		Button save = new Button(getApplicationContext());
		save.setText("Save");
		save.setOnClickListener(new OnClickListener() {
			@Override 
			public void onClick(View v) {
				// User is finished with configuration, time to show the mirror content
				// save all the modules' config settings
				for (Module module : modules) {
					module.saveConfig();
				}
				brightness.saveConfig();
				
				// display the main app
				setContentView(R.layout.activity_main);
				initTextViews();
				
		        // This sets up a receiver to do the time TextView and to (try to) update all fields each time the minute changes 
		        mTimeBroadcastReceiver = new BroadcastReceiver() {
		            @Override
		            public void onReceive(Context ctx, Intent intent) {
		                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
		                	//mTimeText.setText(mTimeSDF.format(new Date()));
		                	mTime.update();
		                	setViewState();
		                }
		            }
		        };
		        registerReceiver(mTimeBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		        setViewState();
			}
		});
		optionsPanel.addView(save);
		optionsPanel.addView(spacer(200));
		ScrollView scroller = new ScrollView(this);
		scroller.addView(optionsPanel);
		setContentView(scroller);
	}
	
	private View spacer(int q) {
		TextView v = new TextView(this);
		v.setBackgroundColor(Color.GRAY);
		v.setHeight(q);
		return v;
	}
	
	public void initTextViews() {
		// hook up the modules to the textviews defined in the layout activity_main.xml. 
		// Why do I use a layout xml here, when all of the config stuff is made programmatically? 
		// Because I'm guessing novice android programmers might want to move the main app display textviews around, and may find the 
		// xml easier to use, but making a layout xml for the config option stuff would've been too annoying.
		mTime.tv = (TextView) findViewById(R.id.time_text);
		mBirthday.tv = (TextView) findViewById(R.id.birthday_text);
		mDay.tv = (TextView) findViewById(R.id.day_text);
		mForecast.tv = (TextView) findViewById(R.id.weather_text);
		mHoliday.tv = (TextView) findViewById(R.id.holiday);
		mFinance.tv = (TextView) findViewById(R.id.finance);
		mTransit.tv = (TextView) findViewById(R.id.transit);
		mCalendar.tv = (TextView) findViewById(R.id.calendar);
		mWeb.tv = (TextView) findViewById(R.id.web);
		for (Module module : modules) 
			module.setTextSize();
	}
	
	public void resetDefaults() {
		// Call this anywhere after the modules have been created to return to default textsizes and strings
		MirrorPrefs prefs = new MirrorPrefs(this);
		prefs.clear();
	}
	
	private void setViewState() {
		// this is the main "update" loop, and gets called once per minute by the broadcast receiver
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).enabled())
				modules.get(i).update();
		}
		if (mCalendar != null && mCalendar.enabled()) 
			CalendarModule.getCalendarEvents(this, mCalendarListener);
		brightness.setBrightness();
    }
	
	private void setGuessFactor() {
		// Set a factor by which to multiply text sizes to account for different screen widths 
		textSizeGuessFactor = 1.0f;
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int w = dm.widthPixels; 
		int d = dm.densityDpi;
		float wi=(float)w/(float)d; // width in inches
		textSizeGuessFactor = wi/4.0f;
		// why divide by 4?  This has the effect of making the text size settings go in steps of .25
		// so it allows finer control over text size without dinking around with unwieldy fractions
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setViewState();
    }

	private void setupMainWindowDisplayMode() {
		View decorView = setSystemUiVisilityMode();
		decorView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				setSystemUiVisilityMode(); // This listener avoids exiting immersive_sticky when soft keyboard is displayed
			}
		});
		setGuessFactor();  	// This is an attempt to guess at a good factor to adjust the default text 
							//	sizes by screen size.  No need to worry about scale since setTextSize uses scaled pixels.
	}
	
	private View setSystemUiVisilityMode() {
		View decorView = getWindow().getDecorView();
		int options = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
		  | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
		if (android.os.Build.VERSION.SDK_INT >= 19)
			options = options | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		decorView.setSystemUiVisibility(options);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		return decorView;
	}
	
}
