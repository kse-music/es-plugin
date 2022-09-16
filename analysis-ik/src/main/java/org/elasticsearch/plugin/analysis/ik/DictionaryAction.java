package org.elasticsearch.plugin.analysis.ik;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.XContentBuilder;
import org.wltea.analyzer.dic.Dictionary;

import java.io.IOException;
import java.util.List;
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
                new Route(POST, "/_dictionary"),
                new Route(GET, "/_dictionary"),
                new Route(DELETE, "/_dictionary"),
                new Route(GET, "/{index}/_dictionary"),
                new Route(POST, "/{index}/_dictionary")));
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        String words = request.param("words");//添加的词
        String identify0 = request.param("identify");//添加到哪个标识下
        String index = request.param("index");
        if(Strings.hasLength(identify0)){
            identify0 = index + " || " + identify0;
        }
        String identify = identify0;
        return channel -> {
            XContentBuilder builder = channel.newBuilder();
            builder.startObject();
            if(Strings.hasLength(words) && Strings.hasLength(identify)){
                Set<String> wordSet = Strings.commaDelimitedListToSet(words);
                if(request.method() == DELETE){
                    Dictionary.getSingleton().disableWords(wordSet,identify);
                    builder.field("type", "delete");
                }else{
                    Dictionary.getSingleton().addWords(wordSet,identify);
                    builder.field("type", "add");
                }
                builder.field("words", wordSet);
            }
            builder.endObject();
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        };
    }

}
