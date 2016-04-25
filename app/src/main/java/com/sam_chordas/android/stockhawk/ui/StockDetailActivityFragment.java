package com.sam_chordas.android.stockhawk.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.StockData;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailActivityFragment extends Fragment {

    public StockDetailActivityFragment() {
    }

    LineChartView mLineChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);
        Intent intent = getActivity().getIntent();
        StockData stockData = intent.getParcelableExtra(getString(R.string.parcelable_stock_data));
        mLineChart = (LineChartView) view.findViewById(R.id.linechart);

        new FetchStockHistoricalData().execute(stockData.symbol);
        return view;
    }

    public void displayLineChart(Map<String,Float> graphData) {
        Collection values = graphData.values();
        Integer maxVal = ((Float) Collections.max(values)).intValue();
        Integer minVal = ((Float) Collections.min(values)).intValue();
        Integer step = (maxVal - minVal) / 10;

        if(step <= 0) {
            step = 5;
        }

        LineSet dataSet = new LineSet();
        for (Map.Entry<String, Float> entry : graphData.entrySet()) {
            dataSet.addPoint(entry.getKey(),entry.getValue());
        }
        mLineChart.setAxisBorderValues(minVal-10,maxVal+10);
        mLineChart.addData(dataSet);
        mLineChart.setEnabled(true);
        mLineChart.setStep(step);
        mLineChart.show();
    }

    private class FetchStockHistoricalData extends AsyncTask<String, String, Map<String, Float>> {
        private OkHttpClient httpClient = new OkHttpClient();

        @Override
        protected Map<String, Float> doInBackground(String... strings) {
            String stock = strings[0];
//            String stock = "AAPL";
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String endDate = dateFormat.format(cal.getTime());
            cal.add(Calendar.DATE, -7);
            String startDate = dateFormat.format(cal.getTime());
            String query = "select * from yahoo.finance.historicaldata where symbol = \""
                    +stock+"\" and startDate = \""
                    +startDate+"\" and endDate = \""+endDate+"\"";
            StringBuilder urlStringBuilder = new StringBuilder();
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            try {
                urlStringBuilder.append(URLEncoder.encode(query, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

            Request request = new Request.Builder().url(urlStringBuilder.toString()).build();
            Response response = null;

            try {
                response = httpClient.newCall(request).execute();
                return Utils.historicalJsonToMap(response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Map<String, Float> graphData) {
            super.onPostExecute(graphData);
            displayLineChart(graphData);
        }
    }
}
