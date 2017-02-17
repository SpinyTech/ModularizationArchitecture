package com.spinytech.macore;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by erfli on 2/14/17.
 */

public class Song implements Parcelable {
    public String name;

    public Song(String name) {
        this.name = name;
    }

    protected Song(Parcel in) {
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
