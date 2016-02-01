package com.cc.eventcalendar.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;


import com.cc.eventcalendar.calendarview.adapter.DayEventAdapter;
import com.cc.eventcalendar.calendarview.adapter.OSEventAdapter;
import com.cc.eventcalendar.calendarview.util.DeviceUtils;
import com.cc.eventcalendar.calendarview.util.OSTimeUtil;
import com.cc.eventcalendar.calendarview.util.StrUtil;

import java.util.Calendar;

/**
 * Created by ChengCn on 12/14/2015.
 */
public class OSEventTimeLineView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final int DEFAULT_ITEM_HEIGHT = 50;
    /**
     * Max zoom factor.
     */
    private static final float MAX_ZOOM_FACTOR = 2;

    /**
     * Min zoom factor.
     */
    private static final float MIN_ZOOM_FACTOR = 0.7f;

    /**
     * The height of the time line view
     */
    private int mHeight;

    /**
     * The height of the 1 hour item
     */
    private int mItemHeight;

    /**
     * The height of the divider
     */
    private int mDividerHeight;

    /**
     * Current scale ratio of the view.
     */
    private float mCurrentZoomFactor = 1;
    private float mFactorBeforeZoom;
    private int mZoomItemHeight;
    private float mBaseDistance;

    /**
     * The color of the divider
     */
    private int mDividerColor;

    private int mEventCommentColor;
    private int mEventCommentSize;
    private int mEventTitleColor;
    private int mEventTitleSize;
    private int mEventBackgroundColor;
    private int mEventLeftMargin;
    private int mAddEventBackgroundColor;
    private int mEventVerticalDividerColor;
    private int mEventVerticalDividerWidth;

    private int mTimeLabelTextColor;
    private int mTimeLabelTextSize;

    private int mClickedPosition = -1;

    private int mActiveBackgroundColor;
    private int mInactiveBackgroundColor;

    /**
     * The padding of the event content. such as the title, comment and so on.
     */
    private int mEventContentPadding;

    /**
     * The minimum content padding;
     */
    private int mMinContentPadding;
    /**
     * The horizontal padding.
     */
    private int mHorizontalPadding;

    /**
     * The start x position of the timeline. if has all day events the top of the view will show the all day items,
     * and the <code>mYStartPosition</code> is equal to the height of the all day items. otherwise it may be 0.
     */
    private int mYStartPosition = 0;

    private EventDrawable mEventDrawable;
    private AddEventDrawable mAddEventDrawable;
    private AllDayDrawable mAllDayDrawable;
    private Paint mDrawPaint;
    private TextPaint mTextPaint;

    // The cache for the draw bounds during the view drawing.
    private Rect mRect;

    /**
     * The start time of current day
     */
    private Calendar mCalendarDay;

    private GestureDetector mDetector;

    private int mVisibleHeight;

    private DayEventAdapter mEventAdapter;

    private OnClickEventItemListener mOnClickItemListener;
    private OnClickAddItemListener mOnClickAddItemListener;
    private OnClickTimeLineItemListener mOnClickTimelineItemListener;

    private int mNowPosition;

    /**
     * The minimum event item height, default is the 10 minute height.
     */
    private int mMinEventItemHeight;

    /**
     * The center of the pinch gesture.
     */
    private Point mPinchCenter;
    private int mItemIndexOfPinchCenter;


    private float[] mTimeLinePointsCache = new float[4 * OSTimeUtil.HOURS_PER_DAY];
    private String mAllDayLabel;

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        mPinchCenter = new Point((int) e.getX(), (int) e.getY());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    /**
     * The interface definition for a callback when click a event item on the time line view.
     */
    public interface OnClickEventItemListener {
        void onClickEvent(OSEventTimeLineView timeLineView, ICalendarEvent event);
    }

    /**
     * The interface definition for a callback when user click one of timeline item of the view.
     */
    public interface OnClickTimeLineItemListener {
        /**
         * The callback method to be invoked when clicked the timeline item
         *
         * @param timeLineView the instance of the timeline view
         * @param position     the position of the clicked item.
         * @param startTime    the start time of the clicked item.
         * @param endTime      the end time of the clicked item.
         * @return true if has consume the clicked event, otherwise false.
         */
        boolean onClickTimeLineItem(OSEventTimeLineView timeLineView, int position, long startTime, long endTime);
    }

    /**
     * The interface definition for a callback when click an add event item on the time line view.
     */
    public interface OnClickAddItemListener {
        void onClickAddItem(OSEventTimeLineView timeLineView, int position);
    }

    public OSEventTimeLineView(Context context) {
        this(context, null);
    }

    public OSEventTimeLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OSEventTimeLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context c, AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.OSCalendarView, defStyleAttr, 0);
        mItemHeight = DeviceUtils.dip2px(c, DEFAULT_ITEM_HEIGHT);
        mHorizontalPadding = DeviceUtils.dip2px(c, 12);
        mEventContentPadding = DeviceUtils.dip2px(c, 8);
        mMinContentPadding = DeviceUtils.dip2px(c, 4);
        mDividerColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineDividerColor, getResources().getColor(R.color.light_gray));
        mDividerHeight = a.getDimensionPixelSize(R.styleable.OSEventTimeLineView_timeLineDividerHeight, 1);
        mEventTitleSize = a.getDimensionPixelSize(R.styleable.OSEventTimeLineView_timeLineTitleSize, DeviceUtils.dip2px(c, 14));
        mEventTitleColor = a.getDimensionPixelSize(R.styleable.OSEventTimeLineView_timeLineTitleColor, getResources().getColor(R.color.add_day_text_color));
        mEventCommentColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineCommentColor, getResources().getColor(R.color.add_day_text_color));
        mEventCommentSize = a.getDimensionPixelSize(R.styleable.OSEventTimeLineView_timeLineCommentSize, DeviceUtils.dip2px(c, 12));
        mTimeLabelTextColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineTimeLabelColor, getResources().getColor(R.color.gray));
        mTimeLabelTextSize = a.getDimensionPixelSize(R.styleable.OSEventTimeLineView_timeLineTimeLabelSize, DeviceUtils.dip2px(c, 14));
        mAddEventBackgroundColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineAddBackground, getResources().getColor(R.color.calendar_event_add_background));
        mEventBackgroundColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineEventBackground, getResources().getColor(R.color.calendar_event_background));
        mEventVerticalDividerColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineVerticalDividerColor, getResources().getColor(R.color.calendar_event_vertical_divider));
        mEventVerticalDividerWidth = a.getDimensionPixelSize(R.styleable.OSEventTimeLineView_timeLineVerticalDividerWidth, DeviceUtils.dip2px(c, 2));
        mActiveBackgroundColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineActiveBackgroundColor, 0);
        mInactiveBackgroundColor = a.getColor(R.styleable.OSEventTimeLineView_timeLineInactiveBackgroundColor, getResources().getColor(R.color.light_gray_transparent));
        a.recycle();

        mZoomItemHeight = mItemHeight;
        // Default minimum event item height is 10 minute height
        mMinEventItemHeight = mZoomItemHeight / 6;
        mDetector = new GestureDetector(c, this);
        mRect = new Rect();
        //mEvents = new ArrayList<>();
        mAllDayLabel = c.getString(R.string.reservation_all_day_label);
        mEventDrawable = new EventDrawable();
        mAddEventDrawable = new AddEventDrawable();
        mAllDayDrawable = new AllDayDrawable(mAllDayLabel);
        mCalendarDay = Calendar.getInstance();
        OSTimeUtil.changeToStartOfDay(mCalendarDay);
        initializePaints();

        mTextPaint.getTextBounds("24", 0, 2, mRect);
        mEventLeftMargin = mHorizontalPadding + 20 + mRect.width();

        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int[] locations = new int[2];
                        getLocationOnScreen(locations);
                        Rect r = new Rect();
                        // r will be populated with the coordinates of your view
                        // that area still visible.
                        getWindowVisibleDisplayFrame(r);
                        mVisibleHeight = r.bottom - locations[1];
                    }
                });
    }

    /**
     * Set the events adapter.
     *
     * @param adapter the date of the event occurred
     */
    public void setEventAdapter(DayEventAdapter adapter) {
        if (mEventAdapter != null) {
            mEventAdapter.unregisterDataSetObserver(dataObserver);
        }

        mEventAdapter = adapter;
        if (mEventAdapter != null) {
            mEventAdapter.registerDataSetObserver(dataObserver);
        }

        invalidateTimelineView();
    }

    public OSEventAdapter getEventAdapter() {
        return mEventAdapter;
    }

    // Adapter listener
    private DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            invalidateTimelineView();
        }

        @Override
        public void onInvalidated() {
            invalidateTimelineView();
        }
    };

    private void invalidateTimelineView() {
        mCalendarDay.clear();
        if (mEventAdapter != null) {
            mCalendarDay.setTimeInMillis(mEventAdapter.getDay().getTimeInMillis());
            OSTimeUtil.changeToStartOfDay(mCalendarDay);
        }

        // reset the clicked position.
        mClickedPosition = -1;
        requestLayout();
    }

    public void setOnClickAddItemListener(OnClickAddItemListener listener) {
        this.mOnClickAddItemListener = listener;
    }

    public void setOnClickEventItemListener(OnClickEventItemListener listener) {
        this.mOnClickItemListener = listener;
    }

    public void setOnClickTimelineItemListener(OnClickTimeLineItemListener onClickTimelineItemListener) {
        this.mOnClickTimelineItemListener = onClickTimelineItemListener;
    }

    /**
     * Initialize the paint instances.
     */
    private void initializePaints() {
        mDrawPaint = new Paint();
        mDrawPaint.setFakeBoldText(false);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(mTimeLabelTextColor);
        mTextPaint.setTextSize(mTimeLabelTextSize);
        mTextPaint.setLinearText(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mZoomItemHeight = (int) (mItemHeight * mCurrentZoomFactor);
        mMinEventItemHeight = mZoomItemHeight / 6;
       /* if (!mAllDayEvents.isEmpty()) {
            mYStartPosition = mAllDayEvents.size() * getHeightOfItem();
        } */
        if (mEventAdapter != null && mEventAdapter.getAllDayEventsCount() > 0) {
            mYStartPosition = mEventAdapter.getAllDayEventsCount() * (mItemHeight + mDividerHeight);
        } else {
            mYStartPosition = 0;
        }
        mHeight = OSTimeUtil.HOURS_PER_DAY * getHeightOfItem() + mYStartPosition;
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
        /*if (OSTimeUtil.isToday(mCalendarDay)) {
            mNowPosition = (int) ((OSEventTimeLineView.this.mHeight - mYStartPosition) * ((float) (System.currentTimeMillis() - mCalendarDay.getTimeInMillis())) / OSTimeUtil.MILLIS_IN_DAY) + mYStartPosition;
            scrollTo(0, mNowPosition);
        }*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // OSLog.e("=====  onDraw (OSEventTimeLineView) =======");
        drawBackground(canvas);
        drawDividerAndTimeLabel(canvas);
        drawEvents(canvas);
        if (mClickedPosition != -1) {
            mAddEventDrawable.draw(canvas);
        }
        drawAllDayEvent(canvas);

    }

    private void drawBackground(Canvas canvas) {
        if (OSTimeUtil.isToday(mCalendarDay)) {

            // Draw the background of the area before now time
            mNowPosition = (int) ((OSEventTimeLineView.this.mHeight - mYStartPosition) * ((float) (System.currentTimeMillis() - mCalendarDay.getTimeInMillis())) / OSTimeUtil.MILLIS_IN_DAY) + mYStartPosition;
            mRect.set(0, mYStartPosition, getWidth(), mNowPosition);
            mDrawPaint.setColor(mInactiveBackgroundColor);
            canvas.drawRect(mRect, mDrawPaint);

            // Draw now time line
            mRect.top = mRect.bottom;
            mRect.bottom = mRect.top + 2;
            mDrawPaint.setColor(getResources().getColor(R.color.red));
            canvas.drawRect(mRect, mDrawPaint);

            // Draw the background of the area after now
            mRect.bottom = mHeight;
            mDrawPaint.setColor(mActiveBackgroundColor);
            canvas.drawRect(mRect, mDrawPaint);
        } else {
            mRect.set(0, mYStartPosition, getWidth(), mHeight);
            mDrawPaint.setColor(mCalendarDay.getTimeInMillis() < System.currentTimeMillis() ? mInactiveBackgroundColor : mActiveBackgroundColor);
            canvas.drawRect(mRect, mDrawPaint);
        }
    }

    private void drawAllDayEvent(Canvas canvas) {
        if (mEventAdapter == null || mEventAdapter.getAllDayEventsCount() <= 0) {
            return;
        }

        int startPosition = 0;
        for (int i = 0; i < mEventAdapter.getAllDayEventsCount(); i++) {
            ICalendarEvent event = mEventAdapter.getAllDayEvent(i);
            mAllDayDrawable.setEvent(event);
            mAllDayDrawable.setStartPosition(startPosition);
            mAllDayDrawable.draw(canvas);
            startPosition += getHeightOfItem();
        }
    }

    private void drawDividerAndTimeLabel(Canvas canvas) {
        int dividerYPosition;
        mDrawPaint.setColor(mDividerColor);
        int width = getMeasuredWidth();
        mTextPaint.setColor(mTimeLabelTextColor);
        mTextPaint.setTextSize(mTimeLabelTextSize);
        for (int i = 0; i < OSTimeUtil.HOURS_PER_DAY; i++) {
            dividerYPosition = i * (mDividerHeight + mZoomItemHeight) + mYStartPosition;
            mTimeLinePointsCache[i << 2] = mHorizontalPadding;
            mTimeLinePointsCache[(i << 2) + 1] = dividerYPosition;
            mTimeLinePointsCache[(i << 2) + 2] = width - mHorizontalPadding;
            mTimeLinePointsCache[(i << 2) + 3] = dividerYPosition + mDividerHeight;

            canvas.drawText(String.valueOf(i), mHorizontalPadding, dividerYPosition + mTextPaint.getTextSize(), mTextPaint);
        }

        canvas.drawLines(mTimeLinePointsCache, mDrawPaint);
    }

    private void drawEvents(Canvas canvas) {
        if (mEventAdapter == null || mEventAdapter.getEventsCount() <= 0) {
            return;
        }
        for (int i = 0; i < mEventAdapter.getEventsCount(); i++) {
            ICalendarEvent event = mEventAdapter.getEvent(i);
            mEventDrawable.setEvent(event);
            mEventDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mBaseDistance = 0;
            mFactorBeforeZoom = mCurrentZoomFactor;
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            if (event.getPointerCount() == 2) {
                mPinchCenter = new Point(
                        (int) (event.getX(0) + event.getX(1)) / 2,
                        (int) (event.getY(0) + event.getY(1)) / 2);
                mItemIndexOfPinchCenter = calcItemIndexOfPosition(mPinchCenter.y);
                //OSLog.e("Start Scroll Y:" + getScrollY() + " mItemHeight:" + mItemHeight);
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (event.getPointerCount() >= 2) {
                // the pinch gesture recognizer
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                // calculate the distance of two points.
                float distance = (float) Math.sqrt(x * x + y * y);
                if (mBaseDistance == 0) {
                    mBaseDistance = distance;
                } else {
                    // remove the add event drawable when doing pinch gesture
                    mClickedPosition = -1;
                    // Calculate the scale.
                    float scale = distance / mBaseDistance;
                    onPinch(scale);
                }
                // intercept TouchEvent
                return true;
            }
        }

        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // clearAnimation();
        if (mFocusItemAnimation.hasStarted() && !mFocusItemAnimation.hasEnded()) {
            return true;
        }
        clearAnimation();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // The Y position of touch point less than the <code>mYStartPosition</code> specify it has clicked the all day event.
        if (e.getY() < mYStartPosition) {
            int allDayPosition = (int) (e.getY() / getHeightOfItem());
            if (mOnClickItemListener != null && mEventAdapter != null && allDayPosition < mEventAdapter.getAllDayEventsCount()) {
                mOnClickItemListener.onClickEvent(this, mEventAdapter.getAllDayEvent(allDayPosition));
            }

            unfocusItem(mClickedPosition);
            mClickedPosition = -1;
            return true;
        }
        final int position = calculateClickPosition(e);
        if (hasEventOnItem(position)) {
            ICalendarEvent event = isClickEvent(e);
            if (event != null) {
                if (mOnClickItemListener != null) {
                    mOnClickItemListener.onClickEvent(this, event);
                }
                unfocusItem(mClickedPosition);
                mClickedPosition = -1;
                return true;
            }

            if (!canFocus(position)) {
                unfocusItem(mClickedPosition);
                mClickedPosition = -1;
                return true;
            }
        }

        if (mClickedPosition != -1) {
            if (mClickedPosition != position) {
                // remove the previous focus item
                unfocusItem(mClickedPosition);

                // Show the add event drawable
                onClickTimeLineItem(position);
            } else {
                if (mOnClickAddItemListener != null) {
                    mOnClickAddItemListener.onClickAddItem(this, mClickedPosition);
                }
                unfocusItem(mClickedPosition);
                mClickedPosition = -1;
            }
        } else {
            onClickTimeLineItem(position);
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return scrolling(distanceY);
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        clearAnimation();
        mFlingAnimation.cancel();
        mFlingAnimation.setStartVelocity(velocityY);
        startAnimation(mFlingAnimation);
        return true;
    }

    /**
     * Doing the pinch gesture
     *
     * @param scaleRate the scale rate.
     */
    private void onPinch(float scaleRate) {
        scaleRate = mFactorBeforeZoom * scaleRate;
        if (scaleRate < MIN_ZOOM_FACTOR) {
            return;
        } else if (scaleRate > MAX_ZOOM_FACTOR) {
            return;
        }

        float deltaRatio = scaleRate - mCurrentZoomFactor;
        mCurrentZoomFactor = scaleRate;

        // Adjust the position of the view. Keep the position of the center item not moving.
        final float scrollDistance = mItemHeight * deltaRatio * (mItemIndexOfPinchCenter - 1);
        scrolling(scrollDistance);

        requestLayout();
    }

    protected void onClickTimeLineItem(int position) {
        if (position < 0 || position > 23 || (mEventAdapter != null && mEventAdapter.getAllDayEventsCount() > 0)
                || (mFocusItemAnimation.hasStarted() && !mFocusItemAnimation.hasEnded())) {
            return;
        }

        if (mOnClickTimelineItemListener != null) {
            long startTime = mCalendarDay.getTimeInMillis() + position * OSTimeUtil.MILLIS_IN_HOUR;
            long endTime = mCalendarDay.getTimeInMillis() + (position + 1) * OSTimeUtil.MILLIS_IN_HOUR;
            if (mOnClickTimelineItemListener.onClickTimeLineItem(this, position, startTime, endTime)) {
                // if the click event has been consume outside rest the click position.
                mClickedPosition = -1;
                return;
            }
        }

        mClickedPosition = position;
        focusItem(position);
    }

    /**
     * Calculate the item index in the specify position.
     *
     * @param position the Y coordinate position in the view.
     * @return the index of the item
     */
    private int calcItemIndexOfPosition(int position) {
        final int relativePosition = getScrollY() + position - mYStartPosition;
        return Math.round(relativePosition / (float) mZoomItemHeight);
    }

    private void unfocusItem(int position) {
        if (position == -1) {
            return;
        }

        mAddEventDrawable.setIsCleaning(true);
        calculateClickItemBounds(position);
        mAddEventDrawable.setDrawingRect(mRect);
        invalidate(mRect);
    }

    private void focusItem(int position) {
        clearAnimation();
        mAddEventDrawable.setIsCleaning(false);
        calculateClickItemBounds(position);
        mAddEventDrawable.setDrawingRect(mRect);
        mFocusItemAnimation.cancel();
        mFocusItemAnimation.setOriginRect(mRect);
        startAnimation(mFocusItemAnimation);
    }

    private FocusItemAnimation mFocusItemAnimation = new FocusItemAnimation();

    private class FocusItemAnimation extends Animation {
        private Rect mTempRect = new Rect();
        private Rect mOriginRect;
        private int verticalOffset = 0;
        private int horizontalOffset = 0;

        FocusItemAnimation() {
            setDuration(300);
        }

        public void setOriginRect(Rect rect) {
            this.mOriginRect = new Rect(rect);
        }

        @Override
        public void cancel() {
            verticalOffset = 0;
            horizontalOffset = 0;
            if (hasEnded()) {
                mOriginRect = null;
                return;
            }
            super.cancel();
            if (mOriginRect != null) {
                invalidate(mOriginRect);
            }

            mOriginRect = null;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (interpolatedTime > 0.5f) {
                verticalOffset = horizontalOffset = Math.round(20 * (interpolatedTime - 1));
            } else {
                verticalOffset = (int) (40 * interpolatedTime * (15 * interpolatedTime - 13)) + 100;
                horizontalOffset = Math.round(40 - 100 * interpolatedTime);
            }
            //(int)(20 * interpolatedTime * (12 * interpolatedTime - 17)) + 100; //(int)(20 * interpolatedTime * (10 * interpolatedTime - 13)) + 60; //Math.round(Math.abs(150 * (interpolatedTime - 0.8f))) - 30;
            //(int)(40 * interpolatedTime * (3 * interpolatedTime - 4)) + 40;// Math.round(Math.abs(100 * (interpolatedTime - 0.8f))) - 20;

            mTempRect.top = mOriginRect.top + verticalOffset;
            mTempRect.bottom = mOriginRect.bottom - verticalOffset;
            mTempRect.left = mOriginRect.left + horizontalOffset;
            mTempRect.right = mOriginRect.right - horizontalOffset;
            mAddEventDrawable.mDrawingRect = mTempRect;
            invalidate(mTempRect);
        }
    }

    private int calculateClickPosition(MotionEvent ev) {
        float yPosition = ev.getY();
        int itemPosition = (int) Math.floor((yPosition + getScrollY() - mYStartPosition) / getHeightOfItem());
        calculateClickItemBounds(itemPosition);
        return itemPosition;
    }

    private void calculateClickItemBounds(int clickPosition) {
        mRect.left = mEventLeftMargin;
        mRect.right = OSEventTimeLineView.this.getMeasuredWidth() - mHorizontalPadding;
        mRect.top = clickPosition * getHeightOfItem() + mDividerHeight + mYStartPosition;
        mRect.bottom = mRect.top + mZoomItemHeight;
    }

    /**
     * Check whether has event on the specified item.
     *
     * @param position the position of the item
     * @return true if have event. otherwise false.
     */
    private boolean hasEventOnItem(int position) {
        if (mEventAdapter == null || mEventAdapter.getEventsCount() <= 0) {
            return false;
        }
        final long itemStartTime = mCalendarDay.getTimeInMillis() + OSTimeUtil.MILLIS_IN_HOUR * position;
        // Sub 1 to make sure the end time of this item is lower than the the next item's start time.
        final long itemEndTime = itemStartTime + OSTimeUtil.MILLIS_IN_HOUR - 1;
        for (int i = 0; i < mEventAdapter.getEventsCount(); i++) {
            ICalendarEvent event = mEventAdapter.getEvent(i);
            if (event.isHappensOnWithoutEdge(itemStartTime) || event.isHappensOnWithoutEdge(itemEndTime)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the position can focus and show the add event button.
     *
     * @param position the position of the clicked
     * @return
     */
    private boolean canFocus(int position) {
        if (mEventAdapter == null || mEventAdapter.getEventsCount() <= 0) {
            return false;
        }
        final long itemStartTime = mCalendarDay.getTimeInMillis() + OSTimeUtil.MILLIS_IN_HOUR * position;
        // Sub 1 to make sure the end time of this item is lower than the the next item's start time.
        final long itemEndTime = itemStartTime + OSTimeUtil.MILLIS_IN_HOUR;
        for (int i = 0; i < mEventAdapter.getEventsCount(); i++) {
            ICalendarEvent event = mEventAdapter.getEvent(i);
            if (event.getStartTime() > itemStartTime && event.getStartTime() < itemEndTime) {
                return ((float) (event.getStartTime() - itemEndTime)) / OSTimeUtil.MILLIS_IN_HOUR < 0.3f;
            } else if (event.getEndTime() > itemStartTime && event.getEndTime() < itemEndTime) {
                return ((float) (itemStartTime - event.getEndTime())) / OSTimeUtil.MILLIS_IN_HOUR < 0.3f;
            }
        }
        return false;
    }

    /**
     * Check  the click position whether is event or not.
     *
     * @param e the touch motion event
     * @return the event if the position has a event, otherwise false.
     */
    private ICalendarEvent isClickEvent(MotionEvent e) {
        if (mEventAdapter == null || mEventAdapter.getEventsCount() <= 0) {
            return null;
        }
        float relativePosition = getScrollY() + e.getY() - mYStartPosition;
        long time = mCalendarDay.getTimeInMillis() + (int) (OSTimeUtil.MILLIS_IN_DAY * relativePosition / (mHeight - mYStartPosition));

        for (int i = 0; i < mEventAdapter.getEventsCount(); i++) {
            ICalendarEvent event = mEventAdapter.getEvent(i);
            if (event.isHappensOn(time)) {
                return event;
            }
        }
        return null;
    }

    private boolean scrolling(float distance) {
        if (distance < 0) {
            if (getScrollY() <= 0) {
                return false;
            } else if (getScrollY() + distance < 0) {
                distance = -getScrollY();
            }
        } else {
            if (mHeight - mVisibleHeight - getScrollY() <= 0) {
                return true;
            } else if (mHeight - mVisibleHeight - getScrollY() - distance < 0) {
                distance = mHeight - mVisibleHeight - getScrollY();
            }
        }

        scrollBy(0, (int) distance);
        return true;
    }

    private int getHeightOfItem() {
        return mZoomItemHeight + mDividerHeight;
    }

    private FlingAnimator mFlingAnimation = new FlingAnimator();

    private class FlingAnimator extends Animation {
        private float mStartVelocity;

        FlingAnimator() {
            setDuration(1000);
        }

        public void setStartVelocity(float startVelocity) {
            mStartVelocity = startVelocity;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float scrollDistance = mStartVelocity * (1 - interpolatedTime) / 256;
            if (!scrolling(-scrollDistance)) {
                cancel();
            }
        }
    }

    private class EventDrawable extends Drawable {

        protected ICalendarEvent mEvent;
        protected Rect mTempRect = new Rect();
        public EventDrawable setEvent(ICalendarEvent event) {
            mEvent = event;
            return this;
        }

        /**
         * Calculate the draw region
         */
        private void calculateRect() {
            mRect.left = mEventLeftMargin;
            mRect.right = OSEventTimeLineView.this.getMeasuredWidth() - mHorizontalPadding;
            mRect.top = (int) ((OSEventTimeLineView.this.mHeight - mYStartPosition) * ((float) (mEvent.getStartTime() - mCalendarDay.getTimeInMillis())) / OSTimeUtil.MILLIS_IN_DAY) + mDividerHeight + mYStartPosition;
            mRect.bottom = (int) ((OSEventTimeLineView.this.mHeight - mYStartPosition) * ((float) (mEvent.getEndTime() - mCalendarDay.getTimeInMillis())) / OSTimeUtil.MILLIS_IN_DAY) + mYStartPosition;
            mRect.top = mRect.top < 0 ? 0 : mRect.top;
            mRect.bottom = mRect.bottom > mHeight ? mHeight : mRect.bottom;
            if (mRect.height() < mMinEventItemHeight) {
                mRect.bottom = mRect.top + mMinEventItemHeight;
            }
        }

        @Override
        public void draw(Canvas canvas) {
            calculateRect();
            drawBackground(canvas);
            drawContent(canvas);
        }

        private void drawBackground(Canvas canvas) {
            mDrawPaint.setColor(mEventVerticalDividerColor);
            mRect.right = mRect.left + mEventVerticalDividerWidth;
            canvas.drawRect(mRect, mDrawPaint);

            mRect.left = mRect.right;
            mRect.right = OSEventTimeLineView.this.getMeasuredWidth() - mHorizontalPadding;
            mDrawPaint.setColor(mEventBackgroundColor);
            canvas.drawRect(mRect, mDrawPaint);
        }

        private void drawContent(Canvas canvas) {
            if (mEvent == null) {
                return;
            }

            mTextPaint.setTextAlign(Paint.Align.LEFT);
            // The The x-coordinate and  y-coordinate of the origin of the text
            float contentX, contentY;
            float titleCreatorDividerPos = mRect.left + (mRect.width() << 1) / 3.0f;
            if (!StrUtil.isEmpty(mEvent.getTitle())) {
                mTextPaint.setColor(mEventTitleColor);
                mTextPaint.setTextSize(mEventTitleSize);
                contentX = mRect.left + mEventContentPadding;
                if (mRect.height() < mTextPaint.getTextSize() + (mMinContentPadding << 1)) {
                    contentY = mRect.bottom - mMinContentPadding;
                    mTextPaint.setTextSize(mRect.height() - (mMinContentPadding << 1));
                    drawTitleAndCreator(canvas, titleCreatorDividerPos, contentX, contentY);
                    return;
                } else if (mRect.height() < mTextPaint.getTextSize() + (mEventContentPadding << 1)) {
                    contentY = mRect.bottom - (((int) (mRect.height() - mTextPaint.getTextSize())) >> 1);
                    drawTitleAndCreator(canvas, titleCreatorDividerPos, contentX, contentY);
                    return;
                }

                if (mRect.height() >= mTextPaint.getTextSize() + (mEventContentPadding << 1)) {
                    contentY = mRect.top + mEventContentPadding + mTextPaint.getTextSize();
                    drawTitleAndCreator(canvas, titleCreatorDividerPos, contentX, contentY);
                }
            }

            drawComment(canvas);
        }

        private void drawTitleAndCreator(Canvas canvas, float titleCreatorDividerPos, float contentX, float contentY) {
            String adjustTitle = TextUtils.ellipsize(mEvent.getTitle(), mTextPaint, titleCreatorDividerPos - contentX, TextUtils.TruncateAt.END).toString();
            mTextPaint.getTextBounds(adjustTitle, 0, adjustTitle.length(), mTempRect);
            canvas.drawText(adjustTitle, contentX, contentY, mTextPaint);
            drawCreator(canvas, titleCreatorDividerPos, contentY);
        }

        /**
         * Draw creator text
         *
         * @param canvas   the canvas.
         * @param startPos the start position of the creator text area.,
         * @param contentY The y-coordinate of the origin of the text being drawn
         */
        private void drawCreator(Canvas canvas, float startPos, float contentY) {
            if (!StrUtil.isEmpty(mEvent.getCreator())) {
                String adjustCreator = TextUtils.ellipsize(mEvent.getCreator(), mTextPaint, mRect.right - mEventContentPadding - startPos, TextUtils.TruncateAt.END).toString();
                mTextPaint.getTextBounds(adjustCreator, 0, adjustCreator.length(), mTempRect);
                canvas.drawText(adjustCreator, mRect.right - mEventContentPadding - mTempRect.width(), contentY, mTextPaint);
            }
        }

        private void drawComment(Canvas canvas) {
            if (StrUtil.isEmpty(mEvent.getComment())) {
                return;
            }

            mTextPaint.setColor(mEventCommentColor);
            mTextPaint.setTextSize(mEventCommentSize);
            final int titleHeight = mTempRect.height();
            float contentX, contentY;
            contentX = mRect.left + mEventContentPadding;

            // The remain height for draw comment include the content padding.
            float maxRemainHeight = mRect.height() - (StrUtil.isEmpty(mEvent.getTitle()) ? 0 : mEventContentPadding + titleHeight);
            if (maxRemainHeight - (mMinContentPadding << 1) < mEventCommentSize) {
                // No enough area to show the comment.
                return;
            } else if (maxRemainHeight - (mEventContentPadding << 1) < mEventCommentSize) {
                int top = mRect.top + (StrUtil.isEmpty(mEvent.getTitle()) ? ((int) (maxRemainHeight - mEventCommentSize) >> 1) : mEventContentPadding + mMinContentPadding + titleHeight);
                contentY = top + mEventTitleSize;
            } else if (maxRemainHeight - (mEventContentPadding << 1) < (mEventCommentSize << 1) + 5 ||
                    calculateWidthFromFontSize(mEvent.getComment(), mTempRect) < mRect.width() - (mEventContentPadding << 1)) {
                // If the remain space can only show single line or the comment is no more than single line.
                int top = mRect.top + (StrUtil.isEmpty(mEvent.getTitle()) ? mEventContentPadding : (mEventContentPadding << 1) + titleHeight);
                contentY = top + mEventTitleSize;
            } else {
                int top = mRect.top + (StrUtil.isEmpty(mEvent.getTitle()) ? mEventContentPadding : (mEventContentPadding << 1) + titleHeight);
                drawMultilineComment(canvas, top, maxRemainHeight - (mEventContentPadding << 1), mEventCommentSize);
                return;
            }

            // Only can show single line comment.
            String ellipsisComment = TextUtils.ellipsize(mEvent.getComment(), mTextPaint, mRect.width() - (mEventContentPadding << 1), TextUtils.TruncateAt.END).toString();
            canvas.drawText(ellipsisComment, contentX, contentY, mTextPaint);
        }

        /**
         * Draw multiline comment.
         *
         * @param canvas            the canvas.
         * @param top               the top position of the comment will be draw.
         * @param maxRemainDrawingHeight   the max remain height for drawing the comment, exclude the padding.
         * @param commentLineHeight the line height of the comment.
         */
        private void drawMultilineComment(Canvas canvas, int top, float maxRemainDrawingHeight, int commentLineHeight) {

            StaticLayout staticLayout = new StaticLayout(mEvent.getComment(), mTextPaint, mRect.width() - (mEventContentPadding << 1), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            canvas.save();
            int textHeight = staticLayout.getLineTop(staticLayout.getLineCount());
           if (textHeight > maxRemainDrawingHeight) {
                float delta = textHeight - maxRemainDrawingHeight;
                int offset = staticLayout.getLineEnd(staticLayout.getLineCount() - 1 - (int) Math.ceil(delta / commentLineHeight));
               String ellipsisComment;
               if (offset == 0) {
                   ellipsisComment = TextUtils.ellipsize(mEvent.getComment(), mTextPaint, mRect.width() - (mEventContentPadding << 1), TextUtils.TruncateAt.END).toString();
               } else {
                   ellipsisComment = mEvent.getComment().substring(0, offset - 3) + "...";
               }
                staticLayout = new StaticLayout(ellipsisComment, mTextPaint, mRect.width() - (mEventContentPadding << 1), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            }
            //text will be drawn from left
            float textXCoordinate = mRect.left + mEventContentPadding;

            canvas.translate(textXCoordinate, top);

            //draws static layout on canvas
            staticLayout.draw(canvas);
            canvas.restore();
        }

        private int calculateWidthFromFontSize(String testString, Rect rect) {
            mTextPaint.getTextBounds(testString, 0, testString.length(), rect);
            return (int) Math.ceil(rect.width());
        }

        private int calculateHeightFromFontSize(String testString, Rect rect) {
            mTextPaint.getTextBounds(testString, 0, testString.length(), rect);
            return (int) Math.ceil(rect.height());
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

    private class AddEventDrawable extends Drawable {
        private boolean mIsCleaning;
        private Rect mDrawingRect;
        private Bitmap mAddBitmap;

        AddEventDrawable() {
            BitmapFactory.Options option = new BitmapFactory.Options();
            mAddBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_new, option);
            int requireSize = mZoomItemHeight / 3;
            mAddBitmap = DeviceUtils.resizeImageBitmap(mAddBitmap, requireSize, true);
        }

        @Override
        public void draw(Canvas canvas) {
            if (mClickedPosition == -1) {
                return;
            }
            drawBackground(canvas);
            canvas.drawBitmap(mAddBitmap, mDrawingRect.left + mHorizontalPadding, mDrawingRect.centerY() - (mAddBitmap.getHeight() >> 1), null);
        }

        /**
         * Set the flag to specify whether is cleaning the add event drawable.
         *
         * @param isCleaning
         */
        public void setIsCleaning(boolean isCleaning) {
            this.mIsCleaning = isCleaning;
        }

        public void setDrawingRect(Rect drawingRect) {
            this.mDrawingRect = new Rect(drawingRect);
        }

        private void drawBackground(Canvas canvas) {
            mDrawPaint.setColor(mIsCleaning ? 0 : mAddEventBackgroundColor);
            canvas.drawRect(mDrawingRect, mDrawPaint);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

    private class AllDayDrawable extends EventDrawable {
        private String mAllDayLabel;
        private int mStartPosition = 0;

        public AllDayDrawable(String allDayLabel) {
            mAllDayLabel = allDayLabel;
        }

        public void setAllDayLabel(String allDayLabel) {
            mAllDayLabel = allDayLabel;
        }

        @Override
        public void draw(Canvas canvas) {
            initRect();
            drawBackground(canvas);
            drawContent(canvas);
        }

        public AllDayDrawable setStartPosition(int startPosition) {
            mStartPosition = startPosition;
            return this;
        }

        private void initRect() {
            mRect.top = mStartPosition + getScrollY();
            mRect.bottom = mRect.top + mItemHeight;
            mRect.right = getWidth();
            mRect.left = 0;
        }

        private void drawBackground(Canvas canvas) {
            mDrawPaint.setColor(mDividerColor);
            canvas.drawLine(0, mRect.top, mRect.right, mRect.top + mDividerHeight, mDrawPaint);
            mRect.top += mDividerHeight;
            mRect.bottom += mDividerHeight;

            mDrawPaint.setColor(mEventBackgroundColor);
            canvas.drawRect(mRect, mDrawPaint);
        }

        private void drawContent(Canvas canvas) {
            // Draw all day label
            mTextPaint.setTextSize(mTimeLabelTextColor);
            mTextPaint.setTextSize(mTimeLabelTextSize);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.getTextBounds(mAllDayLabel, 0, mAllDayLabel.length(), mTempRect);
            canvas.drawText(mAllDayLabel, mEventContentPadding + mTempRect.centerX(), mRect.top + (mItemHeight >> 1), mTextPaint);

            drawVerticalDivider(canvas, mTempRect.width());

            // Draw event content
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            // Adjust the rect.
            mRect.left = mRect.left + mEventVerticalDividerWidth;
            mRect.right = getWidth() - mHorizontalPadding;
            super.drawContent(canvas);
        }

        private void drawVerticalDivider(Canvas canvas, int allDayTextWidth) {
            mRect.left = allDayTextWidth + (mEventContentPadding << 1);
            mRect.right = mRect.left + mEventVerticalDividerWidth;
            mDrawPaint.setColor(mEventVerticalDividerColor);
            canvas.drawRect(mRect, mDrawPaint);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }
}
