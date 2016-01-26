package com.cc.eventcalendar.calendarview;

import android.os.Parcelable;

import java.util.Calendar;

/**
 * The interface definition for event.
 */
public interface ICalendarEvent extends Parcelable {
    class CalendarEventType {
        public static final int CALENDAR_EVENT_TYPE_NORMAL = 0;
    }
    /**
     * set id of the event.
     * @param id the id of the event.
     */
    void setID(String id);

    /**
     * get the id of the event.
     *
     */
    String getID();

    /**
     * set the event's start time
     * @param startTime the start time.
     */
    void setStartTime(long startTime);

    /**
     * get the event's end time.
     * @return The event's end time.
     */
    long getStartTime();

    /**
     * set the event's end time.
     * @param endTime the end time
     */
    void setEndTime(long endTime);

    /**
     * get the event's end time.
     * @return the event's end time.
     */
    long getEndTime();

    /**
     * set the event's title
     * @param title the title
     */
    void setTitle(String title);

    /**
     * get the event's title
     *
     */
    String getTitle();

    /**
     * set event's comment.
     * @param comment the comment
     */
    void setComment(String comment);

    /**
     * get event's comment
     *
     */
    String getComment();

    /**
     * set event's creator
     * @param creator the creator.
     */
    void setCreator(String creator);

    /**
     * get event's creator
     *
     */
    String getCreator();

    /**
     * get the duration of the event.
     *
     */
    long getDuration();

    /**
     * Check whether the event happens on the specified time.
     * @param time the check time
     * @return true if happens on the specified time, otherwise false.
     */
    boolean isHappensOn(long time);

    /**
     * Check whether the event happens on the specified time without edge..
     * @param time the check time
     * @return true if happens on the specified time, otherwise false.
     */
    boolean isHappensOnWithoutEdge(long time);

    /**
     * Check whether the event happens on the specified day.
     * @param oneDay the checked date time
     * @return true if happens on the specified day.
     */
    boolean isHappensOnDay(Calendar oneDay);

    /**
     * Check the event whether is all day event
     * @return true if it is a all day event.
     */
    boolean isAllDayEvent();

    /**
     * Get the type of the event.
     * @return
     */
    int getType();
}
