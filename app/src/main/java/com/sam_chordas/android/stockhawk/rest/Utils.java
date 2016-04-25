package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  private static final String YHD_QUERY = "query";
  private static final String YHD_COUNT = "count";
  private static final String YHD_RESULTS = "results";
  private static final String YHD_QUOTE = "quote";
  private static final String YHD_DATE = "Date";
  private static final String YHD_CLOSE = "Close";

  private static final String YHD_CHANGE = "Change";
  private static final String YHD_SYMBOL = "symbol";
  private static final String YHD_BID = "Bid";
  private static final String YHD_CHANGE_PERCENTAGE = "ChangeinPercent";

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject(YHD_QUERY);
        int count = Integer.parseInt(jsonObject.getString(YHD_COUNT));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject(YHD_RESULTS)
              .getJSONObject(YHD_QUOTE);
          batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject(YHD_RESULTS).getJSONArray(YHD_QUOTE);

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static LinkedHashMap<String,Float> historicalJsonToMap(String JSON) {

    LinkedHashMap<String,Float> historicalData = new LinkedHashMap<String,Float>();

    try {
      JSONObject json = new JSONObject(JSON);

      JSONObject queryJson = json.getJSONObject(YHD_QUERY);
      JSONObject resultsJson = queryJson.getJSONObject(YHD_RESULTS);
      JSONArray quoteJsonArray = resultsJson.getJSONArray(YHD_QUOTE);

      for (int i = (quoteJsonArray.length()-1); i >=0 ; i--) {
        JSONObject quoteJson = quoteJsonArray.getJSONObject(i);

        String date = quoteJson.getString(YHD_DATE);
        Float close = (float) quoteJson.getDouble(YHD_CLOSE);
        historicalData.put(date,close);
      }

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return historicalData;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString(YHD_CHANGE);
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(YHD_SYMBOL));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString(YHD_BID)));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString(YHD_CHANGE_PERCENTAGE), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }
}
