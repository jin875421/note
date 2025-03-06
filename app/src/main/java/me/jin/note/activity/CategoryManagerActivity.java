package me.jin.note.activity;

import android.content.Intent;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.jin.note.R;
import me.jin.note.adapter.CategoryItemAdapter;
import me.jin.note.base.BaseActivity;
import me.jin.note.bean.Category;
import me.jin.note.db.NoteManager;
import me.jin.note.listener.MyRvItemTouchCallback;
import me.jin.note.listener.OnRvItemTouchListener;

public class CategoryManagerActivity extends BaseActivity {
    private Toolbar mToolbar;
    private TextView mTitle;
    private RecyclerView recyclerView;
    private CategoryItemAdapter adapter;
    private List<Category>categoryList=new ArrayList<>();
    private NoteManager noteManager;

    @Override
    protected int setLayoutResId() {
        return R.layout.activity_category_manager;
    }

    @Override
    protected void initData() {
        noteManager=new NoteManager(mContext);
        categoryList=noteManager.selectAllCategoryBean();
        Collections.sort(categoryList);
        adapter=new CategoryItemAdapter(mContext,categoryList);
    }

    @Override
    protected void onResume() {
        categoryList=noteManager.selectAllCategoryBean();
        Collections.sort(categoryList);
        adapter.updateList(categoryList);
        super.onResume();
    }

    @Override
    protected void initView() {
        mToolbar=(Toolbar)findViewById(R.id.toolbar_include);
        mTitle=(TextView)findViewById(R.id.include_tv_center_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("");
        mTitle.setText("管理笔记文件夹");
        recyclerView=(RecyclerView)findViewById(R.id.rv_item_move);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        Snackbar snackbar=Snackbar.make(recyclerView,"长按并拖动手指可调整顺序",Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
        snackbar.show();
    }

    @Override
    protected void initListener() {
        MyRvItemTouchCallback myCallback=new MyRvItemTouchCallback(adapter);
        myCallback.setDragcolor(ContextCompat.getColor(mContext,R.color.colorPrimary));//colorPrimary
        final ItemTouchHelper helper=new ItemTouchHelper(myCallback);
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addOnItemTouchListener(new OnRvItemTouchListener(recyclerView){
            @Override
            public void onLongClick(RecyclerView.ViewHolder vh) {//文件夹拖拽模式
                if (vh.getLayoutPosition()!=0) {//categoryList.size()-1
                    helper.startDrag(vh);
                }
            }
            //holder.getAdapterPosition,getLayoutPosition
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {//文件夹编辑模式
                if (vh.getAdapterPosition()==0)return;
                Intent intent=new Intent(mContext,CategoryEditActivity.class);
                Category category=categoryList.get(vh.getAdapterPosition());
                intent.putExtra("cateId",category.getId());
                intent.putExtra("pos",category.getPos());
                intent.putExtra("category",category.getCategory());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager,menu);
        return true;
    }
    private void saveCatePos(){
        List<Category>newList=new ArrayList<>(categoryList.size());
        for (int i=0;i<categoryList.size();i++){
            Category category=new Category();
            category.setId(categoryList.get(i).getId());//id,必须
            category.setPos(String.valueOf(i));//pos
            newList.add(category);
        }
        for (Category category:newList){
            noteManager.updateCategory(category);//更新文件夹pos
        }
    }
    private void goBack(){
        Intent intent=new Intent(mContext,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goBack();
                break;
            case R.id.category_manager:
                Snackbar snackbar=Snackbar.make(recyclerView,"长按并拖动手指可调整顺序",Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                snackbar.show();
                break;
            case R.id.category_add:
                Intent intent=new Intent(mContext,CategoryAddActivity.class);
                intent.putExtra("cateSize",categoryList.size());
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCatePos();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
