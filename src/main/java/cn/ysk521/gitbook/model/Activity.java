package cn.ysk521.gitbook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 1
 * @author: yangshangkun
 * @Date: 2019-05-08 11:29
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    private String activityId;
    private String content;
}
