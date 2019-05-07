package cn.ysk521.gitbook.service;

import cn.ysk521.gitbook.dto.BuyArticleResult;
import cn.ysk521.gitbook.utils.PropertiesUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @description: service
 * @author: yangshangkun
 * @Date: 2019-05-06 18:11
 **/
@Service
@Slf4j
public class MainService {
    public String parse(String url) {
        String cookie = (String) PropertiesUtil.get("cookie");
        Document readArticleDocument, articleDocument;
        String result = "解析失败,请联系管理员！";
        try {
            readArticleDocument = Jsoup.connect(url).header("Cookie", cookie).get();

//            https://gitbook.cn/m/mazi/vip/order/activity
//            activityId: "5ccea3c67d046e5bb05dbe33"
//            requestUrl: "https://gitbook.cn/gitchat/activity/5ccea3c67d046e5bb05dbe33"
//            sceneId: ""

//            activityOrderPayBtn

            //判断文章是否已经买过，没买过的话，先买文章
            Elements activityOrderPayBtn = readArticleDocument.select("#activityOrderPayBtn");

            if (activityOrderPayBtn != null) {
                if (buyArticle(url, cookie)) {
                    readArticleDocument = Jsoup.connect(url).header("Cookie", cookie).get();
                } else {
                    return "文章购买失败！";
                }
            }


            Elements activityOrderBtn = readArticleDocument.select("#activityOrderBtn");

            Elements alreadyActivityOrderBtn = readArticleDocument.select("#alreadyActivityOrderBtn");

            if (!readArticleDocument.toString().contains("阅读文章")) {
                return "文章尚未发布或无法阅读,请选择其他chat！";
            }


            String text = activityOrderBtn.attr("href");

            System.out.println(text);

            String articleUrl = "https://gitbook.cn" + text;

            System.out.println(articleUrl);

            articleDocument = Jsoup.connect(articleUrl).header("Cookie", cookie).get();

            result = articleDocument
                    //是否只选择文章内容
//                  .select("#article_content")
                    .toString();

//          #profileImg
            String avatar = articleDocument.select("#profileImg").attr("src");

            //替换资源
            result = result.replaceAll("src=\"/", "src=\"https://gitbook.cn");
            result = result.replaceAll("href=\"/", "href=\"https:////gitbook.cn/");
            //替换头像
            result = result.replaceAll(avatar, "https://images.gitbook.cn/FnzjNghzs_ktFPUKeLaFX38rbNsL");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }


    public boolean buyArticle(String url, String cookie) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", cookie);
        requestHeaders.add("Content-Type", "application/json; charset=UTF-8");
        JSONObject postData = new JSONObject();
        String[] urlSplit = url.split("/");
        postData.put("activityId", urlSplit[urlSplit.length - 1]);
        postData.put("requestUrl", url);
        postData.put("sceneId", "");
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(postData), requestHeaders);
        String buyUrl = "https://gitbook.cn/m/mazi/vip/order/activity";

        log.info("url: {}, activityId: {}, requestUrl: {}", url, urlSplit[urlSplit.length - 1], url);
        ResponseEntity<String> response = restTemplate.postForEntity(buyUrl, requestEntity, String.class);
        String resultString = response.getBody();
        System.out.println("resultString " + resultString);
        BuyArticleResult result = JSONObject.parseObject(resultString, BuyArticleResult.class);
        log.info("文章购买结果 result" + result);
        if ("success".equals(result.getMessage())) {
            return true;
        }
        return false;
    }


}
