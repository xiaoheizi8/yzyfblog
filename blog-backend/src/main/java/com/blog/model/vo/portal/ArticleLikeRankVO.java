package com.blog.model.vo.portal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞排行榜项。
 *
 * @author 一朝风月
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleLikeRankVO {

    private Long articleId;
    private String title;
    private Long likeCount;
    private Integer rank;
}

