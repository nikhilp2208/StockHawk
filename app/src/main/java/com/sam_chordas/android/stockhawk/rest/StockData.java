package com.sam_chordas.android.stockhawk.rest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by nikhil.p on 25/04/16.
 */
public class StockData implements Parcelable {
    public String symbol;
    public float bidPrice;
    public String percentChange;
    public float change;
    public int isUp;

    public StockData() {
    }

    protected StockData(Parcel in) {
        symbol = in.readString();
        bidPrice = in.readFloat();
        percentChange = in.readString();
        change = in.readFloat();
        isUp = in.readInt();
    }

    public static final Creator<StockData> CREATOR = new Creator<StockData>() {
        @Override
        public StockData createFromParcel(Parcel in) {
            return new StockData(in);
        }

        @Override
        public StockData[] newArray(int size) {
            return new StockData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(symbol);
        parcel.writeFloat(bidPrice);
        parcel.writeString(percentChange);
        parcel.writeFloat(change);
        parcel.writeInt(isUp);
    }
}
