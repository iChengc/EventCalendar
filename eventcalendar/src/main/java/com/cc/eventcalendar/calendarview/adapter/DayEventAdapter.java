package com.cc.eventcalendar.calendarview.adapter;

import com.cc.eventcalendar.calendarview.ICalendarEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ChengCn on 12/30/2015.
 */
public class DayEventAdapter extends AbsOSEventAdapter {
    private List<ICalendarEvent> mEvents;
    private List<ICalendarEvent> mAllDayEvent;
    private Calendar mDayOfEvent;

    public DayEventAdapter() {
        mDayOfEvent = Calendar.getInstance();
        mAllDayEvent = new ArrayList<>();
        mEvents = new ArrayList<>();
    }

    public DayEventAdapter(List<? extends ICalendarEvent> events, Calendar dayOfEvent) {
        this();
        updateEvents(events, dayOfEvent);
    }

    /**
     * Update the day events of the adapter
     *
     * @param events     the event of one day.
     * @param dayOfEvent the day of the event.
     */
    public void updateEvents(List<? extends ICalendarEvent> events, Calendar dayOfEvent) {
        mEvents.clear();
        mAllDayEvent.clear();
        if (events != null) {
            mEvents.addAll(events);
            pickupAllDayEvent(mEvents);
        }

        if (dayOfEvent != null) {
            mDayOfEvent.setTimeInMillis(dayOfEvent.getTimeInMillis());
        }

        notifyDataSetChanged();
    }

    @Override
    public int getEventsCount() {
        if (mEvents != null) {
            return mEvents.size();
        }
        return 0;
    }

    @Override
    public ICalendarEvent getEvent(int index) {
        if (mEvents != null && index >= 0 && index < mEvents.size()) {
            return mEvents.get(index);
        }
        return null;
    }

    public int getAllDayEventsCount() {
        if (mAllDayEvent != null) {
            return mAllDayEvent.size();
        }
        return 0;
    }

    /**
     * get all day event.
     *
     * @param index the index of the all day event
     * @return
     */
    public ICalendarEvent getAllDayEvent(int index) {
        if (mAllDayEvent != null && index >= 0 && index < mAllDayEvent.size()) {
            return mAllDayEvent.get(index);
        }
        return null;
    }

    public Calendar getDay() {
        return mDayOfEvent;
    }

    /**
     * pick the all day event from the specified events.
     *
     * @param events the events that will be picked
     * @return the all day event list.
     */
    private void pickupAllDayEvent(List<ICalendarEvent> events) {

        if (events == null || events.isEmpty()) {
            return;
        }

        for (int i = events.size() - 1; i >= 0; i--) {
            if (events.get(i).isAllDayEvent()) {
                mAllDayEvent.add(events.get(i));
                events.remove(i);
            }
        }
    }
}
