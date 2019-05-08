package cn.ysk521.gitbook.controller;

import cn.ysk521.gitbook.service.MainService;
import cn.ysk521.gitbook.utils.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @description: controller
 * @author: yangshangkun
 * @Date: 2019-05-06 18:09
 **/
@RestController
@Slf4j
public class MainController {

    @Autowired
    private MainService mainService;
    private static String tongji = "<script>\n" +
            "var _hmt = _hmt || [];\n" +
            "(function() {\n" +
            "  var hm = document.createElement(\"script\");\n" +
            "  hm.src = \"https://hm.baidu.com/hm.js?9dbc1682c689caf53479913e2cb88278\";\n" +
            "  var s = document.getElementsByTagName(\"script\")[0]; \n" +
            "  s.parentNode.insertBefore(hm, s);\n" +
            "})();\n" +
            "</script>\n";


    @RequestMapping(value = "/")
    public String get(@RequestParam(required = false) String url) {
        String hello = PropertiesUtil.get("hello").toString();
        String result = "";
        if (url == null) {
            result = "";
        } else {
            log.info("url " + url);
            Future<String> fResult = mainService.parse(url);
            try {
                result = fResult.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        result = result + tongji + hello;

        return result;
    }

    @RequestMapping("gitchat/column/{courseId}/topic/{courseTopic}")
    public String getCourseColumn(@PathVariable String courseId, @PathVariable String courseTopic) {
        String url = String.format("https://gitbook.cn/gitchat/column/%s/topic/%s", courseId, courseTopic);
        //拼装url,调用上面的方法；
        return get(url);
    }
}
