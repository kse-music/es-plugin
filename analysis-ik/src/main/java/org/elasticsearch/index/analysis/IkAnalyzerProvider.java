package org.elasticsearch.index.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IkAnalyzerProvider extends AbstractIndexAnalyzerProvider<IKAnalyzer> {

    private final IKAnalyzer analyzer;

    private IkAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.analyzer = new IKAnalyzer(new Configuration(index().getName(),env, settings));
    }

    public static IkAnalyzerProvider getIkAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IkAnalyzerProvider(indexSettings, env, name, settings);
    }

    public static IkAnalyzerProvider getIkSmartAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        IkAnalyzerProvider ikAnalyzerProvider = new IkAnalyzerProvider(indexSettings, env, name, settings);
        ikAnalyzerProvider.analyzer.getConfiguration().useSmart();
        return ikAnalyzerProvider;
    }

    @Override
    public IKAnalyzer get() {
        return this.analyzer;
    }

}
