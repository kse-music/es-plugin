package org.wltea.analyzer.cfg;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugin.analysis.ik.AnalysisIkPlugin;
import org.wltea.analyzer.dic.Dictionary;

import java.io.File;
import java.nio.file.Path;

public class Configuration {

	private final Environment environment;
	private final Settings settings;

	//是否启用智能分词
	private boolean useSmart;

	//是否启用远程词典加载
	private final boolean enableRemoteDict;

	//是否启用小写处理
	private final boolean enableLowercase;

	private final String customDictPath;


	@Inject
	public Configuration(Environment env,Settings settings) {
		this.environment = env;
		this.settings=settings;

		this.useSmart = settings.getAsBoolean("use_smart", false);
		this.enableLowercase = settings.getAsBoolean("enable_lowercase", true);
        this.enableRemoteDict = settings.getAsBoolean("enable_remote_dict", true);
        this.customDictPath = settings.get("custom_dict_path", "default");

		Dictionary.initial(this);

	}


	public Path getConfigInPluginDir() {
		return PathUtils.get(new File(AnalysisIkPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(), "config").toAbsolutePath();
	}

	public boolean isUseSmart() {
		return useSmart;
	}

	public Configuration useSmart(boolean useSmart) {
		this.useSmart = useSmart;
		return this;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Settings getSettings() {
		return settings;
	}

	public boolean isEnableRemoteDict() {
		return enableRemoteDict;
	}

	public boolean isEnableLowercase() {
		return enableLowercase;
	}

	public String getCustomDictPath() {
		return customDictPath;
	}
}
