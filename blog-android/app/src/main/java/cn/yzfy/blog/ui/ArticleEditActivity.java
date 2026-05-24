package cn.yzfy.blog.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Article;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.data.net.dto.Tag;
import cn.yzfy.blog.util.SwipeBackHelper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description 发布文章页（对齐 uniapp pages/article/edit.vue），支持标题、摘要、内容、标签、图片上传。
 * @datetime 2026-03-19
 */
public class ArticleEditActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGES = 1001;
    private static final int MAX_IMAGES = 9;

    private TextInputEditText etTitle;
    private TextInputEditText etSummary;
    private TextInputEditText etContent;

    private TextView tvSelectedTags;
    private Button btnSelectTags;

    private RecyclerView rvImages;
    private TextView tvImageHint;
    private Button btnChooseImages;
    private ProgressBar progress;
    private Button btnPublish;

    private final List<Tag> tags = new ArrayList<>();
    private final List<Long> selectedTagIds = new ArrayList<>();
    private final List<String> newTags = new ArrayList<>();

    private final List<Uri> imageUris = new ArrayList<>();
    private final List<String> imageRemoteUrls = new ArrayList<>();

    private SelectedImageAdapter imageAdapter;
    private boolean uploadingImages = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_edit);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // 移动端“左滑返回”体验
        SwipeBackHelper.attach(this);

        etTitle = findViewById(R.id.etTitle);
        etSummary = findViewById(R.id.etSummary);
        etContent = findViewById(R.id.etContent);

        tvSelectedTags = findViewById(R.id.tvSelectedTags);
        btnSelectTags = findViewById(R.id.btnSelectTags);

        rvImages = findViewById(R.id.rvImages);
        tvImageHint = findViewById(R.id.tvImageHint);
        btnChooseImages = findViewById(R.id.btnChooseImages);
        progress = findViewById(R.id.progress);
        btnPublish = findViewById(R.id.btnPublish);

        rvImages.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new SelectedImageAdapter(imageUris, position -> {
            if (position < 0 || position >= imageUris.size()) return;
            if (uploadingImages) {
                Toast.makeText(this, "图片上传中，请稍后再删除", Toast.LENGTH_SHORT).show();
                return;
            }
            imageUris.remove(position);
            imageRemoteUrls.remove(position);
            updateImageHint();
            updateTagSelectedText();
            imageAdapter.refresh();
        });
        rvImages.setAdapter(imageAdapter);

        btnSelectTags.setOnClickListener(v -> openTagDialog());
        btnChooseImages.setOnClickListener(v -> chooseImages());
        btnPublish.setOnClickListener(v -> doPublish());

        if (SessionManager.getToken(getApplicationContext()) == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvSelectedTags.setText("请选择标签");
        updateImageHint();
        updateTagSelectedText();
        loadTags();
    }

    private BlogApi api() {
        String baseUrl = getString(R.string.api_base_url);
        return ApiClient.get(getApplicationContext(), baseUrl).create(BlogApi.class);
    }

    private void loadTags() {
        progress.setVisibility(View.VISIBLE);
        api().tagList().enqueue(new Callback<Result<List<Tag>>>() {
            @Override
            public void onResponse(Call<Result<List<Tag>>> call, Response<Result<List<Tag>>> response) {
                progress.setVisibility(View.GONE);
                Result<List<Tag>> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    Toast.makeText(ArticleEditActivity.this, "标签加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                tags.clear();
                tags.addAll(res.data);
            }

            @Override
            public void onFailure(Call<Result<List<Tag>>> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(ArticleEditActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImageHint() {
        tvImageHint.setText("已选 " + imageUris.size() + "/" + MAX_IMAGES);
    }

    private void updateTagSelectedText() {
        List<String> names = new ArrayList<>();
        for (Long id : selectedTagIds) {
            for (Tag t : tags) {
                if (t != null && t.id != null && t.id.equals(id)) {
                    if (!TextUtils.isEmpty(t.name)) names.add(t.name);
                    break;
                }
            }
        }
        // 新增标签直接展示在末尾
        for (String n : newTags) {
            if (!TextUtils.isEmpty(n)) names.add(n);
        }
        if (names.isEmpty()) {
            tvSelectedTags.setText("请选择标签");
        } else {
            // uniapp 是 “、” 拼接
            tvSelectedTags.setText(joinWithSeparator(names, "、"));
        }
    }

    private String joinWithSeparator(List<String> items, String sep) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            String s = items.get(i);
            if (TextUtils.isEmpty(s)) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(s);
        }
        return sb.toString();
    }

    private void openTagDialog() {
        List<Long> tempSelected = new ArrayList<>(selectedTagIds);
        List<String> tempNewTags = new ArrayList<>(newTags);

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 24);

        // 一、已有标签（可多选）
        androidx.appcompat.widget.AppCompatTextView lblExist = new androidx.appcompat.widget.AppCompatTextView(this);
        lblExist.setText("已有标签");
        lblExist.setTextSize(14);
        lblExist.setTextColor(ContextCompat.getColor(this, R.color.brand_text_primary));
        lblExist.setPadding(0, 0, 0, 12);
        container.addView(lblExist);

        Map<Long, CheckBox> idToCheckBox = new HashMap<>();
        List<CheckBox> checkBoxes = new ArrayList<>();

        if (tags.isEmpty()) {
            androidx.appcompat.widget.AppCompatTextView tip = new androidx.appcompat.widget.AppCompatTextView(this);
            tip.setText("暂无已有标签，可在下方添加自定义标签");
            tip.setTextSize(13);
            tip.setTextColor(ContextCompat.getColor(this, R.color.brand_text_secondary));
            tip.setPadding(0, 0, 0, 16);
            container.addView(tip);
        } else {
            for (Tag t : tags) {
                if (t == null) continue;
                CheckBox cb = new CheckBox(this);
                cb.setText(t.name != null ? t.name : "");
                boolean checked = false;
                if (t.id != null) {
                    for (Long sid : tempSelected) {
                        if (sid != null && sid.equals(t.id)) {
                            checked = true;
                            break;
                        }
                    }
                }
                cb.setChecked(checked);
                container.addView(cb);
                checkBoxes.add(cb);
                idToCheckBox.put(t.id, cb);
            }
        }

        // 二、自定义新标签
        androidx.appcompat.widget.AppCompatTextView lblNew = new androidx.appcompat.widget.AppCompatTextView(this);
        lblNew.setText("自定义标签");
        lblNew.setTextSize(14);
        lblNew.setTextColor(ContextCompat.getColor(this, R.color.brand_text_primary));
        lblNew.setPadding(0, 20, 0, 8);
        container.addView(lblNew);

        EditText etNewTag = new EditText(this);
        etNewTag.setHint("输入新标签名称，点击添加");
        etNewTag.setSingleLine(true);
        etNewTag.setPadding(32, 24, 32, 24);
        etNewTag.setBackgroundResource(R.drawable.bg_comment_input);

        Button btnAddNewTag = new Button(this);
        btnAddNewTag.setText("添加");
        btnAddNewTag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.brand_primary)));
        btnAddNewTag.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        android.widget.LinearLayout newRow = new android.widget.LinearLayout(this);
        newRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        newRow.setPadding(0, 0, 0, 12);
        android.widget.LinearLayout.LayoutParams etParams =
                new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        etParams.setMarginEnd(12);
        newRow.addView(etNewTag, etParams);
        newRow.addView(btnAddNewTag, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        container.addView(newRow);

        // 已选自定义标签展示（可点击移除）
        android.widget.LinearLayout newTagsWrap = new android.widget.LinearLayout(this);
        newTagsWrap.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        newTagsWrap.setPadding(0, 0, 0, 8);
        androidx.appcompat.widget.AppCompatTextView tvNewTags = new androidx.appcompat.widget.AppCompatTextView(this);
        tvNewTags.setTextSize(13);
        tvNewTags.setTextColor(ContextCompat.getColor(this, R.color.brand_text_secondary));
        newTagsWrap.addView(tvNewTags);

        Runnable refreshNewTagsDisplay = () -> {
            if (tempNewTags.isEmpty()) {
                tvNewTags.setText("（未添加）");
            } else {
                tvNewTags.setText(joinWithSeparator(tempNewTags, "、"));
            }
        };
        refreshNewTagsDisplay.run();
        container.addView(newTagsWrap);

        btnAddNewTag.setOnClickListener(v -> {
            String name = etNewTag.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "请输入标签名称", Toast.LENGTH_SHORT).show();
                return;
            }

            // 如果已有同名标签，则直接选中
            Long existId = null;
            for (Tag t : tags) {
                if (t != null && t.name != null && t.name.equalsIgnoreCase(name) && t.id != null) {
                    existId = t.id;
                    break;
                }
            }
            if (existId != null) {
                if (!tempSelected.contains(existId)) tempSelected.add(existId);
                CheckBox existCb = idToCheckBox.get(existId);
                if (existCb != null) existCb.setChecked(true);
                tempNewTags.remove(name);
                Toast.makeText(this, "已勾选已有标签：" + name, Toast.LENGTH_SHORT).show();
            } else {
                String normalized = name.trim();
                if (!tempNewTags.contains(normalized)) {
                    tempNewTags.add(normalized);
                    Toast.makeText(this, "已添加自定义标签", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "该标签已添加", Toast.LENGTH_SHORT).show();
                }
            }
            etNewTag.setText("");
            refreshNewTagsDisplay.run();
        });

        scroll.addView(container);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择或添加标签");
        builder.setView(scroll);
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", (dialog, which) -> {
            selectedTagIds.clear();
            for (int i = 0; i < tags.size(); i++) {
                Tag t = tags.get(i);
                if (t == null || t.id == null) continue;
                CheckBox cb = checkBoxes.get(i);
                if (cb != null && cb.isChecked()) {
                    selectedTagIds.add(t.id);
                }
            }
            newTags.clear();
            newTags.addAll(tempNewTags);
            updateTagSelectedText();
        });
        builder.show();
    }

    private void chooseImages() {
        int remain = MAX_IMAGES - imageUris.size();
        if (remain <= 0) {
            Toast.makeText(this, "最多选择 9 张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQ_PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_IMAGES || resultCode != RESULT_OK || data == null) return;

        if (uploadingImages) {
            Toast.makeText(this, "图片上传中，请稍后再选", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Uri> picked = new ArrayList<>();
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                picked.add(data.getClipData().getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            picked.add(data.getData());
        }

        int remain = MAX_IMAGES - imageUris.size();
        for (Uri uri : picked) {
            if (imageUris.size() >= MAX_IMAGES) break;
            imageUris.add(uri);
            imageRemoteUrls.add(null);
            remain--;
            if (remain <= 0) break;
        }
        updateImageHint();
        imageAdapter.refresh();

        if (imageUris.isEmpty()) return;

        uploadingImages = true;
        btnChooseImages.setEnabled(false);
        btnPublish.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        uploadImagesSequential(0);
    }

    private void uploadImagesSequential(int index) {
        if (index >= imageUris.size()) {
            uploadingImages = false;
            btnChooseImages.setEnabled(true);
            btnPublish.setEnabled(true);
            progress.setVisibility(View.GONE);
            return;
        }

        if (imageRemoteUrls.get(index) != null) {
            uploadImagesSequential(index + 1);
            return;
        }

        Uri uri = imageUris.get(index);
        MultipartBody.Part part;
        try {
            part = createImagePart(uri);
        } catch (Exception e) {
            Toast.makeText(this, "图片处理失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            imageRemoteUrls.set(index, null);
            uploadImagesSequential(index + 1);
            return;
        }

        api().uploadImage(part).enqueue(new Callback<Result<String>>() {
            @Override
            public void onResponse(Call<Result<String>> call, Response<Result<String>> response) {
                Result<String> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    Toast.makeText(ArticleEditActivity.this, "图片上传失败", Toast.LENGTH_SHORT).show();
                    imageRemoteUrls.set(index, null);
                    uploadImagesSequential(index + 1);
                    return;
                }
                imageRemoteUrls.set(index, normalizeUploadUrl(res.data));
                uploadImagesSequential(index + 1);
            }

            @Override
            public void onFailure(Call<Result<String>> call, Throwable t) {
                Toast.makeText(ArticleEditActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                imageRemoteUrls.set(index, null);
                uploadImagesSequential(index + 1);
            }
        });
    }

    private String normalizeUploadUrl(String url) {
        if (url == null) return null;
        url = url.replace("`", "").replace("[", "").replace("]", "").replace(" ", "").trim();
        if (url.startsWith("http")) return url;
        String base = getString(R.string.api_base_url);
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        String path = url.startsWith("/") ? url : "/" + url;
        return base + path;
    }

    private MultipartBody.Part createImagePart(Uri uri) throws IOException {
        String fileName = getFileName(uri);
        String mime = getContentResolver().getType(uri);
        final MediaType mediaType = MediaType.get(mime != null ? mime : "image/*");
        final Uri finalUri = uri;

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream inputStream = getContentResolver().openInputStream(finalUri)) {
                    if (inputStream == null) return;
                    Source source = Okio.source(inputStream);
                    sink.writeAll(source);
                }
            }
        };
        return MultipartBody.Part.createFormData("file", fileName, requestBody);
    }

    private String getFileName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    String name = cursor.getString(idx);
                    if (name != null) {
                        name = name.replaceAll("[（）()\\[\\]]", "_");
                    }
                    return name;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return "image_" + System.currentTimeMillis() + ".jpg";
    }

    private void doPublish() {
        if (uploadingImages) {
            Toast.makeText(this, "图片上传中，请稍后再发布", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
        String summary = etSummary.getText() == null ? "" : etSummary.getText().toString().trim();
        String content = etContent.getText() == null ? "" : etContent.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content.trim())) {
            Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果选择了图片，则必须全部上传完成后才能发布
        if (!imageUris.isEmpty()) {
            for (String u : imageRemoteUrls) {
                if (TextUtils.isEmpty(u)) {
                    Toast.makeText(this, "有图片尚未上传完成", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        List<String> validUrls = new ArrayList<>();
        for (String u : imageRemoteUrls) {
            if (!TextUtils.isEmpty(u)) validUrls.add(u);
        }
        if (!validUrls.isEmpty()) {
            StringBuilder md = new StringBuilder();
            for (String u : validUrls) {
                md.append("![](").append(u).append(")\n");
            }
            content = content + "\n\n" + md.toString().trim();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("summary", TextUtils.isEmpty(summary) ? null : summary);
        body.put("content", content);
        body.put("tagIds", new ArrayList<>(selectedTagIds));
        body.put("newTags", new ArrayList<>(newTags));

        btnPublish.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        api().publishArticle(body).enqueue(new Callback<Result<Article>>() {
            @Override
            public void onResponse(Call<Result<Article>> call, Response<Result<Article>> response) {
                btnPublish.setEnabled(true);
                progress.setVisibility(View.GONE);
                Result<Article> res = response.body();
                if (response.isSuccessful() && res != null && res.isOk()) {
                    Toast.makeText(ArticleEditActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ArticleEditActivity.this,
                            res != null && !TextUtils.isEmpty(res.message) ? res.message : "发布失败",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<Article>> call, Throwable t) {
                btnPublish.setEnabled(true);
                progress.setVisibility(View.GONE);
                Toast.makeText(ArticleEditActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

