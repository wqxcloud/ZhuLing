package com.itant.zhuling.ui.main.tab.news;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.itant.library.recyclerview.CommonAdapter;
import com.itant.library.recyclerview.base.ViewHolder;
import com.itant.zhuling.R;
import com.itant.zhuling.tool.ActivityTool;
import com.itant.zhuling.tool.ToastTool;
import com.itant.zhuling.ui.base.BaseFragment;
import com.itant.zhuling.ui.main.tab.news.detail.NewsDetailActivity;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

/**
 * Created by iTant on 2017/3/26.
 */

public class NewsFragment extends BaseFragment implements NewsContract.View, SwipeRefreshLayout.OnRefreshListener {
    private int page;// 分页页码
    private static final int START_PAGE = 0;

    private NewsContract.Presenter mPresenter;

    private RecyclerView rv_news;
    private List<NewsBean> mNewsBeans;
    private CommonAdapter<NewsBean> mAdapter;

    private SwipeRefreshLayout swipe_refresh_layout;
    private LinearLayoutManager mLayoutManager;
    private int mLastVisibleItem;
    private LinearLayout ll_empty;

    @Override
    public int getLayoutId() {
        // 绑定视图
        return R.layout.fragment_news;
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

        rv_news = (RecyclerView) view.findViewById(R.id.rv_news);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_news.setLayoutManager(mLayoutManager);
        rv_news.setItemAnimator(new DefaultItemAnimator());
        //rv_news.setHasFixedSize(true);如果加了这一句，又运用了开源动画库的话，那么第一次加载RecyclerView没有内容，也没有动画
        // 在这里，无论是上拉加载更多还是下拉刷新我们用的都是SwipeRefreshLayout的加载动画，我们也很想集成强大的XRecyclerView和
        // LoadMoreRecyclerView，可是很遗憾，我两种都尝试过了，会和我们的AppBarLayout以及SwipeRefreshLayout有冲突，同学们可以
        // 尝试一下，我没有成功，或许是我的用法不对吧。
        rv_news.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    mPresenter.getNews(page);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });


        mNewsBeans = new ArrayList<>();
        mAdapter = new CommonAdapter<NewsBean>(getActivity(), R.layout.item_news, mNewsBeans) {
            @Override
            protected void convert(final ViewHolder viewHolder, final NewsBean item, int position) {

                viewHolder.setText(R.id.news_summary_title_tv, item.getTitle());
                viewHolder.setText(R.id.news_summary_digest_tv, item.getDigest());
                viewHolder.setText(R.id.news_summary_ptime_tv, item.getPtime());

                final ImageView iv_news_summary = viewHolder.getView(R.id.news_summary_photo_iv);
                if (!TextUtils.isEmpty(item.getImgsrc())) {
                    final String tag = (String) iv_news_summary.getTag(R.id.news_summary_photo_iv);
                    final String uri = item.getImgsrc();
                    if (!uri.equals(tag)) {
                        // 设置默认图片
                        iv_news_summary.setImageResource(R.mipmap.empty);
                    }

                    // 关于glide跨activity加载图片造成transition动画不流畅的解决办法参考：
                    // http://myhexaville.com/2017/01/23/android-make-shared-transition-faster-better/
                    Glide.with(mContext)
                            .load(item.getImgsrc())
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)// 缓存所有尺寸的图片
                            .placeholder(R.mipmap.empty)
                            .error(R.mipmap.empty)
                            .override(256, 256)
                            .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                                glideAnimation) {
                            iv_news_summary.setTag(item.getImgsrc());
                            iv_news_summary.setImageBitmap(resource);
                        }
                    });
                    /*Glide.with(mContext)
                            .load(item.getImgsrc()).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(new ImageViewTarget<GlideDrawable>(iv_news_summary) {
                                @Override
                                protected void setResource(GlideDrawable resource) {
                                    // 还可以动态设置背景和字体颜色，参考：https://github.com/IhorKlimov/Immersive-app
                                    iv_news_summary.setImageDrawable(resource.getCurrent());
                                }
                            });*/
                } else {
                    // 设置默认图片
                    iv_news_summary.setImageResource(R.mipmap.empty);
                }

                viewHolder.setOnClickListener(R.id.rl_news_item, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 共享元素动画
                        Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
                        intent.putExtra("url_top", item.getImgsrc());
                        intent.putExtra("postId", item.getPostid());

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            // 可以实现共享动画
                            View sharedView = viewHolder.getView(R.id.news_summary_photo_iv);
                            String transitionName = getString(R.string.transition_name_news_to_detail);
                            ActivityOptions transitionActivityOptions =
                                    ActivityOptions.makeSceneTransitionAnimation(getActivity(), sharedView, transitionName);
                            startActivity(intent, transitionActivityOptions.toBundle());
                        } else {
                            ActivityTool.startActivity(getActivity(), intent);
                        }
                    }
                });
            }
        };

        AnimationAdapter animationAdapter = new SlideInBottomAnimationAdapter(mAdapter);
        animationAdapter.setFirstOnly(false);// 不只第一次有动画
        animationAdapter.setDuration(500);
        animationAdapter.setInterpolator(new OvershootInterpolator(0.5f));

        // AnimationAdapter效果可以叠加
        //AnimationAdapter scale = new ScaleInAnimationAdapter(animationAdapter);
        animationAdapter.setFirstOnly(false);// 只有第一次有动画
        //animationAdapter.setDuration(800);

        rv_news.setAdapter(animationAdapter);
        // 我们已经在MultiItemTypeAdapter使用了item的动画，这里就不使用炫酷的Adapter动画了
        //rv_news.setAdapter(mAdapter);

        mPresenter = new NewsPresenter(getActivity(), this);
        mPresenter.getNews(page);
    }

    @Override
    public void onGetNewsSuc(List<NewsBean> beans) {
        int preSize = mNewsBeans.size();
        if (page == START_PAGE) {
            // 是刷新操作，或者是第一次进来，要清空
            mNewsBeans.clear();
            // 在item太短的情况下，不执行这步操作会闪退。
            mAdapter.notifyItemRangeRemoved(0, preSize);
            //mAdapter.notifyDataSetChanged();
            ToastTool.showShort(getActivity(), "刷新成功");
        }

        if (beans != null && beans.size() > 0) {
            // 获取到数据了
            int start = mNewsBeans.size();
            mNewsBeans.addAll(beans);
            mAdapter.notifyItemRangeInserted(start, mNewsBeans.size());
        } else {
            if (page > START_PAGE) {
                ToastTool.showShort(getActivity(), "没有更多的数据啦");
                page--;
            }
        }

        if (mNewsBeans.size() > 0) {
            // 有数据
            ll_empty.setVisibility(View.GONE);
        } else {
            ll_empty.setVisibility(View.VISIBLE);
        }

        // 刷新|加载的动作完成了
        swipe_refresh_layout.setRefreshing(false);
    }

    @Override
    public void onGetNewsFail(String msg) {
        // 第一页的数据拉取失败
        if (page < START_PAGE) {
            page = START_PAGE;
        }
        if (page == START_PAGE) {
            int preSize = mNewsBeans.size();
            // 是刷新操作，或者是第一次进来，要清空
            mNewsBeans.clear();
            // 在item太短的情况下，不执行这步操作会闪退。
            mAdapter.notifyItemRangeRemoved(0, preSize);
            //mAdapter.notifyDataSetChanged();
        } else {
            // 加载更多失败，页数回滚
            page--;
        }

        if (mNewsBeans.size() > 0) {
            // 有数据
            ll_empty.setVisibility(View.GONE);
        } else {
            ll_empty.setVisibility(View.VISIBLE);
        }

        // 刷新|加载的动作完成了
        swipe_refresh_layout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        // 下拉刷新
        page = START_PAGE;
        mPresenter.getNews(page);
    }

    public void scrollToTop() {
        if (rv_news != null) {
            rv_news.smoothScrollToPosition(0);
        }
    }
}
