package com.zapstream.player.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.zapstream.player.app.R;
import com.zapstream.player.app.item.ItemSelect;
import com.zapstream.player.app.utils.ApplicationUtil;

public class AdapterSelect extends RecyclerView.Adapter<AdapterSelect.ViewHolder> {

    private final List<ItemSelect> arrayList;
    private final RecyclerItemClickListener listener;

    public AdapterSelect(List<ItemSelect> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_login_list,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemSelect item = arrayList.get(position);

        holder.title.setText(item.getTitle());
        holder.more.setVisibility(Boolean.TRUE.equals(item.getIsMore()) ? View.VISIBLE : View.GONE);

        try {
            holder.logo.setImageResource(item.getLogoResId());
        } catch (Exception e) {
            ApplicationUtil.log("AdapterSelect","Error setImageResource" ,e);
        }

        holder.linearLayout.setOnClickListener(v -> listener.onClickListener(
                arrayList.get(holder.getAbsoluteAdapterPosition()),
                holder.getAbsoluteAdapterPosition())
        );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView title;
        private final ImageView logo;
        private final ImageView more;
        private final LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.iv_list_title);
            logo = itemView.findViewById(R.id.iv_list_logo);
            more = itemView.findViewById(R.id.iv_more);
            linearLayout = itemView.findViewById(R.id.rl_login_list);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemSelect item, int position);
    }
}