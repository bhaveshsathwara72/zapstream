package nemosofts.streambox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import nemosofts.streambox.R;
import nemosofts.streambox.item.ItemChannel;
import nemosofts.streambox.utils.ApplicationUtil;

public class AdapterChannel extends RecyclerView.Adapter<AdapterChannel.MyViewHolder> {

    private final List<ItemChannel> arrayList;
    private final RecyclerItemClickListener listener;
    private final int columnWidth;

    public AdapterChannel(Context context, List<ItemChannel> arrayList,
                          RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
        Boolean isTvBox  = DeviceUtils.isTvBox(context);
        columnWidth = DeviceUtils.getColumnWidth(context, Boolean.TRUE.equals(isTvBox)? 8 : 7, 0);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_live, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemChannel item = arrayList.get(position);

        holder.logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.logo.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth));
        holder.vw.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth));

        if (item.getName().isEmpty()){
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setText(item.getName());
        }

        holder.linearLayout.setVisibility(View.GONE);

        try {
            String imageUrl = item.getStreamIcon();
            if (imageUrl == null || imageUrl.isEmpty()) {
                holder.logo.setImageResource(R.drawable.placeholder_live);
            } else {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_live)
                        .error(R.drawable.placeholder_live)
                        .into(holder.logo);
            }
        } catch (Exception e) {
            ApplicationUtil.log("Adapter", "Error Picasso load", e);
        }

        holder.vw.setOnClickListener(v -> listener.onClickListener(
                arrayList.get(holder.getAbsoluteAdapterPosition()),
                holder.getAbsoluteAdapterPosition())
        );
    }

    @Override
    public void onViewRecycled(@NonNull MyViewHolder holder) {
        super.onViewRecycled(holder);
        Picasso.get().cancelRequest(holder.logo);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final View vw;
        private final ImageHelperView logo;
        private final TextView title;
        private final LinearLayout linearLayout;

        public MyViewHolder(View view) {
            super(view);

            vw = view.findViewById(R.id.fd_movie_card);
            logo = view.findViewById(R.id.iv_movie);
            title = view.findViewById(R.id.tv_movie_title);
            linearLayout = view.findViewById(R.id.ll_card_star);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemChannel itemChannel, int position);
    }
}