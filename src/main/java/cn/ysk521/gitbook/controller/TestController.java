package cn.ysk521.gitbook.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @description: 测试
 * @author: yangshangkun
 * @Date: 2019-05-06 17:17
 **/
@RestController
public class TestController {
    @RequestMapping("test")
    public String test() throws IOException {

        String cookie = "aliyungf_tc=AQAAACbbil39AQIA6oPPfJQ50G3BTQED; connect.sid=s%3ApgE2xnIlE9k7v6G_nzvqRc6J6gnZedvP.hbBjuK7ReiEbfq9P%2FPUNTAK8Hj4si0mbfQ8htamEL64; SERVER_ID=5aa5eb5e-cf15479d; _ga=GA1.2.143678473.1557132379; _gid=GA1.2.1985478697.1557132379; customerId=5a8ff9ee382e1c19af6efd00; customerToken=1daf35b0-188c-11e8-b5db-156a90db407c; customerMail=; isLogin=yes; Hm_lvt_5667c6d502e51ebd8bd9e9be6790fb5d=1557132379,1557132443; Hm_lpvt_5667c6d502e51ebd8bd9e9be6790fb5d=1557132818";


        //下载网页
        String URL = "https://gitbook.cn/gitchat/activity/5ca044242913a3054e0baca7";
        Document document = Jsoup.connect(URL).header("Cookie", cookie).get();
//在下载的document里进行检索的语句
        Elements test = document.select("#activityOrderBtn");
//这样test标签就是我们最开始右键单击检查的标签
        String Str = test.toString();//将标签转化成字符串

        String text = test.attr("href");//将标签里的文本提取出来
//其他转换方法省略，检索到目标标签，提取标签里的特定元素so easy

        System.out.println(Str);
        System.out.println(text);

        String aa = "https://gitbook.cn" + text;

        System.out.println(aa);
        Document document2 = Jsoup.connect(aa).header("Cookie", cookie).get();

        String c = document2
//                .select("body").select("div.my_container").select("div.mainDiv.main_view").select("div")
                .select("#article_content")
                .toString();

//        #profileImg
        String touxiang=document2.select("#profileImg").attr("src").toString();


        c=c.replaceAll("src=\"/", "src=\"https://gitbook.cn");
        c=c.replaceAll("href=\"/", "href=\"https:////gitbook.cn//");

        c=c.replaceAll(touxiang, "https://images.gitbook.cn/FnzjNghzs_ktFPUKeLaFX38rbNsL");




        System.out.println(c);

        return c;
    }
}
