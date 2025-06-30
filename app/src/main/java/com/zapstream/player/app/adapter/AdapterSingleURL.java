package com.zapstream.player.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ImageHelperView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.zapstream.player.app.R;
import com.zapstream.player.app.dialog.DialogUtil;
import com.zapstream.player.app.item.ItemSingleURL;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.helper.DBHelper;

public class AdapterSingleURL extends RecyclerView.Adapter<AdapterSingleURL.ViewHolder> {

    private final Context ctx;
    private final List<ItemSingleURL> arrayList;
    private final RecyclerItemClickListener listener;
    private final DBHelper dbHelper;

    public AdapterSingleURL(Context ctx,
                            List<ItemSingleURL> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
        this.ctx = ctx;
        dbHelper = new DBHelper(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_url_list,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemSingleURL item = arrayList.get(position);

        String anyName =   item.getAnyName();
        String usersUrl =  ctx.getString(R.string.user_list_url)+" " + item.getSingleURL();
        holder.anyName.setText(anyName);
        holder.videoUrl.setText(usersUrl);
        holder.linearLayout.setOnClickListener(v -> listener.onClickListener(arrayList.get(holder.getAbsoluteAdapterPosition()), position));

        int[] colors = {
                R.color.color_setting_1, // step 1
                R.color.color_setting_2, // step 2
                R.color.color_setting_3, // step 3
                R.color.color_setting_4, // step 4
                R.color.color_setting_5, // step 5
                R.color.color_setting_6, // step 6
                R.color.color_setting_7  // step 7
        };
        int step = (position % 7); // 0-based index
        holder.colorBg.setImageResource(colors[step]);

        holder.linearLayout.setOnLongClickListener(v -> {
            DialogUtil.deleteDialog(ctx, () -> {
                try {
                    dbHelper.removeFromSingleURL(arrayList.get(holder.getAbsoluteAdapterPosition()).getId());
                    arrayList.remove(holder.getAbsoluteAdapterPosition());
                    notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                    Toast.makeText(ctx, ctx.getString(R.string.delete), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    ApplicationUtil.log("AdapterSingleURL", "Error LongClickListener",e);
                }
            });
            return false;
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView anyName;
        private final TextView videoUrl;
        private final ImageHelperView colorBg;
        private final LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            anyName = itemView.findViewById(R.id.tv_any_name);
            videoUrl = itemView.findViewById(R.id.tv_video_url);
            colorBg = itemView.findViewById(R.id.iv_color_bg);

            linearLayout = itemView.findViewById(R.id.ll_single_list);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemSingleURL itemSingleURL, int position);
    }
}