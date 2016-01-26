package com.cc.eventcalendar.calendarview.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

/**
 * Created by ChengCn on 12/8/2015.
 */
public abstract class RecyclePagerAdapter extends PagerAdapter {
    private LinkedList<View> recycledViews = new LinkedList<>();

    public RecyclePagerAdapter() {
        recycledViews = new LinkedList<>();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView;
        boolean isReused = false;
        if (recycledViews != null && recycledViews.size() > 0) {
            itemView = recycledViews.getFirst();
            recycledViews.removeFirst();
            isReused = true;
        } else {
            itemView = getItemView(position);
        }
        container.addView(itemView);
        bindItemView(itemView, position, isReused);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        if (object != null) {
            recycledViews.addLast((View) object);
        }
    }

    /**
     * Clear the cached views.
     */
    protected void clearCache() {
        recycledViews.clear();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * Get item view instance.
     *
     * @param position the position of the current view
     */
    protected abstract View getItemView(int position);

    /**
     * Bind the item view's data.
     *
     * @param itemView the item view.
     * @param position the position of the current item view.
     * @param isReused indicate whether the item view is reused
     */
    protected abstract void bindItemView(View itemView, int position, boolean isReused);
}
