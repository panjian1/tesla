package io.github.tesla.gateway.netty.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.tesla.gateway.cache.GroovyFilterCacheComponent;
import io.github.tesla.gateway.config.SpringContextHolder;
import io.github.tesla.gateway.netty.filter.request.BlackIpHttpRequesFilter;
import io.github.tesla.gateway.netty.filter.request.BlackURLHttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.BlackCookieHttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.DubboAdapterHttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.GrpcAdapterHttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.HttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.RateLimitHttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.SecurityScannerHttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.URLParamHttpRequestFilter;
import io.github.tesla.gateway.netty.filter.request.BlackUaHttpRequestFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class HttpRequestFilterChain {
  public static List<HttpRequestFilter> filters = Lists.newLinkedList();

  private static HttpRequestFilterChain filterChain = new HttpRequestFilterChain();

  static {
    filters.add(BlackIpHttpRequesFilter.newFilter());
    filters.add(BlackCookieHttpRequestFilter.newFilter());
    filters.add(RateLimitHttpRequestFilter.newFilter());
    filters.add(SecurityScannerHttpRequestFilter.newFilter());
    filters.add(BlackUaHttpRequestFilter.newFilter());
    filters.add(BlackURLHttpRequestFilter.newFilter());
    filters.add(URLParamHttpRequestFilter.newFilter());
    filters.add(DubboAdapterHttpRequestFilter.newFilter());
    filters.add(GrpcAdapterHttpRequestFilter.newFilter());
  }

  public static HttpRequestFilterChain requestFilterChain() {
    GroovyFilterCacheComponent filterComponent =
        SpringContextHolder.getBean(GroovyFilterCacheComponent.class);
    if (filterComponent.requestChanged()) {
      List<String> groovyFilters = filterComponent.getRequestGroovyCode();
      for (String groovyFilter : groovyFilters) {
        Class<?> clazz = GroovyCompiler.compile(groovyFilter);
        try {
          HttpRequestFilter requestFilter = (HttpRequestFilter) clazz.newInstance();
          filters.add(requestFilter);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
      Collections.sort(filters, new Comparator<HttpRequestFilter>() {
        @Override
        public int compare(HttpRequestFilter o1, HttpRequestFilter o2) {
          return o1.filterType().order() - o2.filterType().order();
        }
      });
    }
    return filterChain;
  }


  public HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    for (HttpRequestFilter filter : filters) {
      HttpResponse response = filter.doFilter(originalRequest, httpObject, channelHandlerContext);
      // 如果一个filter有返回值，将会中断下一个filter，这里需要注意filter的顺序，默认grpc->dubbo
      if (response != null) {
        return response;
      }
    }
    return null;
  }
}
