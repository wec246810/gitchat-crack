package cn.ysk521.gitbook.service;

import cn.ysk521.gitbook.dao.ActivityMapper;
import cn.ysk521.gitbook.dao.GitchatColumnMapper;
import cn.ysk521.gitbook.dto.BuyArticleResult;
import cn.ysk521.gitbook.model.Activity;
import cn.ysk521.gitbook.model.GitchatColumn;
import cn.ysk521.gitbook.utils.PropertiesUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @description: service
 * @author: yangshangkun
 * @Date: 2019-05-06 18:11
 **/
@Service
@Slf4j
public class MainService {

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private GitchatColumnMapper gitchatColumnMapper;

    @Async
    public Future<String> parse(String url) {
        String cookie = (String) PropertiesUtil.get("cookie");
        try {
            //对url进行解析，判断url是个什么
            if (url.contains("activity")) {
                //chat

                return new AsyncResult<>(getActivity(url, cookie));

            }
            if (url.contains("column") && !url.contains("topic")) {
                //课程页
                return new AsyncResult<>(getCourse(url, cookie));
            }

            if (url.contains("column") && url.contains("topic")) {
                //课程的某一具体topic
                return new AsyncResult<>(getCourseTopicDetail(url, cookie));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("发生异常，请在微信公众号 各种小工具 内联系管理员");
    }


    private String getActivity(String url, String cookie) throws IOException {

        Document readArticleDocument, articleDocument;
        String result = "解析失败,请联系管理员！";
        //先从数据库中进行查询，如果有，直接返回；
        String[] urlSplit = url.split("/");
        String activityId = urlSplit[urlSplit.length - 1];
        Activity activity = activityMapper.selectByPrimaryKey(activityId);
        if (activity != null) {
            return activity.getContent();
        }


        readArticleDocument = Jsoup.connect(url).header("Cookie", cookie).get();


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


        String articleUrl = "https://gitbook.cn" + text;


        articleDocument = Jsoup.connect(articleUrl).header("Cookie", cookie).get();

        result = articleDocument
                //是否只选择文章内容
//                  .select("#article_content")
                .toString();

//          #profileImg
        String avatar = articleDocument.select("#profileImg").attr("src");

        //替换资源
        result = result.replaceAll("src=\"/", "src=\"https://gitbook.cn/");
        result = result.replaceAll("href=\"/", "href=\"https:////gitbook.cn/");
        result = result.replaceAll("\"\\S+/cdnjs", "\"//cdnjs");
        //替换头像
        result = result.replaceAll(avatar, "https://images.gitbook.cn/FnzjNghzs_ktFPUKeLaFX38rbNsL");

        activityMapper.insert(Activity.builder().activityId(activityId).content(result).build());
        return result;
    }

    private String getCourse(String url, String cookie) throws IOException {
        String[] urlSplit = url.split("/");
        String courseId = urlSplit[5];
        //判断数据库中有没有该课程，如果有，则直接返回，如果没有，则进行解析，存入数据库
        GitchatColumn buy = gitchatColumnMapper.selectByPrimaryKey(courseId, "buy");
        String result;
        if (buy == null) {
            //该课程没买，需要先进行一次购买；
            JSONObject postData = new JSONObject();
            postData.put("columnId", courseId);
            postData.put("requestUrl", url);
            boolean buySuccess = buyArticleOrColumn(url, cookie, "https://gitbook.cn/m/mazi/vip/order/column", postData);
            if (!buySuccess) {
                return "课程购买失败!";
            }
            //返回已经购买的课程页，并存到数据库
            Document columnDocument = Jsoup.connect(url).header("Cookie", cookie).get();
            result = columnDocument.toString();
            //替换资源
            result = result.replaceAll("src=\"/", "src=\"https://gitbook.cn/");
            result = result.replaceAll("href=\"/", "href=\"https:////gitbook.cn/");
            result = result.replaceAll("\"\\S+/cdnjs", "\"//cdnjs");
            //变换头像
            String avatar = columnDocument.select("#profileImg2").attr("src");
            result = result.replaceAll(avatar, "https://images.gitbook.cn/FnzjNghzs_ktFPUKeLaFX38rbNsL");
            //换课程链接
            result = result.replaceAll("window.location.href = '/gitchat/column/' + columnId + '/topic/' + id;", " window.location.href = 'http://gitchat.ysk521.cn/?url=https://gitbook.cn/gitchat/column/' + columnId + '/topic/' + id;");

            gitchatColumnMapper.insert(GitchatColumn.builder().columnId(courseId).columnTopic("buy").content(result).build());
        }else {
            result=buy.getContent();
        }

        return result;
    }


    private String getCourseTopicDetail(String url, String cookie) throws IOException {
//        https://gitbook.cn/gitchat/column/5b6d05446b66e3442a2bfa7b/topic/5ba245fb4f0c661a6f732da2
        //解析出课程id和topic
        String[] urlSplit = url.split("/");
        String courseId = urlSplit[5];
        String courseTopic = urlSplit[7];
        GitchatColumn column = gitchatColumnMapper.selectByPrimaryKey(courseId, courseTopic);
        if (column != null) {
            return column.getContent();
        }
        //解析，然后存到数据库
        Document columnDocument = Jsoup.connect(url).header("Cookie", cookie).get();
        String result = columnDocument.toString();
        //做一些处理
        //替换资源
        result = result.replaceAll("src=\"/", "src=\"https://gitbook.cn/");
        result = result.replaceAll("href=\"/", "href=\"https:////gitbook.cn/");
        result = result.replaceAll("\"\\S+/cdnjs", "\"//cdnjs");
        //换头像#profileImg2
        String avatar = columnDocument.select("#profileImg2").attr("src");
        result = result.replaceAll(avatar, "https://images.gitbook.cn/FnzjNghzs_ktFPUKeLaFX38rbNsL");
        //换课程链接
//        window.location.href = '/gitchat/column/' + columnId + '/topic/' + id;
//        window.location.href = 'http://gitchat.ysk521.cn/?url=https://gitbook.cn/gitchat/column/' + columnId + '/topic/' + id;
        result = result.replaceAll("window.location.href = '/gitchat/column/' + columnId + '/topic/' + id;", " window.location.href = 'http://gitchat.ysk521.cn/?url=https://gitbook.cn/gitchat/column/' + columnId + '/topic/' + id;");
        //储存结果
        gitchatColumnMapper.insert(GitchatColumn.builder().columnId(courseId).columnTopic(courseTopic).content(result).build());
        return result;
    }

    private boolean buyColmun(String url, String cookie) {
//        https://gitbook.cn/m/mazi/vip/order/column
//        {"columnId":"5a7c5913a0730e77170e94cd","requestUrl":"https://gitbook.cn/gitchat/column/5a7c5913a0730e77170e94cd"}
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", cookie);
        requestHeaders.add("Content-Type", "application/json; charset=UTF-8");
        JSONObject postData = new JSONObject();
        String[] urlSplit = url.split("/");
        postData.put("columnId", urlSplit[urlSplit.length - 1]);
        postData.put("requestUrl", url);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(postData), requestHeaders);
        String buyUrl = "https://gitbook.cn/m/mazi/vip/order/column";

        return false;
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


    public boolean buyArticleOrColumn(String url, String cookie, String buyUrl, JSONObject params) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Cookie", cookie);
        requestHeaders.add("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(params), requestHeaders);
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
