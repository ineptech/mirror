package com.ineptech.magicmirror.modules;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.Utils;

public class ForecastModule extends Module {
	
	String apikey = "ApiKeyGoesHere";
	double latitude, latitude_def = 45.5200;
	double longitude, longitude_def = -122.6819;
	private static final long timeBetweenCalls = 10 * 60 * 1000; // 10 minutes
	long lastRan = 0;
	int consecFails = 0;
	String cast = "";
	Boolean useCelsius = false;
	CheckBox cbCelsius;
	
	public ForecastModule(Context context) {
		super("Weather");
		desc = "Shows today's temperatures: \"current (high | low)\" as reported by forecast.io.  "
				+ "This will not work until you go to forecast.io and register to get a (free) api key "
				+ "and enter it in to the box below.  To do so, visit developer.forecast.io and click on Register. "
				+ "Then enter your latitude and longitude (be sure to get the signs right).  I hope to add "
				+ "snow/rain/etc icons at some point, haven't gotten to it yet...";
		defaultTextSize = 72;
		sampleString = "100° (90° | 110°)";
		loadConfig();
	}
	
	public void update() {
		if (consecFails > 9) {
			tv.setText("");
			tv.setVisibility(TextView.GONE);
		} else if (Calendar.getInstance().getTimeInMillis() > (lastRan + timeBetweenCalls)) {
			new ForecastTask(this).execute();
		}
	}
	
    private void loadConfig() {
    	latitude = latitude_def;
    	longitude = longitude_def;
    	String latitude_s = prefs.get(name+"_latitude", ""+latitude_def);
    	String longitude_s = prefs.get(name+"_longitude", ""+longitude_def);
    	try {
    		latitude = Double.parseDouble(latitude_s);
    		longitude = Double.parseDouble(longitude_s);
    	} catch (Exception e) { }
    	apikey = prefs.get(name+"_apikey", apikey);
    	useCelsius = prefs.get(name+"_useCelsius", false);
    }
    
    @Override
    public void saveConfig() {
    	super.saveConfig();
    	prefs.set(name+"_latitude", eLat.getText().toString());
    	prefs.set(name+"_longitude", eLong.getText().toString());
    	prefs.set(name+"_apikey", eApikey.getText().toString());
    	useCelsius = cbCelsius.isChecked();
    	prefs.set(name+"_useCelsius", useCelsius);
    }
    
    private EditText eLong;
    private EditText eLat;
    private EditText eApikey;
    
    @Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();
    	LinearLayout holder = new LinearLayout(MainApplication.getContext());
    	holder.setOrientation(LinearLayout.VERTICAL);
    	TextView tv1 = new TextView(MainApplication.getContext());
    	tv1.setText("Latitude: ");
    	holder.addView(tv1);
    	eLat = new EditText(MainApplication.getContext());
    	eLat.setBackgroundColor(Color.LTGRAY);
    	holder.addView(eLat);
    	TextView tv2 = new TextView(MainApplication.getContext());
    	tv2.setText("Longitude: ");
    	holder.addView(tv2);
    	eLong = new EditText(MainApplication.getContext());
    	eLong.setBackgroundColor(Color.LTGRAY);
    	holder.addView(eLong);
    	TextView tv3 = new TextView(MainApplication.getContext());
    	tv3.setText("Api key (get from forecast.io and copy-paste here): ");
    	holder.addView(tv3);
    	eApikey = new EditText(MainApplication.getContext());
    	eApikey.setBackgroundColor(Color.LTGRAY);
    	holder.addView(eApikey);
    	configLayout.addView(holder);
    	
    	eLat.setText(latitude+"");
    	eLong.setText(longitude+"");
    	eApikey.setText(apikey);
    	
    	eLong.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_DECIMAL);
    	eLat.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_DECIMAL);
    	
    	cbCelsius = new CheckBox(MainApplication.getContext());
    	cbCelsius.setText("Use Celsius for temperature?");
    	cbCelsius.setChecked(useCelsius);
    	holder.addView(cbCelsius);
    }
    
	void set() {
		// TODO: add icons for snow and rain.  To do so:
		// First, update ___ to include a key word, e.g. "umbrella", to the forecast display test.
		// Then, trandform it into an icon here with in this method with code like so:  
			// String keyword = "umbrella";
		    // int i = cast.indexOf(keyword);
			// if (i >= 0)
			//		span.setSpan(isUmbrella, i, i+keyword.length, 0);
		
		SpannableString span = new SpannableString(cast);
		SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append(span);
		tv.setText(builder);
	}
	
}

class ForecastTask extends AsyncTask <Void, Void, String>{

	private ForecastModule module;
	String sampleResponse = "{\"latitude\":45.52,\"longitude\":122.6819,\"timezone\":\"Asia/Harbin\",\"offset\":8,\"currently\":{\"time\":1449289114,\"summary\":\"Clear\",\"icon\":\"clear-day\",\"precipType\":\"snow\",\"temperature\":22.35,\"apparentTemperature\":11.63,\"dewPoint\":2.76,\"humidity\":0.42,\"windSpeed\":10.22,\"windBearing\":304,\"visibility\":10,\"cloudCover\":0,\"pressure\":1026.52},\"daily\":{\"data\":[{\"time\":1449244800,\"summary\":\"Clear throughout the day.\",\"icon\":\"clear-day\",\"sunriseTime\":1449270846,\"sunsetTime\":1449302882,\"moonPhase\":0.8,\"precipType\":\"snow\",\"temperatureMin\":10.48,\"temperatureMinTime\":1449320400,\"temperatureMax\":22.73,\"temperatureMaxTime\":1449291600,\"apparentTemperatureMin\":4.25,\"apparentTemperatureMinTime\":1449316800,\"apparentTemperatureMax\":12.86,\"apparentTemperatureMaxTime\":1449291600,\"dewPoint\":3.95,\"humidity\":0.58,\"windSpeed\":5.86,\"windBearing\":292,\"visibility\":8.33,\"pressure\":1024.88}]},\"flags\":{\"sources\":[\"isd\"],\"isd-stations\":[\"508440-99999\",\"509490-99999\",\"540260-99999\",\"540490-99999\"],\"units\":\"us\"}}";
	String sampleForecastURL = "https://api.forecast.io/forecast/api_key_goes_here/45.5200,-122.6819,2015-12-04T20:18:34-0800?units=us&exclude=minutely,hourly";
	
	public ForecastTask(ForecastModule _module) {
		module = _module;
	}
	
	@Override
	protected String doInBackground(Void... params) {
		if (Utils.debug)
			return sampleResponse;
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		String forecastTime = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		forecastTime += "T" + new SimpleDateFormat("HH:mm:ss-0800").format(new Date());
		String forecastURL = "https://api.forecast.io/forecast/"+module.apikey+"/"+module.latitude+","+module.longitude+",";
		String forecastParams = "?exclude=minutely,hourly";
		if (module.useCelsius)
			forecastParams += "&units=si";
		String wholeURL = forecastURL+forecastTime+forecastParams;
		HttpGet httpGet = new HttpGet(wholeURL);
		String text = null;
		try {
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			text = Utils.getASCIIContentFromEntity(entity);
		} catch (Exception e) {
			module.consecFails++;
			return e.getLocalizedMessage();
		}
		return text;
	}

	protected void onPostExecute(String results) {
		if (results!=null) {
			// TODO: improve parsing of the forecast results
			try {
				System.out.println(results);
				module.cast = parseForecast(results);
				module.set();
			} catch (Exception e) {
				module.consecFails++;
				e.printStackTrace();
			}
		} else {
			module.consecFails++;
		}
	}
	
	String temp = "", high = "", low = "", icon = "";
	String parseForecast (String s) throws Exception {
		String re = ".*temperature\":([\\-0-9\\.]+),.*temperatureMin\":([\\-0-9\\.]+),.*temperatureMax\":([\\-0-9\\.]+),.*";
		Pattern r = Pattern.compile(re);
		Matcher m = r.matcher(s);
		if (m.find()) {
			temp = m.group(1);
			low = m.group(2);
			high = m.group(3);
		}
        int ftemp = Math.round(Float.parseFloat(temp));
		int flow = Math.round(Float.parseFloat(low));
		int fhigh = Math.round(Float.parseFloat(high));
		String cast = ftemp +"° ("+fhigh+"° | "+flow+"°)";
        
		// TODO: Check for weather alerts and display something suitable?
		if (s.contains("Alert")) {
			// do something...
		}
		// TODO: Add icons for snow, rain, etc
		
		return cast;
    }
}
