package com.itant.zhuling.ui.main.tab.sentence;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.itant.library.recyclerview.CommonAdapter;
import com.itant.library.recyclerview.base.ViewHolder;
import com.itant.zhuling.R;
import com.itant.zhuling.tool.ToastTool;
import com.itant.zhuling.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

/**
 * Created by iTant on 2017/3/26.
 * 诗句
 */

public class SentenceFragment extends BaseFragment implements SentenceContract.View, SwipeRefreshLayout.OnRefreshListener {
    private int page;// 分页页码
    private static final int START_PAGE = 0;

    private SentenceContract.Presenter mPresenter;

    private RecyclerView rv_sentence;
    private List<SentenceBean> mSentences;
    private CommonAdapter<SentenceBean> mAdapter;

    private SwipeRefreshLayout swipe_refresh_layout;
    private LinearLayoutManager mLayoutManager;
    private int mLastVisibleItem;
    private LinearLayout ll_empty;

    @Override
    public int getLayoutId() {
        // 绑定视图
        return R.layout.fragment_sentence;
    }

    @Override
    public void initViews(View view) {
        ll_empty = (LinearLayout) view.findViewById(R.id.ll_empty);

        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        //设置下拉出现小圆圈是否是缩放出现，出现的位置，最大的下拉位置
        //swipe_refresh_layout.setProgressViewOffset(true, 150, 500);
        //设置下拉圆圈的大小，两个值 LARGE， DEFAULT
        swipe_refresh_layout.setSize(SwipeRefreshLayout.DEFAULT);
        // 设置下拉圆圈上的颜色，蓝色、绿色、橙色、红色
        swipe_refresh_layout.setColorSchemeResources(
                R.color.colorPrimary,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        // 通过 setEnabled(false) 禁用下拉刷新
        swipe_refresh_layout.setEnabled(true);
        // 设定下拉圆圈的背景
        swipe_refresh_layout.setProgressBackgroundColorSchemeResource(R.color.white);
        // 第一次进来，为加载数据的状态
        swipe_refresh_layout.setRefreshing(true);
        swipe_refresh_layout.setOnRefreshListener(this);

        rv_sentence = (RecyclerView) view.findViewById(R.id.rv_sentence);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_sentence.setLayoutManager(mLayoutManager);
        //rv_sentence.setHasFixedSize(true);如果加了这一句，又运用了开源动画库的话，那么第一次加载RecyclerView没有内容，也没有动画
        // 在这里，无论是上拉加载更多还是下拉刷新我们用的都是SwipeRefreshLayout的加载动画，我们也很想集成强大的XRecyclerView和
        // LoadMoreRecyclerView，可是很遗憾，我两种都尝试过了，会和我们的AppBarLayout以及SwipeRefreshLayout有冲突，同学们可以
        // 尝试一下，我没有成功，或许是我的用法不对吧。
        rv_sentence.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        mLastVisibleItem + 1 == mAdapter.getItemCount() &&
                        !swipe_refresh_layout.isRefreshing()) {
                    // 如果到底部了，而且不是正在加载状态，就变为正在加载状态，并及时去加载数据
                    swipe_refresh_layout.setRefreshing(true);
                    // 加载更多
                    page++;
                    mPresenter.getSentences(page);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });


        mSentences = new ArrayList<>();
        mAdapter = new CommonAdapter<SentenceBean>(getActivity(), R.layout.item_sentence, mSentences) {
            @Override
            protected void convert(final ViewHolder viewHolder, final SentenceBean item, int position) {

                viewHolder.setText(R.id.tv_words, item.getWords());
                viewHolder.setOnClickListener(R.id.ll_sentence, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText("sentence", item.getWords());
                        manager.setPrimaryClip(clipData);
                        ToastTool.showShort(getActivity(), "已复制到剪贴板");
                    }
                });
            }
        };


        AnimationAdapter animationAdapter = new SlideInBottomAnimationAdapter(mAdapter);
        animationAdapter.setFirstOnly(false);// 不只第一次有动画
        animationAdapter.setDuration(1000);
        animationAdapter.setInterpolator(new OvershootInterpolator(0.5f));

        // AnimationAdapter效果可以叠加
        //AnimationAdapter scale = new ScaleInAnimationAdapter(animationAdapter);
        animationAdapter.setFirstOnly(true);// 只有第一次有动画
        //animationAdapter.setDuration(800);

        //rv_sentence.setAdapter(animationAdapter);
        // 我们已经在MultiItemTypeAdapter使用了item的动画，这里就不使用炫酷的Adapter动画了
        rv_sentence.setAdapter(mAdapter);

        mPresenter = new SentencePresenter(getActivity(), this);
        mPresenter.getSentences(page);
    }

    @Override
    public void onGetSentenceSuc(List<SentenceBean> beans) {
        int preSize = mSentences.size();
        if (page == START_PAGE) {
            // 是刷新操作，或者是第一次进来，要清空
            mSentences.clear();
            // 在item太短的情况下，不执行这步操作会闪退。
            mAdapter.notifyItemRangeRemoved(0, preSize);
            //mAdapter.notifyDataSetChanged();
            ToastTool.showShort(getActivity(), "刷新成功");
        }

        if (beans != null && beans.size() > 0) {
            // 获取到数据了
            int start = mSentences.size();
            mSentences.addAll(beans);
            mAdapter.notifyItemRangeInserted(start, mSentences.size());
        } else {
            // 没数据了
            if (page > START_PAGE) {
                ToastTool.showShort(getActivity(), "没有更多的数据啦");
                page--;
            }
        }

        if (mSentences.size() > 0) {
            // 有数据
            ll_empty.setVisibility(View.GONE);
        } else {
            ll_empty.setVisibility(View.VISIBLE);
        }

        // 刷新|加载的动作完成了
        swipe_refresh_layout.setRefreshing(false);
    }

    @Override
    public void onGetSentenceFail(String msg) {
        // 刷新|加载的动作完成了
        swipe_refresh_layout.setRefreshing(false);


        // 第一页的数据拉取失败
        if (page < START_PAGE) {
            page = START_PAGE;
        }
        if (page == START_PAGE) {
            int preSize = mSentences.size();
            // 是刷新操作，或者是第一次进来，要清空
            mSentences.clear();
            // 在item太短的情况下，不执行这步操作会闪退。
            mAdapter.notifyItemRangeRemoved(0, preSize);
            //mAdapter.notifyDataSetChanged();
        } else {
            // 加载更多失败，页数回滚
            page--;
        }

        if (mSentences.size() > 0) {
            // 有数据
            ll_empty.setVisibility(View.GONE);
        } else {
            ll_empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        // 下拉刷新
        page = START_PAGE;
        mPresenter.getSentences(page);
    }
}
