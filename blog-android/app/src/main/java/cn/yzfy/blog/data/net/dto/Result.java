package cn.yzfy.blog.data.net.dto;

import androidx.annotation.Nullable;

/**
 * @author 一朝风月
 * @description 统一接口返回包装（与后端 Result<T> 对齐）。
 * @datetime 2026-03-18
 */
public class Result<T> {
    public Integer code;
    public String message;
    @Nullable
    public T data;

    /**
     * 是否请求成功（兼容 code=200 / code=0 / code=null 的场景）。
     */
    public boolean isOk() {
        // 兼容常见返回：code==200 或 code==0；若后端没返回 code，则以 data != null 作为弱判断
        if (code == null) return true;
        return code == 200 || code == 0;
    }
}

