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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;

import io.github.tesla.gateway.config.SpringContextHolder;
import io.github.tesla.gateway.routerules.FilterRuleCacheComponent;
import io.github.tesla.rule.FilterTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author liushiming
 * @version RateLimiterHttpRequestFilter.java, v 0.0.1 2018年1月26日 下午3:56:15 liushiming
 */
public class RateLimitHttpRequestFilter extends HttpRequestFilter {

  private LoadingCache<String, RateLimiter> loadingCache;
  private static final Logger logger = LoggerFactory.getLogger(RateLimitHttpRequestFilter.class);

  private final FilterRuleCacheComponent ruleCache =
      SpringContextHolder.getBean(FilterRuleCacheComponent.class);


  private RateLimitHttpRequestFilter() {
    loadingCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(2, TimeUnit.SECONDS)
        .removalListener(new RemovalListener<String, RateLimiter>() {
          @Override
          public void onRemoval(RemovalNotification<String, RateLimiter> notification) {
            logger.debug("key:{} remove from cache", notification.getKey());
          }
        }).build(new CacheLoader<String, RateLimiter>() {
          @Override
          public RateLimiter load(String key) throws Exception {
            Map<String, List<String>> limiter =
                ruleCache.getUrlFilterRule(RateLimitHttpRequestFilter.this);
            List<String> limitValue = limiter.get(key);
            Double limitValueMax =
                Collections.max(Lists.transform(limitValue, new Function<String, Double>() {

                  @Override
                  public Double apply(String input) {
                    return Double.valueOf(input);
                  }

                }));
            if (limitValue != null) {
              RateLimiter rateLimiter = RateLimiter.create(limitValueMax);
              return rateLimiter;
            } else {
              return null;
            }

          }
        });
  }

  public static HttpRequestFilter newFilter() {
    return new RateLimitHttpRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (httpObject instanceof HttpRequest) {
      HttpRequest httpRequest = (HttpRequest) httpObject;
      String url = httpRequest.uri();
      int index = url.indexOf("?");
      if (index > -1) {
        url = url.substring(0, index);
      }
      RateLimiter rateLimiter = null;
      try {
        rateLimiter = (RateLimiter) loadingCache.get(url);
      } catch (ExecutionException e) {
      }
      // 如果1秒钟没有获取令牌，说明被限制了
      if (rateLimiter != null && !rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
        super.writeFilterLog(Double.toString(rateLimiter.getRate()), this.getClass(),
            "RateLimiter");
        return super.createResponse(HttpResponseStatus.TOO_MANY_REQUESTS, originalRequest);
      }
    }
    return null;
  }

  @Override
  public FilterTypeEnum filterType() {
    return FilterTypeEnum.RateLimitHttpRequestFilter;
  }

}
