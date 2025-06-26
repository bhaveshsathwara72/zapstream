package nemosofts.streambox.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nemosofts.streambox.R;
import nemosofts.streambox.item.ItemCat;
import nemosofts.streambox.utils.ApplicationUtil;

public class AdapterCategoriesFilter extends RecyclerView.Adapter<AdapterCategoriesFilter.ViewHolder> {

    private final List<ItemCat> arrayList;
    private final RecyclerItemClickListener listener;
    private final Set<Integer> selectedItems = new HashSet<>();

    public AdapterCategoriesFilter(List<ItemCat> arrayList, String data,
                                   RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;

        if (!TextUtils.isEmpty(data)) {
            for (String num : data.split(",")) {
                try {
                    if (!num.trim().isEmpty()) {
                        selectedItems.add(Integer.parseInt(num.trim()));
                    }
                } catch (NumberFormatException e) {
                    ApplicationUtil.log("AdapterCategoriesFilter", "Error parsing filter ID: " + num);
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_cat_filter,parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemCat item = arrayList.get(position);
        holder.title.setText(item.getName());

        int itemId;
        try {
            itemId = Integer.parseInt(item.getId().trim());
        } catch (NumberFormatException e) {
            itemId = -1;
        }

        boolean checked = selectedItems.contains(itemId);
        item.setCheckbox(checked);
        holder.checkbox.setImageResource(checked
                ? R.drawable.ic_checkbox
                : R.drawable.ic_checkbox_blank
        );

        holder.relativeLayout.setOnClickListener(v -> {
            item.setCheckbox(!item.isChecked());
            holder.checkbox.setImageResource(Boolean.TRUE.equals(item.isChecked())
                    ? R.drawable.ic_checkbox
                    : R.drawable.ic_checkbox_blank
            );
            listener.onClickListener(item.isChecked(), item.getId());
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView title;
        private final ImageView checkbox;
        private final RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_checkbox);
            checkbox = itemView.findViewById(R.id.iv_checkbox);
            relativeLayout = itemView.findViewById(R.id.rl_checkbox);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(boolean isChecked, String catID);
    }
}