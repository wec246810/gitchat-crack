package cn.ysk521.gitbook.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Properties;

/**
 * @description: propertiesutil
 * @author: yangshangkun
 * @Date: 2019-05-07 09:39
 **/
@Slf4j
public class PropertiesUtil {

    private static String DEFAULT_CONFIG_PROPERRIES = "config-default.properties";

    public static Object get(String key) {
        return get(DEFAULT_CONFIG_PROPERRIES, key);
    }

    public static Object get(String propertiesName, String key) {
        Properties prop = getProperties(propertiesName);
        return prop.get(key);
    }

    public static void set(String key, String value) {
        Properties prop = getProperties(DEFAULT_CONFIG_PROPERRIES);
        prop.put(key, value);
    }

    public static void set(String propertiesName, String key, String value) {
        Properties prop = getProperties(propertiesName);
        prop.put(key, value);
    }


    public static Properties getProperties(String propertiesName) {
        Properties prop = new Properties();
        //创建输入流，用来读取文件
        InputStream is;
        ClassPathResource classPathResource = new ClassPathResource(propertiesName);


        try {
//            is = classPathResource.getInputStream();
            is = new BufferedInputStream(new FileInputStream(ResourceUtils.getFile("file:" + propertiesName)));
            prop.load(is);
        } catch (FileNotFoundException e) {
            log.error("配置文件{}不存在", propertiesName);
            e.printStackTrace();
        } catch (IOException e) {
            log.error("加载配置{}失败！", propertiesName);
            e.printStackTrace();
        }
        return prop;
    }


}
