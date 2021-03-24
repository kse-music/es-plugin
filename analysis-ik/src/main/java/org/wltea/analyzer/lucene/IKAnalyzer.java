package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
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

	/**
	 * 重载Analyzer接口，构造分词组件
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer _IKTokenizer = new IKTokenizer(configuration);

		return new TokenStreamComponents(_IKTokenizer);
    }

}
