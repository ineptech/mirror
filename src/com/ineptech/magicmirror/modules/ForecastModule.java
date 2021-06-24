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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.InputType;
import android.text.style.ImageSpan;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.R;
import com.ineptech.magicmirror.Utils;

public class ForecastModule extends Module {
	
	String apikey = "Go_to_darksky.net/dev_and_register_for_apikey_and_enter_here";
	double latitude, latitude_def = 45.5200;
	double longitude, longitude_def = -122.6819;
	private static final long timeBetweenCalls = 10 * 60 * 1000; // 10 minutes
	long lastRan = 0;
	int consecFails = 0;
	String cast = "";
	Boolean useCelsius = false;
	CheckBox cbCelsius;
	ImageSpan isRain, isClearDay, isClearNight, isCloudy, isFog, isPartlyCloudyDay, isPartlyCloudyNight, isSleet, isSnow, isWind;

	
	public ForecastModule(Context context) {
		super("Weather");
		desc = "Shows today's temperatures: \"current (high | low)\" as reported by forecast.io.  "
				+ "This will not work until you go to forecast.io and register to get a (free) api key "
				+ "and enter it in to the box below.  To do so, visit developer.forecast.io and click on Register. "
				+ "Then enter your latitude and longitude (be sure to get the signs right).  I hope to add "
				+ "snow/rain/etc icons at some point, haven't gotten to it yet...";
		defaultTextSize = 72;
		sampleString = "100� (90� | 110�)";
		loadConfig();
	}

	public void setIconSizes() {
		Context context = MainApplication.getContext();
		int iconSize = Math.round(tv.getTextSize());

		//make each icon
		Drawable iconClearDay = context.getResources().getDrawable(R.drawable.clear_day);
		iconClearDay.setBounds(0,0,iconSize,iconSize);
		isClearDay = new ImageSpan(iconClearDay);

		Drawable iconClearNight = context.getResources().getDrawable(R.drawable.clear_night);
		iconClearNight.setBounds(0,0,iconSize,iconSize);
		isClearNight = new ImageSpan(iconClearNight);

		Drawable iconCloudy = context.getResources().getDrawable(R.drawable.cloudy);
		iconCloudy.setBounds(0,0,iconSize,iconSize);
		isCloudy = new ImageSpan(iconCloudy);

		Drawable iconFog = context.getResources().getDrawable(R.drawable.fog);
		iconFog.setBounds(0,0,iconSize,iconSize);
		isFog = new ImageSpan(iconFog);

		Drawable iconPartlyCloudyDay = context.getResources().getDrawable(R.drawable.partly_cloudy_day);
		iconPartlyCloudyDay.setBounds(0,0,iconSize,iconSize);
		isPartlyCloudyDay = new ImageSpan(iconPartlyCloudyDay);

		Drawable iconPartlyCloudyNight = context.getResources().getDrawable(R.drawable.partly_cloudy_night);
		iconPartlyCloudyNight.setBounds(0,0,iconSize,iconSize);
		isPartlyCloudyNight = new ImageSpan(iconPartlyCloudyNight);

		Drawable iconRain = context.getResources().getDrawable(R.drawable.rain);
		iconRain.setBounds(0,0,iconSize,iconSize);
		isRain = new ImageSpan(iconRain);

		Drawable iconSleet = context.getResources().getDrawable(R.drawable.sleet);
		iconSleet.setBounds(0,0,iconSize,iconSize);
		isSleet = new ImageSpan(iconSleet);

		Drawable iconSnow = context.getResources().getDrawable(R.drawable.snow);
		iconSnow.setBounds(0,0,iconSize,iconSize);
		isSnow = new ImageSpan(iconSnow);

		Drawable iconWind = context.getResources().getDrawable(R.drawable.wind);
		iconWind.setBounds(0,0,iconSize,iconSize);
		isWind = new ImageSpan(iconWind);
	}
	public void update() {
		if (Utils.isNewDay()) {
			consecFails = 0;  // retry every day in case of temporary network failures
			new ForecastTask(this).execute();
		}

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

		tv.setVisibility(TextView.VISIBLE);

		setIconSizes();
		
		SpannableString span = new SpannableString(cast);
		SpannableStringBuilder builder = new SpannableStringBuilder();

		//see if we have an icon that matches the icon in our string
		String rain = "rain", clearDay = "clear-day", clearNight = "clear-night", cloudy = "cloudy", fog = "fog", partlyCloudyDay = "partly-cloudy-day", partlyCloudyNight = "partly-cloudy-night", sleet="sleet", snow="snow", wind="wind";

		int r = cast.indexOf(rain), cD=cast.indexOf(clearDay), cN=cast.indexOf(clearNight), c=cast.indexOf(cloudy), f=cast.indexOf(fog), pCD=cast.indexOf(partlyCloudyDay), pCN=cast.indexOf(partlyCloudyNight),sl=cast.indexOf(sleet),s=cast.indexOf(snow),w=cast.indexOf(wind);

		//if we do, change the word to the icon png
		if (r>=0)
			span.setSpan(isRain, r, r+rain.length(), 0);
		else if (cD>=0)
			span.setSpan(isClearDay, cD, cD+clearDay.length(), 0);
		else if (cN>=0)
			span.setSpan(isClearNight, cN, cN+clearNight.length(), 0);
		else if (pCD>=0)
			span.setSpan(isPartlyCloudyDay, pCD, pCD+partlyCloudyDay.length(), 0);
		else if (pCN>=0)
			span.setSpan(isPartlyCloudyNight, pCN, pCN+partlyCloudyNight.length(), 0);
		else if (c>=0)
			span.setSpan(isCloudy, c, c+cloudy.length(), 0);
		else if (f>=0)
			span.setSpan(isFog, f, f+fog.length(), 0);
		else if (sl>=0)
			span.setSpan(isSleet, sl, sl+sleet.length(), 0);
		else if (s>=0)
			span.setSpan(isSnow, s, s + snow.length(), 0);
		else if (w>=0)
			span.setSpan(isWind, w, w + wind.length(), 0);
		else span.setSpan("", cast.lastIndexOf("°)"), cast.lastIndexOf("°)"), 0);

		builder.append(span);
		tv.setText(builder);
		lastRan = Calendar.getInstance().getTimeInMillis();

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
		forecastTime += "T" + new SimpleDateFormat("HH:mm:ss").format(new Date());

		String forecastURL = "https://api.forecast.io/forecast/"+module.apikey+"/"+module.latitude+","+module.longitude+",";


		String forecastParams = "?exclude=minutely,hourly";
		/****************************/
		//String forecastParams = "?exclude=minutely,hourly,daily";
		/****************************/
		if (module.useCelsius)
			forecastParams += "&units=si";
		//String wholeURL = forecastURL+forecastParams;
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
		//search for icon, temperature, temperature high, temperature low
		//String re = ".*icon\":\"([\\-a-zA-Z]+)\",.*temperature\":([\\-0-9\\.]+),.*";
		String re = ".*icon\":\"([\\-a-zA-Z]+)\",.*temperature\":([\\-0-9\\.]+),.*temperatureMin\":([\\-0-9\\.]+),.*temperatureMax\":([\\-0-9\\.]+),.*";

		Pattern r = Pattern.compile(re);
		Matcher m = r.matcher(s);
		if (m.find()) {
			icon = m.group(1);
			temp = m.group(2);
			low = m.group(3);
			high = m.group(4);
		}

        int ftemp = Math.round(Float.parseFloat(temp));
		int flow = Math.round(Float.parseFloat(low));
		int fhigh = Math.round(Float.parseFloat(high));

		//create string with icon word, temperature, high, low
		//String cast = icon + " " + ftemp +"°";
		String cast = icon + " " + ftemp +"° ("+fhigh+"° | "+flow+"°)";

		return cast;
    }
}
