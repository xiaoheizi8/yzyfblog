package cn.yzfy.blog.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Article;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.data.net.dto.Comment;
import cn.yzfy.blog.data.net.dto.User;
import cn.yzfy.blog.util.JsonUtil;
import cn.yzfy.blog.util.SwipeBackHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description 文章详情页：标题 + 元信息 + 正文 + 评论列表与发表评论。
 * @datetime 2026-03-19
 */
public class ArticleDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ARTICLE_ID = "article_id";

    private TextView tvTitle;
    private TextView tvMeta;
    private TextView tvContent;
    private ProgressBar progress;

    private Button btnLike;
    private Button btnFavorite;
    private Button btnTip;

    private RecyclerView rvComments;
    private CommentListAdapter commentAdapter;
    private EditText etComment;
    private Button btnSend;

    private long articleId;

    private boolean liked = false;
    private boolean favorited = false;
    private long likeCount = 0;

    private static final int MAX_MD_LEN = 100000;
    private static final Pattern IMG_MD_PATTERN = Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");

    private final List<CustomTarget<Drawable>> imageTargets = new ArrayList<>();

    private static class ImageSpanInfo {
        final int start;
        final int end;
        final String url;

        ImageSpanInfo(int start, int end, String url) {
            this.start = start;
            this.end = end;
            this.url = url;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 移动端“左滑返回”体验
        SwipeBackHelper.attach(this);

        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvContent = findViewById(R.id.tvContent);
        progress = findViewById(R.id.progress);

        btnLike = findViewById(R.id.btnLike);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnTip = findViewById(R.id.btnTip);

        btnLike.setOnClickListener(v -> toggleLike());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnTip.setOnClickListener(v -> showTipDialog());
        updateActionButtons();

        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        btnSend = findViewById(R.id.btnSend);

        commentAdapter = new CommentListAdapter();
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        btnSend.setOnClickListener(v -> sendComment());

        articleId = getIntent().getLongExtra(EXTRA_ARTICLE_ID, -1L);
        if (articleId <= 0) {
            Toast.makeText(this, "文章不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDetail();
        loadComments();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private BlogApi api() {
        String baseUrl = getString(R.string.api_base_url);
        return ApiClient.get(getApplicationContext(), baseUrl).create(BlogApi.class);
    }

    private void loadDetail() {
        setLoading(true);
        api().articleDetail(articleId).enqueue(new Callback<Result<Article>>() {
            @Override
            public void onResponse(Call<Result<Article>> call, Response<Result<Article>> response) {
                setLoading(false);
                Result<Article> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    Toast.makeText(ArticleDetailActivity.this, "加载文章失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                Article a = res.data;
                tvTitle.setText(a.title == null ? "" : a.title);
                String meta = "";
                if (a.publishTime != null) meta += a.publishTime;
                meta += " · 浏览 " + (a.viewCount == null ? 0 : a.viewCount);
                tvMeta.setText(meta);
                tvContent.setText(renderMarkdownWithImages(a.content));

                // 对齐 uniapp：加载点赞/收藏状态与数量
                loadLikeStatus();
                loadLikeCount();
                loadFavoriteStatus();
            }

            @Override
            public void onFailure(Call<Result<Article>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ArticleDetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private SpannableStringBuilder renderMarkdownWithImages(@Nullable String content) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (content == null) return sb;

        String md = content;
        if (md.length() > MAX_MD_LEN) {
            md = md.substring(0, MAX_MD_LEN);
        }

        // uniapp 端做过一定清理，这里再补一点，避免无效占位影响解析
        md = md.replace("![]([object Object])", "");
        md = md.replaceAll("!\\[\\]\\(\\[object Object\\]\\)", "");
        md = md.replaceAll("!\\[\\]\\(\\d+\\)", "");

        List<ImageSpanInfo> imageInfos = new ArrayList<>();

        Matcher matcher = IMG_MD_PATTERN.matcher(md);
        int lastIndex = 0;
        while (matcher.find()) {
            // 文本段
            int startIndex = matcher.start();
            if (startIndex > lastIndex) {
                sb.append(md, lastIndex, startIndex);
            }

            // 图片段：用占位符字符 + ImageSpan 承载
            String rawUrl = matcher.group(1);
            String url = normalizeImageUrl(rawUrl);

            int spanStart = sb.length();
            sb.append('\uFFFC'); // object replacement character
            int spanEnd = sb.length();

            Drawable placeholder = new ColorDrawable(Color.parseColor("#EEEEEE"));
            placeholder.setBounds(0, 0, 1, 1);
            ImageSpan imageSpan = new ImageSpan(placeholder, ImageSpan.ALIGN_BASELINE);
            sb.setSpan(imageSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            imageInfos.add(new ImageSpanInfo(spanStart, spanEnd, url));
            lastIndex = matcher.end();
        }

        // 尾部文本
        if (lastIndex < md.length()) {
            sb.append(md.substring(lastIndex));
        }

        // 异步加载真实图片，并替换到 ImageSpan
        if (!imageInfos.isEmpty()) {
            tvContent.post(() -> {
                int width = tvContent.getWidth();
                if (width <= 0) return;
                final int padding = tvContent.getPaddingLeft() + tvContent.getPaddingRight();
                final int targetW = Math.max(1, width - padding);

                // 避免重复 onCreate 导致旧 target 残留
                for (CustomTarget<Drawable> t : imageTargets) {
                    // Glide 清理由自身处理，先清列表
                }
                imageTargets.clear();

                for (ImageSpanInfo info : imageInfos) {
                    CustomTarget<Drawable> target = new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            if (resource == null) return;
                            int dw = resource.getIntrinsicWidth();
                            int dh = resource.getIntrinsicHeight();
                            if (dw <= 0 || dh <= 0) {
                                dw = targetW;
                                dh = targetW;
                            }
                            int targetH = (int) ((float) dh * (float) targetW / (float) dw);
                            targetH = Math.max(1, targetH);
                            resource.setBounds(0, 0, targetW, targetH);

                            ImageSpan newSpan = new ImageSpan(resource, ImageSpan.ALIGN_BASELINE);
                            // 避免重复 span，先移除旧的 ImageSpan
                            ImageSpan[] old = sb.getSpans(info.start, info.end, ImageSpan.class);
                            for (ImageSpan s : old) {
                                sb.removeSpan(s);
                            }
                            sb.setSpan(newSpan, info.start, info.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            // 重新 setText 触发刷新
                            tvContent.setText(sb);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                        }
                    };
                    imageTargets.add(target);
                    Glide.with(ArticleDetailActivity.this)
                            .asDrawable()
                            .load(info.url)
                            .into(target);
                }
            });
        }

        return sb;
    }

    private String normalizeImageUrl(String url) {
        if (url == null) return "";
        String u = url.trim();
        // 去掉可能存在的引号
        if (u.startsWith("\"") && u.endsWith("\"") && u.length() > 2) {
            u = u.substring(1, u.length() - 1);
        }
        if (u.startsWith("http://") || u.startsWith("https://")) {
            return u;
        }
        String base = getString(R.string.api_base_url);
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (u.startsWith("/")) return base + u;
        return base + "/" + u;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageTargets.clear();
    }

    private void updateActionButtons() {
        btnLike.setText((liked ? "已赞" : "点赞") + " (" + likeCount + ")");
        btnFavorite.setText(favorited ? "已收藏" : "收藏");
        btnTip.setText("打赏风月币");

        if (liked) {
            btnLike.setBackgroundResource(R.drawable.bg_action_button);
            btnLike.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1890FF")));
            btnLike.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            btnLike.setBackgroundResource(R.drawable.bg_action_button);
            btnLike.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            btnLike.setTextColor(getColor(R.color.brand_text_secondary));
        }

        if (favorited) {
            btnFavorite.setBackgroundResource(R.drawable.bg_action_button);
            btnFavorite.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6F4FF")));
            btnFavorite.setTextColor(Color.parseColor("#1890FF"));
        } else {
            btnFavorite.setBackgroundResource(R.drawable.bg_action_button);
            btnFavorite.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            btnFavorite.setTextColor(getColor(R.color.brand_primary));
        }

        btnTip.setBackgroundResource(R.drawable.bg_action_button);
        btnTip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
        btnTip.setTextColor(getColor(R.color.brand_primary));
    }

    private Long getCurrentUserId() {
        String json = SessionManager.getUserJson(getApplicationContext());
        if (TextUtils.isEmpty(json)) return null;
        try {
            User user = JsonUtil.fromJson(json, User.class);
            return user != null ? user.id : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void toggleLike() {
        if (SessionManager.getToken(getApplicationContext()) == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        boolean prevLiked = liked;
        Long userId = getCurrentUserId();
        Map<String, Object> body = new HashMap<>();
        if (userId != null) {
            body.put("userId", userId);
        }

        Call<Result<Map<String, Object>>> call = prevLiked
                ? api().articleUnlike(articleId, body)
                : api().articleLike(articleId, body);

        call.enqueue(new Callback<Result<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<Result<Map<String, Object>>> call, Response<Result<Map<String, Object>>> response) {
                Result<Map<String, Object>> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    Toast.makeText(ArticleDetailActivity.this,
                            res != null && !TextUtils.isEmpty(res.message) ? res.message : "点赞失败",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Object lcObj = res.data.get("likeCount");
                if (lcObj != null) {
                    try {
                        likeCount = Long.parseLong(String.valueOf(lcObj));
                    } catch (Exception ignored) {
                    }
                }

                liked = !prevLiked;
                updateActionButtons();
                Toast.makeText(ArticleDetailActivity.this, liked ? "点赞成功" : "已取消点赞", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Result<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(ArticleDetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavorite() {
        if (SessionManager.getToken(getApplicationContext()) == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        boolean prevFavorited = favorited;
        Map<String, Object> body = new HashMap<>();
        body.put("articleId", articleId);

        Call<Result<Void>> call = prevFavorited
                ? api().favoriteRemove(body)
                : api().favoriteAdd(body);

        call.enqueue(new Callback<Result<Void>>() {
            @Override
            public void onResponse(Call<Result<Void>> call, Response<Result<Void>> response) {
                Result<Void> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk()) {
                    Toast.makeText(ArticleDetailActivity.this,
                            res != null && !TextUtils.isEmpty(res.message) ? res.message : "操作失败",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                favorited = !prevFavorited;
                updateActionButtons();
                Toast.makeText(ArticleDetailActivity.this, favorited ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Result<Void>> call, Throwable t) {
                Toast.makeText(ArticleDetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTipDialog() {
        if (SessionManager.getToken(getApplicationContext()) == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("打赏风月币");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("输入风月币数量");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 8, 32, 8);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("确认打赏", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(this, "请输入有效金额", Toast.LENGTH_SHORT).show();
                return;
            }
            int amount;
            try {
                amount = Integer.parseInt(amountStr);
            } catch (Exception e) {
                Toast.makeText(this, "请输入有效金额", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount < 1) {
                Toast.makeText(this, "请输入有效金额", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amount);

            btnTip.setEnabled(false);
            api().tip(articleId, body).enqueue(new Callback<Result<Void>>() {
                @Override
                public void onResponse(Call<Result<Void>> call, Response<Result<Void>> response) {
                    btnTip.setEnabled(true);
                    Result<Void> res = response.body();
                    if (response.isSuccessful() && res != null && res.isOk()) {
                        Toast.makeText(ArticleDetailActivity.this, "打赏成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ArticleDetailActivity.this,
                                res != null && !TextUtils.isEmpty(res.message) ? res.message : "打赏失败",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Result<Void>> call, Throwable t) {
                    btnTip.setEnabled(true);
                    Toast.makeText(ArticleDetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void loadLikeStatus() {
        Long userId = getCurrentUserId();
        api().articleLiked(articleId, userId).enqueue(new Callback<Result<Boolean>>() {
            @Override
            public void onResponse(Call<Result<Boolean>> call, Response<Result<Boolean>> response) {
                Result<Boolean> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    return;
                }
                liked = Boolean.TRUE.equals(res.data);
                updateActionButtons();
            }

            @Override
            public void onFailure(Call<Result<Boolean>> call, Throwable t) {
            }
        });
    }

    private void loadLikeCount() {
        api().articleLikeCount(articleId).enqueue(new Callback<Result<Long>>() {
            @Override
            public void onResponse(Call<Result<Long>> call, Response<Result<Long>> response) {
                Result<Long> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    return;
                }
                likeCount = res.data;
                updateActionButtons();
            }

            @Override
            public void onFailure(Call<Result<Long>> call, Throwable t) {
            }
        });
    }

    private void loadFavoriteStatus() {
        api().favoriteCheck(articleId).enqueue(new Callback<Result<Boolean>>() {
            @Override
            public void onResponse(Call<Result<Boolean>> call, Response<Result<Boolean>> response) {
                Result<Boolean> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    return;
                }
                favorited = Boolean.TRUE.equals(res.data);
                updateActionButtons();
            }

            @Override
            public void onFailure(Call<Result<Boolean>> call, Throwable t) {
            }
        });
    }

    private void loadComments() {
        api().commentList(articleId).enqueue(new Callback<Result<List<Comment>>>() {
            @Override
            public void onResponse(Call<Result<List<Comment>>> call, Response<Result<List<Comment>>> response) {
                Result<List<Comment>> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    return;
                }
                commentAdapter.submit(res.data);
            }

            @Override
            public void onFailure(Call<Result<List<Comment>>> call, Throwable t) {
            }
        });
    }

    private void sendComment() {
        String content = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            return;
        }
        if (SessionManager.getToken(getApplicationContext()) == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("articleId", articleId);
        body.put("content", content);

        btnSend.setEnabled(false);
        api().commentSubmit(body).enqueue(new Callback<Result<Comment>>() {
            @Override
            public void onResponse(Call<Result<Comment>> call, Response<Result<Comment>> response) {
                btnSend.setEnabled(true);
                Result<Comment> res = response.body();
                if (response.isSuccessful() && res != null && res.isOk() && res.data != null) {
                    etComment.setText("");
                    commentAdapter.addComment(res.data);
                    rvComments.scrollToPosition(commentAdapter.getItemCount() - 1);
                } else {
                    Toast.makeText(ArticleDetailActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Comment>> call, Throwable t) {
                btnSend.setEnabled(true);
                Toast.makeText(ArticleDetailActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

