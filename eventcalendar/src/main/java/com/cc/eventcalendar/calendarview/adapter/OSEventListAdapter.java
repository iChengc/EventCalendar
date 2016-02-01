package com.cc.eventcalendar.calendarview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.cc.eventcalendar.calendarview.ICalendarEvent;
import com.cc.eventcalendar.calendarview.R;
import com.cc.eventcalendar.calendarview.util.OSTimeUtil;

import java.util.Calendar;
import java.util.List;

/**
 * Created by ChengCn on 12/14/2015.
 */
public class OSEventListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private DayEventAdapter mDayEventAdapter;

    int mNormalDividerColor, mPMRepairColor;
    String mAllDayLabel;

    public OSEventListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mDayEventAdapter = new DayEventAdapter();
        mNormalDividerColor = mContext.getResources().getColor(R.color.calendar_event_vertical_divider);
        mPMRepairColor = mContext.getResources().getColor(R.color.calendar_all_day_event_vertical_divider);
        mAllDayLabel = mContext.getString(R.string.reservation_all_day_label);
    }

    /**
     * Update the day events of the adapter
     *
     * @param events     the event of one day.
     * @param dayOfEvent the day of the event.
     */
    public void updateEvents(List<? extends ICalendarEvent> events, Calendar dayOfEvent) {
        mDayEventAdapter.updateEvents(events, dayOfEvent);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDayEventAdapter.getEventsCount() + mDayEventAdapter.getAllDayEventsCount();
    }

    @Override
    public ICalendarEvent getItem(int position) {
        if (position < mDayEventAdapter.getAllDayEventsCount()) {
            return mDayEventAdapter.getAllDayEvent(position);
        }

        return mDayEventAdapter.getEvent(position - mDayEventAdapter.getAllDayEventsCount());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;

        if (convertView == null) {
            vh = createEventItemView(position, parent);
            convertView = vh._itemView;
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        bindItemView(vh, position);
        return convertView;
    }

    protected ViewHolder createEventItemView(int position, ViewGroup parent) {
        View view =mLayoutInflater.inflate(R.layout.eventcalendarview_layout_reservation_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    protected void bindItemView(ViewHolder vh, int position) {
        ICalendarEvent event = getItem(position);
        if (event == null) {
            return;
        }

        vh._commentView.setText(event.getComment());
        vh._titleView.setText(event.getTitle());
        vh._creatorView.setText(event.getCreator());

        if (event.isAllDayEvent()) {
            vh._startEndTimeViewContainer.setVisibility(View.GONE);
            vh._allDayView.setVisibility(View.VISIBLE);
            vh._allDayView.setText(mAllDayLabel);
            vh._divider.setBackgroundColor(event.getType() == ICalendarEvent.CalendarEventType.CALENDAR_EVENT_TYPE_NORMAL ? mNormalDividerColor : mPMRepairColor);
        } else {
            vh._startEndTimeViewContainer.setVisibility(View.VISIBLE);
            vh._allDayView.setVisibility(View.GONE);
            vh._startTimeView.setText(OSTimeUtil.formatLocaleDateTime("hhmma", event.getStartTime()));
            vh._endTimeView.setText(OSTimeUtil.formatLocaleDateTime("hhmma", event.getEndTime()));
            vh._divider.setBackgroundColor(mNormalDividerColor);
        }
    }

    static class ViewHolder {
        View _itemView, _divider, _startEndTimeViewContainer;
        TextView _titleView, _creatorView, _commentView, _startTimeView, _endTimeView, _allDayView;

        public ViewHolder(View itemView) {
            _itemView = itemView;
            _titleView = (TextView) itemView.findViewById(R.id.event_title);
            _creatorView = (TextView) itemView.findViewById(R.id.event_creator);
            _commentView = (TextView) itemView.findViewById(R.id.event_comment);
            _divider = itemView.findViewById(R.id.event_divider);
            _startEndTimeViewContainer = itemView.findViewById(R.id.event_start_end_time);
            _startTimeView = (TextView) itemView.findViewById(R.id.event_startTime_label);
            _endTimeView = (TextView) itemView.findViewById(R.id.event_endTime_label);
            _allDayView = (TextView) itemView.findViewById(R.id.event_allDay_label);

        }
    }
}
