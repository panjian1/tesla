/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.tesla.gateway.netty.filter.request;

import io.github.tesla.gateway.cache.DynamicsRouteCacheComponent;
import io.github.tesla.gateway.config.SpringContextHolder;
import io.github.tesla.gateway.protocol.dubbo.DynamicDubboClient;
import io.github.tesla.rule.RequestFilterTypeEnum;
import io.github.tesla.rule.domain.RpcDO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * @author liushiming
 * @version DubboAdapterHttpRequestFilter.java, v 0.0.1 2018年1月26日 下午4:07:13 liushiming
 */
public class DubboAdapterHttpRequestFilter extends HttpRequestFilter {

  private final DynamicDubboClient dubboClient =
      SpringContextHolder.getBean(DynamicDubboClient.class);

  private final DynamicsRouteCacheComponent routeRuleCache =
      SpringContextHolder.getBean(DynamicsRouteCacheComponent.class);


  public static HttpRequestFilter newFilter() {
    return new DubboAdapterHttpRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (originalRequest instanceof FullHttpRequest && dubboClient != null) {
      FullHttpRequest request = (FullHttpRequest) originalRequest;
      String urlPath = request.uri();
      RpcDO rpc = routeRuleCache.getRpc(urlPath);
      if (rpc != null) {
        ByteBuf jsonBuf = request.content();
        String jsonInput = jsonBuf.toString(CharsetUtil.UTF_8);
        String jsonOutput = dubboClient.doRemoteCall(rpc, jsonInput);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(jsonOutput.getBytes(CharsetUtil.UTF_8)));
      } else {
        // 如果从缓存没有查到dubbo的映射信息，说明不是泛化调用，返回空，继续走下一个filter或者去走rest服务发现等
        return null;
      }
    }
    return null;
  }

  @Override
  public RequestFilterTypeEnum filterType() {
    return RequestFilterTypeEnum.DUBBO;
  }

}
