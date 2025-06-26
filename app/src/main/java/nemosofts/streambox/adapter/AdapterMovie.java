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
import nemosofts.streambox.item.ItemMovies;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.helper.SPHelper;

public class AdapterMovie extends RecyclerView.Adapter<AdapterMovie.MyViewHolder> {

    private final List<ItemMovies> arrayList;
    private final RecyclerItemClickListener listener;
    private final int columnWidth;
    private final int columnHeight;
    private final Boolean isTitle;

    public AdapterMovie(Context context, List<ItemMovies> arrayList,
                        RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
        Boolean isTvBox = DeviceUtils.isTvBox(context);
        isTitle = new SPHelper(context).getUICardTitle();
        columnWidth = DeviceUtils.getColumnWidth(context, Boolean.TRUE.equals(isTvBox) ? 8 : 7, 0);
        columnHeight = (int) (columnWidth * 1.15);
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_movie, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemMovies item = arrayList.get(position);

        holder.poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.poster.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight));
        holder.vw.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight));

        holder.title.setVisibility(Boolean.TRUE.equals(isTitle) ? View.VISIBLE : View.GONE);
        holder.title.setText(item.getName());

        if (!item.getRating().isEmpty() && item.getRating().equals("0")){
            holder.llStar.setVisibility(View.GONE);
        } else {
            holder.rating.setText(item.getRating());
        }

        try {
            String imageUrl = item.getStreamIcon();
            if (imageUrl == null || imageUrl.isEmpty()) {
                holder.poster.setImageResource(R.drawable.placeholder_vertical);
            } else {
                Picasso.get()
                        .load(imageUrl)
                        .resize(columnWidth / 2, columnHeight / 2)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_vertical)
                        .error(R.drawable.placeholder_vertical)
                        .into(holder.poster);
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
        Picasso.get().cancelRequest(holder.poster);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final View vw;
        private final ImageHelperView poster;
        private final TextView rating;
        private final TextView title;
        private final LinearLayout llStar;

        public MyViewHolder(View view) {
            super(view);
            vw = view.findViewById(R.id.fd_movie_card);
            poster = view.findViewById(R.id.iv_movie);

            llStar = view.findViewById(R.id.ll_card_star);
            rating = view.findViewById(R.id.tv_movie_rating);
            title = view.findViewById(R.id.tv_movie_title);
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
        void onClickListener(ItemMovies itemMovies, int position);
    }
}