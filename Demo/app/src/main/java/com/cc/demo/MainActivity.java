package com.cc.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.cc.eventcalendar.calendarview.ICalendarEvent;
import com.cc.eventcalendar.calendarview.OSEventCalendarView;
import com.cc.eventcalendar.calendarview.OSEventTimeLineView;
import com.cc.eventcalendar.calendarview.adapter.EventsAdapter;

public class MainActivity extends Activity {
    protected OSEventCalendarView mCalendarView;
    protected EventsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private void init() {
        mCalendarView = (OSEventCalendarView) findViewById(R.id.calendarView);
        mCalendarView.setOnClickEventListener(new OSEventCalendarView.OnClickEventListener() {
            @Override
            public void onClickEvent(OSEventCalendarView view, ICalendarEvent event) {
            }
        });

        mCalendarView.setOnClickNewEventItemListener(new OSEventCalendarView.OnClickNewEventItemListener() {
            @Override
            public void onClickNewEventItem(OSEventCalendarView view, long defaultStartTime) {
                if (defaultStartTime < System.currentTimeMillis()) {
                    defaultStartTime = System.currentTimeMillis();
                }
            }
        });

        mCalendarView.setOnClickTimelineItemListener(new OSEventTimeLineView.OnClickTimeLineItemListener() {
            @Override
            public boolean onClickTimeLineItem(OSEventTimeLineView timeLineView, int position, long startTime, long endTime) {
                return endTime < System.currentTimeMillis();
            }
        });

           /* mCalendarView.setOnMonthNameViewClickListener(new OSCalendarView.OnMonthNameViewClickListener() {
                @Override
                public void onMonthNameViewClickListener(View v, Calendar currentMonth) {
                    showDateSelector(currentMonth.getTimeInMillis());
                }
            });*/
        mCalendarView.setEventsAdapter(mAdapter);
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

    private void onClickItem(Item item) {
        Intent i = new Intent(this, EditActivity.class);
        i.putExtra(EditActivity.ITEM_INTENT_KEY, item);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Item item = data.getParcelableExtra(EditActivity.ITEM_INTENT_KEY);
            if (item == null) {
                return;
            }

        }
    }
}
