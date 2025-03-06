package me.jin.note.activity;

import static me.jin.note.activity.MainActivity.ip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jin.note.R;
import me.jin.note.adapter.FollowListAdapter;
import me.jin.note.adapter.FollowListAdapter2;
import me.jin.note.bean.Follow;
import me.jin.note.bean.UserInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FansActivity extends AppCompatActivity {

    private List<UserInfo> userInfos = new ArrayList<>();
    private List<Follow> followList = new ArrayList<>();
    Gson gson = new Gson();
    private String userId;
    private FollowListAdapter2 followListAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Handler adapterHandler;
    private String url = "http://" + ip + "/Note";
    private String status;
    private int page = 0;
    private ListView listView;
    private ImageView back;
    private String authorId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fans);
        //获取用户信息和登录状态
        SharedPreferences sharedPreferences = this.getSharedPreferences("userName_and_userId", MODE_PRIVATE);
        status = sharedPreferences.getString("status", "");
        userId = sharedPreferences.getString("userId", "");
        //获取上个页面传输来的参数
        authorId = getIntent().getStringExtra("authorId");
        //
        initView();
        initData();
        setListenter();
        // 设置ListView的Adapter
        try {
            setListAdapter();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //向后台接收关注列表
                OkHttpClient client = new OkHttpClient();
                Request getFollowUserInfoList = new Request.Builder()
                        .url(url + "/follow/getFansUserInfoList?userId=" + authorId)
                        .build();
                Request getFollowList = new Request.Builder()
                        .url(url + "/follow/getFansList?userId=" + authorId)
                        .build();
                //发起请求并获取响应
                try {
                    Response followUserInfoListResponse = client.newCall(getFollowUserInfoList).execute();
                    Response followListResponse = client.newCall(getFollowList).execute();
                    //获取响应体
                    if(followUserInfoListResponse.isSuccessful() && followListResponse.isSuccessful()){
                        //处理数据
                        ResponseBody followUserInfoListBody = followUserInfoListResponse.body();
                        ResponseBody followListBody = followListResponse.body();
                        if(followUserInfoListBody!=null&&followListBody!=null){
                            String followUserInfoListData = followUserInfoListBody.string();
                            String followListData = followListBody.string();
                            followList = gson.fromJson(followListData, new TypeToken<List<Follow>>() {
                            }.getType());
                            userInfos = gson.fromJson(followUserInfoListData, new TypeToken<List<UserInfo>>() {
                            }.getType());
                            //装载数据
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(!userInfos.isEmpty()){
                                        try {
                                            followListAdapter = new FollowListAdapter2(FansActivity.this,
                                                    R.layout.adapter_follow_item,
                                                    userInfos,userId,
                                                    FollowListAdapter.NORMAL_TYPE,
                                                    adapterHandler);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        listView.setAdapter(followListAdapter);
                                    }
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }

    private void setListenter() {
        back.setOnClickListener(v -> finish());
        //给item设置点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FansActivity.this, UserInfoActivity.class);
//                Toast.makeText(MyFollowActivity.this, "点击了" + position, Toast.LENGTH_SHORT).show();
                intent.putExtra("AuthorId", followList.get(position).getUserId());
                //跳转
                startActivity(intent);
            }
        });
    }

    private void initView() {
        listView = findViewById(R.id.followList);
        back = findViewById(R.id.back);
    }
    private void setListAdapter() throws InterruptedException {
        // 创建并设置关注列表展示的Adapter
        followListAdapter = new FollowListAdapter2(getApplicationContext(),
                R.layout.adapter_follow_item,
                userInfos,
                userId,
                FollowListAdapter.NORMAL_TYPE,
                adapterHandler);
        listView.setAdapter(followListAdapter);
    }
}