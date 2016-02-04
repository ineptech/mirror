package com.ineptech.magicmirror.modules;

import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.Utils;

public class WebModule extends Module {

	private static final long timeBetweenCalls = 10 * 60 * 1000; // Only update every 10 minutes
	long lastRan = 0;
	int consecFails = 0;
	public String mUrl; // list of stock tickers currently configured to be displayed
    final String prefsUrl = "WebUrlString";
    final String defaultUrl = "";
    
	public WebModule() {
		super("Web Module");
		desc = "This module simply pulls the specified web page(s) and displays whatever it finds.  I mean *everything* it finds "
				+ "there, the entire file -  make sure whatever the url points to is limited to a brief message.  This allows "
				+ "you to add arbitrary text in to the mirror display.  Try it out with http://ineptech.com/test.html if you like.";
		defaultTextSize = 40;
		sampleString = "Arbitrary Web Content";
		mUrl = "";
    	loadConfig();
	}
	
	private void loadConfig() {
    	mUrl = prefs.get(prefsUrl, defaultUrl);
    }
	
    @Override
    public void saveConfig() {
    	super.saveConfig();
    	prefs.set(prefsUrl, mUrl);
    }

    @Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();
    	
    	// add a display of each item in the map
    	if (mUrl.length() > 0) {
    		Button remove = new Button(MainApplication.getContext());
    		remove.setText("X");
    		remove.setOnClickListener
    		(new View.OnClickListener() {
    			public void onClick(View v) {
    				mUrl = "";
    				saveConfig();
    				makeConfigLayout();
    			}
    		});
    		LinearLayout holder = new LinearLayout(MainApplication.getContext());
    		holder.setOrientation(LinearLayout.HORIZONTAL);
    		TextView hdtv = new TextView(MainApplication.getContext());
    		hdtv.setText(mUrl);
    		holder.addView(hdtv);
    		holder.addView(remove);
    		configLayout.addView(holder);
    	}
    	// widgets for adding a new Url
    	if (mUrl.length() == 0) {
	    	final EditText addurl = new EditText(MainApplication.getContext());
	    	addurl.setText("http://yoursite.com/page.txt");
	    	Button plus = new Button(MainApplication.getContext());
	    	plus.setText("+");
	    	plus.setOnClickListener
			(new View.OnClickListener() {
				public void onClick(View v) {
					mUrl = addurl.getText().toString();
					saveConfig();
					makeConfigLayout();
				}
			});
	    	LinearLayout addholder = new LinearLayout(MainApplication.getContext());
	    	addholder.addView(plus);
	    	addholder.addView(addurl);
	    	configLayout.addView(addholder);
    	}
    	
    	
    }
    
	public void update() {
		if (consecFails > 9) {
			tv.setText("");
			tv.setVisibility(TextView.GONE);
		} else if (Calendar.getInstance().getTimeInMillis() > (lastRan + timeBetweenCalls)) {
			tv.setVisibility(TextView.VISIBLE);
			new WebTask(this).execute();
		}
	}
	
	public void newText(String s) {
		// for now, just overwrite every time this is called
		Spanned span = Html.fromHtml(s);
		tv.setText(span);
		tv.setText(s);
	}
}

class WebTask extends AsyncTask <Void, Void, String>{

	private WebModule module;
	
	public WebTask(WebModule _module) {
		module = _module;
	}

	@Override
	protected String doInBackground(Void... params) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		
		String text = "";
		
		if (module.mUrl.length() > 0) {
			try {
				String urlStr = module.mUrl;
				URL url = new URL(urlStr);
				URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
				url = uri.toURL();
				// the above looks convoluted, but is necessary to get the urlencoding correct
				HttpGet httpGet = new HttpGet(uri);  
				HttpResponse response = httpClient.execute(httpGet, localContext);
				HttpEntity entity = response.getEntity();
				text += "\n" + Utils.getASCIIContentFromEntity(entity);
				
			} catch (Exception e) {	}
		}
		return text;
	}

	protected void onPostExecute(String results) {
		if (results!=null) {
			module.newText(results);
		} else {
			module.consecFails++;
		}
	}
}
