package org.wltea.analyzer.dic;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.PathUtils;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.help.ESPluginLoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.wltea.analyzer.dic.DictionaryConfig.*;

/**
 * 管理所有词典的加载 包括远程词典
 * 内部工具
 * @author DingHao
 * @since 2021/3/13 23:39
 */
class DictionaryLoader {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(DictionaryLoader.class);

    private static final RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(10 * 1000).setConnectTimeout(10 * 1000).setSocketTimeout(60 * 1000).build();

    private DictSegment _MainDict;
    private DictSegment _QuantifierDict;
    private DictSegment _StopWords;

    DictSegments globalDict;

    private final Map<String,DictSegments> customDict;

    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    private final Configuration configuration;

    private final DictionaryConfig dictionaryConfig;

    DictionaryLoader(Configuration cfg) {
        this.configuration = cfg;
        this.globalDict = new DictSegments();
        this.dictionaryConfig = new DictionaryConfig(cfg);
        this.customDict = new ConcurrentHashMap<>(2);
    }

    private boolean isGlobalDict(){
        return configuration.getCustomMainDictIdentify().equals("default");
    }

    DictSegments getDictSegments(String identify){
        return customDict.computeIfAbsent(identify,a->new DictSegments());
    }

    public Map<String, DictSegments> getCustomDict() {
        return customDict;
    }

    void loadDict(){
        loadMainDict();
        loadSurnameDict();
        loadQuantifierDict();
        loadSuffixDict();
        loadPrepDict();
        loadStopWordDict();
        if(isGlobalDict()){
            startMonitor(configuration.getCustomMainDictIdentify());
        }
        globalDict.setMainDict(_MainDict);
        globalDict.setQuantifierDict(_QuantifierDict);
        globalDict.setStopWords(_StopWords);
    }

    void loadCustomDict(String identify){
        if(Strings.hasLength(identify) && !"default".equals(identify)){
            DictSegments dictSegments = new DictSegments();
            DictSegment customDictSegment = new DictSegment((char) 0);
            DictSegment stopDictSegment = new DictSegment((char) 0);
            loadRemoteDict(REMOTE_EXT_DICT,identify,customDictSegment);
            loadRemoteDict(REMOTE_EXT_STOP,identify,stopDictSegment);
            if(!customDict.containsKey(identify)){//是否去掉？
                startMonitor(identify);
            }
            dictSegments.setMainDict(customDictSegment);
            dictSegments.setStopWords(stopDictSegment);
            customDict.put(identify,dictSegments);
        }
    }

    void reLoadDict(String identify){
        logger.info("start to reload ik dict.");
        // 新开一个实例加载词典，减少加载过程对当前词典使用的影响
        DictionaryLoader tmpDictLoader = new DictionaryLoader(configuration);
        tmpDictLoader.loadMainDict();
        tmpDictLoader.loadStopWordDict();
        if(customDict.containsKey(identify)){
            tmpDictLoader.loadCustomDict(identify);
            customDict.putAll(tmpDictLoader.customDict);
        }
        _MainDict = tmpDictLoader._MainDict;
        _StopWords = tmpDictLoader._StopWords;
        globalDict.setMainDict(_MainDict);
        globalDict.setStopWords(_StopWords);
        logger.info("reload ik dict finished.");
    }

    /**
     * 加载主词典及扩展词典
     */
    private void loadMainDict() {
        // 建立一个主词典实例
        _MainDict = new DictSegment((char) 0);

        // 读取主词典文件
        Path file = dictionaryConfig.getDictPath(PATH_DIC_MAIN);
        loadDictFile(_MainDict, file, false, "Main Dict");
        // 加载扩展词典
        loadExtDict();
        // 加载远程自定义词库
        if(isGlobalDict()){
            loadRemoteDict(REMOTE_EXT_DICT,configuration.getCustomMainDictIdentify(),_MainDict);
        }

    }

    /**
     * 加载用户配置的扩展词典到主词库表
     */
    private void loadExtDict() {
        // 加载扩展词典配置
        List<String> extDictFiles = dictionaryConfig.getDictFiles(EXT_DICT,true);
        for (String extDictName : extDictFiles) {
            // 读取扩展词典文件
            logger.info("[Dict Loading] " + extDictName);
            Path file = PathUtils.get(extDictName);
            loadDictFile(_MainDict, file, false, "Extra Dict");
        }
    }

    /**
     * 加载远程词库表
     */
    private void loadRemoteDict(String dictType,String identify,DictSegment dictSegment) {
        List<String> remoteDictFiles = dictionaryConfig.getDictFiles(dictType);
        for (String location : remoteDictFiles) {
            logger.info("[Dict Loading] {} identify {}" ,location,identify);
            List<String> lists = getRemoteWords(location,identify);
            // 如果找不到扩展的字典，则忽略
            if (lists == null) {
                logger.error("[Dict Loading] " + location + " load failed");
                continue;
            }
            for (String theWord : lists) {
                if (theWord != null && !"".equals(theWord.trim())) {
                    // 加载扩展词典数据到主内存词典中
//                    logger.info(theWord);
                    dictSegment.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            }
        }
    }

    /**
     * 加载用户扩展的停止词词典
     */
    private void loadStopWordDict() {
        // 建立主词典实例
        _StopWords = new DictSegment((char) 0);

        // 读取主词典文件
        Path file = dictionaryConfig.getDictPath(PATH_DIC_STOP);
        loadDictFile(_StopWords, file, false, "Main Stopwords");

        // 加载扩展停止词典
        List<String> extStopWordDictFiles = dictionaryConfig.getDictFiles(EXT_STOP,true);
        for (String extStopWordDictName : extStopWordDictFiles) {
            logger.info("[Dict Loading] " + extStopWordDictName);
            // 读取扩展词典文件
            file = PathUtils.get(extStopWordDictName);
            loadDictFile(_StopWords, file, false, "Extra Stopwords");
        }

        // 加载远程停用词典
        if(isGlobalDict()){
            loadRemoteDict(REMOTE_EXT_STOP,configuration.getCustomMainDictIdentify(),_StopWords);
        }

    }

    private void startMonitor(String identify){
        if(configuration.isEnableRemoteDict()){
            // 建立监控线程
            for (String location : dictionaryConfig.getDictFiles(REMOTE_EXT_DICT)) {
                // 10 秒是初始延迟可以修改的 60是间隔时间 单位秒
                pool.scheduleAtFixedRate(new Monitor(location,identify), 10, 60, TimeUnit.SECONDS);
            }
            for (String location : dictionaryConfig.getDictFiles(REMOTE_EXT_STOP)) {
                pool.scheduleAtFixedRate(new Monitor(location,identify), 10, 60, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 从远程服务器上下载自定义词条
     */
    private List<String> getRemoteWords(String location,String identify) {
        SpecialPermission.check();
        return AccessController.doPrivileged((PrivilegedAction<List<String>>) () -> {
            List<String> buffer = new ArrayList<>();

            sendGet(location+"?dicPath="+identify,response -> {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String charset = "UTF-8";
                    // 获取编码，默认为utf-8
                    HttpEntity entity = response.getEntity();
                    if(entity!=null){
                        Header contentType = entity.getContentType();
                        if(contentType!=null&&contentType.getValue()!=null){
                            String typeValue = contentType.getValue();
                            if(typeValue!=null&&typeValue.contains("charset=")){
                                charset = typeValue.substring(typeValue.lastIndexOf("=") + 1);
                            }
                        }
                        BufferedReader in;
                        try{
                            if (entity.getContentLength() > 0 || entity.isChunked()) {
                                in = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
                                String line;
                                while ((line = in.readLine()) != null) {
                                    buffer.add(line);
                                }
                                in.close();
                                response.close();
                            }
                        }catch (IllegalStateException | IOException e){
                            logger.error("getRemoteWords {} error",  location);
                        }
                    }
                }
            });

            return buffer;
        });
    }

    /**
     * 加载量词词典
     */
    private void loadQuantifierDict() {
        // 建立一个量词典实例
        _QuantifierDict = new DictSegment((char) 0);
        // 读取量词词典文件
        Path file = dictionaryConfig.getDictPath(PATH_DIC_QUANTIFIER);
        loadDictFile(_QuantifierDict, file, false, "Quantifier");

    }

    private void loadSurnameDict() {
        DictSegment _SurnameDict = new DictSegment((char) 0);
        Path file = dictionaryConfig.getDictPath(PATH_DIC_SURNAME);
        loadDictFile(_SurnameDict, file, true, "Surname");
    }

    private void loadSuffixDict() {
        DictSegment _SuffixDict = new DictSegment((char) 0);
        Path file = dictionaryConfig.getDictPath(PATH_DIC_SUFFIX);
        loadDictFile(_SuffixDict, file, true, "Suffix");
    }

    private void loadPrepDict() {
        DictSegment _PrepDict = new DictSegment((char) 0);
        Path file = dictionaryConfig.getDictPath(PATH_DIC_PREP);
        loadDictFile(_PrepDict, file, true, "Preposition");
    }

    private void loadDictFile(DictSegment dict, Path file, boolean critical, String name) {
        try (InputStream is = new FileInputStream(file.toFile())) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), 512);
            String word = br.readLine();
            if (word != null) {
                if (word.startsWith("\uFEFF"))
                    word = word.substring(1);
                for (; word != null; word = br.readLine()) {
                    word = word.trim();
                    if (word.isEmpty()) continue;
                    dict.fillSegment(word.toCharArray());
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("ik-analyzer: " + name + " not found", e);
            if (critical) throw new RuntimeException("ik-analyzer: " + name + " not found!!!", e);
        } catch (IOException e) {
            logger.error("ik-analyzer: " + name + " loading failed", e);
        }
    }


    public void sendGet(String location, Consumer<CloseableHttpResponse> consumer){
        send(new HttpGet(location),consumer);
    }

    private void send(HttpRequestBase httpRequestBase, Consumer<CloseableHttpResponse> consumer){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        httpRequestBase.setConfig(config);
        try(CloseableHttpResponse response = httpclient.execute(httpRequestBase)){
            consumer.accept(response);
        }catch (IllegalStateException | IOException e){
            logger.error("send {} error!",httpRequestBase.getURI());
        }
    }
}
