package cn.ysk521.gitbook.controller;

import cn.ysk521.gitbook.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


    @RequestMapping("/")
    public String get(@RequestParam String url) {
        log.info("url " + url);
        String result = mainService.parse(url);
        return result;
    }
}
