package com.cc.demo;

import android.os.Parcel;
import android.os.Parcelable;

import com.cc.eventcalendar.calendarview.adapter.BaseEvent;
import com.cc.eventcalendar.calendarview.util.OSTimeUtil;

import java.util.Calendar;

/**
 * Created by ChengCn on 12/28/2015.
 */
public class Item extends BaseEvent implements Parcelable {
    private String mId;
    private String mTitle;
    private String mCreator;
    private String mComment;
    private boolean mIsAllDay;

    public String getID() {
        return mId;
    }

    public void setID(String mId) {
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getCreator() {
        return mCreator;
    }



    @Override
    public boolean isAllDayEvent() {
        if (getDuration() != OSTimeUtil.MILLIS_IN_DAY) {
            return false;
        }

        return OSTimeUtil.isStartOfDay(mStartTime);
    }

    @Override
    public int getType() {
        return 0;
    }

    public void setCreator(String mCreator) {
        this.mCreator = mCreator;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String mComment) {
        this.mComment = mComment;
    }

    public boolean isAllDay() {
        return mIsAllDay;
    }

    public void setIsAllDay(boolean mIsAllDay) {
        this.mIsAllDay = mIsAllDay;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {

        @Override
        public Item createFromParcel(Parcel source) {
            Item item = new Item();
            item.mId = source.readString();
            item.mComment = source.readString();
            item.mTitle = source.readString();
            item.mCreator = source.readString();
            item.mStartTime = source.readLong();
            item.mEndTime = source.readLong();
            item.mIsAllDay = source.readInt() == 1;
            return item;
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mComment);
        dest.writeString(mTitle);
        dest.writeString(mCreator);
        dest.writeLong(mStartTime);
        dest.writeLong(mEndTime);
        dest.writeInt(mIsAllDay ? 1 : 0);
    }
}
