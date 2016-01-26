package com.cc.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by ChengCn on 12/28/2015.
 */
public class Adapter extends BaseAdapter {
    private List<Item> mData;
    private Context mContext;
    public Adapter(Context context, List<Item> data) {
        mContext = context;
        mData = data;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void itemRefreshed(Item refreshItem) {
        for (Item item : mData) {
            if (item.getID().equals(refreshItem.getID())) {
                mData.remove(item);
                mData.add(refreshItem);
                Collections.sort(mData, new Comparator<Item>() {
                    @Override
                    public int compare(Item lhs, Item rhs) {
                        return lhs.getID().compareTo(rhs.getID());
                    }
                });
                notifyDataSetChanged();
                return;
            }
        }

        mData.add(refreshItem);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_item, parent, false);
            vh =new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        Item item = (Item) getItem(position);
        if (item == null) {
            return convertView;
        }
        bindView(item, vh);
        return convertView;
    }

    private void bindView(Item item, ViewHolder vh) {
        vh._dividerView.setBackgroundColor(mContext.getResources().getColor(item.isAllDay() ? android.R.color.holo_red_light : android.R.color.holo_blue_bright));
        vh._timeView.setText(item.isAllDay() ? "All-Day" : (formatDate(item.getStartTime(), "kk:mm") + "-" + formatDate(item.getEndTime(), "kk:mm")));
        vh._commentView.setText(item.getComment());
        vh._creatorView.setText(item.getCreator());
        vh._titleView.setText(item.getTitle());
    }

    private String formatDate(long date, String formatter) {
        DateFormat format = new SimpleDateFormat(formatter);
        return format.format(new Date(date));
    }

    private static class ViewHolder {
        TextView _titleView;
        TextView _creatorView;
        TextView _commentView;
        TextView _timeView;
        View _dividerView;

        public ViewHolder(View v) {
            _titleView = (TextView) v.findViewById(R.id.item_title);
            _creatorView = (TextView) v.findViewById(R.id.item_creator);
            _commentView = (TextView) v.findViewById(R.id.item_comment);
            _timeView = (TextView) v.findViewById(R.id.item_time);
            _dividerView = v.findViewById(R.id.item_divider);
        }
    }
}
