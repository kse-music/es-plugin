package org.wltea.analyzer.cfg;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.core.PathUtils;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugin.analysis.ik.AnalysisIkPlugin;
import org.wltea.analyzer.dic.Dictionary;

import java.io.File;
import java.nio.file.Path;

public class Configuration {

	private final Environment environment;

	//是否启用智能分词
	private boolean useSmart;

	//是否启用远程词典加载
	private final boolean enableRemoteDict;

	//是否启用小写处理
	private final boolean enableLowercase;

	private String identify;

	@Inject
	public Configuration(String indexName,Environment env,Settings settings) {
		this.environment = env;
		this.enableLowercase = settings.getAsBoolean("enable_lowercase", true);
        this.enableRemoteDict = settings.getAsBoolean("enable_remote_dict", true);
        this.identify = settings.get("identify");
		if(Strings.hasLength(identify)){
			this.identify = indexName + " || " + identify;
		}
		Dictionary.initial(this);
	}

	public Path getConfigInPluginDir() {
		return PathUtils.get(new File(AnalysisIkPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), "config").toAbsolutePath();
	}

	public boolean isUseSmart() {
		return useSmart;
	}

	public void useSmart() {
		this.useSmart = true;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public boolean isEnableRemoteDict() {
		return enableRemoteDict;
	}

	public boolean isEnableLowercase() {
		return enableLowercase;
	}

	public String getIdentify() {
		return identify;
	}
}
