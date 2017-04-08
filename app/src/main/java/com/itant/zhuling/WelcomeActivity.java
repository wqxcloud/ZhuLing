package com.itant.zhuling;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.itant.zhuling.ui.MainActivity;
import com.itant.zhuling.widget.leaf.FloatLeafLayout;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WelcomeActivity extends AppCompatActivity {

    private ImageView iv_launcher_inner;
    private TextView tv_app_name;
    boolean isShowingRubberEffect = false;

    private FloatLeafLayout fll_ling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.zoomin, 0);
        setContentView(R.layout.activity_welcome);
        initView();
        initAnimation();
    }

    private void initView() {
        iv_launcher_inner = (ImageView) findViewById(R.id.iv_launcher_inner);
        tv_app_name = (TextView) findViewById(R.id.tv_app_name);

        fll_ling = (FloatLeafLayout) findViewById(R.id.fll_ling);
    }

    private void initAnimation() {
        startLogoInner1();
        startLogoOuterAndAppName();

        startLing();
    }

    private void startLing() {
        fll_ling.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // 我们必须等view加载之后才开始动画，否则取到的宽高为0
                fll_ling.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                fll_ling.floatLing();
            }
        });
    }

    private void startLogoInner1() {
        iv_launcher_inner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // 我们必须等view加载之后才开始动画，否则取到的宽高为0
                iv_launcher_inner.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Animation animation = AnimationUtils.loadAnimation(WelcomeActivity.this, R.anim.anim_top_in);
                iv_launcher_inner.startAnimation(animation);
            }
        });
    }

    private void startLogoOuterAndAppName() {
        tv_app_name.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // 我们必须等view加载之后才开始动画，否则取到的宽高为0
                tv_app_name.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(1000);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float fraction = animation.getAnimatedFraction();
                        if (fraction >= 0.8 && !isShowingRubberEffect) {
                            isShowingRubberEffect = true;
                            startShowAppName();
                            finishActivity();
                        } else if (fraction >= 0.95) {
                            valueAnimator.cancel();
                            startLogoInner2();
                        }
                    }
                });
                valueAnimator.start();
            }
        });
    }

    private void startShowAppName() {
        YoYo.with(Techniques.FadeIn).duration(1000).playOn(tv_app_name);
    }

    private void startLogoInner2() {
        YoYo.with(Techniques.Bounce).duration(1000).playOn(iv_launcher_inner);
    }

    private void finishActivity() {
        Observable.timer(1800, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long value) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        overridePendingTransition(0, android.R.anim.fade_out);
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fll_ling.onDestroy();
    }
}
