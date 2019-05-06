package cn.ysk521.gitbook.dao;
import cn.ysk521.gitbook.model.Cookie;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CookieMapper {
    int insert(Cookie record);

    int insertSelective(Cookie record);

    List<String> findCookie();

}