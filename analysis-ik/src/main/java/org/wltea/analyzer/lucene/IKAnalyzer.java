package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.wltea.analyzer.cfg.Configuration;

/**
 * IK分词器，Lucene Analyzer接口实现
 * 兼容Lucene 4.0版本
 */
public final class IKAnalyzer extends Analyzer{
	
	private Configuration configuration;

	/**
	 * IK分词器Lucene  Analyzer接口实现类
	 * 
	 * 默认细粒度切分算法
	 */
	public IKAnalyzer(){

    }

    /**
	 * IK分词器Lucene Analyzer接口实现类
	 * 
	 * @param configuration IK配置
	 */
	public IKAnalyzer(Configuration configuration){
		super();
        this.configuration = configuration;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return new TokenStreamComponents(new IKTokenizer(configuration));
    }

}
