package me.jin.note.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.richeditor.RichEditor;
import me.jin.note.R;
import me.jin.note.base.BaseRecyclerViewAdapter;
import me.jin.note.bean.Note;


public class NoteAdapter extends BaseRecyclerViewAdapter<Note> {
    private Context mContext;
    public NoteAdapter(Context context){
        this.mContext=context;
   }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_note,parent,false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((NoteHolder)holder).bindView(mDataList.get(position));
        if (mDataList.get(position).isFlag()){
            ((NoteHolder)holder).root.setSelected(true);
            ((NoteHolder)holder).trashImg.setVisibility(View.VISIBLE);
        }else {
            ((NoteHolder)holder).root.setSelected(false);
            ((NoteHolder)holder).trashImg.setVisibility(View.GONE);
        }
    }

    class NoteHolder extends BaseRvViewHolder{
        WebView content;
        TextView title;
        TextView noteFirstTime;
        CircleImageView trashImg;
        RelativeLayout root;
        NoteHolder(View itemView){
            super(itemView);
            content=itemView.findViewById(R.id.note_content);
            title=(TextView)itemView.findViewById(R.id.note_title);
            noteFirstTime=(TextView)itemView.findViewById(R.id.note_date_first);
            root=(RelativeLayout) itemView.findViewById(R.id.note_root);
            trashImg=(CircleImageView)itemView.findViewById(R.id.icon_trash);
        }

        @Override
        protected void bindView(Note note) {
            title.setText(note.getTitle());
            // 设置缩放比例
            WebSettings webSettings = content.getSettings();
            webSettings.setSupportZoom(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            content.setInitialScale(70);
            content.loadData( note.getContent(), "text/html; charset=UTF-8", null);
            noteFirstTime.setText(note.getFirstTime());
        }
    }
}
