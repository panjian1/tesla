package io.github.tesla.gateway.netty.filter.response;

import io.github.tesla.rule.ResponseFilterTypeEnum;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;


public abstract class HttpResponseFilter {

  public abstract HttpResponse doFilter(HttpRequest originalRequest, HttpResponse httpResponse);

  public abstract ResponseFilterTypeEnum filterType();
}
