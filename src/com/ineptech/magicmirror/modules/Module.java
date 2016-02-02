package com.ineptech.magicmirror.modules;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainActivity;
import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.MirrorPrefs;

public class Module {

	/*
	 * Base class for all display modules.  should contain everything that is common to all modules.
	 *  
	 */
	
	public TextView tv;  // the TextView in which the module's output is displayed once the app is running
	String name;
	String desc = "";
	public LinearLayout configLayout;  // Layout to hold all configuration options widgets
	private CheckBox cbEnabled; 
	private Boolean isEnabled;
	MirrorPrefs prefs;
	int defaultTextSize = 40;
	int textSize;
	
	String sampleString = "Sample Output";
	
	public Module(String n) {
		name = n;
		prefs = new MirrorPrefs(MainApplication.getContext());
	}
	
	public void makeConfigLayout() {
		// builds the layout displayed in the config panel on load, where users add or remove stuff
		// super just includes the enabled/disabled checkbox, subclasses can add more config options
		Context context = MainApplication.getContext();
		if (configLayout == null) 
			configLayout = new LinearLayout(context);
		else 
			configLayout.removeAllViews();
		configLayout.setOrientation(LinearLayout.VERTICAL);
		TextView title = new TextView(context);
		title.setText(name + " Module");
		title.setTextSize(32*MainActivity.textSizeGuessFactor);
		TextView description = new TextView(context);
		description.setText(desc);
		description.setTextSize(12*MainActivity.textSizeGuessFactor);
		configLayout.addView(title);
		configLayout.addView(description);
		if (cbEnabled == null)
			cbEnabled = new CheckBox(context);
		isEnabled = prefs.get(name+"_enabled", true);
		cbEnabled.setText("Enable this module?");
		cbEnabled.setChecked(isEnabled);
		configLayout.addView(cbEnabled);
		configLayout.addView(fontSizeWidget());
		
		addBorder(configLayout);
		
	}
	
	public static void addBorder(View v) {
		// Add a border to make the config page look slightly less awful
	    GradientDrawable border = new GradientDrawable();
	    border.setColor(Color.BLACK); 
	    border.setStroke(10, Color.RED); 
	    v.setPadding(14, 14, 14, 14);
	    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
	    	v.setBackgroundDrawable(border);
	    } else {
	    	v.setBackground(border);
	    }
	}
	
	public void saveConfig() {
		// called when the config panel is closed, should save everything to prefs
		isEnabled = cbEnabled.isChecked();
		prefs.set(name+"_enabled", cbEnabled.isChecked());
	}
	
	public void update() {
		// called once per minute to update the module's display content
	}
	
	public Boolean enabled() {
		/// tell whether this module was enabled in the config panel or not
		return isEnabled;
	}
	
	// Generic widget for changing text size of this module's display - also updates sample output on config screen
	private Button textSizePlus;
	private Button textSizeMinus;
	private TextView textSizeTv;
	private TextView sample;
	private LinearLayout fontSizeWidget() {
		textSize = defaultTextSize;
		textSize = prefs.get(name+"_textsize", defaultTextSize);
		Context context = MainApplication.getContext();
		LinearLayout ll = new LinearLayout(context);
		textSizeTv = new TextView(context);
		textSizeTv.setText("text size: " + textSize);
		textSizePlus = new Button(context);
		textSizeMinus = new Button(context);
		textSizePlus.setText("+");
		textSizeMinus.setText("-");
		textSizeMinus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeTextSize(-1);
			}
		});
		textSizePlus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeTextSize(1);
			}
		});
		ll.addView(textSizeMinus);
		ll.addView(textSizeTv);
		ll.addView(textSizePlus);
		
		// add sample output
		LinearLayout holder = new LinearLayout(context);
		holder.setOrientation(LinearLayout.VERTICAL);
		sample = new TextView(context);
		sample.setText(sampleString);
		holder.addView(ll);
		holder.addView(sample);
		setTextSize();
		return holder;
	}
	
	public void changeTextSize(int i) {
		textSize += i;
		if (textSize < 1) 
			textSize = 1;
		setTextSize();
		if (textSizeTv != null) textSizeTv.setText("text size: " + textSize);
		prefs.set(name+"_textsize", textSize);
	}
	
	public void setTextSize() {
		float adjSize = textSize*MainActivity.textSizeGuessFactor;
		if (sample != null)
			sample.setTextSize(adjSize);
		if (tv != null)
			tv.setTextSize(adjSize);
	}
	
}
