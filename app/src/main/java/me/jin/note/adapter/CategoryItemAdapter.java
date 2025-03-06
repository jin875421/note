package me.jin.note.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import me.jin.note.R;
import me.jin.note.bean.Category;
import me.jin.note.listener.OnItemDrag;



public class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.ViewHolder>
        implements OnItemDrag{
    private Context mContext;
    private List<Category> list;

    public CategoryItemAdapter(Context context, List<Category> list){
        this.mContext = context;
        this.list = list;
    }
    public void updateList(List<Category>categoryList){
        this.list=categoryList;
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_category,parent,false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (position==0){
            holder.iv_cate.setImageDrawable(ContextCompat.getDrawable(mContext,R.mipmap.createtask_fill));
            holder.tv_cate.setText(list.get(0).getCategory());
        }else {
            holder.tv_cate.setText(list.get(position).getCategory());
            holder.move.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onMove(int fromPosition,int toPosition){
        if (fromPosition==0||toPosition==0){
            return;
        }
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(list, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }
    @Override
    public void onSwiped(int position){
        list.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_cate;
        TextView tv_cate;
        ImageView move;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_cate=(TextView)itemView.findViewById(R.id.tv_cate);
            iv_cate=(ImageView)itemView.findViewById(R.id.iv_cate);
            move=(ImageView)itemView.findViewById(R.id.move);
        }
    }
}
