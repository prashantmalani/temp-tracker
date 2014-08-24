package com.prashant.temptracker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
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
import android.widget.TextView;
import android.os.Build;

public class DataActivity extends ActionBarActivity {

	private static final String TAG = "DataActivity";
	private static final int NUM_X_LABELS = 10;
	private String mUrlString;

	// List used to store the temperature data
	private int[] mTempData;

	private LineGraphView mGraphView;

	// TODO: Add corresponding lists for time stamp data, humidity, etc...

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
			drawGraph(mTempData);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGraphView = new LineGraphView(this, "SensorGraph")  {
			   @Override
			   protected String formatLabel(double value, boolean isValueX) {
			      // add a custom format labeler so that we print integers labels
			      return ""+((int) value);
			   }
		};

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
	 * Function which performs the JSON parsing of the php results.
	 * This code has been taken from :
	 * http://m-zeeshanarif.blogspot.com/2013/05/android-connecting-to-server-mysql.html
	 *
	 */
	public void getDataFromServer(String url) {
		JSONArray jArray;
		String result = null;
		StringBuilder sb;
		InputStream is;

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
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"), 8);
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
		}

		// Extract the column data
		try {
			jArray = new JSONArray(result);
			JSONObject jsonObject = null;
			int length = jArray.length();
			mTempData = new int[length];
			for (int i = 0; i < length; i++) {
				jsonObject = jArray.getJSONObject(i);
				String ct_name = jsonObject.getString("temp");
				// TODO: Add humidity sensor data?
				// TODO: Add time-stamp data
				mTempData[length - 1 - i] = Integer.parseInt(ct_name);
			}
		} catch (JSONException e) {
			Log.e(TAG, "NO JSON DATA FOUND");
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Create the required GraphViewData arrays from the info, and
	 * add all the necessary series to create the graph.
	 * TODO: Add series for humidity data.
	 */
	public void drawGraph(int tempArray[]) {
		int arrayLength = tempArray.length;
		List<GraphViewData> dataTempList = new ArrayList<GraphViewData>();

		for (int i = 0; i < arrayLength; i++) {
			dataTempList.add(new GraphViewData(i, tempArray[i]));
		}

		GraphViewData[] graphTempArray = new GraphViewData[dataTempList.size()];
		graphTempArray = dataTempList.toArray(graphTempArray);


		//Now add the series and draw the graph.
		GraphViewSeries tempSeries = new GraphViewSeries(graphTempArray);

		mGraphView.removeAllSeries();
		mGraphView.addSeries(tempSeries);

		LinearLayout layout = (LinearLayout) findViewById(R.id.graphLayout);
		layout.removeAllViews();
		mGraphView.getGraphViewStyle().setNumHorizontalLabels(NUM_X_LABELS);
		layout.addView(mGraphView);
		layout.invalidate();				
	}
}
