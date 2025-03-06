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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import me.jin.note.R;
import me.jin.note.activity.PostSearchActivity;
import me.jin.note.adapter.PageAdapter;
import me.jin.note.bean.UserInfo;

public class CommunityFragment extends Fragment {
    private String url="http://"+ip+"/travel/posts/getpostlist";
    private String searchUrl="http://"+ip+"/travel/posts/search";
    private RelativeLayout lsda;
    private List<UserInfo> userInfos = new ArrayList<>();
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String status;
    private ImageView searchBtn;
    private View view;
    private int page = 0;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private PageAdapter adapter;
    private List<Fragment> fragments;
    private final Handler handler = new Handler(Looper.getMainLooper());
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_community,container,false);
        searchBtn = view.findViewById(R.id.note_search);
        lsda=view.findViewById(R.id.community_top);
        tabLayout = view.findViewById(R.id.tbl);
        viewPager2 = view.findViewById(R.id.vp2);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userName_and_userId", MODE_PRIVATE);
        status = sharedPreferences.getString("status","");
        setListener();
//        initData();
        initPage();

        adapter = new PageAdapter(fragments,getActivity());

        viewPager2.setAdapter(adapter);
        TabLayoutMediator mediator = new TabLayoutMediator(
                tabLayout,
                viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {

                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position){
                            case 0:
                                tab.setText("推荐");
                                break;
                            case 1:
                                tab.setText("关注");
                                break;
                            default:
                                break;
                        }
                    }
                }
        );
        mediator.attach();
        return view;
    }

    private void initPage() {
        fragments = new ArrayList<>();
        fragments.add(new PostsFragment());
        fragments.add(new FollowFragment());
    }


    public void setListener(){
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转搜索页
                Intent intent = new Intent(getActivity(), PostSearchActivity.class);
                startActivity(intent);
            }
        });

    }

}