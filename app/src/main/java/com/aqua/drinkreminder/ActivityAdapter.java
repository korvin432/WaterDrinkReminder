package com.aqua.drinkreminder;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>  {
    private LayoutInflater inflater;
    private ArrayList<ActivityModel> imageModelArrayList;
    private Context context;

    public ActivityAdapter(Context ctx, ArrayList<ActivityModel> imageModelArrayList){

        this.context = ctx;
        inflater = LayoutInflater.from(ctx);
        this.imageModelArrayList = imageModelArrayList;
    }

    @Override
    public ActivityAdapter.ActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_recycler_item, parent, false);
        ActivityViewHolder holder = new ActivityViewHolder(view);

        return holder;
    }


    @Override
    public void onBindViewHolder(ActivityAdapter.ActivityViewHolder holder, int position) {

        holder.iv.setBackground(ContextCompat.getDrawable(context, imageModelArrayList.get(position).getImageDrawable()));
        holder.activity.setText(imageModelArrayList.get(position).getActivity());
        holder.description.setText(imageModelArrayList.get(position).getDescription());
    }


    @Override
    public int getItemCount() {
        return imageModelArrayList.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        TextView activity;
        TextView description;
        RelativeLayout iv;

        public ActivityViewHolder(View itemView) {
            super(itemView);

            activity = (TextView) itemView.findViewById(R.id.activity);
            description = (TextView) itemView.findViewById(R.id.description);
            iv = (RelativeLayout) itemView.findViewById(R.id.card_background);
            itemView.setOnTouchListener(this);
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    }
}
