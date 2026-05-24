package cn.yzfy.blog.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.net.dto.Message;

/**
 * @author 一朝风月
 * @description 弹幕视图 - 实现留言从右向左滚动的动画效果
 */
public class DanmakuView extends FrameLayout {

    private static final int[] DANMAKU_COLORS = {
            0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D, 0xFF95E1D3, 0xFFF38181, 0xFFAA96DA
    };

    private static final int[] DANMAKU_TEXT_COLORS = {
            0xFFFFFFFF, 0xFF2C3E50, 0xFF2C3E50, 0xFFFFFFFF, 0xFF2C3E50, 0xFF2C3E50
    };

    private static final int MAX_DANMAKU = 5;
    private static final long DURATION = 4000L;
    private static final long INTERVAL = 1500L;

    private final Random random = new Random();
    private final List<View> activeDanmaku = new ArrayList<>();
    private final List<Message> pendingMessages = new ArrayList<>();
    private long lastAddTime = 0;

    public DanmakuView(@NonNull Context context) {
        super(context);
        init();
    }

    public DanmakuView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DanmakuView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void addMessage(Message message) {
        if (message == null || message.content == null || message.content.isEmpty()) return;

        if (activeDanmaku.size() >= MAX_DANMAKU) {
            pendingMessages.add(message);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastAddTime < INTERVAL && activeDanmaku.size() > 0) {
            pendingMessages.add(message);
            postDelayed(() -> addMessage(message), INTERVAL);
            return;
        }

        lastAddTime = now;
        showDanmaku(message);
    }

    public void addMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) return;
        for (Message msg : messages) {
            addMessage(msg);
        }
    }

    private void showDanmaku(Message message) {
        int colorIndex = random.nextInt(DANMAKU_COLORS.length);
        int bgColor = DANMAKU_COLORS[colorIndex];
        int textColor = DANMAKU_TEXT_COLORS[colorIndex];

        TextView tv = new TextView(getContext());
        tv.setText(message.content);
        tv.setTextSize(14);
        tv.setTextColor(textColor);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(12), dp(6), dp(12), dp(6));

        tv.measure(0, 0);
        int width = tv.getMeasuredWidth();
        int height = dp(36);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                width > 0 ? width : dp(150), height);
        params.gravity = Gravity.CENTER_VERTICAL;

        int parentWidth = getWidth();
        if (parentWidth == 0) parentWidth = getResources().getDisplayMetrics().widthPixels;
        params.leftMargin = parentWidth;

        tv.setLayoutParams(params);
        tv.setBackgroundColor(bgColor);
        tv.setAlpha(0.9f);

        addView(tv);
        activeDanmaku.add(tv);

        int finalParentWidth = parentWidth;
        ValueAnimator animator = ValueAnimator.ofInt(parentWidth, -width - dp(20));
        animator.setDuration(DURATION + random.nextInt(1000));
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            tv.setTranslationX(value - finalParentWidth);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeView(tv);
                activeDanmaku.remove(tv);
                if (!pendingMessages.isEmpty()) {
                    Message next = pendingMessages.remove(0);
                    post(() -> addMessage(next));
                }
            }
        });

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(tv, "alpha", 0f, 0.9f);
        fadeIn.setDuration(200);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(fadeIn, animator);
        set.start();
    }

    private int dp(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void clear() {
        for (View v : activeDanmaku) {
            v.clearAnimation();
            if (v.getParent() != null) {
                removeView(v);
            }
        }
        activeDanmaku.clear();
        pendingMessages.clear();
    }
}
