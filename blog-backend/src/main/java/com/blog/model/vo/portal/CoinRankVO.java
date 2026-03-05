package com.blog.model.vo.portal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风月币排行榜项。
 *
 * <p>用于前后端展示用户风月币排行信息。
 *
 * @author 一朝风月
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinRankVO {

    private Long userId;
    private String nickname;
    private String avatar;
    private Long coin;
    private Integer rank;
}

