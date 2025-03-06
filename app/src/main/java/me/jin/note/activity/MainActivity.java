package me.jin.note.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;

import java.util.ArrayList;
import java.util.List;

import me.jin.note.R;
import me.jin.note.adapter.PageAdapter;
import me.jin.note.fragment.CommunityFragment;
import me.jin.note.fragment.NoteListFragment;

public class MainActivity extends AppCompatActivity {
    public static final String ip = "172.29.190.170:8080";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private List<Fragment> fragments;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化sdk(全局设置)
        SparkChainConfig config =  SparkChainConfig.builder()
                .appID("c42f8479")
                .apiKey("bec3141d480886080ec1963a3abcdb75")
                .apiSecret("NWU2MWFmYTQ4YmNjMmNhMTJmN2Y2OGYx");
        int ret = SparkChain.getInst().init(getApplicationContext(), config);

        tabLayout = findViewById(R.id.tbl);
        viewPager = findViewById(R.id.vp);
        //初始化
        initpages();
        //实例化
        PageAdapter adapter = new PageAdapter(fragments,this);
        viewPager.setAdapter(adapter);
        TabLayoutMediator mediator = new TabLayoutMediator(
                tabLayout,
                viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position){
                            case 0:
                                tab.setText("笔记");
                                break;
                            case 1:
                                tab.setText("社区");
                                break;
                        }
                    }
                }
        );
        mediator.attach();

    }
    private void initpages(){
        fragments = new ArrayList<>();
        fragments.add(new NoteListFragment());
        fragments.add(new CommunityFragment());
    }

}