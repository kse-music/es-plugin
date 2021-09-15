package cn.hiboot.es;

import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.plugins.*;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/8/26 16:03
 */
public class PrintPlugin extends Plugin implements ActionPlugin,AnalysisPlugin,ScriptPlugin,SearchPlugin{

    public PrintPlugin() {
        System.out.println("print Plugin initialize");
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController,
                                             ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings, SettingsFilter settingsFilter,
                                             IndexNameExpressionResolver indexNameExpressionResolver, Supplier<DiscoveryNodes> nodesInCluster) {
        return Collections.singletonList(new PrintPluginHandler());
    }

}
