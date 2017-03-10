package com.dk3k.framework.server.nettyServer.support;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    public static Map<String, String> parseParams(FullHttpRequest request) throws IOException {
        HttpMethod method = request.getMethod();

        Map<String, String> parmMap = new HashMap<>();
        if (HttpMethod.GET == method) {
            QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
            for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                parmMap.put(entry.getKey(), entry.getValue().get(0));
            }

        } else if (HttpMethod.POST == method) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
            //decoder.offer(request);

            List<InterfaceHttpData> params = decoder.getBodyHttpDatas();
            for (InterfaceHttpData param : params) {
                Attribute data = (Attribute) param;
                parmMap.put(data.getName(), data.getValue());
            }
        } else {
            // 不支持其它方法
            //throw new RuntimeException("shouldn't happen");
        }

        return parmMap;

    }


}
