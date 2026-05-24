package cn.yzfy.blog.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;

import cn.yzfy.blog.R;
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
 * @description 文章列表页（对齐 uniapp：pages/article/list），支持下拉刷新与分页扩展。
 * @datetime 2026-03-18
 */
public class ArticlesFragment extends Fragment {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progress;
    private TextView empty;
    private MaterialToolbar toolbar;

    private ArticleListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_articles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
        recyclerView = view.findViewById(R.id.recyclerView);
        progress = view.findViewById(R.id.progress);
        empty = view.findViewById(R.id.empty);
        toolbar = view.findViewById(R.id.toolbar);

        adapter = new ArticleListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(this::load);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);
        refreshLayout.setProgressBackgroundColorSchemeResource(R.color.surface_card);

        toolbar.setTitle("文章");
        load();
    }

    /**
     * 控制加载态展示。
     */
    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (!loading) refreshLayout.setRefreshing(false);
    }

    /**
     * 拉取文章列表（当前使用第一页，后续会支持分页与上拉加载）。
     */
    private void load() {
        setLoading(true);
        empty.setVisibility(View.GONE);

        String baseUrl = getString(R.string.api_base_url);
        BlogApi api = ApiClient.get(requireContext().getApplicationContext(), baseUrl).create(BlogApi.class);
        api.articleList(1, 10, null, null).enqueue(new Callback<Result<PageResult<Article>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<PageResult<Article>>> call, @NonNull Response<Result<PageResult<Article>>> response) {
                setLoading(false);
                Result<PageResult<Article>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isOk() || body.data == null) {
                    empty.setText("加载失败，请下拉重试");
                    empty.setVisibility(View.VISIBLE);
                    return;
                }
                if (body.data.records == null || body.data.records.isEmpty()) {
                    adapter.submit(java.util.Collections.emptyList());
                    empty.setText("暂无文章");
                    empty.setVisibility(View.VISIBLE);
                    return;
                }
                adapter.submit(body.data.records);
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

