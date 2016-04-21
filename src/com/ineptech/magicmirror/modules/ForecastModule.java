package com.ineptech.magicmirror.modules;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.R;
import com.ineptech.magicmirror.Utils;

public class ForecastModule extends Module {

	String apikey;// fetched from sensitive-data in resources
	double latitude,  latitude_def  = 40.852676;
	double longitude, longitude_def = 14.267968;
	private static final long timeBetweenCalls = 10 * 60 * 1000; // 10 minutes
	long lastRan = 0;
	int consecFails = 0;
	String cast = "";
	int cast_show;
	Boolean useCelsius = true;
	CheckBox cbCelsius;
	Context ctx;


	public ForecastModule(Context context) {
		super("Weather");
		desc = "Shows today's temperatures: \"current (high | low)\" as reported by forecast.io.  "
				+ "This will not work until you go to forecast.io and register to get a (free) api key "
				+ "and enter it in to the box below.  To do so, visit developer.forecast.io and click on Register. "
				+ "Then enter your latitude and longitude (be sure to get the signs right).";
		defaultTextSize = 72;
		sampleString = "100\u00B0 (90° | 110°)";
		ctx = context;
		apikey = ctx.getResources().getString(R.string.forecast_api);
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
		cbCelsius.setText("Use Celsius units?");
		cbCelsius.setChecked(useCelsius);
		holder.addView(cbCelsius);

	}

	void set() {

		Drawable myIcon = ctx.getResources().getDrawable(cast_show);
		myIcon.setBounds(0, 0, tv.getLineHeight(), tv.getLineHeight());

		// add weather icon before temperature
		ImageSpan is = new ImageSpan(myIcon);
		final Spannable text = new SpannableString("  " + cast);
		text.setSpan(is, 0,1, 0);
		tv.setText(text, TextView.BufferType.SPANNABLE);
		lastRan = Calendar.getInstance().getTimeInMillis();
	}

}

class ForecastTask extends AsyncTask <Void, Void, String>{

	private ForecastModule module;
	String sampleResponse = "{\"latitude\":45.52,\"longitude\":122.6819,\"timezone\":\"Asia/Harbin\",\"offset\":8,\"currently\":{\"time\":1449289114,\"summary\":\"Clear\",\"icon\":\"clear-day\",\"precipType\":\"snow\",\"temperature\":22.35,\"apparentTemperature\":11.63,\"dewPoint\":2.76,\"humidity\":0.42,\"windSpeed\":10.22,\"windBearing\":304,\"visibility\":10,\"cloudCover\":0,\"pressure\":1026.52},\"daily\":{\"data\":[{\"time\":1449244800,\"summary\":\"Clear throughout the day.\",\"icon\":\"clear-day\",\"sunriseTime\":1449270846,\"sunsetTime\":1449302882,\"moonPhase\":0.8,\"precipType\":\"snow\",\"temperatureMin\":10.48,\"temperatureMinTime\":1449320400,\"temperatureMax\":22.73,\"temperatureMaxTime\":1449291600,\"apparentTemperatureMin\":4.25,\"apparentTemperatureMinTime\":1449316800,\"apparentTemperatureMax\":12.86,\"apparentTemperatureMaxTime\":1449291600,\"dewPoint\":3.95,\"humidity\":0.58,\"windSpeed\":5.86,\"windBearing\":292,\"visibility\":8.33,\"pressure\":1024.88}]},\"flags\":{\"sources\":[\"isd\"],\"isd-stations\":[\"508440-99999\",\"509490-99999\",\"540260-99999\",\"540490-99999\"],\"units\":\"us\"}}";
	//String sampleForecastURL = "https://api.forecast.io/forecast/api_key_goes_here/45.5200,-122.6819,2015-12-04T20:18:34-0800?units=us&exclude=minutely,hourly";

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
			try {
				System.out.println(results);
				List<String> tmp = parseForecast(results);
				module.cast = tmp.get(0);
				module.cast_show = iconResources.get(tmp.get(1));
				module.set();
			} catch (Exception e) {
				module.consecFails++;
				e.printStackTrace();
			}
		} else {
			module.consecFails++;
		}
	}

	private final Map<String, Integer> iconResources = new HashMap<String, Integer>() {{
		put("clear-day", R.drawable.clear_day);
		put("clear-night", R.drawable.clear_night);
		put("cloudy", R.drawable.cloudy);
		put("fog", R.drawable.fog);
		put("partly-cloudy-day", R.drawable.partly_cloudy_day);
		put("partly-cloudy-night", R.drawable.partly_cloudy_night);
		put("rain", R.drawable.rain);
		put("sleet", R.drawable.sleet);
		put("snow", R.drawable.snow);
		put("wind", R.drawable.wind);
	}};

	String temp = "", high = "", low = "", icon = "";
	List<String> parseForecast (String s) throws Exception {
		String re = ".*icon\":\"([\\a-z\\.]+)\",.*temperature\":([\\-0-9\\.]+),.*temperatureMin\":([\\-0-9\\.]+),.*temperatureMax\":([\\-0-9\\.]+),.*";
		Pattern r = Pattern.compile(re);
		Matcher m = r.matcher(s);
		if (m.find()) {
			icon = m.group(1);
			temp = m.group(2);
			low  = m.group(3);
			high = m.group(4);
		}
		int ftemp = Math.round(Float.parseFloat(temp));
		int flow  = Math.round(Float.parseFloat(low));
		int fhigh = Math.round(Float.parseFloat(high));

		List<String> cast =  Arrays.asList(""+ftemp +"℃ (min:"+flow+"° | max:"+fhigh+"°)", icon);

		// TODO: Check for weather alerts and display something suitable?
		if (s.contains("Alert")) {
			// do something...
		}
		return cast;
	}
}
