package me.jin.note.activity;


import static me.jin.note.activity.MainActivity.ip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import me.jin.note.R;
import me.jin.note.activity.login.LoginActivity;
import me.jin.note.adapter.RespondDetailAdapter;
import me.jin.note.bean.Comment;
import me.jin.note.bean.ReturnCommentRespond;
import me.jin.note.bean.UploadComment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RespondDetail extends AppCompatActivity {
    private Comment comment;
    List<ReturnCommentRespond> returnCommentResponds;
    private RespondDetailAdapter adapter;
    private ListView listView;
    private Button submit;
    private String status;
    private String postId;
    private EditText chatInputEt;
    private ImageView avatar;
    private TextView username;
    private TextView time;
    private TextView text;
    private String url = "http://"+ip+"/Note/";
    private ImageView backButton;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respond_detail);
        setView();
        initData();
        setListener();


    }

    private void setListener() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status==""){
                    // 创建AlertDialog构建器
                    AlertDialog.Builder builder = new AlertDialog.Builder(RespondDetail.this);
                    builder.setTitle("账号未登录！")
                            .setMessage("是否前往登录账号")
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确定”按钮后的操作
                                    Intent intent = new Intent(RespondDetail.this, LoginActivity.class);
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

                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //生成评论实体CommentRespond
                            String text = chatInputEt.getText().toString();;
                            String id = UUID.randomUUID().toString();
                            Date date = new Date();
                            SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd :HH:mm:ss");
                            System.out.println(dateFormat.format(date));
                            String time = dateFormat.format(date);
                            String parentId = comment.getCommentId();
                            SharedPreferences sharedPreferences = getSharedPreferences("userName_and_userId", MODE_PRIVATE);
                            String userId = sharedPreferences.getString("userId","");
                            UploadComment commentRespond = new UploadComment(postId, userId, text, id, time, parentId);
                            //okHttp
                            Gson gson = new Gson();
                            String json = gson.toJson(commentRespond);
                            RequestBody body = RequestBody.create(
                                    MediaType.parse("application/json;charset=utf-8"),
                                    json
                            );
                            Request request = new Request.Builder()
                                    .post(body)
                                    .url(url + "comment/addComment")
                                    .build();
                            //3.Call对象
                            Call call = client.newCall(request);
                            call.enqueue((new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                }
                                // 这里可以包含获取数据的逻辑，比如使用OkHttp请求数据
                                // 返回模拟的数据
                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    //获取响应的数据
                                    String result = response.body().string();
                                    if (result!=null){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                getCommentResponseData();
                                                adapter.notifyDataSetChanged();
                                                setListViewHeightBasedOnChildren(listView);
                                            }
                                        });
                                    }
                                    //清空EditText
                                    chatInputEt.setText("");
                                }
                            }));
                        }
                    }).start();
                    //点击提交后收回键盘
                    hideInput();
                }
            }
        });
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int last = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
            last = listItem.getMeasuredHeight();
        }
        totalHeight = totalHeight + last;

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void getCommentResponseData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //okHttp
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json;charset=utf-8"),
                        comment.getCommentId()
                );
                Request request = new Request.Builder()
                       .post(body)
                       .url(url + "comment/getReturnCommendRespondList")
                       .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        returnCommentResponds = new ArrayList<>();
                        //获取相应的数据
                        String result = response.body().string();
                        //反序列化消息
                        JsonArray jsonArray = JsonParser.parseString(result).getAsJsonArray();
                        for (JsonElement jsonElement : jsonArray) {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            ReturnCommentRespond returnCommentRespond = new Gson().fromJson(jsonObject, ReturnCommentRespond.class);
                            returnCommentResponds.add(returnCommentRespond);
                        }
                        // 更新UI线程中的ListView
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new RespondDetailAdapter(
                                        RespondDetail.this,
                                        R.layout.activity_respond_detail_adapter,
                                        returnCommentResponds
                                );
                                listView.setAdapter(adapter);
                                setListViewHeightBasedOnChildren(listView);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public void setView(){
        listView = findViewById(R.id.respond_list);
        submit = findViewById(R.id.submit);
        chatInputEt = findViewById(R.id.chatInputEt);
        username = findViewById(R.id.username);
        avatar = findViewById(R.id.avatar);
        time = findViewById(R.id.time);
        text = findViewById(R.id.text);
        backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false); // 使用水平方向
    }

    private void initData() {
        SharedPreferences sharedPreferences = getSharedPreferences("userName_and_userId", MODE_PRIVATE);
        status = sharedPreferences.getString("status","");
        //获取intent对象
        Intent intent = getIntent();
        //获得封装数据
        Bundle bundle = intent.getBundleExtra("bundle");
        comment = (Comment) bundle.getSerializable("comment");
        //获取评论回复所属帖子Id
        postId = comment.getPostId();
        //显示头像
        Glide.with(RespondDetail.this)
                .load(url + comment.getAvatar())
                .placeholder(R.mipmap.loading)
                .fallback(R.mipmap.blank)
                .circleCrop()
                .into(avatar);
        username.setText(comment.getUsername());
        time.setText(comment.getUploadTime());
        text.setText(comment.getComment());
        returnCommentResponds = comment.getReturnCommentResponds();
        //绑定adapter
        adapter = new RespondDetailAdapter(
                RespondDetail.this,
                R.layout.activity_respond_detail_adapter,
                returnCommentResponds);
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);
    }

    public void showInput(final EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}