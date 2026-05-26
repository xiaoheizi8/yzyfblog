package cn.yzfy.blog.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Map;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.data.net.dto.User;
import cn.yzfy.blog.util.JsonUtil;
import com.bumptech.glide.Glide;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import java.io.InputStream;
import android.database.Cursor;
import android.provider.OpenableColumns;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description “我的”页面（对齐 uniapp：pages/profile/index），展示用户信息与钱包。
 * @datetime 2026-03-19
 */
public class ProfileFragment extends Fragment {

    private MaterialToolbar toolbar;
    private View avatarWrap;
    private ImageView avatarImg;
    private TextView avatarText;
    private TextView tvName;
    private TextView tvSubtitle;

    private View walletCard;
    private TextView tvBalance;
    private TextView tvIncomeExpense;
    private Button btnSignIn;

    private LinearLayout itemMyArticle;
    private LinearLayout itemFavorite;
    private LinearLayout itemEditProfile;
    private LinearLayout itemLogout;

    private static final int REQ_PICK_AVATAR = 2002;
    private boolean uploadingAvatar = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar);
        avatarWrap = view.findViewById(R.id.avatarWrap);
        avatarImg = view.findViewById(R.id.avatarImg);
        avatarText = view.findViewById(R.id.avatarText);
        tvName = view.findViewById(R.id.tvName);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);

        walletCard = view.findViewById(R.id.walletCard);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvIncomeExpense = view.findViewById(R.id.tvIncomeExpense);
        btnSignIn = view.findViewById(R.id.btnSignIn);

        itemMyArticle = view.findViewById(R.id.itemMyArticle);
        itemFavorite = view.findViewById(R.id.itemFavorite);
        itemEditProfile = view.findViewById(R.id.itemEditProfile);
        itemLogout = view.findViewById(R.id.itemLogout);

        toolbar.setTitle("我的");

        avatarWrap.setOnClickListener(v -> ensureLoginThen(this::pickAvatar));

        btnSignIn.setOnClickListener(v -> signIn());

        itemMyArticle.setOnClickListener(v -> ensureLoginThen(() ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new MyArticlesFragment())
                        .commit()
        ));
        itemFavorite.setOnClickListener(v -> ensureLoginThen(() ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new FavoriteFragment())
                        .commit()
        ));
        itemEditProfile.setOnClickListener(v -> ensureLoginThen(() -> {
            startActivity(new Intent(requireContext(), EditProfileActivity.class));
        }));
        itemLogout.setOnClickListener(v -> {
            Context ctx = requireContext().getApplicationContext();
            SessionManager.clear(ctx);
            Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
            bindUser(null);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUserAndWallet();
    }

    private void refreshUserAndWallet() {
        Context ctx = requireContext().getApplicationContext();
        String json = SessionManager.getUserJson(ctx);
        if (TextUtils.isEmpty(json)) {
            bindUser(null);
            return;
        }
        User user = null;
        try {
            user = JsonUtil.fromJson(json, User.class);
        } catch (Exception ignored) {
        }
        bindUser(user);
        if (SessionManager.getToken(ctx) != null) {
            loadWallet();
        } else {
            walletCard.setVisibility(View.GONE);
        }
    }

    private void bindUser(@Nullable User user) {
        if (user == null) {
            avatarText.setText("风");
            if (avatarImg != null) avatarImg.setVisibility(View.GONE);
            avatarText.setVisibility(View.VISIBLE);
            tvName.setText("未登录");
            tvSubtitle.setText("请先登录账号");
            walletCard.setVisibility(View.GONE);
            return;
        }
        String displayName = !TextUtils.isEmpty(user.nickname)
                ? user.nickname
                : (!TextUtils.isEmpty(user.username) ? user.username : "风月用户");
        tvName.setText(displayName);
        tvSubtitle.setText("点击头像可修改资料");

        String first = displayName.substring(0, 1);

        String avatar = user.avatar;
        if (avatar != null && !TextUtils.isEmpty(avatar)) {
            if (avatarImg != null) {
                avatarText.setVisibility(View.GONE);
                avatarImg.setVisibility(View.VISIBLE);
                Glide.with(requireContext().getApplicationContext())
                        .load(normalizeAvatarUrl(avatar))
                        .circleCrop()
                        .into(avatarImg);
            }
        } else {
            avatarText.setVisibility(View.VISIBLE);
            if (avatarImg != null) avatarImg.setVisibility(View.GONE);
            avatarText.setText(first);
        }
    }

    private String normalizeAvatarUrl(String avatar) {
        String u = avatar.trim();
        if (u.startsWith("http://") || u.startsWith("https://")) return u;
        String base = getString(R.string.api_base_url);
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (u.startsWith("/")) return base + u;
        return base + "/" + u;
    }

    private void pickAvatar() {
        if (uploadingAvatar) return;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择头像"), REQ_PICK_AVATAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK_AVATAR || resultCode != android.app.Activity.RESULT_OK || data == null) return;
        if (data.getData() == null) return;
        if (uploadingAvatar) return;

        Uri uri = data.getData();
        uploadingAvatar = true;
        Toast.makeText(requireContext(), "头像上传中...", Toast.LENGTH_SHORT).show();

        try {
            String baseUrl = getString(R.string.api_base_url);
            BlogApi api = ApiClient.get(requireContext().getApplicationContext(), baseUrl).create(BlogApi.class);
            MultipartBody.Part part = createImagePart(uri);
            api.uploadAvatar(part).enqueue(new Callback<Result<String>>() {
                @Override
                public void onResponse(@NonNull Call<Result<String>> call, @NonNull Response<Result<String>> response) {
                    uploadingAvatar = false;
                    Result<String> res = response.body();
                    if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                        Toast.makeText(requireContext(), "头像上传失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String url = res.data;
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("avatar", url);
                    api.updateProfile(body).enqueue(new Callback<Result<User>>() {
                        @Override
                        public void onResponse(@NonNull Call<Result<User>> call2, @NonNull Response<Result<User>> response2) {
                            Result<User> res2 = response2.body();
                            if (!response2.isSuccessful() || res2 == null || !res2.isOk() || res2.data == null) {
                                Toast.makeText(requireContext(), "资料保存失败", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            User updated = res2.data;
                            SessionManager.saveUserJson(requireContext().getApplicationContext(), JsonUtil.toJson(updated));
                            bindUser(updated);
                            Toast.makeText(requireContext(), "头像已更新", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<Result<User>> call2, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<Result<String>> call, @NonNull Throwable t) {
                    uploadingAvatar = false;
                    Toast.makeText(requireContext(), "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            uploadingAvatar = false;
            Toast.makeText(requireContext(), "头像处理失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private MultipartBody.Part createImagePart(Uri uri) throws java.io.IOException {
        String fileName = getFileName(uri);
        String mime = requireContext().getContentResolver().getType(uri);
        final MediaType mediaType = MediaType.get(mime != null ? mime : "image/*");

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws java.io.IOException {
                try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
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
            cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return cursor.getString(idx);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return "avatar_" + System.currentTimeMillis() + ".jpg";
    }

    private void loadWallet() {
        walletCard.setVisibility(View.VISIBLE);
        String baseUrl = getString(R.string.api_base_url);
        BlogApi api = ApiClient.get(requireContext().getApplicationContext(), baseUrl).create(BlogApi.class);
        api.wallet().enqueue(new Callback<Result<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Map<String, Object>>> call, @NonNull Response<Result<Map<String, Object>>> response) {
                Result<Map<String, Object>> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    walletCard.setVisibility(View.GONE);
                    return;
                }
                Map<String, Object> m = res.data;
                Object balance = m.get("balance");
                Object income = m.get("totalIncome");
                Object expense = m.get("totalExpense");
                tvBalance.setText(balance == null ? "0" : String.valueOf(balance));
                String ie = "累计收入：" + (income == null ? "0" : String.valueOf(income))
                        + " · 累计支出：" + (expense == null ? "0" : String.valueOf(expense));
                tvIncomeExpense.setText(ie);
            }

            @Override
            public void onFailure(@NonNull Call<Result<Map<String, Object>>> call, @NonNull Throwable t) {
                walletCard.setVisibility(View.GONE);
            }
        });
    }

    private void signIn() {
        ensureLoginThen(() -> {
            String baseUrl = getString(R.string.api_base_url);
            BlogApi api = ApiClient.get(requireContext().getApplicationContext(), baseUrl).create(BlogApi.class);
            api.signIn().enqueue(new Callback<Result<Void>>() {
                @Override
                public void onResponse(@NonNull Call<Result<Void>> call, @NonNull Response<Result<Void>> response) {
                    Result<Void> res = response.body();
                    if (response.isSuccessful() && res != null && res.isOk()) {
                        Toast.makeText(requireContext(), "签到成功", Toast.LENGTH_SHORT).show();
                        loadWallet();
                    } else {
                        Toast.makeText(requireContext(), "签到失败", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Result<Void>> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private interface LoginAction {
        void run();
    }

    private void ensureLoginThen(LoginAction action) {
        Context ctx = requireContext().getApplicationContext();
        if (SessionManager.getToken(ctx) == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            return;
        }
        action.run();
    }
}

