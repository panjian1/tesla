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

import java.net.URLDecoder;
import java.util.List;

import io.github.tesla.rule.RequestFilterTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * URL参数黑名单参数拦截
 */
public class URLParamHttpRequestFilter extends HttpRequestFilter {

  public static HttpRequestFilter newFilter() {
    return new URLParamHttpRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (httpObject instanceof HttpRequest) {
      HttpRequest httpRequest = (HttpRequest) httpObject;
      String url = null;
      try {
        String uri = httpRequest.uri().replaceAll("%", "%25");
        url = URLDecoder.decode(uri, "UTF-8");
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (url != null) {
        int index = url.indexOf("?");
        if (index > -1) {
          String argsStr = url.substring(index + 1);
          String[] args = argsStr.split("&");
          for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv.length == 2) {
              List<String> patterns = super.getRule(this);
              for (String pattern : patterns) {
                String param = kv[1].toLowerCase();
                if (pathMatcher.match(pattern, param)) {
                  super.writeFilterLog(param, this.getClass(), pattern);
                  return super.createResponse(HttpResponseStatus.FORBIDDEN, originalRequest);
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public RequestFilterTypeEnum filterType() {
    return RequestFilterTypeEnum.URLParamHttpRequestFilter;
  }

}
