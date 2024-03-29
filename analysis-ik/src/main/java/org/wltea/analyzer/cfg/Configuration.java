package org.wltea.analyzer.cfg;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.wltea.analyzer.dic.Dictionary;

public class Configuration {

	private final Environment environment;

	//是否启用智能分词
	private boolean useSmart;

	//是否启用远程词典加载
	private final boolean enableRemoteDict;

	//是否启用小写处理
	private final boolean enableLowercase;

	private final String identify;

	@Inject
	public Configuration(String indexName,Environment env,Settings settings) {
		this.environment = env;
		this.enableLowercase = settings.getAsBoolean("enable_lowercase", true);
        this.enableRemoteDict = settings.getAsBoolean("enable_remote_dict", true);
		this.identify = indexName;//默认每个索引下一个性化词典
		Dictionary.initial(this);
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
