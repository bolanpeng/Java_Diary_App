package com.shelley.diary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.shelley.diary.backend.myApi.model.MyDiary;

import java.util.List;


public class DiaryListAdapter extends ArrayAdapter<MyDiary>{
    private Context mContext;
    private List<MyDiary> mDiaries;

    public DiaryListAdapter(Context context, List<MyDiary> objects) {
        super(context, R.layout.diary_row_item, objects);
        this.mContext = context;
        this.mDiaries = objects;
    }

    @Override
    public MyDiary getItem(int position) {
        return super.getItem(getCount() - position - 1);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
            convertView = mLayoutInflater.inflate(R.layout.diary_row_item, null);
        }

        //MyDiary myDiary = mDiaries.get(position);
        MyDiary myDiary = getItem(position);

        TextView titleView = (TextView) convertView.findViewById(R.id.diary_title);
        titleView.setText(myDiary.getTitle());

        TextView contentView = (TextView) convertView.findViewById(R.id.diary_content);
        contentView.setText(myDiary.getContent());

        return convertView;
    }
}
