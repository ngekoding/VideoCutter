package com.work.braincraftdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class FrameViewAdapter extends RecyclerView.Adapter<FrameViewAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater = null;
    private static final int EMPTY_VIEW = -1;
    private int lastPosition = -1;
    protected OnRecyclerViewItemActionListener listener = null;
    ArrayList<Bitmap> thumbnailList;

    public interface OnRecyclerViewItemActionListener<TransactionHistory> {
        void onRecyclerViewActionClick(int position);
    }

    public FrameViewAdapter(Context con, OnRecyclerViewItemActionListener listener) {

        this.mContext = con;
        inflater = LayoutInflater.from(mContext);
        this.listener = listener;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(mContext);

        if (viewType == EMPTY_VIEW) {
            TextView textView = new TextView(mContext);
            textView.setText("");
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.parseColor("#374875"));
            double position = (parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom()) / 1.5;
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            EmptyViewHolder evh = new EmptyViewHolder(textView);
            return evh;
        }

        View v = inflater.inflate(R.layout.item_home_card, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            final ItemViewHolder vh = (ItemViewHolder) holder;

            if (thumbnailList != null && thumbnailList.get(position) != null) {
                Glide.with(mContext).load(thumbnailList.get(position)).into(vh.imageView);
            }

        }

    }


    @Override
    public int getItemViewType(int position) {
        if (thumbnailList == null || thumbnailList.isEmpty()) {
            return EMPTY_VIEW;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {

        if (thumbnailList != null && !thumbnailList.isEmpty()) {
            return thumbnailList.size();
        }
        return EMPTY_VIEW;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class EmptyViewHolder extends ViewHolder {
        public EmptyViewHolder(View v) {
            super(v);
        }
    }

    public void setData(ArrayList<Bitmap> thumbnailList) {
        this.thumbnailList = thumbnailList;
        notifyDataSetChanged();
    }

    public class ItemViewHolder extends ViewHolder {
        ImageView imageView;


        public ItemViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.imageView);


        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
