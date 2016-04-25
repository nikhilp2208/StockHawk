package com.sam_chordas.android.stockhawk.ui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.LinearEase;
import com.db.chart.view.animation.style.BaseStyleAnimation;
import com.db.chart.view.animation.style.DashAnimation;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailActivityFragment extends Fragment {

    public StockDetailActivityFragment() {
    }

    LineChartView mLineChart;
    StockData mStockData;
    TextView mStockSymbolTextView;
    TextView mBidPriceTextView;
    TextView mChangeTextView;
    TextView mChangePercTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);
        Intent intent = getActivity().getIntent();
        mStockData = intent.getParcelableExtra(getString(R.string.parcelable_stock_data));
        mLineChart = (LineChartView) view.findViewById(R.id.linechart);

        mStockSymbolTextView = (TextView) view.findViewById(R.id.stock_detail_text_view);
        mStockSymbolTextView.setText(mStockData.symbol.toUpperCase());
        mStockSymbolTextView.setContentDescription(mStockData.symbol);

        mBidPriceTextView = (TextView) view.findViewById(R.id.bid_price_text_view);
        mBidPriceTextView.setText(Float.toString(mStockData.bidPrice));
        mBidPriceTextView.setContentDescription(Float.toString(mStockData.bidPrice));

        mChangeTextView = (TextView) view.findViewById(R.id.change_text_view);
        mChangeTextView.setText(Float.toString(mStockData.change));
        mChangeTextView.setContentDescription(Float.toString(mStockData.change));

        mChangePercTextView = (TextView) view.findViewById(R.id.change_perc_text_view);
        mChangePercTextView.setText(mStockData.percentChange);
        mChangePercTextView.setContentDescription(mStockData.percentChange);

        new FetchStockHistoricalData().execute(mStockData.symbol);
        return view;
    }

    public void displayLineChart(LinkedHashMap<String,Float> graphData) {
        Collection values = graphData.values();
        Integer maxVal = ((Float) Collections.max(values)).intValue();
        Integer minVal = ((Float) Collections.min(values)).intValue();
        Log.d("SDAF","max_val "+maxVal);
        Log.d("SDAF", "min_val " + minVal);
        Integer step = (maxVal - minVal) / 5;
        if(step <= 0) {
            step = 5;
        }

        Log.d("SDAF", "step " + step);

        LineSet dataSet = new LineSet();
        for (LinkedHashMap.Entry<String, Float> entry : graphData.entrySet()) {
            dataSet.addPoint(entry.getKey(),entry.getValue());
        }
        if (mStockData.isUp == 0) {
            dataSet.setColor(getResources().getColor(R.color.material_red_700));
        } else {
            dataSet.setColor(getResources().getColor(R.color.material_green_700));
        }
        mLineChart.setGrid(ChartView.GridType.FULL, new Paint());
        mLineChart.setAxisBorderValues(minVal - step, maxVal + step);
        mLineChart.addData(dataSet);
        mLineChart.setEnabled(true);
        mLineChart.setStep(step);
        Animation anim = new Animation();
        mLineChart.animateSet(0, new DashAnimation());
        mLineChart.show(anim);
    }

    private class FetchStockHistoricalData extends AsyncTask<String, String, LinkedHashMap<String, Float>> {
        private OkHttpClient httpClient = new OkHttpClient();

        @Override
        protected LinkedHashMap<String, Float> doInBackground(String... strings) {
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
        protected void onPostExecute(LinkedHashMap<String, Float> graphData) {
            super.onPostExecute(graphData);
            displayLineChart(graphData);
        }
    }
}
