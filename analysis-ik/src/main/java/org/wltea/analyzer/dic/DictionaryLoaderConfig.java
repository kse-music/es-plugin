package org.wltea.analyzer.dic;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.core.PathUtils;
import org.elasticsearch.plugin.analysis.ik.AnalysisIkPlugin;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.help.ESPluginLoggerFactory;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 词典加载器配置
 *
 * @author DingHao
 * @since 2021/3/23 15:19
 */
class DictionaryLoaderConfig {

    private static final Logger logger = ESPluginLoggerFactory.getLogger(DictionaryLoaderConfig.class);

    private static final String SEMICOLON = ";";
    private static final String FILE_NAME = "IKAnalyzer.cfg.xml";

    static final String PATH_DIC_MAIN = "main.dic";
    static final String PATH_DIC_PREP = "preposition.dic";
    static final String PATH_DIC_QUANTIFIER = "quantifier.dic";
    static final String PATH_DIC_STOP = "stopword.dic";
    static final String PATH_DIC_SUFFIX = "suffix.dic";
    static final String PATH_DIC_SURNAME = "surname.dic";

    static final String EXT_DICT = "ext_dict";
    static final String REMOTE_EXT_DICT = "remote_ext_dict";
    static final String EXT_STOP = "ext_stopwords";
    static final String REMOTE_EXT_STOP = "remote_ext_stopwords";

    private final Properties props;
    private final String dictRoot;

    DictionaryLoaderConfig(Configuration configuration) {
        this.props = new Properties();

        Path conf_dir = configuration.getEnvironment().configFile().resolve(AnalysisIkPlugin.PLUGIN_NAME);
        Path configFile = conf_dir.resolve(FILE_NAME);

        InputStream input = null;
        try {
            logger.info("try load config from {}", configFile);
            input = new FileInputStream(configFile.toFile());
        } catch (FileNotFoundException e) {
            conf_dir = PathUtils.get(new File(AnalysisIkPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), "config").toAbsolutePath();
            configFile = conf_dir.resolve(FILE_NAME);
            try {
                logger.info("try load config from {}", configFile);
                input = new FileInputStream(configFile.toFile());
            } catch (FileNotFoundException ex) {
                // We should report origin exception
                logger.error("ik-analyzer", e);
            }
        }

        dictRoot = conf_dir.toAbsolutePath().toString();

        if (input != null) {
            try {
                props.loadFromXML(input);
            } catch (IOException e) {
                logger.error("ik-analyzer", e);
            }
        }
    }


    Path getDictPath(String dic) {
        return PathUtils.get(dictRoot, dic);
    }

    /**
     * 获取指定外部词典文件
     * @param dictKey dict key
     * @return 词典文件
     */
    List<String> getDictFiles(String dictKey) {
        return getDictFiles(dictKey,false);
    }

    /**
     * 获取指定外部词典文件
     * @param dictKey dict key
     * @param recursive 是否递归,true:表示是目录则递归查找词典文件
     * @return 词典文件
     */
    List<String> getDictFiles(String dictKey, boolean recursive) {
        List<String> dictFiles = new ArrayList<>(2);
        String remoteExtDictCfg = props.getProperty(dictKey);
        if (remoteExtDictCfg != null) {
            String[] filePaths = remoteExtDictCfg.split(SEMICOLON);
            for (String filePath : filePaths) {
                if (filePath != null && !"".equals(filePath.trim())) {
                    if(recursive){
                        walkFileTree(dictFiles, getDictPath(filePath));
                    }else {
                        dictFiles.add(filePath);
                    }
                }
            }
        }
        return dictFiles;
    }

    private void walkFileTree(List<String> files, Path path) {
        if (Files.isRegularFile(path)) {
            files.add(path.toString());
        } else if (Files.isDirectory(path)) try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    files.add(file.toString());
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    logger.error("[Ext Loading] listing files", e);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("[Ext Loading] listing files", e);
        } else {
            logger.warn("[Ext Loading] file not found: " + path);
        }
    }

}
