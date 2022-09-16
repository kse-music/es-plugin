package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.lucene.IKTokenizer;

public class IkTokenizerFactory extends AbstractTokenizerFactory {

    private final Configuration configuration;

    private IkTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings, name);
        this.configuration = new Configuration(index().getName(),env, settings);
    }

    public static IkTokenizerFactory getIkTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IkTokenizerFactory(indexSettings, env, name, settings);
    }

    public static IkTokenizerFactory getIkSmartTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        IkTokenizerFactory ikTokenizerFactory = new IkTokenizerFactory(indexSettings, env, name, settings);
        ikTokenizerFactory.configuration.useSmart();
        return ikTokenizerFactory;
    }

    @Override
    public Tokenizer create() {
        return new IKTokenizer(configuration);
    }

}
