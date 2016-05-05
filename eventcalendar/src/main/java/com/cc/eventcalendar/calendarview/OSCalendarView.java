package com.cc.eventcalendar.calendarview;

import android.app.Service;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cc.eventcalendar.calendarview.adapter.RecyclePagerAdapter;
import com.cc.eventcalendar.calendarview.util.DeviceUtils;
import com.cc.eventcalendar.calendarview.util.OSTimeUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ChengCn on 12/8/2015.
 */
public class OSCalendarView extends FrameLayout {
    /**
     * The default minimal date.
     */
    protected static final String DEFAULT_MIN_DATE = "01/01/1900";

    /**
     * The default maximal date.
     */
    protected static final String DEFAULT_MAX_DATE = "01/01/2100";

    /**
     * The calendar view mode month
     */
    public static final int CALENDAR_VIEW_MODE_MONTH = 0;

    /**
     * The canlendar view mode week
     */
    public static final int CALENDAR_VIEW_MODE_WEEK = 1;

    private static final int DEFAULT_DATE_TEXT_SIZE = 16;

    /**
     * The name of the month to display.
     */
    private TextView mMonthName;

    /**
     * The back to today button
     */
    private TextView mBackToToday;

    /**
     * The header with week day names.
     */
    private ViewGroup mDayNamesHeader;

    /**
     * The month calendar container.
     */
    private OSEventViewPager mMonthViewPager;

    /**
     * The month calendar container.
     */
    private OSEventViewPager mWeekViewPager;

    private ViewGroup mCalendarMonthViewContainer;

    /**
     * Use to get the relative position of the <Code>mCalendarMonthViewContainer</Code> in the calendar view.
     */
    private View mCalendarContainer;

    /**
     * The number of day per week to be shown.
     */
    private final int mDaysPerWeek = 7;

    /**
     * The min date that will be shown
     */
    private Calendar mMinDate;

    /**
     * The max date that will be shown
     */
    private Calendar mMaxDate;

    /**
     * The template date
     */
    protected Calendar mTempDate;

    /**
     * today
     */
    protected Calendar mToday;

    /**
     * The selected date.
     */
    private Calendar mSelectedDay;

    /**
     * Cached abbreviations for day of week names.
     */
    private String[] mDayNamesShort;

    /**
     * Cached full-length day of week names.
     */
    private String[] mDayNamesLong;

    private int mCurrentCalendarMode = CALENDAR_VIEW_MODE_MONTH;

    private MonthAdapter mMonthAdapter;
    private WeeksAdapter mWeekAdapter;

    private int mFirstDayOfWeek = Calendar.SUNDAY;

    private int mDateTextSize;
    private int mTodayBackgroundColor;
    private int mSelectedDayBackgroundColor;
    private int mFocusedMonthDateColor;
    private int mUnfocusedMonthDateColor;
    private int mEventFlagColor;

    /**
     * The week number of today since <code>mMinDate</code>
     */
    private int mWeekNumOfToday = 1;

    /**
     * The month number of today since <code>mMinDate</code>
     */
    private int mMonthNumOfToday = 1;

    // specify current is vertical scrolling.
    private boolean mIsVerticalScrolling;

    // specify current is horizontal scrolling.
    private boolean mIsHorizontalScrolling;

    /**
     * Use to cache the scrolling distance.
     */
    private float mScrollingDistance;

    protected GestureDetector mGestureDetector;

    private float mDownX;
    private float mDownY;
    private float mLastY;
    private int mTouchSlop = ViewConfiguration.getTouchSlop();

    private MonthView mFocusedMonthView;
    private WeekView mFocusedWeekView;

    protected ViewGroup mContentView;

    private OnDateChangeListener mOnDateChangeListener;
    private OnMonthNameViewClickListener mMonthNameViewClickListener;

    /**
     * The callback used to indicate the user changes the date.
     */
    public interface OnDateChangeListener {

        /**
         * Called upon change of the selected day.
         *
         * @param view            The view associated with this listener.
         * @param selectedDayTime the selected time in millisecond.
         */
        void onSelectedDayChange(OSCalendarView view, long selectedDayTime);
    }

    /**
     * The interface definition for a callback when the month name view was clicked
     */
    public interface OnMonthNameViewClickListener {

        /**
         * The callback method when the month name view was clicked.
         *
         * @param v            the month name view
         * @param currentMonth current month
         */
        void onMonthNameViewClickListener(View v, Calendar currentMonth);
    }

    public OSCalendarView(Context context) {
        this(context, null);
    }

    public OSCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OSCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        setClickable(true);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OSCalendarView, defStyleAttr, 0);

        final String minDateStr = a.getString(R.styleable.OSCalendarView_minDate);
        Date date = OSTimeUtil.parseDate(minDateStr);
        if (date == null) {
            date = OSTimeUtil.parseDate(DEFAULT_MIN_DATE);
        }
        mMinDate = Calendar.getInstance(Locale.getDefault());
        mMinDate.setTimeInMillis(date.getTime());

        final String maxDateStr = a.getString(R.styleable.OSCalendarView_maxDate);
        date = OSTimeUtil.parseDate(maxDateStr);
        if (date == null) {
            date = OSTimeUtil.parseDate(DEFAULT_MAX_DATE);
        }
        mMaxDate = Calendar.getInstance(Locale.getDefault());
        mMaxDate.setTimeInMillis(date.getTime());

        if (mMaxDate.before(mMinDate)) {
            throw new IllegalArgumentException("Max date cannot be before min date.");
        }

        mDateTextSize = a.getDimensionPixelSize(R.styleable.OSCalendarView_dateTextSize, DeviceUtils.dip2px(context, DEFAULT_DATE_TEXT_SIZE));
        mTodayBackgroundColor = a.getColor(R.styleable.OSCalendarView_todayBackgroundColor, context.getResources().getColor(R.color.red));
        mSelectedDayBackgroundColor = a.getColor(R.styleable.OSCalendarView_selectedDayBackgroundColor, context.getResources().getColor(R.color.blue));
        mFocusedMonthDateColor = a.getColor(R.styleable.OSCalendarView_focusedMonthDateColor, context.getResources().getColor(R.color.black));
        mUnfocusedMonthDateColor = a.getColor(R.styleable.OSCalendarView_unfocusedMonthDateColor, context.getResources().getColor(R.color.gray));
        mEventFlagColor = a.getColor(R.styleable.OSCalendarView_eventFlagColor, context.getResources().getColor(R.color.dark_gray));
        mCurrentCalendarMode = a.getInt(R.styleable.OSCalendarView_calendarMode, CALENDAR_VIEW_MODE_MONTH);
        a.recycle();

        mTempDate = Calendar.getInstance();
        mToday = Calendar.getInstance();
        mSelectedDay = Calendar.getInstance();
        OSTimeUtil.changeToStartOfDay(mSelectedDay);

        mMonthNumOfToday = OSTimeUtil.getMonthsSinceDate(mMinDate, mTempDate);
        mWeekNumOfToday = OSTimeUtil.getWeeksSinceDate(mMinDate, mTempDate, mFirstDayOfWeek);

        initView();
    }

    private void initView() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext()
                .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View content = layoutInflater.inflate(R.layout.eventcalendarview_layout_calendar_view, null, false);

        mDayNamesHeader = (ViewGroup) content.findViewById(R.id.calendar_vew_day_names);
        mMonthName = (TextView) content.findViewById(R.id.calendar_vew_month_name);
        mMonthViewPager = (OSEventViewPager) content.findViewById(R.id.calendar_vew_month_view);
        mWeekViewPager = (OSEventViewPager) content.findViewById(R.id.calendar_vew_week_view);
        mCalendarMonthViewContainer = (ViewGroup) content.findViewById(R.id.calendar_month_view_container);
        mCalendarContainer = content.findViewById(R.id.event_calendar_view_container);
        mContentView = (ViewGroup) content.findViewById(R.id.calendar_view_content);
        mBackToToday = (TextView) content.findViewById(R.id.calendar_vew_back_to_today);

        mMonthName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMonthNameViewClickListener != null) {
                    //OSLog.i("On Tab the month name view");
                    WeekView view = getCurrentFocusWeekView();
                    mTempDate.setTimeInMillis(view.getFirstDay().getTimeInMillis());
                    mTempDate.set(Calendar.DAY_OF_MONTH, view.mSelectedDayIndex);
                    mMonthNameViewClickListener.onMonthNameViewClickListener(v, mTempDate);
                }
            }
        });

        mBackToToday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(mToday, true);
            }
        });
        addView(content);
        requestDisallowInterceptTouchEvent(true);
        setUpHeader();
        setUpViewPager();

        setMonthDisplayed(mToday);
    }

    /**
     * Sets up the strings to be used by the header.
     */
    private void setUpHeader() {
        mDayNamesShort = new String[mDaysPerWeek];
        mDayNamesLong = new String[mDaysPerWeek];
        for (int i = mFirstDayOfWeek; i < mDaysPerWeek + mFirstDayOfWeek; i++) {
            mDayNamesShort[i - mFirstDayOfWeek] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_SHORTEST);
            mDayNamesLong[i - mFirstDayOfWeek] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_LONG);
        }

        TextView label;
        for (int i = 0, count = mDayNamesHeader.getChildCount(); i < count; i++) {
            label = (TextView) mDayNamesHeader.getChildAt(i);
            label.setText(mDayNamesShort[i]);
            label.setContentDescription(mDayNamesLong[i]);
            label.setVisibility(View.VISIBLE);

        }
        mDayNamesHeader.invalidate();
    }

    private void setUpViewPager() {
        mWeekAdapter = new WeeksAdapter();
        mWeekViewPager.setAdapter(mWeekAdapter);
        mWeekViewPager.setCurrentItem(mWeekNumOfToday, false);
        // mWeekViewPager.setVisibility(View.INVISIBLE);
        mWeekViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            /**
             * The flag indicate that whether the new selected page is triggered by scrolling
             */
            private boolean mIsTriggerByScrolling = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Nothing to do.
            }

            @Override
            public void onPageSelected(int position) {
                //OSLog.e("-------- mWeekViewPager onPageSelected  --------");
                mFocusedWeekView = (WeekView) mWeekViewPager.findViewWithTag(WeeksAdapter.VIEW_TAG_PREFIX + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // OSLog.e("onPageScrollStateChanged:" + state);
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mIsTriggerByScrolling = true;
                } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (mFocusedWeekView != null && mCurrentCalendarMode == CALENDAR_VIEW_MODE_WEEK) {
                        if (mIsTriggerByScrolling) {
                            mFocusedWeekView.setSelectedDayIndex(0);
                            mTempDate.setTimeInMillis(mFocusedWeekView.getFirstDay().getTimeInMillis());
                            //mTempDate.add(Calendar.DAY_OF_MONTH, mFocusedWeekView.mSelectedDayIndex);
                            mSelectedDay.setTimeInMillis(mTempDate.getTimeInMillis());
                            // mFocusedWeekView.onDateTapped(mSelectedDay);
                        }

                        final int selectedDayOfMonth = mSelectedDay.get(Calendar.DAY_OF_MONTH);
                        // If the selected day of month is not equal current month should adjust the month view pager index.
                        if (mSelectedDay.get(Calendar.MONTH) != mFocusedMonthView.mMonth) {
                            int newItemIndex = OSTimeUtil.getMonthsSinceDate(mMinDate, mTempDate);
                            mMonthViewPager.setCurrentItem(newItemIndex, false);
                        }
                        mFocusedMonthView.setSelectedDayIndex(selectedDayOfMonth + mFocusedMonthView.mFirstWeekDayOfMonth - 1);

                        setMonthDisplayed(mSelectedDay);
                        daySelected(mSelectedDay);
                    }

                    // Reset the flag if the state change to idle.
                    mIsTriggerByScrolling = false;
                }
            }
        });
        // mWeekAdapter.notifyDataSetChanged();

        mMonthAdapter = new MonthAdapter();
        mMonthViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            /**
             * The flag indicate that whether the new selected page is triggered by scrolling
             */
            private boolean mIsTriggerByScrolling = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Nothing to do.
            }

            @Override
            public void onPageSelected(int position) {
                //OSLog.e("-------- mMonthViewPager onPageSelected  --------");
                mFocusedMonthView = (MonthView) mMonthViewPager.findViewWithTag(WeeksAdapter.VIEW_TAG_PREFIX + position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // OSLog.e("onPageScrollStateChanged:" + state);
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mIsTriggerByScrolling = true;
                    return;
                } else if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (mFocusedMonthView == null) {
                        return;
                    }

                    // When switch month the week pager must be switching followed.
                    if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH) {
                        if (mIsTriggerByScrolling) {
                            mFocusedMonthView.setSelectedDayIndex(mFocusedMonthView.mFirstWeekDayOfMonth);

                            Calendar firstDay = mFocusedMonthView.getFirstDay();
                            mTempDate.setTimeInMillis(firstDay.getTimeInMillis());

                            // The first day of month is selected if a new month will be focus.
                            mTempDate.add(Calendar.DAY_OF_MONTH, mFocusedMonthView.mSelectedDayIndex);
                        /*if (mTempDate.get(Calendar.MONTH) != mFocusedMonthView.mMonth) {
                            mTempDate.add(Calendar.DAY_OF_MONTH, mDaysPerWeek);
                        }*/
                            mSelectedDay.setTimeInMillis(mTempDate.getTimeInMillis());
                            //mFocusedMonthView.onDateTapped(mSelectedDay);
                        }
                        final int weekNum = OSTimeUtil.getWeeksSinceDate(mMinDate, mSelectedDay, mFirstDayOfWeek);
                        // if the week number is not the current focus week, need to change the week number of the week view pager. otherwise change the selected day index.
                        if (mWeekViewPager.getCurrentItem() != weekNum) {
                            mWeekViewPager.setCurrentItem(weekNum, false);
                        }
                        mFocusedWeekView.setSelectedDayIndex(mSelectedDay.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek);
                        setMonthDisplayed(mSelectedDay);
                        daySelected(mSelectedDay);
                    }

                    // Reset the flag
                    mIsTriggerByScrolling = false;
                }
            }
        });
        mMonthViewPager.setAdapter(mMonthAdapter);
        mMonthViewPager.setCurrentItem(mMonthNumOfToday);
        if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_WEEK) {
            switchToWeekMode();
        } else {
            switchToMonthMode();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.selectedDayTime = mSelectedDay.getTimeInMillis();
        ss.mode = mCurrentCalendarMode;
        return ss;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCalendarMode(ss.mode);
        mSelectedDay.setTimeInMillis(ss.selectedDayTime);
        setMonthDisplayed(mSelectedDay);
        daySelected(mSelectedDay);
        mMonthViewPager.setCurrentItem(OSTimeUtil.getMonthsSinceDate(mMinDate, mSelectedDay), false);
        mWeekViewPager.setCurrentItem(OSTimeUtil.getWeeksSinceDate(mMinDate, mSelectedDay, mFirstDayOfWeek), false);
    }

    /**
     * This is here so we can identify single tap events and set the
     * selected day correctly
     */
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            mIsVerticalScrolling = false;
            mScrollingDistance = 0;
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return OSCalendarView.this.singleTapUp(e);
        }
    };

    /**
     * Sets the month displayed at the top of this view based on time. Override
     * to add custom events when the title is changed.
     *
     * @param calendar A day in the new focus month.
     */
    private void setMonthDisplayed(Calendar calendar) {

        final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY
                | DateUtils.FORMAT_SHOW_YEAR;
        final long millis = calendar.getTimeInMillis();
        String newMonthName = DateUtils.formatDateRange(getContext(), millis, millis, flags);
        mMonthName.setText(newMonthName);
        mMonthName.invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //OSLog.e("=====  dispatchTouchEvent  ======");
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                finishScrolling();
            case MotionEvent.ACTION_CANCEL:
                mDownY = 0;
                mIsHorizontalScrolling = false;
                mIsVerticalScrolling = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // OSLog.e("=====  onInterceptTouchEvent  ======");
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownY = event.getY();
                mDownX = event.getX();
                return false;
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                float deltaY = y - mDownY;
                mLastY = y;
                if (!mIsVerticalScrolling && !mIsHorizontalScrolling && Math.abs(deltaY) > mTouchSlop) {

                    if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH) {
                        if (deltaY < 0 && mDownY >= mCalendarContainer.getTop() && mDownY <= mFocusedMonthView.mHeight + mCalendarContainer.getTop()) {
                            mIsVerticalScrolling = true;
                            return true;
                        }
                    } else {
                        if (deltaY > 0 && mDownY >= mCalendarContainer.getTop() && mDownY <= mWeekViewPager.getMeasuredHeight() + mCalendarContainer.getTop()) {
                            mIsVerticalScrolling = true;
                            return true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDownY = 0;
                mIsHorizontalScrolling = false;
                mIsVerticalScrolling = false;
                break;

        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //OSLog.e("++++++++  onTouchEvent  ++++++++" + (mGestureDetector.onTouchEvent(event) ? "true" : "false"));
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final float y = event.getY();
                final float x = event.getX();
                final float deltaX = x - mDownX;
                if (!mIsVerticalScrolling && Math.abs(deltaX) > mTouchSlop) {
                    mIsHorizontalScrolling = true;
                    return false;
                }
                if (mIsVerticalScrolling) {
                    final float distanceY = y - mLastY;
                    doScrolling(distanceY);
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDownY = 0;
                mIsHorizontalScrolling = false;
                mIsVerticalScrolling = false;
                break;

        }
        return true;
    }

    protected void doScrolling(float distanceY) {
        mScrollingDistance += distanceY;
        if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH) {
            if ((mCalendarMonthViewContainer.getTop() == 0 && mScrollingDistance > 0)
                    || (mCalendarMonthViewContainer.getTop() + getMaxScrollOffset() == 0
                    && -mScrollingDistance > getMaxScrollOffset())) {
                return;
            } else {
                translateCalendarView(distanceY);
            }
        } else {
            if (mScrollingDistance > 0 && mScrollingDistance < getMaxScrollOffset()) {
                translateCalendarView(distanceY);
            }
        }
    }

    protected boolean singleTapUp(MotionEvent e) {
        if (mMonthViewPager.isEnabled()) {
            WeekView weekView = getCurrentFocusWeekView();
            // if we cannot find a day for the given location we are done
            if (!weekView.getDayFromLocation(e.getX(), e.getY() - mCalendarContainer.getTop(), mTempDate)) {
                return false;
            }
            // it is possible that the touched day is outside the valid range
            // we draw whole weeks but range end can fall not on the week end
            if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
                return false;
            }

            mSelectedDay.setTimeInMillis(mTempDate.getTimeInMillis());
            // need to switch month if tap the different month
            if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH) {
                final int selectDayOfMonth = mTempDate.get(Calendar.DAY_OF_MONTH);
                final int selectDayOfWeek = mTempDate.get(Calendar.DAY_OF_WEEK);
                final int weekNumber = OSTimeUtil.getWeeksSinceDate(mMinDate, mTempDate, mFirstDayOfWeek);

                // If has switch month view no need to <code>daySelected(int)</code> again, it has been called on callback method {@link ViewPager#onPageSelected}.
                if (switchMonthView(mTempDate.get(Calendar.MONTH), selectDayOfMonth, true)) {
                    return true;
                }

                if (mFocusedWeekView.mWeekNumber != weekNumber) {
                    mWeekViewPager.setCurrentItem(weekNumber, false);
                }

                mFocusedWeekView.setSelectedDayIndex(selectDayOfWeek - mFirstDayOfWeek);

            } else {
                switchMonthView(mTempDate.get(Calendar.MONTH), mTempDate.get(Calendar.DAY_OF_MONTH), false);
            }
            daySelected(mSelectedDay);
            setMonthDisplayed(mSelectedDay);
            weekView.onDateTapped(mSelectedDay);
            return true;
        }

        return false;
    }

    private void daySelected(Calendar selectedDay) {
        //OSLog.e("On Day " + OSTimeUtil.formatDate(day.getTimeInMillis(), "yyyy/MM/dd") + " selected");
        if (mOnDateChangeListener != null) {
            mOnDateChangeListener.onSelectedDayChange(this, selectedDay.getTimeInMillis());
        }
        mSelectedDay.setTimeInMillis(selectedDay.getTimeInMillis());
        OSTimeUtil.changeToStartOfDay(mSelectedDay);
        onDaySelected(selectedDay);
        if (OSTimeUtil.isSameDay(mSelectedDay, mToday)) {
            mBackToToday.setVisibility(View.GONE);
        } else {
            if (selectedDay.getTimeInMillis() < mToday.getTimeInMillis()) {
                mBackToToday.setBackgroundResource(R.drawable.ic_right_arrow);
            } else {
                mBackToToday.setBackgroundResource(R.drawable.ic_left_arrow);
            }
            mBackToToday.setVisibility(View.VISIBLE);
        }
    }

    /**
     * The callback method when one of day was selected.
     */
    protected void onDaySelected(Calendar selectedDay) {
    }

    /**
     * Invalidate the calendar view.
     */
    protected void invalidateCalendarView() {
        if (mFocusedMonthView != null) {
            mFocusedMonthView.invalidate();
        }

        if (mFocusedWeekView != null) {
            mFocusedWeekView.invalidate();
        }
    }

    /**
     * Set the selected day.
     *
     * @param selectedDay the selected day
     */
    public void setSelectedDay(Calendar selectedDay) {
        setSelectedDay(selectedDay, false);
    }

    /**
     * Set the selected day.
     *
     * @param selectedDay the selected day
     * @param isAnimate   specify whether is animate to change to the seletected day.
     */
    public void setSelectedDay(Calendar selectedDay, boolean isAnimate) {
        if (selectedDay == null || selectedDay.getTimeInMillis() == mSelectedDay.getTimeInMillis()) {
            return;
        }

        mSelectedDay.setTimeInMillis(selectedDay.getTimeInMillis());

        final int weekNum = OSTimeUtil.getWeeksSinceDate(mMinDate, mSelectedDay, mFirstDayOfWeek);
        final int monthNum = OSTimeUtil.getMonthsSinceDate(mMinDate, mSelectedDay);
        if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_WEEK) {
            if (mFocusedWeekView.mWeekNumber != weekNum) {
                mWeekViewPager.setCurrentItem(weekNum, isAnimate);
            } else {
                if (mFocusedMonthView.mMonthNumber != monthNum) {
                    mMonthViewPager.setCurrentItem(monthNum, isAnimate);
                }
                setMonthDisplayed(mSelectedDay);
                daySelected(mSelectedDay);
            }
        } else {
            if (mFocusedMonthView.mMonthNumber != monthNum) {
                mMonthViewPager.setCurrentItem(monthNum, isAnimate);
            } else {
                if (mFocusedWeekView.mWeekNumber != weekNum) {
                    mWeekViewPager.setCurrentItem(weekNum, isAnimate);
                }
                setMonthDisplayed(mSelectedDay);
                daySelected(mSelectedDay);
            }
        }

        // Reset the selected day index.
        mFocusedWeekView.setSelectedDayIndex(mSelectedDay.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek);
        mFocusedMonthView.setSelectedDayIndex(mSelectedDay.get(Calendar.DAY_OF_MONTH) + mFocusedMonthView.mFirstWeekDayOfMonth - 1);
    }

    /**
     * Set the mode of the calendar.
     *
     * @param mode the mode of the calendar.
     * @see {@link #CALENDAR_VIEW_MODE_MONTH}, {@link #CALENDAR_VIEW_MODE_WEEK}.
     */
    public void setCalendarMode(int mode) {
        if (mode == mCurrentCalendarMode) {
            return;
        }

        if (mode == CALENDAR_VIEW_MODE_MONTH) {
            switchToMonthMode();
        } else if (CALENDAR_VIEW_MODE_WEEK == mode) {
            switchToWeekMode();
        }
    }

    /**
     * Update the today's time, this may be useful when the App isn't closed for a long time.
     *
     * @param todayInMilliseconds the today's time in millisecond.
     */
    public void setToday(long todayInMilliseconds) {
        if (todayInMilliseconds < mMinDate.getTimeInMillis() || todayInMilliseconds > mMaxDate.getTimeInMillis()) {
            throw new IllegalArgumentException("The today's time is out of range.");
        }
        mTempDate.setTimeInMillis(todayInMilliseconds);
        if (!OSTimeUtil.isSameDay(mTempDate, mToday)) {
            mToday.setTimeInMillis(todayInMilliseconds);
            invalidateCalendarView();
        }
    }

    /**
     * Switch the month view pager to change the visible month view.
     *
     * @param focusMonth       the focus month.
     * @param selectDayOfMonth the selected day of month
     * @param animate          specify whether is changing month with animation or not.
     * @return true if has switch the month, otherwise false.
     */
    private boolean switchMonthView(int focusMonth, int selectDayOfMonth, boolean animate) {
        boolean ret = false;
        if (focusMonth != mFocusedMonthView.mMonth) {
            if (focusMonth == mFocusedMonthView.getMonthOfFirstWeekDay()) {
                ret = true;
                mMonthViewPager.setCurrentItem(mFocusedMonthView.mMonthNumber - 1, animate);
            } else if (focusMonth == mFocusedMonthView.getMonthOfLastWeekDay()) {
                ret = true;
                mMonthViewPager.setCurrentItem(mFocusedMonthView.mMonthNumber + 1, animate);
            }
        }

        // Reset the current focus month view selected day index.
        mFocusedMonthView.setSelectedDayIndex(selectDayOfMonth + mFocusedMonthView.mFirstWeekDayOfMonth - mFirstDayOfWeek);
        return ret;
    }

    private void finishScrolling() {
        if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH && mScrollingDistance < 0) {
            if (-mScrollingDistance > (mFocusedMonthView.mHeight >>> 2) && -mScrollingDistance < mFocusedMonthView.mHeight) {
                smoothToWeekMode();
            } else if (-mScrollingDistance < (mFocusedMonthView.mHeight >>> 2)) {
                smoothToMonthMode();
            } else {
                switchToWeekMode();
            }
        } else if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_WEEK && mCalendarMonthViewContainer.getTop() + getMaxScrollOffset() > 0) {
            if (mScrollingDistance > getMaxScrollOffset()) {
                switchToMonthMode();
            } else {
                smoothToMonthMode();
            }
        }
    }

    /**
     * Scroll the calendar view.
     *
     * @param distance the scroll distance.
     */
    private void translateCalendarView(float distance) {
        int offset = (int) distance;
        if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH) {
            if (mCalendarMonthViewContainer.getTop() + offset > 0) {
                offset = -mCalendarMonthViewContainer.getTop();
            } else if (mCalendarMonthViewContainer.getTop() + offset < -getMaxScrollOffset()) {
                offset = -getMaxScrollOffset() - mCalendarMonthViewContainer.getTop();
            }

            mCalendarMonthViewContainer.offsetTopAndBottom(offset);
        } else {
            if (mCalendarMonthViewContainer.getTop() + offset > 0) {
                offset = -mCalendarMonthViewContainer.getTop();
            }
            mCalendarMonthViewContainer.offsetTopAndBottom(offset);
        }

        checkSelectDayPosition(offset < 0);
    }

    private class SmoothToMonthModeAnimator extends Animation implements Animation.AnimationListener {
        private int mStartPosition, mEndPosition, mSwitchToMode;
        private float mLastInterpolatedTime;

        SmoothToMonthModeAnimator(int startPosition, int endPosition, int switchToMode) {
            mStartPosition = startPosition;
            mEndPosition = endPosition;
            mSwitchToMode = switchToMode;
            setDuration(200);
            setAnimationListener(this);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            int distance = (int) ((mEndPosition - mStartPosition) * (interpolatedTime - mLastInterpolatedTime));
            translateCalendarView(distance);
            mLastInterpolatedTime = interpolatedTime;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // Nothing to do.
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mSwitchToMode == CALENDAR_VIEW_MODE_WEEK) {
                switchToWeekMode();
            } else {
                switchToMonthMode();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // Nothing to do.
        }
    }

    private void switchToMonthMode() {
        MarginLayoutParams lp = (MarginLayoutParams) mCalendarMonthViewContainer.getLayoutParams();
        if (lp == null) {
            lp = new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        lp.topMargin = 0;
        mCalendarMonthViewContainer.setLayoutParams(lp);
        mWeekViewPager.setVisibility(View.INVISIBLE);
        mCurrentCalendarMode = CALENDAR_VIEW_MODE_MONTH;
        requestLayout();
    }

    private void switchToWeekMode() {
        MarginLayoutParams lp = (MarginLayoutParams) mCalendarMonthViewContainer.getLayoutParams();
        if (lp == null) {
            lp = new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        lp.topMargin = -getMaxScrollOffset();
        mCalendarMonthViewContainer.setLayoutParams(lp);
        mWeekViewPager.setVisibility(View.VISIBLE);
        mCurrentCalendarMode = CALENDAR_VIEW_MODE_WEEK;
        requestLayout();
    }

    private void smoothToMonthMode() {
        final int end = (int)(mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH ? -mScrollingDistance : getMaxScrollOffset() - mScrollingDistance);
        mCalendarMonthViewContainer.clearAnimation();
        Animation animation = new SmoothToMonthModeAnimator(0, end, CALENDAR_VIEW_MODE_MONTH);
        mCalendarMonthViewContainer.startAnimation(animation);
    }

    private void smoothToWeekMode() {
        mCalendarMonthViewContainer.clearAnimation();
        Animation animation = new SmoothToMonthModeAnimator((int)(getMaxScrollOffset() + mScrollingDistance), 0, CALENDAR_VIEW_MODE_WEEK);
        mCalendarMonthViewContainer.startAnimation(animation);
    }

    /**
     * go to the specified date.
     *
     * @param destDate the date that will be show
     * @param animate  specify whether is changing to the date within animation.
     */
    public void goTo(Calendar destDate, boolean animate) {
        if (destDate == null) {
            return;
        }
        if (destDate.before(mMinDate) || destDate.after(mMaxDate)) {
            throw new IllegalArgumentException("Time not between " + mMinDate.getTime()
                    + " and " + mMaxDate.getTime());
        }

        setSelectedDay(destDate, animate);
    }

    /**
     * @return max scrolling offset of the month view.
     */
    private int getMaxScrollOffset() {
        int offset;
        if (mFocusedMonthView == null) {
            // if the moth view pager not instantiate item, need to instantiate a template month view to measure the height of it.
            MonthView view = (MonthView) mMonthAdapter.getItemView(0);
            view.init(0);
            view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST));
            offset = view.mHeight - view.mWeekCellHeight;
        } else {
            offset = mFocusedMonthView.mHeight - mFocusedMonthView.mWeekCellHeight;
        }

        return offset;
    }

    /**
     * Check the selected day whether is arrive or leave the top of the view. show the week view if the selected day arrival top,
     * or dismiss it if leaving.
     *
     * @param isScrollingUp specify the whether is scrolling up
     */
    private void checkSelectDayPosition(boolean isScrollingUp) {
        if (isScrollingUp && mWeekViewPager.getVisibility() == View.INVISIBLE) {
            if (mFocusedMonthView.mSelectedTop + mCalendarMonthViewContainer.getTop() <= 0) {
                mWeekViewPager.setVisibility(View.VISIBLE);
            }
        } else if (!isScrollingUp && mWeekViewPager.getVisibility() == View.VISIBLE) {
            if (mFocusedMonthView.mSelectedTop + mCalendarMonthViewContainer.getTop() >= 0) {
                mWeekViewPager.setVisibility(View.INVISIBLE);
            }
        }

        Log.e("ooooo", "+++ checkSelectDayPosition +++" + (mWeekViewPager.getVisibility() == View.VISIBLE ? "Visible" : "Invisible"));
    }

    public Calendar getMaxDate() {
        return mMaxDate;
    }

    public void setMaxDate(long maxDate) {
        this.mMaxDate.setTimeInMillis(maxDate);
    }

    public Calendar getMinDate() {
        return mMinDate;
    }

    public void setMinDate(long minDate) {
        this.mMinDate.setTimeInMillis(minDate);
    }

    public OnDateChangeListener getOnDateChangeListener() {
        return mOnDateChangeListener;
    }

    public void setOnDateChangeListener(OnDateChangeListener onDateChangeListener) {
        this.mOnDateChangeListener = onDateChangeListener;
    }

    public OnMonthNameViewClickListener getMonthNameViewClickListener() {
        return mMonthNameViewClickListener;
    }

    public void setOnMonthNameViewClickListener(OnMonthNameViewClickListener monthNameViewClickListener) {
        this.mMonthNameViewClickListener = monthNameViewClickListener;
    }

    /**
     * get selected date time in millisecond
     */
    public long getSelectedDateTime() {
        return mSelectedDay.getTimeInMillis();
    }

    /**
     * Get the selected day as a {@link Calendar} instance.
     */
    public Calendar getSelectedDay() {
        return mSelectedDay;
    }

    /**
     * @return current focus week calendar view.
     */
    protected WeekView getCurrentFocusWeekView() {
        WeekView weekView;
        if (CALENDAR_VIEW_MODE_MONTH == mCurrentCalendarMode) {
            weekView = mFocusedMonthView; //(WeekView) mMonthViewPager.findViewWithTag(WeeksAdapter.VIEW_TAG_PREFIX + mMonthViewPager.getCurrentItem());
        } else {
            weekView = mFocusedWeekView; // (WeekView) mWeekViewPager.findViewWithTag(WeeksAdapter.VIEW_TAG_PREFIX + mWeekViewPager.getCurrentItem());
        }

        return weekView;
    }

    /**
     * Check whether has event at the specified date
     *
     * @param date the date
     * @return
     */
    protected boolean hasEvents(Calendar date) {
        return false;
    }

    /**
     * <p>
     * This is a specialized adapter for creating a list of weeks with
     * selectable days. It can be configured to display the week number, start
     * the week on a given day, show a reduced number of days, or display an
     * arbitrary number of weeks at a time.
     * </p>
     */
    private class WeeksAdapter extends RecyclePagerAdapter {

        // the tag prefix of the view
        public static final String VIEW_TAG_PREFIX = "CalendarView:";

        protected int mTotalCount;

        public WeeksAdapter() {
            init();
        }

        @Override
        protected View getItemView(int position) {
            //OSLog.e("---- getItemView(WeeksAdapter) ------");
            WeekView weekView = new WeekView(getContext());
            AbsListView.LayoutParams params =
                    new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
            weekView.setLayoutParams(params);
            if (mFocusedWeekView == null && position == mWeekViewPager.getCurrentItem()) {
                mFocusedWeekView = weekView;
            }
            return weekView;
        }

        @Override
        protected void bindItemView(View itemView, int position, boolean isReused) {
            // OSLog.e("---- bindItemView (WeeksAdapter:" + position + ") ------");
            itemView.setTag(VIEW_TAG_PREFIX + position);
            WeekView weekView = ((WeekView) itemView);
            weekView.init(position);

            // If current mode is CALENDAR_VIEW_MODE_MONTH need to change the selected day index of the focus week view.
            if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_MONTH) {
                mTempDate.setTimeInMillis(mFocusedMonthView.getFirstDay().getTimeInMillis());
                mTempDate.add(Calendar.DAY_OF_MONTH, mFocusedMonthView.mSelectedDayIndex);

                // If the selected day is in the current week change the selected day index.
                if (mTempDate.get(Calendar.WEEK_OF_YEAR) == weekView.mFirstDay.get(Calendar.WEEK_OF_YEAR)) {
                    weekView.mSelectedDayIndex = mTempDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
                }
            } /*else {
                mTempDate.setTimeInMillis(weekView.getFirstDay().getTimeInMillis());
                mTempDate.add(Calendar.DAY_OF_MONTH, weekView.mSelectedDayIndex);
                if (position == mWeekViewPager.getCurrentItem()) {
                    daySelected(mTempDate);
                }
            }*/
        }

        /**
         * Set up the gesture detector and selected time
         */
        protected void init() {
            // mSelectedDayOfWeek = mSelectedDate.get(Calendar.DAY_OF_WEEK);
            mTotalCount = OSTimeUtil.getWeeksSinceDate(mMinDate, mMaxDate, mFirstDayOfWeek);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTotalCount;
        }
    }

    private class MonthAdapter extends WeeksAdapter {

        @Override
        protected View getItemView(int position) {
            //OSLog.e("---- getItemView (MonthAdapter) ------");
            MonthView monthView = new MonthView(getContext());
            AbsListView.LayoutParams params =
                    new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
            monthView.setLayoutParams(params);
            if (mFocusedMonthView == null && position == mMonthViewPager.getCurrentItem()) {
                mFocusedMonthView = monthView;
            }
            return monthView;
        }

        @Override
        protected void bindItemView(View itemView, int position, boolean isReused) {
            //OSLog.e("---- bindItemView (MonthAdapter:" + position + ") ------");
            itemView.setTag(VIEW_TAG_PREFIX + position);
            MonthView monthView = ((MonthView) itemView);
            monthView.init(position);

            if (mFocusedWeekView == null) {
                return;
            }
            // If current mode is CALENDAR_VIEW_MODE_WEEK mode need to change the selected day index of the focus month view.
            if (mCurrentCalendarMode == CALENDAR_VIEW_MODE_WEEK) {
                mTempDate.setTimeInMillis(mFocusedWeekView.getFirstDay().getTimeInMillis());
                mTempDate.add(Calendar.DAY_OF_MONTH, mFocusedWeekView.mSelectedDayIndex);
                monthView.mSelectedDayIndex = mTempDate.get(Calendar.DAY_OF_MONTH) + monthView.mFirstWeekDayOfMonth - 1;
            } /*else {
                mTempDate.setTimeInMillis(monthView.getFirstDay().getTimeInMillis());
                mTempDate.add(Calendar.DAY_OF_MONTH, monthView.mSelectedDayIndex);
                if (position == mMonthViewPager.getCurrentItem()) {
                    daySelected(mTempDate);
                }
            }*/
        }

        /**
         * Set up the gesture detector and selected time
         */
        protected void init() {
            //mSelectedDayOfMonth = mSelectedDate.get(Calendar.DAY_OF_MONTH) + mSelectedDate.get(Calendar.DAY_OF_WEEK_IN_MONTH);
            mTotalCount = OSTimeUtil.getMonthsSinceDate(mMinDate, mMaxDate);
            // mFocusedMonth = mSelectedDate.get(Calendar.MONTH);
            notifyDataSetChanged();
        }
    }

    /**
     * <p>
     * This is a dynamic view for drawing a single week. It can be configured to
     * display the week number, start the week on a given day, or show a reduced
     * number of days. See {@link WeeksAdapter} for usage.
     * </p>
     */
    protected class WeekView extends View {

        // event flag radius.
        private static final int EVENT_FLAG_RADIUS = 2;

        /**
         * The default padding between date text and event flag.
         */
        private static final int EVENT_FLAG_PADDING = 5;

        /**
         * Default padding
         */
        private static final int DEFAULT_PADDING = 14;

        protected final Rect mTempRect = new Rect();

        protected final Paint mDrawPaint = new Paint();

        protected final Paint mDayDrawPaint = new Paint();

        // Cache the number strings so we don't have to recompute them each time
        protected String[] mDayNumbers;

        // The first day displayed by this item
        protected Calendar mFirstDay;

        // The month of the last day in this week
        protected int mLastWeekDayMonth = -1;

        // The position of this week, equivalent to weeks since the week of Jan 1st, 1900
        private int mWeekNumber = -1;

        /**
         * The week of current year.
         */
        private int mWeek;

        // Quick reference to the width of this view, matches parent
        protected int mWidth;

        // The height this view should draw at in pixels, set by height param
        protected int mHeight;

        // The number of days + a spot for week number if it is displayed
        protected int mNumCells;

        protected int mEventFlagRadius;
        protected int mEventFlagPadding;
        protected int mDefaultPadding;

        /**
         * The selected day index, start from 0.
         */
        protected int mSelectedDayIndex;

        public WeekView(Context context) {
            super(context);
            mGestureDetector = new GestureDetector(context, mGestureListener);
            mEventFlagRadius = DeviceUtils.dip2px(context, EVENT_FLAG_RADIUS);
            mEventFlagPadding = DeviceUtils.dip2px(context, EVENT_FLAG_PADDING);
            mDefaultPadding = DeviceUtils.dip2px(context, DEFAULT_PADDING);

            // Sets up any standard paints that will be used
            initializePaints();
        }

        /**
         * Initializes this week view.
         *
         * @param weekNumber The number of the week this view represents. The
         *                   week number is a zero based index of the weeks since
         *                   {@link android.widget.CalendarView#getMinDate()}.
         */
        public void init(int weekNumber) {
            mNumCells = mDaysPerWeek;
            mWeekNumber = weekNumber;
            if (mWeekNumber != mWeekNumOfToday) {
                if (OSTimeUtil.getWeeksSinceDate(mMinDate, mSelectedDay, mFirstDayOfWeek) == mWeekNumber) {
                    mSelectedDayIndex = mSelectedDay.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
                } else {
                    mSelectedDayIndex = 0;
                }
            } else {
                if (OSTimeUtil.isSameDay(mSelectedDay, mToday)) {
                    mTempDate.setTimeInMillis(System.currentTimeMillis());
                    mSelectedDayIndex = mTempDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
                } else {
                    mSelectedDayIndex = mSelectedDay.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
                }
            }
            mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());
            mTempDate.add(Calendar.WEEK_OF_YEAR, mWeekNumber);
            mTempDate.setFirstDayOfWeek(mFirstDayOfWeek);

            mWeek = mTempDate.get(Calendar.WEEK_OF_YEAR);

            // Now adjust our starting day based on the start day of the week
            int diff = mFirstDayOfWeek - mTempDate.get(Calendar.DAY_OF_WEEK);
            mTempDate.add(Calendar.DAY_OF_MONTH, diff);

            // Allocate space for caching the day numbers and focus values
            mDayNumbers = new String[mNumCells];

            mFirstDay = (Calendar) mTempDate.clone();

            for (int i = 0; i < mNumCells; i++) {
                // do not draw dates outside the valid range to avoid user confusion
                if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
                    mDayNumbers[i] = "";
                } else {
                    mDayNumbers[i] = String.format(Locale.getDefault(), "%d",
                            mTempDate.get(Calendar.DAY_OF_MONTH));
                }
                mTempDate.add(Calendar.DAY_OF_MONTH, 1);
            }
            // We do one extra add at the end of the loop, if that pushed us to
            // new month undo it
            if (mTempDate.get(Calendar.DAY_OF_MONTH) == 1) {
                mTempDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            mLastWeekDayMonth = mTempDate.get(Calendar.MONTH);
        }

        /**
         * Initialize the paint instances.
         */
        private void initializePaints() {
            mDrawPaint.setFakeBoldText(false);
            mDrawPaint.setAntiAlias(true);
            mDrawPaint.setStyle(Paint.Style.FILL);

            mDayDrawPaint.setFakeBoldText(true);
            mDayDrawPaint.setAntiAlias(true);
            mDayDrawPaint.setStyle(Paint.Style.FILL);
            mDayDrawPaint.setTextAlign(Paint.Align.CENTER);
            mDayDrawPaint.setTextSize(mDateTextSize);
        }

        /**
         * Returns the month of the last day in this week
         *
         * @return The month the last day of this view is in
         */
        public int getMonthOfLastWeekDay() {
            return mLastWeekDayMonth;
        }

        /**
         * Returns the first day in this view.
         *
         * @return The first day in the view.
         */
        public Calendar getFirstDay() {
            return mFirstDay;
        }

        /**
         * Calculates the day that the given x position is in, accounting for
         * week number.
         *
         * @param x The x position of the touch event.
         * @param y The x position of the touch event.
         * @return True if a day was found for the given location.
         */
        public boolean getDayFromLocation(float x, float y, Calendar outCalendar) {
            int start = getTop();
            int end = mWidth;
            if (x < start || x > end || y > mHeight || y < start) {
                outCalendar.clear();
                return false;
            }

            // Selection is (x - start) / (pixels/day) which is (x - start) * day / pixels
            int dayPosition = (int) ((x - start) * mDaysPerWeek / (end - start));
            // OSLog.e("getDayFromLocation:" + dayPosition);

            outCalendar.setTimeInMillis(mFirstDay.getTimeInMillis());
            outCalendar.add(Calendar.DAY_OF_MONTH, dayPosition);
            // OSLog.e("getDayFromLocation:======" + outCalendar.get(Calendar.DAY_OF_WEEK));
            return true;
        }

        protected void onDateTapped(Calendar selectedDay) {
            mSelectedDayIndex = selectedDay.get(Calendar.DAY_OF_WEEK) - 1;
            // OSLog.e("mSelectedDayIndex:" + mSelectedDayIndex);
            invalidate();
        }

        public void setSelectedDayIndex(int selectedDayIndex) {
            if (mSelectedDayIndex == selectedDayIndex) {
                return;
            }

            mSelectedDayIndex = selectedDayIndex;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            drawWeek(canvas);
        }

        /**
         * Draws the week and month day numbers for this week.
         *
         * @param canvas The canvas to draw on
         */
        private void drawWeek(Canvas canvas) {
            final float textHeight = mDayDrawPaint.getTextSize();
            final int y = (int) ((mHeight + textHeight - mEventFlagPadding - (mEventFlagRadius << 1)) / 2);
            final int nDays = mDaysPerWeek;
            final int divisor = 2 * nDays;
            mTempDate.setTimeInMillis(mFirstDay.getTimeInMillis());
            for (int i = 0; i < nDays; i++) {
                drawDayCell(canvas, i, divisor, y);
                mTempDate.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        protected void drawDayCell(Canvas canvas, int index, int divisor, int y) {
            int x = (2 * index + 1) * mWidth / divisor;
            mDayDrawPaint.getTextBounds(mDayNumbers[index], 0, mDayNumbers[index].length(), mTempRect);
            if (mSelectedDayIndex == index) {
                //OSLog.e("drawDayCell  mSelectedDayIndex:" + mSelectedDayIndex);
                int yOrigin = (mTempRect.height() >> 1) + mEventFlagPadding + (mEventFlagRadius << 1);
                drawSelectedBackground(canvas, x, y - (mTempRect.height() >> 1) + (mDefaultPadding / 6), yOrigin);
                mDayDrawPaint.setColor(getContext().getResources().getColor(R.color.white));
            } else {
                mDayDrawPaint.setColor(OSTimeUtil.isSameDay(mToday, mTempDate) ? mTodayBackgroundColor : mFocusedMonthDateColor);
            }
            canvas.drawText(mDayNumbers[index], x, y, mDayDrawPaint);

            if (hasEvents(mTempDate)) {
                drawEventFlag(canvas, x, y + mEventFlagPadding);
            }
        }

        protected void drawEventFlag(Canvas canvas, int x, int y) {
            mDrawPaint.setColor(mEventFlagColor);
            canvas.drawCircle(x, y + mEventFlagRadius, mEventFlagRadius, mDrawPaint);
        }

        protected void drawSelectedBackground(Canvas canvas, int x, int y, float radius) {
            mDrawPaint.setColor(OSTimeUtil.isSameDay(mToday, mTempDate) ? mTodayBackgroundColor : mSelectedDayBackgroundColor);
            canvas.drawCircle(x, y, radius, mDrawPaint);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mWidth = w;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            mHeight = ((int) mDayDrawPaint.getTextSize()) + (mEventFlagRadius << 1) + mEventFlagPadding + mDefaultPadding;
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
        }
    }

    /**
     * <p>
     * This is a dynamic view for drawing a single month. It can be configured to
     * display the month number, start the month on a given day, or show a reduced
     * number of days. See {@link WeeksAdapter} for usage.
     * </p>
     */
    protected class MonthView extends WeekView {
        // The position of this month, the total month from mMinDate.
        private int mMonthNumber;

        // The month of the view
        private int mMonth;

        // The number of week in this month.
        private int mNumOfWeek;

        /*// Whether this view has only focused days.
        protected boolean mHasUnfocusedDay;

        // Whether this view has a focused day.
        protected boolean mHasFocusedDay;*/

        // Quick lookup for checking which days are in the focus month
        private boolean[] mFocusDay;

        // The left edge of the selected day
        protected int mSelectedTop = -1;

        // The right edge of the selected day
        protected int mSelectedBottom = -1;

        // The month of the first day in this week
        protected int mMonthOfFirstWeekDay = -1;

        // The month of current focused.
        private int mCurrentFocusMonth = -1;

        public int mWeekCellHeight;

        /**
         * the first week day of this month.[0-6]
         */
        private int mFirstWeekDayOfMonth = 0;

        public MonthView(Context context) {
            super(context);
            mWeekCellHeight = ((int) mDayDrawPaint.getTextSize()) + (mEventFlagRadius << 1) + mEventFlagPadding + mDefaultPadding;
        }

        public void init(int monthNumber) {
            // OSLog.e("===== init(MonthView) =======");
            mMonthNumber = monthNumber;
            mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());

            mTempDate.add(Calendar.MONTH, mMonthNumber);
            mTempDate.setFirstDayOfWeek(mFirstDayOfWeek);
            mMonth = mTempDate.get(Calendar.MONTH);

            mNumOfWeek = mTempDate.getMaximum(Calendar.WEEK_OF_MONTH);
            mNumCells = mDaysPerWeek * mNumOfWeek;
            // Allocate space for caching the day numbers and focus values
            mDayNumbers = new String[mNumCells];
            mFocusDay = new boolean[mNumCells];

            // Now adjust our starting day based on the start day of the week
            mFirstWeekDayOfMonth = mTempDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
            mCurrentFocusMonth = mTempDate.get(Calendar.MONTH);
            int diff = mFirstDayOfWeek - mTempDate.get(Calendar.DAY_OF_WEEK);
            mTempDate.add(Calendar.DAY_OF_MONTH, diff);

            mFirstDay = (Calendar) mTempDate.clone();
            mMonthOfFirstWeekDay = mTempDate.get(Calendar.MONTH);

            // mHasUnfocusedDay = true;
            for (int i = 0; i < mNumOfWeek; i++) {
                for (int j = 0; j < mDaysPerWeek; j++) {
                    final boolean isFocusedDay = (mTempDate.get(Calendar.MONTH) == mCurrentFocusMonth);
                    mFocusDay[i * mDaysPerWeek + j] = isFocusedDay;
                    // mHasFocusedDay |= isFocusedDay;
                    //mHasUnfocusedDay &= !isFocusedDay;
                    // do not draw dates outside the valid range to avoid user confusion
                    if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
                        mDayNumbers[i] = "";
                    } else {
                        mDayNumbers[i * mDaysPerWeek + j] = String.format(Locale.getDefault(), "%d",
                                mTempDate.get(Calendar.DAY_OF_MONTH));
                    }
                    mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            // We do one extra add at the end of the loop, if that pushed us to
            // new month undo it
            if (mTempDate.get(Calendar.DAY_OF_MONTH) == 1) {
                mTempDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            mLastWeekDayMonth = mTempDate.get(Calendar.MONTH);

            if (monthNumber != mMonthNumOfToday) {
                if (OSTimeUtil.getMonthsSinceDate(mMinDate, mSelectedDay) == monthNumber) {
                    mSelectedDayIndex = mSelectedDay.get(Calendar.DAY_OF_MONTH) + mFirstWeekDayOfMonth - 1;
                } else {
                    mSelectedDayIndex = mFirstWeekDayOfMonth;
                }
            } else {
                if (OSTimeUtil.isSameDay(mSelectedDay, mToday)) {
                    mTempDate.setTimeInMillis(System.currentTimeMillis());
                    mSelectedDayIndex = mTempDate.get(Calendar.DAY_OF_MONTH) + mFirstWeekDayOfMonth - 1;
                } else {
                    mSelectedDayIndex = mSelectedDay.get(Calendar.DAY_OF_MONTH) + mFirstWeekDayOfMonth - 1;
                }
            }
            updateSelectionPositions();
        }

        @Override
        public void setSelectedDayIndex(int selectedDayIndex) {
            super.setSelectedDayIndex(selectedDayIndex);
            updateSelectionPositions();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            mHeight = mWeekCellHeight * mNumOfWeek;
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            drawMonth(canvas);
        }

        private void drawMonth(Canvas canvas) {
            final float textHeight = mDayDrawPaint.getTextSize();
            final int y = (int) ((mWeekCellHeight + textHeight - mEventFlagPadding - (mEventFlagRadius << 1)) / 2);
            final int numOfWeek = mNumOfWeek;
            final int divisor = 2 * mDaysPerWeek;
            mTempDate.setTimeInMillis(mFirstDay.getTimeInMillis());
            for (int i = 0; i < numOfWeek; i++) {
                for (int j = 0; j < mDaysPerWeek; j++) {
                    drawDayCell(canvas, i, j, divisor, y + i * mWeekCellHeight);
                    mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }

        protected void drawDayCell(Canvas canvas, int iIndex, int jIndex, int divisor, int y) {
            int x = (2 * jIndex + 1) * mWidth / divisor;
            int index = iIndex * mDaysPerWeek + jIndex;
            mDayDrawPaint.getTextBounds(mDayNumbers[index], 0, mDayNumbers[index].length(), mTempRect);
            if (index == mSelectedDayIndex) {
                int yOrigin = (mTempRect.height() >> 1) + mEventFlagPadding + (mEventFlagRadius << 1);
                drawSelectedBackground(canvas, x, y - (mTempRect.height() >> 1) + (mDefaultPadding / 6), yOrigin);
                mDayDrawPaint.setColor(getContext().getResources().getColor(R.color.white));
            } else {
                if (OSTimeUtil.isSameDay(mTempDate, mToday)) {
                    mDayDrawPaint.setColor(mTodayBackgroundColor);
                } else if (mFocusDay[index]) {
                    mDayDrawPaint.setColor(mFocusedMonthDateColor);
                } else {
                    mDayDrawPaint.setColor(mUnfocusedMonthDateColor);
                }
            }
            canvas.drawText(mDayNumbers[index], x, y, mDayDrawPaint);

            if (hasEvents(mTempDate)) {
                drawEventFlag(canvas, x, y + mEventFlagPadding);
            }
        }

        @Override
        public boolean getDayFromLocation(float x, float y, Calendar outCalendar) {
            int start = 0;
            int xEnd = mWidth;
            int yEnd = mHeight;
            if (x < start || x > xEnd || y < start || y > yEnd) {
                outCalendar.clear();
                return false;
            }

            int dayPosition;
            // Selection is (x - start) / (pixels/day) which is (x - start) * day / pixels
            int xPosition = (int) ((x - start) * mDaysPerWeek / (xEnd - start));
            int yPosition = ((int) y - start) / mWeekCellHeight;
            dayPosition = yPosition * mDaysPerWeek + xPosition;
            // OSLog.e("getDayFromLocation:" + dayPosition);

            outCalendar.setTimeInMillis(mFirstDay.getTimeInMillis());
            outCalendar.add(Calendar.DAY_OF_MONTH, dayPosition);
            // OSLog.e("getDayFromLocation:======" + outCalendar.get(Calendar.DAY_OF_WEEK));
            return true;
        }

        @Override
        protected void onDateTapped(Calendar selectedDay) {
            mSelectedDayIndex = selectedDay.get(Calendar.DAY_OF_MONTH) + mFirstWeekDayOfMonth - mFirstDayOfWeek;
            // OSLog.e("--------- Selected day(" + mSelectedDayIndex + ")------------------" + OSTimeUtil.formatDate(selectedDay.getTimeInMillis(), "yyyy/MM/dd"));
            updateSelectionPositions();
            invalidate();
        }

        /**
         * Returns the month of the first day in this week.
         *
         * @return The month the first day of this view is in.
         */
        public int getMonthOfFirstWeekDay() {
            return mMonthOfFirstWeekDay;
        }

        /**
         * This calculates the positions for the selected day lines.
         */
        protected void updateSelectionPositions() {
            final int selectedPosition = mSelectedDayIndex;
            if (selectedPosition < 0) {
                return;
            }

            mSelectedTop = selectedPosition / mDaysPerWeek * mWeekCellHeight;
            mSelectedBottom = mSelectedTop + mWeekCellHeight;
        }
    }

    /**
     * User interface state that is stored by TextView for implementing
     * {@link View#onSaveInstanceState}.
     */
    public static class SavedState extends BaseSavedState {
        long selectedDayTime;
        int mode;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(selectedDayTime);
            out.writeInt(mode);
        }

        @Override
        public String toString() {
            String str = "OSCalendarView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " selected time=" + selectedDayTime + " Mode=" + mode + "}";
            return str;
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            selectedDayTime = in.readLong();
            mode = in.readInt();
        }
    }
}
