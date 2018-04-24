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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.tesla.gateway.netty.filter.FilterUtil;
import io.github.tesla.filter.RequestFilterTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * cooki黑名单过滤
 */
public class BlackCookieHttpRequestFilter extends HttpRequestFilter {

  public static HttpRequestFilter newFilter() {
    return new BlackCookieHttpRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (httpObject instanceof HttpRequest) {
      HttpRequest httpRequest = (HttpRequest) httpObject;
      List<String> headerValues = FilterUtil.getHeaderValues(httpRequest, "Cookie");
      List<Pattern> patterns = super.getCommonRule(this);
      if (headerValues.size() > 0 && headerValues.get(0) != null) {
        String[] cookies = headerValues.get(0).split(";");
        for (String cookie : cookies) {
          for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(cookie.toLowerCase());
            if (matcher.find()) {
              super.writeFilterLog(cookie, BlackIpHttpRequesFilter.class, pattern.pattern());
              return super.createResponse(HttpResponseStatus.FORBIDDEN, originalRequest);
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public RequestFilterTypeEnum filterType() {
    return RequestFilterTypeEnum.BlackCookieHttpRequestFilter;
  }

}
