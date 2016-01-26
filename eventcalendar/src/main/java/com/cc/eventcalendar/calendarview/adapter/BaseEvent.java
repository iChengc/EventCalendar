package com.cc.eventcalendar.calendarview.adapter;

import android.os.Parcelable;

import com.cc.eventcalendar.calendarview.ICalendarEvent;
import com.cc.eventcalendar.calendarview.util.OSTimeUtil;

import java.util.Calendar;


/**
 * Created by ChengCn on 1/8/2016.
 */
public abstract class BaseEvent implements ICalendarEvent, Parcelable {

    protected long mStartTime;
    protected long mEndTime;

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long mEndTime) {
        this.mEndTime = mEndTime;
    }

    @Override
    public long getDuration() {
        return mEndTime - mStartTime;
    }

    @Override
    public boolean isHappensOn(long time) {
        return time >= mStartTime && time <= mEndTime;
    }

    @Override
    public boolean isHappensOnWithoutEdge(long time) {
        return time > mStartTime && time < mEndTime;
    }

    @Override
    public boolean isHappensOnDay(Calendar oneDay) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mStartTime);
        if (OSTimeUtil.isSameDay(oneDay, c)) {
            return true;
        }

        c.setTimeInMillis(mEndTime);
        return OSTimeUtil.isSameDay(oneDay, c);
    }
}
