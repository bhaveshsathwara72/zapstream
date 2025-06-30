package com.zapstream.player.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.utils.EncrypterUtils;
import androidx.nemosofts.utils.FormatUtils;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import com.zapstream.player.app.R;
import com.zapstream.player.app.item.ItemEpg;

public class AdapterEpgListings extends RecyclerView.Adapter<AdapterEpgListings.ViewHolder> {

    private final List<ItemEpg> arrayList;
    private final Boolean is12h;

    public AdapterEpgListings(Boolean is12h, List<ItemEpg> arrayList) {
        this.arrayList = arrayList;
        this.is12h = is12h;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_epg_listings,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemEpg item = arrayList.get(position);
        holder.tvEpg.setText(EncrypterUtils.decodeBase64(item.getTitle()));
        String timeStamp = FormatUtils.getTimestamp(item.getStartTimestamp(), is12h)
                + " - " + FormatUtils.getTimestamp(item.getStopTimestamp(), is12h);
        holder.tvEpgTime.setText(timeStamp);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvEpg;
        TextView tvEpgTime;

        public ViewHolder(View view) {
            super(view);

            tvEpg = view.findViewById(R.id.iv_epg_test);
            tvEpgTime = view.findViewById(R.id.iv_epg_start_time);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

}