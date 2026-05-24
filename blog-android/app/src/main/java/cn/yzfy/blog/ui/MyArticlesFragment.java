package cn.yzfy.blog.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Collections;
import java.util.List;

import cn.yzfy.blog.R;
import cn.yzfy.blog.data.SessionManager;
import cn.yzfy.blog.data.net.ApiClient;
import cn.yzfy.blog.data.net.BlogApi;
import cn.yzfy.blog.data.net.dto.Article;
import cn.yzfy.blog.data.net.dto.PageResult;
import cn.yzfy.blog.data.net.dto.Result;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author 一朝风月
 * @description 我的文章列表（对齐 uniapp pages/article/my），展示当前用户发布的文章。
 * @datetime 2026-03-20
 */
public class MyArticlesFragment extends Fragment {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progress;
    private TextView empty;
    private MaterialToolbar toolbar;

    private ArticleListAdapter adapter;

    private long current = 1;
    private long size = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_articles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        progress = view.findViewById(R.id.progress);
        empty = view.findViewById(R.id.empty);
        toolbar = view.findViewById(R.id.toolbar);

        toolbar.setTitle("我的文章");

        adapter = new ArticleListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(this::load);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.surface_card);
        load();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (!loading) refreshLayout.setRefreshing(false);
    }

    private void load() {
        if (SessionManager.getToken(requireContext().getApplicationContext()) == null) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
            return;
        }

        setLoading(true);
        empty.setVisibility(View.GONE);

        String baseUrl = getString(R.string.api_base_url);
        BlogApi api = ApiClient.get(requireContext().getApplicationContext(), baseUrl).create(BlogApi.class);
        api.articleMy(current, size).enqueue(new Callback<Result<PageResult<Article>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<PageResult<Article>>> call, @NonNull Response<Result<PageResult<Article>>> response) {
                setLoading(false);
                Result<PageResult<Article>> res = response.body();
                if (!response.isSuccessful() || res == null || !res.isOk() || res.data == null) {
                    empty.setText("加载失败，请下拉重试");
                    empty.setVisibility(View.VISIBLE);
                    adapter.submit(Collections.emptyList());
                    return;
                }

                List<Article> list = res.data.records == null ? Collections.emptyList() : res.data.records;
                if (list.isEmpty()) {
                    empty.setText("暂无文章");
                    empty.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.GONE);
                }
                adapter.submit(list);
            }

            @Override
            public void onFailure(@NonNull Call<Result<PageResult<Article>>> call, @NonNull Throwable t) {
                setLoading(false);
                empty.setText("网络错误，请检查网络后重试");
                empty.setVisibility(View.VISIBLE);
            }
        });
    }
}

