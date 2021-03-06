package com.itant.zhuling.ui.main.navigation.about.contents;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.itant.zhuling.R;
import com.itant.zhuling.ui.base.BaseSwipeActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;

/**
 * Created by iTant on 2017/3/26.
 */

public class HelpActivity extends BaseSwipeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_about_help);
        // 右划关闭
        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        setTitle("帮助");

        initView();
    }

    public void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            toolbar.setTitleTextColor(Color.WHITE);
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
