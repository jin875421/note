package me.jin.note.fragment;

import static me.jin.note.activity.MainActivity.ip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.wrapper.HeaderAndFooterWrapper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jin.note.R;
import me.jin.note.activity.CategoryManagerActivity;
import me.jin.note.activity.MainActivity;
import me.jin.note.activity.NoteAddActivity;
import me.jin.note.activity.NoteEditActivity;
import me.jin.note.activity.SearchActivity;
import me.jin.note.activity.UserInfoActivity;
import me.jin.note.activity.login.LoginActivity;
import me.jin.note.adapter.CategoryAdapter;
import me.jin.note.adapter.NoteAdapter;
import me.jin.note.bean.Category;
import me.jin.note.bean.Note;
import me.jin.note.bean.UserInfo;
import me.jin.note.contract.GetNoteContract;
import me.jin.note.db.NoteManager;
import me.jin.note.listener.OnRecyclerViewItemClickListener;
import me.jin.note.presenter.GetNotePresenterImpl;
import me.jin.note.utils.SPUtil;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteListFragment extends Fragment implements GetNoteContract.View,
        OnRecyclerViewItemClickListener {

    public String url = "http://" + ip + "/Note";
    private Toolbar mToolbar;
    private FloatingActionButton note_add;
    private TextView mTitle, userName, userId;
    private ImageView iv_update, iv_search;
    private DrawerLayout drawerLayout;
    private RecyclerView rv_drawer;
    private NoteAdapter mAdapter;
    private GetNotePresenterImpl mPresenter;
    private List<Note> mNoteList = new ArrayList<>();
    private List<Category> mCategoryList = new ArrayList<>();
    private boolean isDeleteModel = false;
    private NoteManager noteManager;
    private CategoryAdapter cate_adapter;
    private HeaderAndFooterWrapper rv_wrapper;
    private LinearLayout settings, cate_manager;
    private static String currentCate;
    private ActionBarDrawerToggle toggle;

    private CircleImageView head_img;
    private String user;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_note_list, container, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("userName_and_userId", 0);
        user = sharedPreferences.getString("userId", "");
        initData();
        initView(view);
        initListener();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initData() {
        currentCate = SPUtil.getString(requireContext(), "currentCate", "所有");
        noteManager = new NoteManager(requireContext());
        mAdapter = new NoteAdapter(requireContext());
        cate_adapter = new CategoryAdapter(requireContext(), R.layout.item_category, mCategoryList);
        mPresenter = new GetNotePresenterImpl(this);
        mPresenter.getCategory(requireContext());
    }

    private void initView(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        ((MainActivity) requireActivity()).setSupportActionBar(mToolbar);
        ((MainActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle("");
        iv_update = view.findViewById(R.id.note_update);
        iv_search = view.findViewById(R.id.note_search);
        RecyclerView mRecyclerView = view.findViewById(R.id.rv_note);
        note_add = view.findViewById(R.id.note_add);
        mTitle = view.findViewById(R.id.tv_title);
        mTitle.setText(currentCate);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        mRecyclerView.setAdapter(mAdapter);
        initDrawer(view);
    }

    private void initDrawer(View view) {
        drawerLayout = view.findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(requireActivity(), drawerLayout, mToolbar,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
        rv_drawer = view.findViewById(R.id.rv_drawer_left);
        rv_drawer.setLayoutManager(new LinearLayoutManager(requireContext()));//setLayoutManager
        //initHeaderAndFooter
        View header = LayoutInflater.from(requireContext()).inflate(R.layout.layout_drawer_header, rv_drawer, false);
        View footer = LayoutInflater.from(requireContext()).inflate(R.layout.layout_drawer_footer, rv_drawer, false);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("userName_and_userId", 0);
        userName = header.findViewById(R.id.user_name);
        userId = header.findViewById(R.id.user_id);
        head_img = header.findViewById(R.id.advtar);
        String status = sharedPreferences.getString("status", null);
        if ("1".equals(status)) {
            userName.setText(sharedPreferences.getString("userName", ""));
            userId.setText(sharedPreferences.getString("userId", ""));
            loadHeader();
        }

        settings = footer.findViewById(R.id.settings);
        cate_manager = footer.findViewById(R.id.category_manager);
        rv_wrapper = new HeaderAndFooterWrapper(cate_adapter);
        rv_wrapper.addHeaderView(header);
        rv_wrapper.addFootView(footer);
        rv_drawer.setAdapter(rv_wrapper);//setAdapter
    }

    private void loadHeader() {
        //加载头像
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String urlWithParams = "http://" + ip + "/Note/" + "user/getUserInfo?userId=" + user;
                Request request = new Request.Builder()
                        .url(urlWithParams)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        //获取成功
                        String responseData = response.body().string();
                        Gson gson = new Gson();
                        UserInfo userInfo = gson.fromJson(responseData, UserInfo.class);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (userInfo.getAvatar() != null) {
                                    RequestOptions requestOptions = new RequestOptions()
                                            .transform(new CircleCrop());
                                    Glide.with(NoteListFragment.this)
                                            .load("http://" + ip + "/Note/" + userInfo.getAvatar())
                                            .apply(requestOptions)
                                            .into(head_img);
                                    System.out.println("http://" + ip + "/Note/" + userInfo.getAvatar());
                                } else {
                                    //加载默认头像
                                    head_img.setImageResource(R.drawable.user_defaultimage);
                                }
                            }
                        });
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void uploadData(View v) {
        // 上传数据的操作
        if (noteManager.isEmpty()) {
            // 如果没有笔记，可以考虑显示提示消息
            Snackbar.make(v, "没有笔记可以更新", Snackbar.LENGTH_SHORT).show();
        } else {
            // 发送更新请求
            mPresenter.getNote(requireContext(), currentCate);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 建立网络连接
                    Gson gson = new Gson();
                    String updateUrl = url + "/note/update";
                    OkHttpClient client = new OkHttpClient();
                    // 将 List<Note> 转化为 JSON 字符串
                    for (Note note : mNoteList)
                    {
                        note.setUserId(user);
                    }
                    String jsonString = gson.toJson(mNoteList);

                    // 将 userId 和 jsonString 封装成 RequestBody
                    RequestBody requestBody = new FormBody.Builder()
                            .add("jsonString", jsonString)
                            .build();

                    Request request = new Request.Builder()
                            .url(updateUrl)
                            .post(requestBody)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            // 成功响应
                            String responseBody = response.body().string();
                            System.out.println("Response: " + responseBody);

                            // UI更新
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(v, "更新成功", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // 请求失败
                            System.out.println("Request failed: " + response.code());

                            // UI更新
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(v, "更新失败，请稍后再试", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(v, "请求失败，请检查网络", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void pullData(View v) {
        // 显示拉取提示
        Snackbar.make(v, "正在拉取数据...", Snackbar.LENGTH_LONG).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 拉取数据的操作，例如请求云端数据
                String pullUrl = url + "/note/getNotes?userId="+user;  // 假设拉取数据的URL
                System.out.println(pullUrl);
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(pullUrl)
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        // 成功响应，处理数据
                        String responseBody = response.body().string();
                        // 假设返回的数据是一个笔记列表的 JSON 格式
                        Gson gson = new Gson();
                        Type noteListType = new TypeToken<List<Note>>(){}.getType();
                        List<Note> notes = gson.fromJson(responseBody, noteListType);

                        // 在后台线程处理数据库操作
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 清空现有笔记数据
                                noteManager.clearTable();
                                // 遍历并插入拉取的数据
                                for (Note note : notes) {
                                    noteManager.delete(note.getId());
                                    noteManager.insert(note);
                                }
                                //刷新页面
                                mPresenter.getNote(requireContext(), currentCate);
                                mAdapter.notifyDataSetChanged();
                                // 更新UI提示
                                Snackbar.make(v, "拉取数据成功", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // 请求失败
                        System.out.println("Request failed: " + response.code());

                        // UI更新
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(v, "拉取失败，请稍后再试", Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(v, "请求失败，请检查网络", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }


    private void initListener() {
        iv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查用户是否已登录
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userName_and_userId", 0x0000);
                String status = sharedPreferences.getString("status","");
                if (status.equals("1")) {  // 使用字符串 "1"
                    // 已登录，弹出选择框
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("选择操作")
                            .setItems(new String[]{"上传数据", "拉取数据"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: // 上传数据
                                            uploadData(v);
                                            break;
                                        case 1: // 拉取数据
                                            pullData(v);
                                            break;
                                    }
                                }
                            })
                            .create()
                            .show();
                } else {
                    // 未登录，跳转至登录页面
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), SearchActivity.class);
                startActivity(intent);
            }
        });
        mAdapter.setOnRecyclerViewItemClickListener(this);
        note_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), NoteAddActivity.class);
                startActivity(intent);
            }
        });
        //点击category,设置category
        cate_adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                mNoteList.clear();
                mAdapter.updateData(mNoteList);
                currentCate = mCategoryList.get(position - 1).getCategory();//header
                SPUtil.putString(requireContext(), "currentCate", currentCate);
                mTitle.setText(currentCate);
                mPresenter.getNote(requireContext(), currentCate);//获取当前分类笔记
                hideDrawer();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 部分设置
            }
        });
        cate_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), CategoryManagerActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
        //用户信息
        head_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userName_and_userId", 0x0000);
                String status = sharedPreferences.getString("status","");
                if (status.equals("1")) {
                    //已登录
                    Intent intent = new Intent(requireContext(), UserInfoActivity.class);
                    intent.putExtra("AuthorId", sharedPreferences.getString("userId", ""));
                    startActivity(intent);
                }else {
                    //跳转至登录页面
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void getNoteSuccess(List<Note> list) {
        if (list == null) {
            Snackbar snackbar = Snackbar.make(note_add, currentCate + "文件夹空空如也哦", Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            snackbar.show();
        } else {
            mNoteList = list;
            mAdapter.updateData(list);
        }
    }

    @Override
    public void getError(String msg) {
    }

    @Override
    public void getCategorySuccess(List<Category> categoryList) {
        mCategoryList = categoryList;
        cate_adapter.updateData(mCategoryList);
    }

    @Override
    public void onItemClick(int pos) {
        if (isDeleteModel) {
            //如果是删除模式，单击子项变色
            if (mNoteList.get(pos).isFlag()) { //onBindViewHolder
                mNoteList.get(pos).setFlag(false);
            } else {
                mNoteList.get(pos).setFlag(true);
            }
            mAdapter.notifyDataSetChanged(); //notice
        } else {
            //非删除模式，单击进入编辑模式
            SPUtil.putString(requireContext(), "category", currentCate);
            Intent intent = new Intent(getActivity(), NoteEditActivity.class);
            intent.putExtra("currentCate", currentCate);
            intent.putExtra("id", mNoteList.get(pos).getId());
            intent.putExtra("content", mNoteList.get(pos).getContent());
            intent.putExtra("title", mNoteList.get(pos).getTitle());
            intent.putExtra("uniqueId", mNoteList.get(pos).getUniqueId());
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getNote(requireContext(), currentCate);
    }

    @Override
    public boolean onItemLongClick(int pos) {
        if (!isDeleteModel) {
            //进入删除模式
            mNoteList.get(pos).setFlag(true);
            //pos添加到deleteItem中
            mAdapter.notifyDataSetChanged();
            isDeleteModel = true;
            requireActivity().supportInvalidateOptionsMenu();//通知系统更新菜单
        }
        return false;
    }

    private void deleteNote() {
        for (int i = mNoteList.size() - 1; i > -1; i--) {//倒序，adapter.notifyItemRemoved会更新pos
            Note note = mNoteList.get(i);
            if (note.isFlag()) {
                noteManager.delete(note.getId());
                mNoteList.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
        isDeleteModel = false;
        requireActivity().supportInvalidateOptionsMenu();//通知系统更新菜单
        mNoteList.clear();
        mAdapter.updateData(mNoteList);
        mPresenter.getNote(requireContext(), currentCate);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (isDeleteModel) {
            inflater.inflate(R.menu.menu_delete_mode, menu);
        } else {
            inflater.inflate(R.menu.menu_note_list, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle.onOptionsItemSelected(item);
                return true;
            case R.id.list_delete:
                Snackbar snackbar = Snackbar.make(note_add, "单击笔记可删除笔记", Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                snackbar.show();
                isDeleteModel = true;
                requireActivity().supportInvalidateOptionsMenu();//通知系统更新菜单
                return true;
            case R.id.delete:
                deleteNote();
                return true;
            case R.id.manager:
                Intent intent = new Intent(requireContext(), CategoryManagerActivity.class);
                startActivity(intent);
                requireActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideDrawer() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
    }
}