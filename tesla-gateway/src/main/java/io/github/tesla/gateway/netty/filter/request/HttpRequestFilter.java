package io.github.tesla.gateway.netty.filter.request;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import io.github.tesla.gateway.config.SpringContextHolder;
import io.github.tesla.gateway.netty.filter.FilterUtil;
import io.github.tesla.gateway.routerules.FilterRuleCacheComponent;
import io.github.tesla.rule.FilterTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


public abstract class HttpRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger("ProxyFilterLog");

  protected static final PathMatcher pathMatcher = new AntPathMatcher();

  public abstract HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext);

  public abstract FilterTypeEnum filterType();

  protected List<String> getRule(HttpRequestFilter filterClazz) {
    FilterRuleCacheComponent ruleCache =
        SpringContextHolder.getBean(FilterRuleCacheComponent.class);
    return ruleCache.getPubicFilterRule(filterClazz);
  }

  protected HttpResponse createResponse(HttpResponseStatus httpResponseStatus,
      HttpRequest originalRequest) {
    HttpHeaders httpHeaders = new DefaultHttpHeaders();
    httpHeaders.add("Transfer-Encoding", "chunked");
    HttpResponse httpResponse =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
    List<String> originHeader = FilterUtil.getHeaderValues(originalRequest, "Origin");
    if (originHeader.size() > 0) {
      httpHeaders.set("Access-Control-Allow-Credentials", "true");
      httpHeaders.set("Access-Control-Allow-Origin", originHeader.get(0));
    }
    httpResponse.headers().add(httpHeaders);
    return httpResponse;
  }

  protected void writeFilterLog(String fact, Class<?> type, String cause) {
    logger.info("type:{},fact:{},cause:{}", type.getSimpleName(), fact, cause);
  }
}
