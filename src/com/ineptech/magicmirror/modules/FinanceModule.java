package com.ineptech.magicmirror.modules;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
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

public class FinanceModule extends Module {

	private static final long timeBetweenCalls = 10 * 60 * 1000; // Only update every 10 minutes
	long lastRan = 0;
	int consecFails = 0;
	public static ArrayList<String> mStocks; // list of stock tickers currently configured to be displayed
    final String prefsStocks = "StockListString";
    final String defaultStocks = "^GSPC,GOOG,AAPL";

	
	public FinanceModule() {
		super("Stock ticker");
		desc = "Pulls stock quotes from Yahoo's free (no API key needed) finance API.  The tickers for the "
				+ "indexes are: S&P 500=^GSPC, Dow Jones = ^DJI, Nasdaq = ^IXIC.  Only displays on weekdays "
				+ "before 5pm.  Use the widgets below to add the ticker codes of any stocks you'd like to track.";
		defaultTextSize = 72;
		sampleString = "Microsoft: +2.3%";
		mStocks = new ArrayList<>();
    	loadConfig();
	}
	
	private void loadConfig() {
    	String s = prefs.get(prefsStocks, defaultStocks);
    	String[] stocks = s.split(",");
    	mStocks.clear();
    	for (String stock : stocks) {
    		mStocks.add(stock);
    	}
    }
	
    @Override
    public void saveConfig() {
    	super.saveConfig();
    	String stocks = "";
    	for (String stock : mStocks) {
    		if (stocks.length() > 0) 
    			stocks += ",";
    		stocks += stock;
    	}
    	prefs.set(prefsStocks, stocks);
    }

    @Override
    public void makeConfigLayout() {
    	super.makeConfigLayout();
    	
    	// add a display of each item in the map
    	for (final String stock : mStocks) {
    		Button remove = new Button(MainApplication.getContext());
    		remove.setText("X");
    		remove.setOnClickListener
    		(new View.OnClickListener() {
    			public void onClick(View v) {
    				mStocks.remove(stock);
    				saveConfig();
    				makeConfigLayout();
    			}
    		});
    		LinearLayout holder = new LinearLayout(MainApplication.getContext());
    		holder.setOrientation(LinearLayout.HORIZONTAL);
    		TextView hdtv = new TextView(MainApplication.getContext());
    		hdtv.setText(stock);
    		holder.addView(hdtv);
    		holder.addView(remove);
    		configLayout.addView(holder);
    	}
    	// widgets for adding a new Stock
    	final EditText addstock = new EditText(MainApplication.getContext());
    	addstock.setText("MSFT");
    	Button plus = new Button(MainApplication.getContext());
    	plus.setText("+");
    	plus.setOnClickListener
		(new View.OnClickListener() {
			public void onClick(View v) {
				mStocks.add(addstock.getText().toString());
				saveConfig();
				makeConfigLayout();
			}
		});
    	LinearLayout addholder = new LinearLayout(MainApplication.getContext());
    	addholder.addView(plus);
    	addholder.addView(addstock);
    	
    	configLayout.addView(addholder);
    }
    
	public void update() {
		if (consecFails > 9 || Utils.afterFive() || !Utils.isWeekday()) {
			tv.setText("");
			tv.setVisibility(TextView.GONE);
		} else if (Calendar.getInstance().getTimeInMillis() > (lastRan + timeBetweenCalls)) {
			tv.setVisibility(TextView.VISIBLE);
			new FinanceTask(this).execute();
		}
	}
	
	
}

class FinanceTask extends AsyncTask <Void, Void, String>{

	private FinanceModule module;
	private String sampleResponse = "<query yahoo:count=\"1\" yahoo:created=\"2015-12-24T22:04:37Z\" yahoo:lang=\"en-US\"><results><quote symbol=\"^GSPC\"><AverageDailyVolume/><Change>-3.30</Change><DaysLow>2058.73</DaysLow><DaysHigh>2067.36</DaysHigh><YearLow>1867.00</YearLow><YearHigh>2134.00</YearHigh><MarketCapitalization/><LastTradePriceOnly>2060.99</LastTradePriceOnly><DaysRange>2058.73 - 2067.36</DaysRange><Name>S&P 500</Name><Symbol>^GSPC</Symbol><Volume>250570206</Volume><StockExchange>SNP</StockExchange></quote></results></query>";
	
	public FinanceTask(FinanceModule _module) {
		module = _module;
	}

	@Override
	protected String doInBackground(Void... params) {
		if (Utils.debug)
			return sampleResponse;
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		
		String stocklist = "";
		for (String s : module.mStocks) {
			if (stocklist.length() > 0)
				stocklist += ",";
			stocklist += "\"" + s + "\"";
		}
		
		// example valid url: https://query.yahooapis.com/v1/public/yql?q=select * from yahoo.finance.quote where symbol in ("AAPL,MSFT")&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys
		String text = null;
		try {
			String urlStr = "https://query.yahooapis.com/v1/public/yql?q=select * from yahoo.finance.quote where symbol in ("+stocklist+")&env=store://datatables.org/alltableswithkeys";
			URL url = new URL(urlStr);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			url = uri.toURL();
			// the above looks convoluted, but is necessary to get the urlencoding correct
			HttpGet httpGet = new HttpGet(uri);  
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
			String newcontent = parseResults(results);
			Spanned span = Html.fromHtml(newcontent);
			module.tv.setText(span);
		} else {
			module.consecFails++;
		}
	}
	
	String parseResults(String s) {
		String r = "";
		try {
			Boolean done = false;
			while (!done) {
				String q = xmlPluck(s, "quote");
				if (q.length() == 0) {
					done = true;
				} else {
					// q should nowlook like this: 
					// ...<Change>-48.40</Change>...<LastTradePriceOnly>1890.28</LastTradePriceOnly>...<Name>S&P 500</Name><Symbol>^GSPC</Symbol>...
					String change = xmlPluck(q, "Change");
					Boolean gain = change.substring(0,1).compareTo("+")==0;
					double dChange = Double.parseDouble(change.substring(1));
					String last = xmlPluck(q, "LastTradePriceOnly");
					double dLast = Double.parseDouble(last);
					double percentChange = (100.0*dChange/dLast);
					String name = xmlPluck(q, "Name");
					if (name.endsWith("."))
						name = name.substring(0, name.length()-1);  // Because "Apple Inc.: +1%" looks clunky
					DecimalFormat df = new DecimalFormat("#.#"); 
					r += name + ": "+ (gain ? "+" : "-") + df.format(percentChange) + "%<br>";
					// remove the parsed quote from s for next loop
					s = s.substring(s.indexOf("</quote")+8);
				}
			}
			module.consecFails = 0;
			module.lastRan = Calendar.getInstance().getTimeInMillis();
		} catch (Exception e) {
			r = "Stock ticker error :(";
		}
		return r;
	}
	
	String xmlPluck(String s, String tag) {
		// this is a very simple and hacky way of getting values out of xml.  I did it to avoid importing a library.
		// don't reuse it anywhere that real xml support is necessary!
		String r = "";
		int begin = s.indexOf("<"+tag);
		if (begin >= 0) {
			begin += s.substring(begin).indexOf(">") + 1;
			int end = s.indexOf("</"+tag+">");
			if (end > 0) 
				r = s.substring(begin, end);
			else
				r = s.substring(begin);
		}
		return r;
	}
}
