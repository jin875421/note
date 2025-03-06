package me.jin.note.activity;

import static me.jin.note.activity.MainActivity.ip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jin.note.R;
import me.jin.note.adapter.PostListAdapter;
import me.jin.note.bean.Note;
import me.jin.note.bean.NoteWithUserInfo;
import me.jin.note.bean.UserInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PostSearchActivity extends AppCompatActivity {
    private String url="http://"+ip+"/Note/note";
    private String searchUrl="http://"+ip+"/Note/note/search";
    private EditText searchText1;
    private ListView listView;
    private RefreshLayout refreshLayout;
    private PostListAdapter postAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int page = 0;
    private String status;
    private List<Note> notes = new ArrayList<>();
    private List<UserInfo> userInfos = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);
        init();
        SharedPreferences sharedPreferences = this.getSharedPreferences("userName_and_userId", MODE_PRIVATE);
        status = sharedPreferences.getString("status","");
        setListenter();


    }

    private void setListenter() {
        searchText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchText = v.getText().toString().trim();
                    if (!searchText.isEmpty()) {
                        // 开启线程接收帖子数据
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                OkHttpClient client = new OkHttpClient();
                                // 创建请求获取 Note 类
                                Request request = new Request.Builder()
                                        .url(searchUrl + "?searchText=" + searchText)
                                        .build();
                                try {
                                    // 发起请求并获取响应
                                    Response response = client.newCall(request).execute();
                                    // 检测响应是否成功
                                    if (response.isSuccessful()) {
                                        // 获取响应数据
                                        ResponseBody responseBody = response.body();
                                        if (responseBody != null) {
                                            // 处理数据
                                            String responseData = responseBody.string();
                                            Gson gson = new Gson();
                                            List<NoteWithUserInfo> postWithUserInfoList = gson.fromJson(responseData, new TypeToken<List<NoteWithUserInfo>>() {}.getType());
                                            notes = new ArrayList<>();
                                            userInfos = new ArrayList<>();
                                            for (NoteWithUserInfo noteWithUserInfo : postWithUserInfoList) {
                                                notes.add(noteWithUserInfo.getNote());
                                                userInfos.add(noteWithUserInfo.getUserInfo());
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (notes != null && userInfos != null) {
                                                            PostListAdapter postAdapter = new PostListAdapter(PostSearchActivity.this, R.layout.post_item, notes, userInfos);
                                                            listView.setAdapter(postAdapter);
                                                        } else {
                                                            // 处理异常情况
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    return true;
                }
                return false;
            }
        });
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                notes = new ArrayList<>();
                userInfos = new ArrayList<>();

                String searchText = searchText1.getText().toString().trim();

                if (!searchText.isEmpty()) {
                    // 开启线程接收帖子数据
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            // 创建请求获取 Post 类
                            Request request = new Request.Builder()
                                    .url(searchUrl + "?searchText=" + searchText)
                                    .build();
                            try {
                                // 发起请求并获取响应
                                Response response = client.newCall(request).execute();
                                // 检测响应是否成功
                                if (response.isSuccessful()) {
                                    // 获取响应数据
                                    ResponseBody responseBody = response.body();
                                    if (responseBody != null) {
                                        // 处理数据
                                        String responseData = responseBody.string();
                                        Gson gson = new Gson();
                                        List<NoteWithUserInfo> postWithUserInfoList = gson.fromJson(responseData, new TypeToken<List<NoteWithUserInfo>>() {}.getType());
                                        notes = new ArrayList<>();
                                        userInfos = new ArrayList<>();
                                        for (NoteWithUserInfo postWithUserInfo : postWithUserInfoList) {
                                            notes.add(postWithUserInfo.getNote());
                                            userInfos.add(postWithUserInfo.getUserInfo());
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (notes != null && userInfos != null) {
                                                        PostListAdapter postAdapter = new PostListAdapter(PostSearchActivity.this, R.layout.post_item, notes, userInfos);
                                                        listView.setAdapter(postAdapter);
                                                    } else {
                                                        // 处理异常情况
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }

        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                PostListAdapter postListAdapter = (PostListAdapter) parent.getAdapter();
                //获取点击项数据对象
                NoteWithUserInfo clickItem = (NoteWithUserInfo) postListAdapter.getItem(i);
                Intent intent = new Intent(PostSearchActivity.this, PostDisplayActivity.class);
                intent.putExtra("postwithuserinfo", clickItem);
                startActivityForResult(intent,1);
            }
        });

    }

    private void init() {
        searchText1 = findViewById(R.id.et_searchtext);
        listView = findViewById(R.id.post_display);
        refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
        postAdapter = new PostListAdapter(this, R.layout.post_item, notes, userInfos);
        listView.setAdapter(postAdapter);
    }
}