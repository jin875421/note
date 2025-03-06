package me.jin.note.adapter;

import static me.jin.note.activity.MainActivity.ip;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jin.note.R;
import me.jin.note.activity.PostDisplayActivity;
import me.jin.note.base.BaseRecyclerViewAdapter;
import me.jin.note.bean.Note;
import me.jin.note.bean.NoteWithUserInfo;
import me.jin.note.bean.UserInfo;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostListAdapter extends BaseAdapter {
    private Context context;
    private int layoutId;
    private List<Note> notes;
    private List<UserInfo> userInfos;
    private String url = "http://"+ip+"/Note/";
    private final Handler handler = new Handler(Looper.getMainLooper());
    public PostListAdapter(Context context, int layoutId, List<Note> notes, List<UserInfo> userInfos){
        this.notes = notes;
        this.layoutId = layoutId;
        this.context = context;
        this.userInfos = userInfos;
    }
    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Object getItem(int i) {
        return new NoteWithUserInfo(notes.get(i),userInfos.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = LayoutInflater.from(context).inflate(layoutId,null);
        ImageView useravatar = v.findViewById(R.id.imageViewUserAvatar);
        TextView username = v.findViewById(R.id.textViewUsername);
        TextView title = v.findViewById(R.id.textViewTitle);
        WebView content = v.findViewById(R.id.note_content);
        TextView likeCount = v.findViewById(R.id.like_count);
        TextView commentCount = v.findViewById(R.id.comment_count);
        Note note1 = notes.get(i);
        title.setText(note1.getTitle());
        WebSettings webSettings = content.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        content.setInitialScale(80);
        content.loadData( note1.getContent(), "text/html; charset=UTF-8", null);
        // 调整 WebView 的焦点和可点击性
        content.setFocusable(false);
        content.setFocusableInTouchMode(false);
        content.setClickable(false);
//        content.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NoteWithUserInfo clickItem = new NoteWithUserInfo(note1,userInfos.get(i));
//                Intent intent = new Intent(, PostDisplayActivity.class);
//                intent.putExtra("postwithuserinfo", clickItem);
//                startActivityForResult(intent,1);
//            }
//        });
        username.setText(userInfos.get(i).getUserName());
        RequestOptions requestOptions = new RequestOptions()
                .transform(new CircleCrop())
                .placeholder(R.mipmap.loading);
        ;
        Glide.with(context)
                .load(url+ userInfos.get(i).getAvatar())
                .apply(requestOptions)
                .into(useravatar);
        //从服务器查询点赞和评论数
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url+"note/getLikeCount?postId="+note1.getUniqueId())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()){
                        String responseData = response.body().string();
                        //获取点赞数
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (responseData.equals("0")) {
                                    likeCount.setText("0");
                                } else {
                                    likeCount.setText(responseData);
                                }
                            }
                        });
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url+"comment/getCommentCount?postId="+note1.getUniqueId())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()){
                        String responseData = response.body().string();
                        //给控件设置评论数
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (responseData.equals("0")) {
                                    commentCount.setText("0");
                                } else {
                                    commentCount.setText(responseData);
                                }
                            }
                        });

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        // 查询用户额外信息
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                OkHttpClient client = new OkHttpClient();
//                RequestBody requestBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("userId", note1.getUserId())
//                        .build();
//                Request request = new Request.Builder()
//                        .url(url+"/userExtraInfo/getUserExtraInfo")
//                        .post(requestBody)
//                        .build();
//                try {
//                    Response response = client.newCall(request).execute();
//                    if(response.isSuccessful()){
//                        String responseData = response.body().string();
//                        if(responseData.equals("")){
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.i("PostListAdapter", "无数据");
//                                }
//                            });
//                        } else {
//                            UserExtraInfo userExtraInfo = new Gson().fromJson(responseData, UserExtraInfo.class);
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.i("PostListAdapter", "获取用户额外数据成功");
//                                    int level = userExtraInfo.getLevel();
//                                    switch (level){
//                                        case 1:
//                                            Glide.with(context).load(R.mipmap.lv1).into(levelImage);
//                                            break;
//                                        case 2:
//                                            Glide.with(context).load(R.mipmap.lv2).into(levelImage);
//                                            break;
//                                        case 3:
//                                            Glide.with(context).load(R.mipmap.lv3).into(levelImage);
//                                            break;
//                                        case 4:
//                                            Glide.with(context).load(R.mipmap.lv4).into(levelImage);
//                                            break;
//                                        case 5:
//                                            Glide.with(context).load(R.mipmap.lv5).into(levelImage);
//                                            break;
//                                        default:
//                                            levelImage.setVisibility(View.GONE);
//                                            break;
//                                    }
//                                }
//                            });
//                        }
//                    } else {
//                        Log.i("PostListAdapter", "获取用户额外数据失败");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        return v;
    }
    private int convertDpToPixel(int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}

