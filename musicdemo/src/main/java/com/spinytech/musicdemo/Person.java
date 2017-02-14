package com.spinytech.musicdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by erfli on 2/14/17.
 */

public class Person implements Parcelable {
    public String name;

    public Person(String name) {
        this.name = name;
    }

    protected Person(Parcel in) {
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

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}
