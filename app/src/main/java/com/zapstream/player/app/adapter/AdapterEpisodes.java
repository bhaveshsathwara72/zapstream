package com.zapstream.player.app.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.nemosofts.utils.FormatUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import com.zapstream.player.app.R;
import com.zapstream.player.app.item.ItemEpisodes;
import com.zapstream.player.app.utils.ApplicationUtil;
import com.zapstream.player.app.utils.helper.DBHelper;

public class AdapterEpisodes extends RecyclerView.Adapter<AdapterEpisodes.MyViewHolder> {

    private final List<ItemEpisodes> arrayList;
    private final RecyclerItemClickListener listener;
    private final String seriesCover;
    private final String plotData;
    private final DBHelper dbHelper;

    public AdapterEpisodes(Context ctx, List<ItemEpisodes> arrayList, String cover, String plot,
                           RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
        this.seriesCover = cover;
        this.plotData = plot;
        dbHelper = new DBHelper(ctx);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_episodes_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemEpisodes item = arrayList.get(position);

        try {
            String imageUrl = item.getCoverBig();

            // Helper method to load seriesCover or fallback image
            Runnable loadSeriesCoverOrFallback = () -> {
                if (seriesCover == null || seriesCover.isEmpty()) {
                    holder.poster.setImageResource(R.drawable.placeholder_horizontal);
                } else {
                    Picasso.get()
                            .load(seriesCover)
                            .resize(450, 300)
                            .centerCrop()
                            .placeholder(R.drawable.placeholder_horizontal)
                            .error(R.drawable.placeholder_horizontal)
                            .into(holder.poster);
                }
            };

            if (TextUtils.isEmpty(imageUrl)) {
                loadSeriesCoverOrFallback.run();
            } else {
                Picasso.get()
                        .load(imageUrl)
                        .resize(450, 300)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_horizontal)
                        .error(R.drawable.placeholder_horizontal)
                        .into(holder.poster, new Callback() {
                            @Override
                            public void onSuccess() {
                                // Optional: log success or leave empty
                            }

                            @Override
                            public void onError(Exception e) {
                                loadSeriesCoverOrFallback.run();
                            }
                        });
            }
        } catch (Exception e) {
            ApplicationUtil.log("Adapter", "Error loading image with Picasso", e);
        }

        holder.title.setText(item.getTitle());

        try {
            long seek = dbHelper.getSeekFull(
                    DBHelper.TABLE_SEEK_EPISODES,
                    item.getId(), item.getTitle()
            );
            if (seek > 0){
                holder.pb.setVisibility(View.VISIBLE);
                holder.pb.setProgress(Math.toIntExact(seek));
            } else {
                holder.pb.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.pb.setVisibility(View.GONE);
        }

        holder.rb.setRating(getRating(item));

        try {
            holder.duration.setText(FormatUtils.formatTimeDuration(item.getDuration()));
        } catch (Exception e) {
            holder.duration.setText("0");
        }

        holder.plot.setText(item.getPlot().isEmpty() ? plotData : item.getPlot());
        holder.relativeLayout.setOnClickListener(v -> listener.onClickListener(
                arrayList.get(holder.getAbsoluteAdapterPosition()),
                holder.getAbsoluteAdapterPosition())
        );
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView poster;
        private final TextView title;
        private final TextView duration;
        private final TextView plot;
        private final RatingBar rb;
        private final ProgressBar pb;
        private final RelativeLayout relativeLayout;

        private MyViewHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.iv_episodes);
            title = view.findViewById(R.id.tv_episodes);
            rb = view.findViewById(R.id.rb_episodes_list);
            duration = view.findViewById(R.id.tv_duration);
            plot = view.findViewById(R.id.tv_plot);
            relativeLayout = view.findViewById(R.id.rl_episodes);
            pb = view.findViewById(R.id.pr_episodes);
        }
    }

    private float getRating(ItemEpisodes item) {
        try {
            // Adding null check before parsing the rating
            String ratingString = item.getRating().isEmpty() ? "0" : item.getRating();
            double newRating = 0.0; // default value
            if (ratingString != null && !ratingString.isEmpty()) {
                newRating = convertToFiveRating(Double.parseDouble(ratingString));
            }
            return (float) newRating;
        } catch (Exception e) {
            return 0;
        }
    }

    public static double convertToFiveRating(double oldRating) {
        return (oldRating - 1) * 4 / 9 + 1;
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
        void onClickListener(ItemEpisodes itemEpisodes, int position);
    }
}