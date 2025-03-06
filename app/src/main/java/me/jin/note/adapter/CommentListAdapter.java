package me.jin.note.adapter;


import static me.jin.note.activity.MainActivity.ip;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import me.jin.note.R;
import me.jin.note.bean.Comment;

public class CommentListAdapter extends BaseAdapter {
    private Context context;//上下文环境
    private int comment_layout_id;
    private List<Comment> commentList;
    private String url = "http://"+ip+"/Note/";

    public CommentListAdapter(Context context, int comment_layout_id, List<Comment> commentList){
        this.context = context;
        this.comment_layout_id = comment_layout_id;
        this.commentList = commentList;
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //获取comment对象
        View commentView = LayoutInflater.from(context).inflate(comment_layout_id, null);
        //获取控件对象
        ImageView avatar = commentView.findViewById(R.id.avatar);
        TextView username = commentView.findViewById(R.id.username);
        TextView time = commentView.findViewById(R.id.time);
        TextView text = commentView.findViewById(R.id.text);
        TextView first_respond = commentView.findViewById(R.id.first_respond);
        TextView respond_num = commentView.findViewById(R.id.respond_num);
        LinearLayout respond = commentView.findViewById(R.id.respond);
        Log.v("CommentListAdapter", "lzx adapter中的数组的长度: " + commentList.size());
        //获取当前要显示的对象
        Comment comment = commentList.get(position);
        Log.v("CommentListAdapter", "lzx adapter中要绑定的comment" + comment);
        //显示头像
        Glide.with(context)
                .load(url + comment.getAvatar())
                .placeholder(R.mipmap.loading)
                .fallback(R.mipmap.blank)
                .circleCrop()
                .into(avatar);
        username.setText(comment.getUsername());
        time.setText(comment.getUploadTime());
        text.setText(comment.getComment());
        if(comment.getReturnCommentResponds().size() != 0){
            respond.setVisibility(View.VISIBLE);
            first_respond.setText(comment.getReturnCommentResponds().get(0).getUserName()
                    + " 回复 "
                    + comment.getUsername()
                    + ":"
                    + comment.getReturnCommentResponds().get(0).getText());
            respond_num.setText(comment.getReturnCommentResponds().size() + "条回复");

        }else {
            //隐藏控件
            respond.setVisibility(View.GONE);
        }

        //绑定评论回复点击事件
        respond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnRespondClickListener.onRespondClick(position);
            }
        });

        return commentView;
    }

    public interface onRespondClickListener{
        void onRespondClick(int i);
    }

    private onRespondClickListener mOnRespondClickListener;

    public void setOnRespondClickListener(onRespondClickListener listener){
        this.mOnRespondClickListener = listener;
    }

}