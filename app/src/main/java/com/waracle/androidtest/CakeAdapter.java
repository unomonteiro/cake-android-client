package com.waracle.androidtest;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CakeAdapter extends RecyclerView.Adapter<CakeAdapter.CakeViewHolder> {

    // Can you think of a better way to represent these items???
    private ArrayList<Cake> mItems;

    @NonNull
    @Override
    public CakeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_layout, parent, false);
        return new CakeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CakeViewHolder holder, int position) {
        Cake cake = mItems.get(position);
        holder.image.setTag(cake.getImage());
        holder.image.setImageResource(R.drawable.ic_cake);
        if (!TextUtils.isEmpty(cake.getImage())) {
            ImageLoader.getInstance().load(cake.getImage(), holder.image);
        }
        holder.title.setText(cake.getTitle());
        holder.desc.setText(cake.getDesc());

    }

    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        } else {
            return mItems.size();
        }
    }

    void setItems(ArrayList<Cake> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    class CakeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView desc;
        ImageView image;

        CakeViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            image = itemView.findViewById(R.id.image);
        }
    }
}
