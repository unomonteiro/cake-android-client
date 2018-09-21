package com.waracle.androidtest;

import android.os.Parcel;
import android.os.Parcelable;

public class Cake implements Parcelable {
    private final String title;
    private final String desc;
    private final String image;

    Cake(String title, String desc, String image) {
        this.title = title;
        this.desc = desc;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    protected Cake(Parcel in) {
        title = in.readString();
        desc = in.readString();
        image = in.readString();
    }

    public static final Creator<Cake> CREATOR = new Creator<Cake>() {
        @Override
        public Cake createFromParcel(Parcel in) {
            return new Cake(in);
        }

        @Override
        public Cake[] newArray(int size) {
            return new Cake[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(image);
    }
}
