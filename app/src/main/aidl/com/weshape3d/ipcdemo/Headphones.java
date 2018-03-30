package com.weshape3d.ipcdemo;

import android.os.Parcel;
import android.os.Parcelable;

public class Headphones implements Parcelable {
    public int id;
    public String brand;
    public String name;
    public String price;

    protected Headphones(Parcel in) {
        id = in.readInt();
        brand = in.readString();
        name = in.readString();
        price = in.readString();
    }

    public Headphones(int id, String brand, String name, String price) {
        this.id = id;
        this.brand = brand;
        this.name = name;
        this.price = price;
    }

    public static final Creator<Headphones> CREATOR = new Creator<Headphones>() {
        @Override
        public Headphones createFromParcel(Parcel in) {
            return new Headphones(in);
        }

        @Override
        public Headphones[] newArray(int size) {
            return new Headphones[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(brand);
        parcel.writeString(name);
        parcel.writeString(price);
    }
}
