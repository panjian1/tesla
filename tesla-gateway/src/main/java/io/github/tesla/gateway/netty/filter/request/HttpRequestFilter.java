package io.github.tesla.gateway.netty.filter.request;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.github.tesla.filter.RequestFilterTypeEnum;
import io.github.tesla.gateway.cache.ApiAndFilterCacheComponent;
import io.github.tesla.gateway.config.SpringContextHolder;
import io.github.tesla.gateway.netty.filter.FilterUtil;
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

  private static final String LINE_SEPARATOR_UNIX = "\n";

  private static final String LINE_SEPARATOR_WINDOWS = "\r\n";


  public abstract HttpResponse doFilter(HttpRequest originalRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext);

  public abstract RequestFilterTypeEnum filterType();

  public String filterName() {
    return filterType().name();
  }

  protected List<Pattern> getCommonRule(HttpRequestFilter filterClazz) {
    ApiAndFilterCacheComponent ruleCache =
        SpringContextHolder.getBean(ApiAndFilterCacheComponent.class);
    Set<Pattern> compilePatterns = Sets.newHashSet();
    Set<String> rules = ruleCache.getPubicFilterRule(filterClazz);
    for (String rule : rules) {
      String[] rulesSplits = new String[] {rule};
      if (filterClazz instanceof BlackCookieHttpRequestFilter
          || filterClazz instanceof URLParamHttpRequestFilter
          || filterClazz instanceof BlackURLHttpRequestFilter) {
        if (StringUtils.contains(rule, LINE_SEPARATOR_UNIX)) {
          rulesSplits = StringUtils.split(rule, LINE_SEPARATOR_UNIX);
        } else if (StringUtils.contains(rule, LINE_SEPARATOR_WINDOWS)) {
          rulesSplits = StringUtils.split(rule, LINE_SEPARATOR_UNIX);
        }
      }
      for (String rulesSplit : rulesSplits) {
        try {
          Pattern compilePattern = Pattern.compile(rulesSplit);
          compilePatterns.add(compilePattern);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }
    return Lists.newArrayList(compilePatterns);
  }

  protected Map<String, Set<String>> getUrlRule(HttpRequestFilter filterClazz) {
    ApiAndFilterCacheComponent ruleCache =
        SpringContextHolder.getBean(ApiAndFilterCacheComponent.class);
    Map<String, Set<String>> rules = ruleCache.getUrlFilterRule(filterClazz);
    return rules;
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
