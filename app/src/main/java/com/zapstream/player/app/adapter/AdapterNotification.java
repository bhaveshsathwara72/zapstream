package com.zapstream.player.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.zapstream.player.app.R;
import com.zapstream.player.app.item.ItemNotification;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.ViewHolder> {

    private final List<ItemNotification> arrayList;
    private final RecyclerItemClickListener listener;

    public AdapterNotification(List<ItemNotification> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_notification,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemNotification item = arrayList.get(position);

        holder.title.setText(item.getTitle());
        holder.titleSub.setText(item.getMsg());
        holder.notifyOn.setText(item.getDate());
        holder.relativeLayout.setOnClickListener(v -> listener.onClickListener(
                item,
                holder.getAbsoluteAdapterPosition())
        );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        TextView titleSub;
        TextView notifyOn;
        RelativeLayout relativeLayout;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_notify_title);
            titleSub = view.findViewById(R.id.tv_notify_sub_title);
            notifyOn = view.findViewById(R.id.tv_notify_on);
            relativeLayout = view.findViewById(R.id.rl_notify);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemNotification notification, int position);
    }
}