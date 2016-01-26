package com.cc.demo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditActivity extends AppCompatActivity {
    public static final String ITEM_INTENT_KEY = "com.cc.chengcn.exame.ITEM_INTENT_KEY";
    private EditText mTitleEdit;
    private EditText mCommentEdit;
    private TextView mStartTimeEdit;
    private TextView mEndTimeEdit;
    private TextView mCreatorEdit;
    private Switch mIsAllSwitch;

    private Item mItem;
    private Calendar mCalendar;

    private boolean mIsTimeShow = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mItem = getIntent().getParcelableExtra(ITEM_INTENT_KEY);
        if (mItem == null) {
            finish();
            return;
        }
        mCalendar = Calendar.getInstance();
        initView();
    }

    private void initView() {
        mTitleEdit = (EditText) findViewById(R.id.edit_title);
        mCommentEdit = (EditText) findViewById(R.id.edit_comment);
        mStartTimeEdit = (TextView) findViewById(R.id.edit_startTime);
        mEndTimeEdit = (TextView) findViewById(R.id.edit_endTime);
        mCreatorEdit = (TextView) findViewById(R.id.edit_creator);
        mIsAllSwitch = (Switch) findViewById(R.id.edit_isAllDay);

        mTitleEdit.setText(mItem.getTitle());
        mCommentEdit.setText(mItem.getComment());
        mStartTimeEdit.setText(formatDate(mItem.getStartTime(), "yyyy-MM-dd kk:mm"));
        mEndTimeEdit.setText(formatDate(mItem.getEndTime(), "yyyy-MM-dd kk:mm"));
        mCreatorEdit.setText(getString(R.string.creator_label, mItem.getCreator()));
        mStartTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.setTimeInMillis(mItem.getStartTime());
                if (mIsAllSwitch.isChecked()) {
                    showDatePicker((TextView)v, true);
                } else {
                    showDateTimePicker((TextView)v, true);
                }
            }
        });
        mEndTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.setTimeInMillis(mItem.getEndTime());
                if (mIsAllSwitch.isChecked()) {
                    showDatePicker((TextView) v, false);
                } else {
                    showDateTimePicker((TextView) v, false);
                }
            }
        });
        mIsAllSwitch.setChecked(mItem.isAllDay());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            onClickSaveMenu();
            return true;
        }
        return false;
    }

    private void onClickSaveMenu() {
        mItem.setTitle(mTitleEdit.getText().toString());
        mItem.setComment(mCommentEdit.getText().toString());
        mItem.setIsAllDay(mIsAllSwitch.isChecked());
        finishEdit();
    }

    private void finishEdit() {
        Intent data = new Intent();
        data.putExtra(ITEM_INTENT_KEY, mItem);
        setResult(RESULT_OK, data);
        finish();
    }

    private String formatDate(long date, String formatter) {
        DateFormat format = new SimpleDateFormat(formatter);
        return format.format(new Date(date));
    }

    private void showDatePicker(final TextView v, final boolean isStartTime) {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mCalendar.set(Calendar.HOUR_OF_DAY, 0);
                mCalendar.set(Calendar.MINUTE, 0);
                mCalendar.set(Calendar.SECOND, 0);
                mCalendar.set(Calendar.MILLISECOND, 0);
                v.setText(formatDate(mCalendar.getTimeInMillis(), mIsAllSwitch.isChecked() ? "yyyy-MM-dd" : "yyyy-MM-dd kk:mm"));
                if (isStartTime) {
                    mItem.setStartTime(mCalendar.getTimeInMillis());
                } else {
                    mItem.setEndTime(mCalendar.getTimeInMillis());
                }
            }
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void showDateTimePicker(final TextView v, final boolean isStartTime) {
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (!mIsTimeShow ) {
                    showTimePicker(v, isStartTime);
                    mIsTimeShow = true;
                }
            }
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void showTimePicker(final TextView v, final boolean isStartTime) {
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                v.setText(formatDate(mCalendar.getTimeInMillis(), mIsAllSwitch.isChecked() ? "yyyy-MM-dd" : "yyyy-MM-dd kk:mm"));
                if (isStartTime) {
                    mItem.setStartTime(mCalendar.getTimeInMillis());
                } else {
                    mItem.setEndTime(mCalendar.getTimeInMillis());
                }
                mIsTimeShow = false;
            }
        }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);
        dialog.show();
    }
}
