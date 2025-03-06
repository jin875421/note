package me.jin.note.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import me.jin.note.R;
import me.jin.note.base.BaseActivity;
import me.jin.note.bean.Category;
import me.jin.note.bean.Note;
import me.jin.note.db.NoteManager;
import me.jin.note.utils.Logger;
import me.jin.note.utils.SPUtil;

public class CategoryEditActivity extends BaseActivity {
    private EditText et_cate_edit;
    private ImageView deleteImg;
    private Toolbar toolbar;
    private TextView mTitle;
    private String cate;
    //private String catePos;
    private NoteManager noteManager;
    private String cateId;
    private List<Note>mNoteList=new ArrayList<>();

    @Override
    protected int setLayoutResId() {
        return R.layout.activity_category_add;
    }

    @Override
    protected void initData() {
        Intent intent=getIntent();
        cate=intent.getStringExtra("category");
        cateId=intent.getStringExtra("cateId");
        //catePos=intent.getStringExtra("pos");
        noteManager=new NoteManager(mContext);
    }

    @Override
    protected void initView() {
        et_cate_edit=(EditText)findViewById(R.id.et_cate_name);
        et_cate_edit.setText(cate);
        deleteImg=(ImageView)findViewById(R.id.delete);
        toolbar=(Toolbar)findViewById(R.id.toolbar_include);
        mTitle=(TextView)findViewById(R.id.include_tv_center_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("");
        mTitle.setText("编辑文件夹");
        TextWatcher watcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(et_cate_edit.getText().toString())){
                    deleteImg.setVisibility(View.VISIBLE);
                }else {
                    deleteImg.setVisibility(View.INVISIBLE);
                    et_cate_edit.setHint("笔记文件夹名称");
                }
            }
        };
        et_cate_edit.addTextChangedListener(watcher);
    }

    @Override
    protected void initListener() {
        deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(et_cate_edit.getText().toString())){
                    et_cate_edit.setText("");
                    et_cate_edit.setHint("笔记文件夹名称");
                }
            }
        });
    }
    private void deleteCategory(){
        //笔记不为空删除笔记,为空仅删除文件夹
        if (noteManager.selectCategory(cate)!=null){
            mNoteList=noteManager.selectCategory(cate);
            for (Note note:mNoteList){
                noteManager.delete(note.getId());//删除笔记
            }
        }
        noteManager.deleteCategory(cateId);//删除文件夹
        SPUtil.putString(mContext,"currentCate","所有");
        goToCateManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_edit,menu);
        return true;
    }
    private void goBack(){
        String s=et_cate_edit.getText().toString();
        Logger.i(s);
        if (!TextUtils.isEmpty(s)){
            Category category=new Category();
            category.setId(cateId);//noteManager需要id
            category.setCategory(s);
            //category.setPos(catePos);
            noteManager.updateCategory(category);//更新文件夹
            goToCateManager();
        }else {
            showDeleteDialog();//文件夹名称为为空
        }
    }
    private void showDeleteCateAndNoteDialog(){
        AlertDialog dialog=new AlertDialog.Builder(mContext).create();
        dialog.setMessage("你确定要删除该文件夹及其笔记吗？");
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(et_cate_edit.getText().toString())){
                    et_cate_edit.setHint("笔记文件夹名称");
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCategory();
                goToCateManager();
            }
        });
        dialog.show();
    }
    private void showDeleteDialog(){
        AlertDialog dialog=new AlertDialog.Builder(mContext).create();
        dialog.setMessage("文件夹名称不能为空，你确定要删除该文件夹吗？");
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                et_cate_edit.setHint("笔记文件夹名称");
            }
        });
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noteManager.deleteCategory(cateId);//删除文件夹
                goToCateManager();
            }
        });
        dialog.show();
    }
    private void goToCateManager(){
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goBack();//判断editView
                return true;
            case R.id.delete_cate:
                showDeleteCateAndNoteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
