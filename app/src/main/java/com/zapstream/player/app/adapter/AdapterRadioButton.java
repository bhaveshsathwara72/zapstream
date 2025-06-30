package com.zapstream.player.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.zapstream.player.app.R;
import com.zapstream.player.app.item.ItemRadioButton;

public class AdapterRadioButton extends RecyclerView.Adapter<AdapterRadioButton.ViewHolder> {

    private final List<ItemRadioButton> arrayList;
    private int rowIndexID;
    private final Boolean isTvBox;

    public AdapterRadioButton(Context context, List<ItemRadioButton> arrayList, int position) {
        this.arrayList = arrayList;
        this.rowIndexID = position;
        isTvBox  = DeviceUtils.isTvBox(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_radio_button,parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemRadioButton item = arrayList.get(position);

        holder.rd.setText(item.getName());

        holder.rd.setChecked(rowIndexID == item.getId());
        if (Boolean.TRUE.equals(isTvBox) && rowIndexID == item.getId()){
            holder.rd.requestFocus();
        }

        holder.rd.setOnClickListener(v -> {
            rowIndexID = arrayList.get(holder.getAbsoluteAdapterPosition()).getId();
            notifyDataSetChanged();
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final RadioButton rd;

        public ViewHolder(View itemView) {
            super(itemView);
            rd = itemView.findViewById(R.id.rd_item);
        }
    }

    public int getData() {
       return rowIndexID;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}