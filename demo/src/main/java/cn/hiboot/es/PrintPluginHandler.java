package cn.hiboot.es;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2021/8/26 17:16
 */
public class PrintPluginHandler extends BaseRestHandler {

    private final String printName = "printPluginTest";

    @Override
    public List<Route> routes() {
        return unmodifiableList(asList(new Route(GET, "print")));
    }

    @Override
    public String getName() {
        return printName;
    }
    /**
     * 处理业务逻辑
     */
    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        // 接收的参数
        System.out.println("params==" + request.params());

        long t1 = System.currentTimeMillis();

        String name = request.param("name");

        long cost = System.currentTimeMillis() - t1;
        // 返回内容，这里返回消耗时间 请求参数 插件名称
        return channel -> {
            XContentBuilder builder = channel.newBuilder();
            builder.startObject();
            builder.field("cost", cost);
            builder.field("name", name);
            builder.field("time", new Date());
            builder.field("pluginName",printName);
            builder.field("print","this is print plugin test");
            builder.endObject();
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        };

    }
}
