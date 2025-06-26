package nemosofts.streambox.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import nemosofts.streambox.R;
import nemosofts.streambox.dialog.DialogUtil;
import nemosofts.streambox.item.ItemVideoDownload;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.helper.DBHelper;

public class AdapterDownloadVideos extends RecyclerView.Adapter<AdapterDownloadVideos.MyViewHolder> {

    private final DBHelper dbHelper;
    private final Context context;
    private final List<ItemVideoDownload> arrayList;
    private final RecyclerItemClickListener listener;
    private final int columnWidth;
    private final int columnHeight;
    private final Boolean isTvBox;

    public AdapterDownloadVideos(Context context, List<ItemVideoDownload> arrayList,
                                 RecyclerItemClickListener listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.listener = listener;
        dbHelper = new DBHelper(context);
        columnWidth = DeviceUtils.getColumnWidth(context,6, 0);
        columnHeight = (int) (columnWidth * 1.15);
        isTvBox = DeviceUtils.isTvBox(context);
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
        ItemVideoDownload item = arrayList.get(position);

        holder.title.setText(item.getName());
        holder.rating.setVisibility(View.GONE);

        holder.poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.poster.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight));
        holder.vw.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight));

        try {
            String imageUrl = item.getStreamIcon();
            if (imageUrl == null || imageUrl.isEmpty()) {
                holder.poster.setImageResource(R.drawable.bg_card_item_load);
            } else {
                Picasso.get()
                        .load(imageUrl)
                        .resize(Boolean.TRUE.equals(isTvBox) ? columnWidth : 250, Boolean.TRUE.equals(isTvBox) ? columnHeight : 350)
                        .centerCrop()
                        .placeholder(R.color.bg_color_load)
                        .error(R.color.bg_color_load)
                        .into(holder.poster);
            }
        } catch (Exception e) {
            ApplicationUtil.log("Adapter", "Error Picasso load", e);
        }

        holder.vw.setOnLongClickListener(v -> {
            DialogUtil.deleteDialog(context, () -> {
                try {
                    dbHelper.removeFromDownload(
                            DBHelper.TABLE_DOWNLOAD_MOVIES,
                            arrayList.get(holder.getAbsoluteAdapterPosition()).getStreamID()
                    );
                    deleteFiles(item.getVideoURL());
                    deleteFiles(item.getStreamIcon());
                    arrayList.remove(holder.getAbsoluteAdapterPosition());
                    notifyItemRemoved(holder.getAbsoluteAdapterPosition());
                    listener.onDelete();
                } catch (Exception e) {
                    ApplicationUtil.log("Adapter","Error longClickListener" ,e);
                }
            });
            return false;
        });
        holder.vw.setOnClickListener(v -> listener.onClickListener(
                arrayList.get(holder.getAbsoluteAdapterPosition()),
                holder.getAbsoluteAdapterPosition())
        );
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final View vw;
        private final ImageHelperView poster;
        private final TextView rating;
        private final TextView title;

        public MyViewHolder(View view) {
            super(view);
            vw = view.findViewById(R.id.fd_movie_card);
            poster = view.findViewById(R.id.iv_movie);
            rating = view.findViewById(R.id.tv_movie_rating);
            title = view.findViewById(R.id.tv_movie_title);
        }
    }

    @SuppressWarnings("UseSpecificDelete")
    private void deleteFiles(String path) {
        if (path == null){
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Path filePath = Paths.get(path);
                Files.delete(filePath);
                Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
            } else {
                File file = new File(path);
                if (file.exists() && file.delete()) {
                    Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            ApplicationUtil.log("AdapterDownload", "Error deleting file", e);
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
        void onClickListener(ItemVideoDownload itemVideo, int position);
        void onDelete();
    }
}