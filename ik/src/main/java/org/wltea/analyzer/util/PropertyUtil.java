package org.wltea.analyzer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2020/2/16 20:50
 */
public class PropertyUtil {


    public static Properties load(String name) {
        return load(name, PropertyUtil.class.getClassLoader());
    }

    public static Properties load(String name, ClassLoader classLoader) {
        return load(name,false,classLoader);
    }

    public static Properties loadFromXML(String name) {
        return loadFromXML(name, PropertyUtil.class.getClassLoader());
    }

    public static Properties loadFromXML(String name, ClassLoader classLoader) {
        return load(name, true, classLoader);
    }

    public static List<String> readAllLine(InputStream inputStream) {
        List<String> rs = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))){
            String theWord;
            do {
                theWord = br.readLine();
                if (theWord != null && !"".equals(theWord.trim())) {
                    rs.add(theWord);
                }
            } while (theWord != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rs;
    }

    private static Properties load(String name, boolean isXml,ClassLoader classLoader) {
        Properties properties = new Properties();
        try (InputStream input = classLoader.getResourceAsStream(name)) {
            if(input == null){
                return properties;
            }
            if(isXml){
                properties.loadFromXML(input);
            }else {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }


}
