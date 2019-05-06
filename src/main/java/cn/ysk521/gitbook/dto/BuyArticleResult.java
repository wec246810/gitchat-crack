package cn.ysk521.gitbook.dto;

import lombok.Data;

/**
 * @description: 购买文章的结果
 * @author: yangshangkun
 * @Date: 2019-05-06 19:09
 **/
@Data
public class BuyArticleResult {

    /**
     * code : 0
     * message : success
     */

    private int code;
    private String message;

}
