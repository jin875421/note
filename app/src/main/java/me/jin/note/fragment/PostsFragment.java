package me.jin.note.fragment;

import static android.content.Context.MODE_PRIVATE;
import static me.jin.note.activity.MainActivity.ip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jin.note.R;
import me.jin.note.activity.PostDisplayActivity;
import me.jin.note.adapter.PostListAdapter;
import me.jin.note.bean.Note;
import me.jin.note.bean.NoteWithUserInfo;
import me.jin.note.bean.UserInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PostsFragment extends Fragment {
    private ListView postList;
    private String url="http://"+ip+"/Note/note/getnotelist";
    private List<Note> notes = new ArrayList<>();
    private List<UserInfo> userInfos = new ArrayList<>();
    private String userId;
    private RefreshLayout refreshLayout;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean firstLoad = true;
    private int page = 0;
    private PostListAdapter postAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userName_and_userId", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId","");
        postAdapter = new PostListAdapter(getActivity(),R.layout.post_item,notes,userInfos);
        initView(view);
        initData();
        setlistener();
        return view;
    }
    public void initView(View view){
        postList = view.findViewById(R.id.post_display);
        refreshLayout = view.findViewById(R.id.refreshLayout);

    }
    public void initData(){
        //开启线程接收帖子数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                //创建请求获取Post类
                Request request = new Request.Builder()
                        .url(url+"?page="+ page)
                        .build();
                try {
                    //发起请求并获取响应
                    Response response = client.newCall(request).execute();
                    //检测响应是否成功
                    if (response.isSuccessful()){
                        //获取响应数据
                        ResponseBody responseBody = response.body();
                        if (responseBody!=null){
                            //处理数据
                            String responseData = responseBody.string();
                            Gson gson = new Gson();
                            List<NoteWithUserInfo> postWithUserInfoList = gson.fromJson(responseData,new TypeToken<List<NoteWithUserInfo>>(){}.getType());
                            for (NoteWithUserInfo postWithUserInfo: postWithUserInfoList){
                                notes.add(postWithUserInfo.getNote());
                                userInfos.add(postWithUserInfo.getUserInfo());
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (notes !=null&&userInfos!=null){
                                        postAdapter = new PostListAdapter(getActivity(),R.layout.post_item,notes,userInfos);
                                        postList.setAdapter(postAdapter);
                                    }

                                }
                            });

                        }else {
                            //处理空数据
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void setlistener(){
        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                page = 0;
                notes = new ArrayList<>();
                userInfos = new ArrayList<>();
                initData();
                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                page++;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        //创建请求获取Post类
                        Request request = new Request.Builder()
                                .url(url+"?page="+ page)
                                .build();
                        try {
                            //发起请求并获取响应
                            Response response = client.newCall(request).execute();
                            //检测响应是否成功
                            if (response.isSuccessful()){
                                //获取响应数据
                                ResponseBody responseBody = response.body();
                                if (responseBody!=null){
                                    //处理数据
                                    String responseData = responseBody.string();
                                    Gson gson = new Gson();
                                    List<NoteWithUserInfo> postWithUserInfoList = gson.fromJson(responseData,new TypeToken<List<NoteWithUserInfo>>(){}.getType());
                                    for (NoteWithUserInfo postWithUserInfo: postWithUserInfoList){
                                        notes.add(postWithUserInfo.getNote());
                                        userInfos.add(postWithUserInfo.getUserInfo());
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (notes !=null&&userInfos!=null){
                                                postAdapter.notifyDataSetChanged();
                                            }else {

                                            }

                                        }
                                    });

                                }else {
                                    //处理空数据
                                }
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                refreshlayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });

        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                PostListAdapter postListAdapter = (PostListAdapter) parent.getAdapter();
                //获取点击项数据对象
                NoteWithUserInfo clickItem = (NoteWithUserInfo) postListAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), PostDisplayActivity.class);
                intent.putExtra("postwithuserinfo", clickItem);
                startActivityForResult(intent,1);
            }
        });
    }
}