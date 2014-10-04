package com.prashant.temptracker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DataActivity extends ActionBarActivity {

	private static final String TAG = "DataActivity";
	private static final int NUM_X_LABELS = 4;
	private static final long MS_PER_SECOND = 1000;
	private String mUrlString;

	/*
	 * Lists used to store the temperature data.
	 * The size and indices should be in sync with {@link DataActivity#mTimeData}.
	 */
	private int[] mTempData;

	/*
	 * Lists used to store the humidity data.
	 * The size and indices should be in sync with {@link DataActivity#mTimeData}.
	 */
	private int[] mHumidityData;

	/*
	 * List used to store the time stamp data.
	 * The size and indices of should be in sync with {@link DataActivity#mTempData} and
	 * {@link DataActivity#mHumidityData}.
	 */
	private long[] mTimeData;

	private boolean mIsDataValid = false;

	private LineGraphView mGraphView;


	/*
	 * Class to perform the obtaining of data from the mySQL web server
	 * I think a feasible way to use this is to have the onPostExecute() function
	 * be the place where the graphs are drawn.
	 *
	 * Therefore we can create a sort of periodic thread which simply calls execute every
	 * x seconds and updates the graph data (this depends on how often the sensor data
	 * is updated.
	 */
	private class GetSensorDataTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void...params) {
			// We only expect one URL
			getDataFromServer(mUrlString);
			return null;
		}

		protected void onPostExecute(Void param) {
			if (mIsDataValid) {
				drawGraph(mTimeData, mTempData, mHumidityData);
			} else {
				Context context = getApplicationContext();
				Toast.makeText(context, "Invalid URL entered!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGraphView = new LineGraphView(this, "SensorGraph")  {
			   @Override
			   protected String formatLabel(double value, boolean isValueX) {
			      // add a custom format labeler so that we print integers labels
				   if (isValueX) {
					   SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
					   sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

					   /*
					    * MySQL stores epoch time in seconds, but for some reason java uses
					    * milliseonds, therefore we need to multiple the value received.
					    */
					   return sdf.format(new Date((long)value * MS_PER_SECOND));
				   } else {
					   return ""+((int) value);
				   }
			   }
		};

		mGraphView.getGraphViewStyle().setNumHorizontalLabels(NUM_X_LABELS);
		// Both temperature and pressure shouldn't vary beyond 10 and 90
		mGraphView.setManualYAxisBounds(50, 10);
		mGraphView.setShowLegend(true);
	    mGraphView.setLegendAlign(LegendAlign.TOP);
	    mGraphView.setGraphViewStyle(new GraphViewStyle(Color.DKGRAY, Color.DKGRAY, Color.LTGRAY));
	    // mGraphView.setViewPort(2, 40);
	    mGraphView.setScalable(true);
	    // mGraphView.setScrollable(true);

		setContentView(R.layout.activity_data);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		Intent intent = getIntent();
		mUrlString = intent.getStringExtra("Url");
		mUrlString = "http://" + mUrlString + "/testdb.php";

		// Need to do something with this URL now...
		new GetSensorDataTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.data, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_data, container,
					false);
			
			return rootView;
		}
	}

	/*
	 * Function which performs the JSON parsing of the PHP results.
	 * This code has been taken from :
	 * http://m-zeeshanarif.blogspot.com/2013/05/android-connecting-to-server-mysql.html
	 *
	 */
	public void getDataFromServer(String url) {
		JSONArray jArray;
		String result = null;
		StringBuilder sb;
		InputStream is;
		mIsDataValid = false;

		// Connect to the web server and get the raw data
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		try {
			HttpClient httpclient = new DefaultHttpClient();

			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e) {
			Log.e(TAG, "Error in http connection:" + e.toString());
			return;
		}

		// convert the response to string
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,
					"iso-8859-1"), 8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");

			String line = "0";
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			Log.e(TAG, "Error converting result:" + e.toString());
			return;
		}

		// Extract the column data
		try {
			jArray = new JSONArray(result);
			JSONObject jsonObject = null;
			int length = jArray.length();
			mTempData = new int[length];
			mTimeData = new long[length];
			mHumidityData = new int[length];
			for (int i = 0; i < length; i++) {
				jsonObject = jArray.getJSONObject(i);
				String ct_name = jsonObject.getString("temp");
				String ct_time = jsonObject.getString("timestamp");
				String ct_hum = jsonObject.getString("hum");
				// TODO: Add humidity sensor data?
				// TODO: Add time-stamp data
				mTempData[length - 1 - i] = Integer.parseInt(ct_name);
				mTimeData[length - 1 - i] =  Long.parseLong(ct_time);
				mHumidityData[length - 1 -i] = Integer.parseInt(ct_hum);
			}
		} catch (JSONException e) {
			Log.e(TAG, "NO JSON DATA FOUND");
			return;
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}

		// If we have reached here, then the data is valid
		mIsDataValid = true;
	}
	
	/*
	 * Create the required GraphViewData arrays from the info, and
	 * add all the necessary series to create the graph.
	 * TODO: Add series for humidity data.
	 */
	public void drawGraph(long timeArray[], int tempArray[], int humArray[]) {
		int arrayLength = tempArray.length;
		List<GraphViewData> dataTempList = new ArrayList<GraphViewData>();
		List<GraphViewData> dataHumList = new ArrayList<GraphViewData>();

		for (int i = 0; i < arrayLength; i++) {
			dataTempList.add(new GraphViewData(timeArray[i], tempArray[i]));
			dataHumList.add(new GraphViewData(timeArray[i], humArray[i]));
		}

		GraphViewData[] graphTempArray = new GraphViewData[dataTempList.size()];
		graphTempArray = dataTempList.toArray(graphTempArray);

		GraphViewData[] graphHumArray = new GraphViewData[dataHumList.size()];
		graphHumArray = dataHumList.toArray(graphHumArray);

		//Now add the series and draw the graph.
		GraphViewSeries tempSeries = new GraphViewSeries("Temp ",
				new GraphViewSeriesStyle(Color.GREEN, 3), graphTempArray);
		GraphViewSeries humSeries = new GraphViewSeries("Humd ",
				new GraphViewSeriesStyle(Color.BLUE, 3), graphHumArray);

		mGraphView.removeAllSeries();
		mGraphView.addSeries(tempSeries);
		mGraphView.addSeries(humSeries);

		LinearLayout layout = (LinearLayout) findViewById(R.id.graphLayout);
		layout.removeAllViews();
		layout.addView(mGraphView);
		layout.invalidate();				
	}
}
