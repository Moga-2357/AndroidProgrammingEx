package com.example.androidproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<ScheduleItem> mScheduleList;

    public ScheduleAdapter(List<ScheduleItem> scheduleList) {
        this.mScheduleList = scheduleList;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        ScheduleItem scheduleItem = mScheduleList.get(position);
        holder.mTitle.setText(scheduleItem.getTitle());
        holder.mDate.setText(scheduleItem.getDate());
    }

    @Override
    public int getItemCount() {
        return mScheduleList.size();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        public TextView mTitle;
        public TextView mDate;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.tv_schedule_title);
            mDate = itemView.findViewById(R.id.tv_schedule_date);
        }
    }
}
