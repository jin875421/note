package me.jin.note.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import me.jin.note.R;
import me.jin.note.adapter.NoteAdapter;
import me.jin.note.bean.Category;
import me.jin.note.bean.Note;
import me.jin.note.contract.GetNoteContract;
import me.jin.note.presenter.GetNotePresenterImpl;
import me.jin.note.utils.SPUtil;
import me.jin.note.listener.OnRecyclerViewItemClickListener;

public class SearchActivity extends AppCompatActivity implements GetNoteContract.View {

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private List<Note> mNoteList = new ArrayList<>();
    private List<Note> searchResultList = new ArrayList<>();
    private NoteAdapter mAdapter;
    private GetNotePresenterImpl mPresenter;
    private String currentCate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEditText = findViewById(R.id.search_edit_text);
        searchResultsRecyclerView = findViewById(R.id.rv_note);
        currentCate = SPUtil.getString(SearchActivity.this, "currentCate", "所有");

        mAdapter = new NoteAdapter(SearchActivity.this);
        mPresenter = new GetNotePresenterImpl(this);

        // 设置 RecyclerView 的布局管理器
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        searchResultsRecyclerView.setLayoutManager(layoutManager);

        // 获取笔记数据
        mPresenter.getNote(SearchActivity.this, currentCate);


        // 添加监听器进行实时搜索
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 实时筛选笔记内容
                if (!TextUtils.isEmpty(searchEditText.getText().toString())) {
                    String searchText = searchEditText.getText().toString();
                    searchResultList.clear(); // 清空搜索结果

                    for (Note note : mNoteList) {
                        if (note.getContent().contains(searchText)) {
                            searchResultList.add(note); // 添加符合条件的笔记
                        }
                    }

                    // 更新适配器数据
                    mAdapter.updateData(searchResultList);
                } else {
                    // 如果没有输入搜索内容，显示所有笔记
                    mAdapter.updateData(mNoteList);
                }
            }
        });

        // 设置点击事件
        mAdapter.setOnRecyclerViewItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                // 点击项时跳转到笔记编辑页面
                SPUtil.putString(SearchActivity.this, "category", currentCate);
                Intent intent = new Intent(SearchActivity.this, NoteEditActivity.class);
                intent.putExtra("currentCate", currentCate);
                intent.putExtra("id", mNoteList.get(pos).getId());
                intent.putExtra("content", mNoteList.get(pos).getContent());
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(int pos) {
                // 长按项的处理（可以根据需求实现）
                return false;
            }
        });
    }

    // 获取笔记成功
    @Override
    public void getNoteSuccess(List<Note> list) {
        if (list == null || list.isEmpty()) {
            Snackbar.make(searchResultsRecyclerView, "没有笔记数据", Snackbar.LENGTH_SHORT).show();
        } else {
            mNoteList = list;  // 更新笔记数据
            mAdapter.updateData(mNoteList);  // 更新适配器
            searchResultsRecyclerView.setAdapter(mAdapter); // 确保 RecyclerView 设置了适配器
        }
    }

    // 获取分类成功
    @Override
    public void getCategorySuccess(List<Category> categoryList) {
        // 目前不需要处理分类，只是初始化
    }

    // 获取数据出错
    @Override
    public void getError(String msg) {
        Snackbar.make(searchResultsRecyclerView, "获取数据失败: " + msg, Snackbar.LENGTH_SHORT).show();
    }
}
