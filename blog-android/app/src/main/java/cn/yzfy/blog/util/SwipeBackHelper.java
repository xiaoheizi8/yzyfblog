package cn.yzfy.blog.util;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author 一朝风月
 * @description 左侧边缘向右滑动返回上一级（轻量实现）。仅在从屏幕左侧边缘触发且水平拖拽明显时拦截手势，拖拽过程中移动 contentView，松手后根据距离触发 finish 或回弹。
 * @datetime 2026-03-19
 */
public final class SwipeBackHelper implements View.OnTouchListener {

    private final Activity activity;
    private final View contentView;

    private final int edgeSizePx;
    private final int triggerDistancePx;

    private float downX;
    private float downY;
    private boolean tracking;

    private SwipeBackHelper(Activity activity, View contentView) {
        this.activity = activity;
        this.contentView = contentView;

        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        float density = dm.density;
        this.edgeSizePx = (int) (28 * density); // 约 28dp
        this.triggerDistancePx = (int) (120 * density); // 约 120dp
    }

    public static void attach(Activity activity) {
        if (activity == null) return;
        View root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        View content = root;
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) root;
            if (vg.getChildCount() > 0) {
                content = vg.getChildAt(0);
            }
        }
        content.setOnTouchListener(new SwipeBackHelper(activity, content));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (activity == null || contentView == null) return false;
        if (activity.isFinishing()) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                tracking = downX <= edgeSizePx;
                if (tracking) {
                    // 重置状态，避免上一次异常留下 translation
                    contentView.setTranslationX(0);
                }
                return tracking;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!tracking) return false;

                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (dx < 0) {
                    // 向左滑不处理
                    return true;
                }

                // 如果明显上下拖动，认为是正常滚动，不触发返回
                if (Math.abs(dy) > Math.abs(dx) * 1.2f) {
                    return false;
                }

                float clamped = Math.min(dx, contentView.getWidth() * 0.35f);
                contentView.setTranslationX(clamped);
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (!tracking) return false;

                float dx = event.getX() - downX;
                if (dx > triggerDistancePx) {
                    contentView.animate().translationX(contentView.getWidth()).setDuration(150).withEndAction(() -> {
                        activity.overridePendingTransition(0, 0);
                        activity.finish();
                    });
                } else {
                    contentView.animate().translationX(0).setDuration(180).start();
                }
                tracking = false;
                return true;
            }
        }
        return false;
    }
}

