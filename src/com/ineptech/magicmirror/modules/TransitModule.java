package com.ineptech.magicmirror.modules;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.R;
import com.ineptech.magicmirror.Utils;

public class TransitModule extends Module{

	int consecFails = 0;
	ImageSpan isBus;
	ImageSpan isTrain;
	String busResult = "";
	String trainResult = "";
	
	ArrayList<TransitModuleItem> items;
	
	// These are sample URLs for a bus and train in Portland, OR.  You'll need to research the transit service 
	// in your city and provide a URL and regex that works in your area.
	String defaultUrl1 = "https://developer.trimet.org/ws/V1/arrivals?locIDs=4466&route=44&appID=ApiKeyGoesHere";
	String defaultUrl2 = "https://developer.trimet.org/ws/V1/arrivals?locIDs=11502&route=190&appID=ApiKeyGoesHere";
	String defaultRegex = "estimated=\"(\\d+)\"";
	
	public TransitModule() {
		super("Mass Transit");
		desc = "Shows the next arrival time for buses and trains.  To add a transit item:"
				+ "\n * First enter a name (\"Bus\" will be replaced with a bus icon, and "
				+ "\"Train\" with a train icon)\n * Next, find out what URL you should use "
				+ "to get the next arrival time of your bus or train (consult the \"Developers\" "
				+ "section of your transit service website) and paste in the URL field"
				+ "\n * Finally, enter a regular expression (regex) in the last field that extracts "
				+ "the arrival time (in epoch time) from the transit service API response.  "
				+ "\n This can be tricky to configure - see github.com/ineptech/mirror for more info.";
		defaultTextSize = 72;
		sampleString = "Train: 17m  Bus: 23m";
		items = new ArrayList<>();
		loadConfig();
		
	}
	
	public void setIconSizes() {
		Context context = MainApplication.getContext();
		int iconSize = Math.round(tv.getTextSize());
		Drawable iconBus = context.getResources().getDrawable(R.drawable.bus);
		iconBus.setBounds(0,0,iconSize,iconSize);
		isBus = new ImageSpan(iconBus);		
		Drawable iconTrain = context.getResources().getDrawable(R.drawable.train);
		iconTrain.setBounds(0,0,iconSize,iconSize);
		isTrain = new ImageSpan(iconTrain);
	}

	public void update() {
		if (consecFails > 9 || (!Utils.isTimeForWork() && !Utils.debug)) {
			tv.setText("");
			tv.setVisibility(TextView.GONE);
		} else {
			tv.setVisibility(TextView.VISIBLE);
			for (TransitModuleItem item : items) {
				new TransitTask(item).execute();
			}
		}
	}
	
	void set() {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		for (TransitModuleItem item : items) {
			if (item.text.length() > 0) {
				String s = item.name + " " +item.text + "   ";
				SpannableString ss = new SpannableString(s);
				int b = s.indexOf("Bus");
				if (b >= 0)
					ss.setSpan(isBus, b, b+3, 0);
				int t = s.indexOf("Train");
				if (t >= 0)
					ss.setSpan(isTrain, t, t+5, 0);
				builder.append(ss);
			}
		}
		tv.setText(builder);
	}
	
	@Override
	public void setTextSize() {
		super.setTextSize();
		if (tv != null) 
			setIconSizes();
	}
	
    @Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();

    	// add a display of each item in the list
    	for (final TransitModuleItem item : items) {
    		Button remove = new Button(MainApplication.getContext());
    		remove.setText("X");
    		remove.setOnClickListener
    		(new View.OnClickListener() {
    			public void onClick(View v) {
    				items.remove(item);
    				makeConfigLayout();
    			}
    		});
    		LinearLayout holder = new LinearLayout(MainApplication.getContext());
    		holder.setOrientation(LinearLayout.HORIZONTAL);
    		TextView hdtv = new TextView(MainApplication.getContext());
    		hdtv.setText(item.name);
    		holder.addView(hdtv);
    		holder.addView(remove);
    		configLayout.addView(holder);
    	}
    	// widgets for adding a new Transit item
    	final EditText addname = new EditText(MainApplication.getContext());
    	addname.setText("Train work");
    	final EditText addurl = new EditText(MainApplication.getContext());
    	addurl.setText(defaultUrl2);
    	final EditText addregex = new EditText(MainApplication.getContext());
    	addregex.setText(defaultRegex);
    	Button plus = new Button(MainApplication.getContext());
    	plus.setText("+");
    	final TransitModule m = this;
    	plus.setOnClickListener
		(new View.OnClickListener() {
			public void onClick(View v) {
				items.add(new TransitModuleItem(m, addname.getText().toString(), addurl.getText().toString(),addregex.getText().toString()));
				makeConfigLayout();
			}
		});
    	LinearLayout addholder = new LinearLayout(MainApplication.getContext());
    	addholder.setOrientation(LinearLayout.VERTICAL);
    	addholder.addView(addname);
    	addholder.addView(addurl);
    	addholder.addView(addregex);
    	LinearLayout addholder2 = new LinearLayout(MainApplication.getContext());
    	addholder2.addView(plus);
    	addholder2.addView(addholder);
    	// Add a test button
    	Button test = new Button(MainApplication.getContext());
    	final TextView testresult = new TextView(MainApplication.getContext());
    	test.setText("Test");
    	test.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			testresult.setText("Sending request...");
    			new TransitTask(addurl.getText().toString(), addregex.getText().toString(), testresult).execute();
			}
    	});
    	addholder.addView(test);
    	addholder.addView(testresult);
    	
    	
    	if (items.size() < 9)
    		configLayout.addView(addholder2);
    }

	
	void loadConfig() {
		// replace the "items" arraylist with what's stored in prefs (or if that's empty, with the default values)
		items = new ArrayList<>();
		for (int i = 1; i < 10; i++) {
			String prefname = "TransitItem_"+i;
			String name = prefs.get(prefname +"_name", "");
			String url = prefs.get(prefname +"_url", "");
			String regex = prefs.get(prefname +"_regex", "");
			if (name.length() > 0 && url.length() > 0 && regex.length() > 0) {
				items.add(new TransitModuleItem(this, name, url, regex));
			} 
		}
		if (items.size() == 0) {
			// add defaults
			items.add(new TransitModuleItem(this, "Bus", defaultUrl1, defaultRegex));
			//items.add(new TransitModuleItem(this, "Train", defaultUrl1, defaultRegex));
		}
	}
	
	@Override
	public void saveConfig() {
		super.saveConfig();
		for (int i = 1; i < 10; i++) {
			prefs.remove("TransitItem_"+i+"_name");
			prefs.remove("TransitItem_"+i+"_url");
			prefs.remove("TransitItem_"+i+"_regex");
		}
		for (int i = 1; i <= items.size(); i++) {
			TransitModuleItem item = items.get(i-1);
			prefs.set("TransitItem_"+i+"_name", item.name);
			prefs.set("TransitItem_"+i+"_url", item.url);
			prefs.set("TransitItem_"+i+"_regex", item.regex);
		}
	}
}

class TransitModuleItem {
	TransitModule module;
	String name;
	String url;
	String regex;
	String text = "";
	
	public TransitModuleItem(TransitModule _module, String _name, String _url, String _regex) {
		module = _module;
		name = _name;
		url = _url;
		regex = _regex;
	}
	
	void taskFailure() {
		module.consecFails++;
	}
}

// Task to query the transit API and return results to the TransitModuleItem
class TransitTask extends AsyncTask <Void, Void, String> {
	
	private TransitModuleItem item;	
	private Boolean test = false;
	private TextView testResponse;
	private String testUrl;
	private String testRegex;
	
	public TransitTask(TransitModuleItem _item) { // constructor for mirror display view
		item = _item;
	}
	public TransitTask(String _url, String _regex, TextView _r) { // constructor for config screen view
		test = true;
		testUrl = _url;
		testRegex = _regex;
		testResponse = _r;
	}
	
	@Override
	protected String doInBackground(Void... params) {
		String url="";
		if (test)
			url = testUrl;
		else
			url = item.url;
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(url);
		String text = null;
		try {
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			text = Utils.getASCIIContentFromEntity(entity);
		} catch (Exception e) {
			item.taskFailure();
			return e.getLocalizedMessage();
		}
		return text;
	}

	protected void onPostExecute(String results) {
		if (results!=null) {
			if (test) { 
				// Config screen is open - update the config screen results textview
				String parsedresults = Utils.parseTransitResult(results, testRegex);
				if (parsedresults.length() == 0)
					testResponse.setText("Got a response but couldn't apply the regex. Test in a regex tester?  Response was: \n"+results);
				else
					testResponse.setText("(Apparently) success!  ETA for this transit item is: \n" +parsedresults);
			} else {
				// mirror display is open - update the textview in the transitmoduleitem
				String text = Utils.parseTransitResult(results, item.regex);
				if (text.length() > 0) {
		    		item.module.consecFails = 0;
		    		item.text = text;
		    		item.module.set();
		    	}
			}
		} else {   
			if (test)
				testResponse.setText("No response.  Try testing the url in a browser?");
			else
				item.taskFailure();
		}
	}	
}
