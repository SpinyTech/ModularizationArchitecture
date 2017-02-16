package com.spinytech.picdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by erfli on 2/16/17.
 */

public class Date implements Parcelable {
    String date;

    public Date() {
        date = new java.util.Date().toString();
    }

    protected Date(Parcel in) {
        date = in.readString();
    }

    public static final Creator<Date> CREATOR = new Creator<Date>() {
        @Override
        public Date createFromParcel(Parcel in) {
            return new Date(in);
        }

        @Override
        public Date[] newArray(int size) {
            return new Date[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
    }
}
