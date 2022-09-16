package org.elasticsearch.plugin.analysis.ik;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.XContentBuilder;
import org.wltea.analyzer.dic.Dictionary;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.elasticsearch.rest.RestRequest.Method.*;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/3/22 15:28
 */
public class DictionaryAction extends BaseRestHandler {

    @Override
    public String getName() {
        return "dictionary_add_word_action";
    }

    @Override
    public List<Route> routes() {
        return unmodifiableList(asList(
                new Route(DELETE, "/{index}/_dictionary"),
                new Route(GET, "/{index}/_dictionary"),
                new Route(POST, "/{index}/_dictionary")));
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        Map<String, Object> sourceAsMap = XContentHelper.convertToMap(request.requiredContent(), false, request.getXContentType()).v2();
        String words = sourceAsMap.get("words").toString();//添加的词
        String type = sourceAsMap.get("type").toString();//主词典 or 停用词
        String identify = request.param("index");
        return channel -> {
            XContentBuilder builder = channel.newBuilder();
            builder.startObject();
            if(Strings.hasLength(words) && Strings.hasLength(identify)){
                Set<String> wordSet = Strings.commaDelimitedListToSet(words);
                if(request.method() == DELETE){
                    Dictionary.getSingleton().disableWords(wordSet,identify,type);
                }else{
                    Dictionary.getSingleton().addWords(wordSet,identify,type);
                }
                builder.field("status", "success");
            }
            builder.endObject();
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        };
    }

}
