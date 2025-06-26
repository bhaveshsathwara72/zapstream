package nemosofts.streambox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.utils.DeviceUtils;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nemosofts.streambox.R;
import nemosofts.streambox.dialog.DialogUtil;
import nemosofts.streambox.item.ItemUsersDB;
import nemosofts.streambox.utils.ApplicationUtil;
import nemosofts.streambox.utils.helper.DBHelper;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.ViewHolder> {

    private final Context ctx;
    private final List<ItemUsersDB> arrayList;
    private final RecyclerItemClickListener listener;
    private final DBHelper dbHelper;
    private final int columnWidth;

    public AdapterUsers(Context ctx, List<ItemUsersDB> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
        this.ctx = ctx;
        dbHelper = new DBHelper(ctx);
        columnWidth = DeviceUtils.getColumnWidth(ctx, 6, 0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_users_list,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemUsersDB item = arrayList.get(position);

        String loginType = switch (item.getUserType()) {
            case "xui" -> "Xtream Codes / Xui";
            case "stream" -> "1-stream";
            case "playlist" -> "M3U Playlist";
            default -> "";
        };
        holder.type.setText(loginType);

        holder.colorBg.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth));

        int[] colors = {
                R.drawable.gradient_1, // step 1
                R.drawable.gradient_2, // step 2
                R.drawable.gradient_3, // step 3
                R.drawable.gradient_4, // step 4
                R.drawable.gradient_5, // step 5
                R.drawable.gradient_6, // step 6
                R.drawable.gradient_7  // step 7
        };
        int step = (position % 7); // 0-based index
        holder.colorBg.setImageResource(colors[step]);

        if (!item.getAnyName().isEmpty()){
            holder.anyNameLogo.setText(String.valueOf(item.getAnyName().toUpperCase().charAt(0)));
        } else {
            holder.anyNameLogo.setText("");
        }
        holder.anyName.setText(item.getAnyName());

        holder.relativeLayout.setOnClickListener(v -> listener.onClickListener(
                holder.getAbsoluteAdapterPosition())
        );
        holder.relativeLayout.setOnLongClickListener(v -> {
            delete(holder.getAbsoluteAdapterPosition());
            return false;
        });

        holder.relativeLayoutBg.setOnClickListener(v -> listener.onClickListener(
                holder.getAbsoluteAdapterPosition())
        );
        holder.relativeLayoutBg.setOnLongClickListener(v -> {
            delete(holder.getAbsoluteAdapterPosition());
            return false;
        });
    }

    private void delete(int position) {
        DialogUtil.deleteDialog(ctx, () -> {
            try {
                dbHelper.removeFromUser(arrayList.get(position).getId());
                arrayList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(ctx, ctx.getString(R.string.delete), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                ApplicationUtil.log("AdapterUsers", "Error deleteDialog",e);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView anyName;
        private final TextView anyNameLogo;
        private final TextView type;
        private final ImageHelperView colorBg;
        private final RelativeLayout relativeLayout;
        private final RelativeLayout relativeLayoutBg;

        public ViewHolder(View itemView) {
            super(itemView);
            anyName = itemView.findViewById(R.id.tv_users_list);
            anyNameLogo = itemView.findViewById(R.id.tv_users_logo);
            type = itemView.findViewById(R.id.tv_users_list_sub);
            colorBg = itemView.findViewById(R.id.iv_color_bg);

            relativeLayoutBg = itemView.findViewById(R.id.rl_users_bg);
            relativeLayout = itemView.findViewById(R.id.rl_users_list);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(int position);
    }
}