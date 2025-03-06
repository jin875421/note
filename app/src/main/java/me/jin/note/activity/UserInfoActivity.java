package me.jin.note.activity;


import static me.jin.note.activity.MainActivity.ip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import me.jin.note.R;
import me.jin.note.activity.login.LoginActivity;
import me.jin.note.adapter.PostListAdapter;
import me.jin.note.bean.Note;
import me.jin.note.bean.NoteWithUserInfo;
import me.jin.note.bean.UserInfo;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UserInfoActivity extends AppCompatActivity {
    private String status;
    private String userId;
    private String authorId;
    private String authorName;
    private ImageView backbtn,avatar;
    private Button follow;
    private TextView username,fansCount,followCount;
    private ListView postList;
    private String url="http://"+ip+"/Note/";
    private List<Note> posts = new ArrayList<>();
    private List<UserInfo> userInfos = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isFollow = false;
    private RelativeLayout follows,fans,rtltReview,achievement;
    private ImageView menuButton; // 添加菜单按钮的声明

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        SharedPreferences sharedPreferences = getSharedPreferences("userName_and_userId", MODE_PRIVATE);
        //获取用户状态和用户名
        status = sharedPreferences.getString("status","");
        userId = sharedPreferences.getString("userId","");
        //获取上个页面传来的值
        authorId = getIntent().getStringExtra("AuthorId");
        System.out.println("authorId:"+authorId);
        try {
            if (isFollowed(authorId)){
                isFollow = true;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initview();
        setListener();
        initData();
        loadPost();
        setMenuButtonListener(); // 添加菜单按钮监听器
    }

    private void setListener() {
        //点击跳转至展示此用户关注的人
        follows.setOnClickListener(v->{
            Intent intent = new Intent(UserInfoActivity.this, FollowsActivity.class);
            intent.putExtra("authorId",authorId);
            startActivity(intent);
        });
        //点击跳转至展示此用户的粉丝
        fans.setOnClickListener(v->{
            Intent intent = new Intent(UserInfoActivity.this, FansActivity.class);
            intent.putExtra("authorId",authorId);
            startActivity(intent);
        });

        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                PostListAdapter postListAdapter = (PostListAdapter) parent.getAdapter();
                //获取点击项数据对象
                NoteWithUserInfo clickItem = (NoteWithUserInfo) postListAdapter.getItem(i);
                Intent intent = new Intent(UserInfoActivity.this, PostDisplayActivity.class);
                intent.putExtra("postwithuserinfo", clickItem);
                startActivity(intent);
            }
        });
        backbtn.setOnClickListener(v -> finish());
        //关注按钮
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //处理关注点击事件
                if (status==""){
                    showLoginDialog();
                } else if (userId.equals(authorId)) {
                    //提示
                    Toast.makeText(UserInfoActivity.this, "不能关注自己", Toast.LENGTH_SHORT).show();
                } else {
                    //未关注，进行关注
                    if(!isFollow){
                        follow.setBackground(getResources().getDrawable(R.drawable.round_button_followed_background));
                        follow.setTextColor(Color.parseColor("#181A23"));
                        follow.setText("已关注");
                        isFollow = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //进行关注
                                OkHttpClient client1 = new OkHttpClient();
                                RequestBody formBody = new FormBody.Builder()
                                        .add("userId", userId)
                                        .add("followId", authorId)
                                        .build();
                                Request request = new Request.Builder()
                                        .url(url+"follow/addFollow")
                                        .post(formBody)
                                        .build();
                                //发起请求
                                try {
                                    Response response = client1.newCall(request).execute();
                                    //检测请求是否成功
                                    if (response.isSuccessful()){

                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }else{
                        //弹出确认窗口
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);
                        builder.setTitle("取消关注");
                        builder.setMessage("确定取消关注吗？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //已关注，取消关注
                                follow.setText("关注");
                                follow.setTextColor(Color.parseColor("#ffffff"));
                                follow.setBackground(getResources().getDrawable(R.drawable.round_button_unfollowed_background));
                                isFollow = false;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String deleteUrl = url+"follow/deleteFollow";
                                        //进行关注
                                        OkHttpClient client1 = new OkHttpClient();
                                        RequestBody formBody = new FormBody.Builder()
                                                .add("userId", userId)
                                                .add("followId", authorId)
                                                .build();
                                        Request request = new Request.Builder()
                                                .url(deleteUrl)
                                                .post(formBody)
                                                .build();
                                        //发起请求
                                        try {
                                            Response response = client1.newCall(request).execute();
                                            //检测请求是否成功
                                            if (response.isSuccessful()){

                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();

                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }

                }
            }
        });
    }

    public void initview(){
         backbtn = findViewById(R.id.back);
         avatar = findViewById(R.id.avatar);
         follow = findViewById(R.id.follow);
         username = findViewById(R.id.user_name);
         fansCount = findViewById(R.id.fans_count);
         followCount = findViewById(R.id.follow_count);
         postList = findViewById(R.id.post_list);
         follows = findViewById(R.id.follows);
         fans = findViewById(R.id.fans);
         menuButton = findViewById(R.id.menu); // 初始化菜单按钮
        //判断是否是本人
        if(userId.equals(authorId)){
            menuButton.setVisibility(View.VISIBLE);
        } else {
            menuButton.setVisibility(View.GONE);
        }
    }
    public void initData() {
        //通过id查询个人数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String urlWithParams = "http://" + ip + "/Note/" + "user/getUserInfo?userId=" + authorId;
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //设置头像和用户名
                                username.setText(userInfo.getUserName());
                                authorName = userInfo.getUserName();
                                if (userInfo.getAvatar() != null) {
                                    RequestOptions requestOptions = new RequestOptions()
                                            .transform(new CircleCrop());
                                    Glide.with(UserInfoActivity.this)
                                            .load("http://" + ip + "/Note/" + userInfo.getAvatar())
                                            .apply(requestOptions)
                                            .into(avatar);
                                } else {
                                    //加载默认头像
                                    avatar.setImageResource(R.drawable.user_defaultimage);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //加载关注人数
                String getFollowCountUrl = "http://" + ip + "/Note/" + "follow/getFollowCount?userId=" + authorId;
                Request request2 = new Request.Builder()
                        .url(getFollowCountUrl)
                        .build();
                try {
                    Response response2 = client.newCall(request2).execute();
                    if (response2.isSuccessful()) {
                        //返回成功
                        String responseData2 = response2.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //加载关注人数
                                int count = Integer.parseInt(responseData2);
                                followCount.setText(String.valueOf(count));
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String getFansCountUrl = "http://" + ip + "/Note/" + "follow/getFansCount?userId=" + authorId;
                Request request3 = new Request.Builder()
                        .url(getFansCountUrl)
                        .build();
                try {
                    Response response3 = client.newCall(request3).execute();
                    if (response3.isSuccessful()) {
                        //返回成功
                        String responseData3 = response3.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //加载关注人数
                                int count = Integer.parseInt(responseData3);
                                fansCount.setText(String.valueOf(count));
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //获取关注状态
                if (!status.equals("")) {
                    try {
                        if (isFollowed(authorId)) {
                            isFollow = true;
                            follow.setText("已关注");
                            follow.setTextColor(Color.parseColor("#181A23"));
                            follow.setBackground(getResources().getDrawable(R.drawable.round_button_followed_background));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
    public void loadPost(){
        //加载帖子
        new Thread(new Runnable() {
            @Override
            public void run() {
                //
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://"+ip+"/Note/note/getMynotelist"+"?userId="+authorId)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    //处理响应数据
                    if(response.isSuccessful()){
                        ResponseBody responseBody = response.body();
                        if (responseBody != null){
                            //处理数据
                            String responseData = responseBody.string();
                            Gson gson = new Gson();
                            List<NoteWithUserInfo> postWithUserInfoList = gson.fromJson(responseData,new TypeToken<List<NoteWithUserInfo>>(){}.getType());
                            posts = new ArrayList<>();
                            userInfos = new ArrayList<>();
                            for(NoteWithUserInfo postWithUserInfo: postWithUserInfoList){
                                //解析数据
                                posts.add(postWithUserInfo.getNote());
                                userInfos.add(postWithUserInfo.getUserInfo());
                            }
                            //进行数据加载
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (posts.size()!=0&&userInfos.size()!=0){
                                        PostListAdapter postAdapter = new PostListAdapter(UserInfoActivity.this,R.layout.post_item,posts,userInfos);
                                        postList.setAdapter(postAdapter);
                                        setListViewHeightBasedOnChildren(postList);
                                    }else {
                                        System.out.println("数据为空");
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
    public void showLoginDialog() {
        // 创建AlertDialog构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);
        builder.setTitle("账号未登录！")
                .setMessage("是否前往登录账号")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确定”按钮后的操作
                        Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“取消”按钮后的操作
                        dialog.dismiss(); // 关闭对话框
                    }
                });

        // 创建并显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //查询关注状态
    private boolean isFollowed(String authorId) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1]; // 用于存储结果

        new Thread(() -> {

            OkHttpClient client = new OkHttpClient();
            String urlWithParams = "http://"+ip+"/Note/" + "follow/isFollow?userId=" + userId + "&followId=" + authorId;
            Request request = new Request.Builder()
                    .url(urlWithParams)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseData = response.body().string();
                if ("true".equals(responseData)) {
                    result[0] = true;
                } else {
                    result[0] = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result[0] = false; // 或根据需要处理异常情况
            } finally {
                latch.countDown(); // 通知主线程任务已完成
            }
        }).start();

        latch.await(); // 阻塞主线程，等待子线程完成
        return result[0];
    }
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void setMenuButtonListener() {
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.edit_profile:
                        // 跳转到修改个人信息页面
                        Intent intent = new Intent(UserInfoActivity.this, UpdatePersonalInformationActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.logout:
                        // 登出账号
                        logout();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("userName_and_userId", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("status");
        editor.remove("userId");
        editor.apply();

        Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}