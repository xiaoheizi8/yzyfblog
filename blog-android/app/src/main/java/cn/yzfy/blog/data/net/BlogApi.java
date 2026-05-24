package cn.yzfy.blog.data.net;

import cn.yzfy.blog.data.net.dto.Article;
import cn.yzfy.blog.data.net.dto.PageResult;
import cn.yzfy.blog.data.net.dto.Result;
import cn.yzfy.blog.data.net.dto.Comment;
import cn.yzfy.blog.data.net.dto.Tag;
import cn.yzfy.blog.data.net.dto.Message;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PUT;
import okhttp3.MultipartBody;

/**
 * @author 一朝风月
 * @description 后端 API 定义（对齐 uniapp 端 portal 接口）。
 * @datetime 2026-03-18
 */
public interface BlogApi {

    @GET("portal/article/list")
    Call<Result<PageResult<Article>>> articleList(
            @Query("current") long current,
            @Query("size") long size,
            @Query("categoryId") Long categoryId,
            @Query("tagId") Long tagId
    );

    @GET("portal/article/{id}")
    Call<Result<Article>> articleDetail(@Path("id") long id);

    /**
     * 登录（用户名+密码），返回 token 与 user。
     */
    @POST("portal/auth/login")
    Call<Result<Map<String, Object>>> login(@Body Map<String, String> body);

    /**
     * 注册（用户名+密码）。
     */
    @POST("portal/auth/register")
    Call<Result<Void>> register(@Body Map<String, String> body);

    /**
     * 风月币排行榜。
     */
    @GET("portal/coin/ranking")
    Call<Result<List<Map<String, Object>>>> coinRanking(@Query("limit") int limit);

    /**
     * 当前登录用户钱包。
     */
    @GET("portal/coin/wallet")
    Call<Result<Map<String, Object>>> wallet();

    /**
     * 每日签到领币（需要登录）。
     */
    @POST("portal/coin/sign-in")
    Call<Result<Void>> signIn();

    /**
     * 文章评论列表。
     */
    @GET("portal/comment/list")
    Call<Result<List<Comment>>> commentList(@Query("articleId") long articleId);

    /**
     * 发表评论。
     */
    @POST("portal/comment/submit")
    Call<Result<Comment>> commentSubmit(@Body Map<String, Object> body);

    /**
     * 当前登录用户的文章列表（对齐 uniapp：/pages/article/my）。
     */
    @GET("portal/article/my")
    Call<Result<PageResult<Article>>> articleMy(
            @Query("current") long current,
            @Query("size") long size
    );

    /**
     * 我的收藏列表（对齐 uniapp：/pages/favorite/list）。
     */
    @GET("portal/favorite/list")
    Call<Result<List<Article>>> favoriteList();

    /**
     * 文章：是否已点赞（可选传 userId）。
     */
    @GET("portal/article/{id}/liked")
    Call<Result<Boolean>> articleLiked(
            @Path("id") long id,
            @Query("userId") Long userId
    );

    /**
     * 文章：点赞数。
     */
    @GET("portal/article/{id}/like-count")
    Call<Result<Long>> articleLikeCount(@Path("id") long id);

    /**
     * 文章：点赞。
     */
    @POST("portal/article/{id}/like")
    Call<Result<Map<String, Object>>> articleLike(
            @Path("id") long id,
            @Body Map<String, Object> body
    );

    /**
     * 文章：取消点赞。
     */
    @POST("portal/article/{id}/unlike")
    Call<Result<Map<String, Object>>> articleUnlike(
            @Path("id") long id,
            @Body Map<String, Object> body
    );

    /**
     * 文章：是否已收藏。
     */
    @GET("portal/favorite/check")
    Call<Result<Boolean>> favoriteCheck(@Query("articleId") long articleId);

    /**
     * 收藏：添加。
     */
    @POST("portal/favorite/add")
    Call<Result<Void>> favoriteAdd(@Body Map<String, Object> body);

    /**
     * 收藏：取消。
     */
    @POST("portal/favorite/remove")
    Call<Result<Void>> favoriteRemove(@Body Map<String, Object> body);

    /**
     * 打赏风月币。
     */
    @POST("portal/coin/article/{articleId}/tip")
    Call<Result<Void>> tip(
            @Path("articleId") long articleId,
            @Body Map<String, Object> body
    );

    /**
     * 标签列表，用于文章发布。
     */
    @GET("portal/tag/list")
    Call<Result<List<Tag>>> tagList();

    /**
     * 上传图片（文章图片、封面图等）。
     */
    // 实际上传时请传入 MultipartBody.Part，name 固定为 "file"（和后端参数一致）。
    @Multipart
    @POST("portal/upload/image")
    Call<Result<String>> uploadImage(@Part okhttp3.MultipartBody.Part file);

    /**
     * 上传头像图片（portal/auth -> avatar）。
     */
    @Multipart
    @POST("portal/upload/avatar")
    Call<Result<String>> uploadAvatar(@Part MultipartBody.Part file);

    /**
     * 获取当前登录用户个人资料（编辑资料页）。
     */
    @GET("portal/auth/profile")
    Call<Result<cn.yzfy.blog.data.net.dto.User>> profile();

    /**
     * 修改个人资料（昵称、头像、邮箱、手机号等）。
     */
    @PUT("portal/auth/profile")
    Call<Result<cn.yzfy.blog.data.net.dto.User>> updateProfile(@Body Map<String, Object> body);

    /**
     * 发布文章（对齐 uniapp pages/article/edit.vue）。
     */
    @POST("portal/article")
    Call<Result<Article>> publishArticle(@Body Map<String, Object> body);

    /**
     * 留言列表（弹幕墙）。
     */
    @GET("portal/message/list")
    Call<Result<List<Message>>> messageList(@Query("limit") int limit);

    /**
     * 提交留言。
     */
    @POST("portal/message/submit")
    Call<Result<Message>> messageSubmit(@Body Map<String, Object> body);
}


