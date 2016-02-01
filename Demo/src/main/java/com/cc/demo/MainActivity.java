package com.cc.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cc.eventcalendar.calendarview.ICalendarEvent;
import com.cc.eventcalendar.calendarview.OSEventCalendarView;
import com.cc.eventcalendar.calendarview.OSEventTimeLineView;
import com.cc.eventcalendar.calendarview.adapter.EventsAdapter;
import com.cc.eventcalendar.calendarview.util.OSTimeUtil;

import java.util.Calendar;

public class MainActivity extends Activity {
    protected OSEventCalendarView mCalendarView;
    protected EventsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = new EventsAdapter();
        init();
    }

    private void init() {
        mCalendarView = (OSEventCalendarView) findViewById(R.id.calendarView);
        mCalendarView.setEventViewMode(OSEventCalendarView.EVENT_VIEW_MODE_TIMELINE);
        mCalendarView.setOnClickEventListener(new OSEventCalendarView.OnClickEventListener() {
            @Override
            public void onClickEvent(OSEventCalendarView view, ICalendarEvent event) {
                onClickItem((Event) event, 0);
            }
        });

        mCalendarView.setOnClickNewEventItemListener(new OSEventCalendarView.OnClickNewEventItemListener() {
            @Override
            public void onClickNewEventItem(OSEventCalendarView view, long defaultStartTime) {
               /* if (defaultStartTime < System.currentTimeMillis()) {
                    defaultStartTime = System.currentTimeMillis();
                }*/
                onClickItem(null, defaultStartTime);
            }
        });

        Event item = new Event();
        item.setTitle("Demo");
        item.setComment("Event Calendar view demo");
        Calendar c = Calendar.getInstance();
        OSTimeUtil.changeToStartOfDay(c );
        item.setID("id-1");
        item.setStartTime(c.getTimeInMillis());
        item.setEndTime(c.getTimeInMillis() + OSTimeUtil.MILLIS_IN_HOUR);
        mAdapter.addEvent(item);
       /*  mCalendarView.setOnClickTimelineItemListener(new OSEventTimeLineView.OnClickTimeLineItemListener() {
            @Override
            public boolean onClickTimeLineItem(OSEventTimeLineView timeLineView, int position, long startTime, long endTime) {
                return endTime < System.currentTimeMillis();
            }
        });

           mCalendarView.setOnMonthNameViewClickListener(new OSCalendarView.OnMonthNameViewClickListener() {
                @Override
                public void onMonthNameViewClickListener(View v, Calendar currentMonth) {
                    showDateSelector(currentMonth.getTimeInMillis());
                }
            });*/
        mCalendarView.setEventsAdapter(mAdapter);
        findViewById(R.id.change_list_mode_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.setEventViewMode(OSEventCalendarView.EVENT_VIEW_MODE_LIST);
            }
        });

        findViewById(R.id.change_timeline_mode_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.setEventViewMode(OSEventCalendarView.EVENT_VIEW_MODE_TIMELINE);
            }
        });
    }


    /*
        private void showDateSelector(long time) {
           showFragmentDialog(DialogBuilder.buildDateTimePickDialog(DateTimePickerDialog.TIME_PICKER_MODE_DATE, time, new DialogButtonClickListener() {
                @Override
                public void onClickLeftButton(DialogInterface dialog, Object data) {
                    // Nothing to do.
                }

                @Override
                public void onClickRightButton(DialogInterface dialog, Object data) {
                    long selectedDate = (long) data;
                    mTempDate.setTimeInMillis(selectedDate);
                    mCalendarView.goTo(mTempDate, true);
                }

                @Override
                public void onClickConfirmButton(DialogInterface dialog, Object data) {
                    // Nothing to do.
                }
            }), "DateTimePicker");
        }
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onClickItem(Event item, long startTime) {
        Intent i = new Intent(this, EditActivity.class);
        i.putExtra(EditActivity.ITEM_INTENT_KEY, item);
        i.putExtra(EditActivity.ITEM_TIME_INTENT_KEY, startTime);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 &&  data != null) {
            Event item = data.getParcelableExtra(EditActivity.ITEM_INTENT_KEY);
            if (item == null) {
                return;
            }
            if (resultCode == 0) {
                mAdapter.addEvent(item);
            } else {
                mAdapter.updateEvent(item);
            }
        }
    }
}
