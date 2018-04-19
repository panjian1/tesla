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
package io.github.tesla.gateway.netty.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import io.github.tesla.gateway.netty.ChannelThreadLocal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.ssl.SslHandler;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NettyHttpServletRequest implements HttpServletRequest {

  private static final String SSL_CIPHER_SUITE_ATTRIBUTE = "javax.servlet.request.cipher_suite";
  private static final String SSL_PEER_CERT_CHAIN_ATTRIBUTE =
      "javax.servlet.request.X509Certificate";

  private static final Locale DEFAULT_LOCALE = Locale.getDefault();

  private URIParser uriParser;

  private HttpRequest originalRequest;

  private NettyServletInputStream inputStream;

  private BufferedReader reader;

  private Map<String, List<String>> params;

  private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

  private String characterEncoding;

  private String contextPath;

  private ChannelHandlerContext channelHandlerContext;

  public NettyHttpServletRequest(HttpRequest request, String contextPath,
      ChannelHandlerContext ctx) {
    this.originalRequest = request;
    this.contextPath = contextPath;
    this.uriParser = new URIParser(contextPath);
    uriParser.parse(request.uri());
    this.inputStream = new NettyServletInputStream((HttpContent) request);
    this.reader = new BufferedReader(new InputStreamReader(inputStream));
    this.channelHandlerContext = ctx;
    if (HttpMethod.POST.equals(request.method())) {
      if (request instanceof FullHttpRequest) {
        HttpPostRequestParameters httpPostRequestParameters =
            new HttpPostRequestParameters(((FullHttpRequest) request).content());
        params = httpPostRequestParameters.getHttpRequestParameters();
      } else {
        params = Collections.emptyMap();
      }
    } else {
      params = new QueryStringDecoder(request.uri()).parameters();
    }
    SslHandler sslHandler = channelHandlerContext.pipeline().get(SslHandler.class);
    if (sslHandler != null) {
      SSLSession session = sslHandler.engine().getSession();
      if (session != null) {
        attributes.put(SSL_CIPHER_SUITE_ATTRIBUTE, session.getCipherSuite());
        try {
          attributes.put(SSL_PEER_CERT_CHAIN_ATTRIBUTE, session.getPeerCertificates());
        } catch (SSLPeerUnverifiedException ex) {
          // do nothing here
        }
      }
    }
  }

  public HttpRequest getOriginalRequest() {
    return originalRequest;
  }

  @Override
  public String getContextPath() {
    return contextPath;
  }

  @Override
  public Cookie[] getCookies() {
    String cookieString = this.originalRequest.headers().get(HttpHeaderNames.COOKIE);
    if (cookieString != null) {
      Set<io.netty.handler.codec.http.cookie.Cookie> cookies =
          ServerCookieDecoder.STRICT.decode(cookieString);
      if (!cookies.isEmpty()) {
        Cookie[] cookiesArray = new Cookie[cookies.size()];
        int indx = 0;
        for (io.netty.handler.codec.http.cookie.Cookie c : cookies) {
          Cookie cookie = new Cookie(c.name(), c.value());
          cookie.setDomain(c.domain());
          cookie.setMaxAge((int) c.maxAge());
          cookie.setPath(c.path());
          cookie.setSecure(c.isSecure());
          cookiesArray[indx] = cookie;
          indx++;
        }
        return cookiesArray;

      }
    }
    return null;
  }

  @Override
  public long getDateHeader(String name) {
    String longVal = getHeader(name);
    if (longVal == null) {
      return -1;
    }

    return Long.parseLong(longVal);
  }

  @Override
  public String getHeader(String name) {
    return this.originalRequest.headers().get(name);
  }

  @Override
  public Enumeration getHeaderNames() {
    return Utils.enumeration(this.originalRequest.headers().names());
  }



  @Override
  public Enumeration getHeaders(String name) {
    return Utils.enumeration(this.originalRequest.headers().getAll(name));
  }

  @Override
  public int getIntHeader(String name) {
    return this.originalRequest.headers().getInt(name, -1);
  }

  @Override
  public String getMethod() {
    return this.originalRequest.method().name();
  }

  @Override
  public String getQueryString() {
    return this.uriParser.getQueryString();
  }

  @Override
  public String getRequestURI() {
    return this.uriParser.getRequestUri();
  }

  @Override
  public StringBuffer getRequestURL() {
    StringBuffer url = new StringBuffer();
    String scheme = this.getScheme();
    int port = this.getServerPort();
    String urlPath = this.getRequestURI();


    url.append(scheme); // http, https
    url.append("://");
    url.append(this.getServerName());
    if (("http".equalsIgnoreCase(scheme) && port != 80)
        || ("https".equalsIgnoreCase(scheme) && port != 443)) {
      url.append(':');
      url.append(this.getServerPort());
    }

    url.append(urlPath);
    return url;
  }

  @Override
  public int getContentLength() {
    return HttpUtil.getContentLength(this.originalRequest, -1);
  }

  @Override
  public String getContentType() {
    return this.originalRequest.headers().get(HttpHeaderNames.CONTENT_TYPE);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return this.inputStream;
  }

  @Override
  public String getCharacterEncoding() {
    if (characterEncoding == null) {
      characterEncoding = Utils.getCharsetFromContentType(this.getContentType());
    }
    return this.characterEncoding;
  }

  @Override
  public String getParameter(String name) {
    String[] values = getParameterValues(name);
    return values != null ? values[0] : null;
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return Utils.enumerationFromKeys(this.params);
  }

  @Override
  public String[] getParameterValues(String name) {
    List<String> values = this.params.get(name);
    if (values == null || values.isEmpty())
      return null;
    return values.toArray(new String[values.size()]);
  }

  @Override
  public Map getParameterMap() {
    return this.params;
  }

  @Override
  public String getProtocol() {
    return this.originalRequest.protocolVersion().toString();
  }

  @Override
  public Object getAttribute(String name) {
    if (attributes != null) {
      return this.attributes.get(name);
    }
    return null;
  }

  @Override
  public Enumeration getAttributeNames() {
    return Utils.enumerationFromKeys(this.attributes);
  }

  @Override
  public void removeAttribute(String name) {
    if (this.attributes != null) {
      this.attributes.remove(name);
    }
  }

  @Override
  public void setAttribute(String name, Object o) {
    this.attributes.put(name, o);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return this.reader;
  }

  @Override
  public String getRequestedSessionId() {
    // doesn't implement it yet
    return null;
  }

  @Override
  public HttpSession getSession() {
    // doesn't implement it yet
    return null;
  }

  @Override
  public HttpSession getSession(boolean create) {
    // doesn't implement it yet
    return null;
  }

  @Override
  public String getPathInfo() {
    return this.uriParser.getPathInfo();
  }

  @Override
  public Locale getLocale() {
    String locale = this.originalRequest.headers().get(HttpHeaderNames.ACCEPT_LANGUAGE,
        DEFAULT_LOCALE.toString());
    return new Locale(locale);
  }

  @Override
  public String getRemoteAddr() {
    InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().remoteAddress();
    return addr.getAddress().getHostAddress();
  }

  @Override
  public String getRemoteHost() {
    InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().remoteAddress();
    return addr.getHostName();
  }

  @Override
  public int getRemotePort() {
    InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().remoteAddress();
    return addr.getPort();
  }

  @Override
  public String getServerName() {
    InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().localAddress();
    return addr.getHostName();
  }

  @Override
  public int getServerPort() {
    InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().localAddress();
    return addr.getPort();
  }

  @Override
  public String getServletPath() {
    String servletPath = this.uriParser.getServletPath();
    if ("/".equals(servletPath)) {
      return "";
    }
    return servletPath;
  }

  @Override
  public String getScheme() {
    return this.isSecure() ? "https" : "http";
  }

  @Override
  public boolean isSecure() {
    return ChannelThreadLocal.get().pipeline().get(SslHandler.class) != null;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return true;
  }

  @Override
  public String getLocalAddr() {
    InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().localAddress();
    return addr.getAddress().getHostAddress();
  }

  @Override
  public String getLocalName() {
    return getServerName();
  }

  @Override
  public int getLocalPort() {
    return getServerPort();
  }

  @Override
  public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    this.characterEncoding = env;
  }

  @Override
  public Enumeration getLocales() {
    Collection<Locale> locales = Utils.parseAcceptLanguageHeader(
        this.originalRequest.headers().get(HttpHeaderNames.ACCEPT_LANGUAGE));

    if (locales == null || locales.isEmpty()) {
      locales = new ArrayList<>();
      locales.add(Locale.getDefault());
    }
    return Utils.enumeration(locales);
  }

  @Override
  public String getAuthType() {
    throw new IllegalStateException("Method 'getAuthType' not yet implemented!");
  }

  @Override
  public String getPathTranslated() {
    throw new IllegalStateException("Method 'getPathTranslated' not yet implemented!");
  }

  @Override
  public String getRemoteUser() {
    throw new IllegalStateException("Method 'getRemoteUser' not yet implemented!");
  }

  @Override
  public Principal getUserPrincipal() {
    throw new IllegalStateException("Method 'getUserPrincipal' not yet implemented!");
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    throw new IllegalStateException("Method 'isRequestedSessionIdFromURL' not yet implemented!");
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    throw new IllegalStateException("Method 'isRequestedSessionIdFromUrl' not yet implemented!");
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return false;
  }

  @Override
  public boolean isUserInRole(String role) {
    throw new IllegalStateException("Method 'isUserInRole' not yet implemented!");
  }

  @Override
  public String getRealPath(String path) {
    throw new IllegalStateException("Method 'getRealPath' not yet implemented!");
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String path) {
    throw new IllegalStateException("Method 'getRequestDispatcher' not yet implemented!");
  }

  @Override
  public long getContentLengthLong() {
    throw new IllegalStateException("Method 'getContentLengthLong' not yet implemented!");
  }

  @Override
  public ServletContext getServletContext() {
    throw new IllegalStateException("Method 'getRequestDispatcher' not yet implemented!");
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new IllegalStateException("Method 'startAsync' not yet implemented!");
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    throw new IllegalStateException("Method 'startAsync' not yet implemented!");
  }

  @Override
  public boolean isAsyncStarted() {
    throw new IllegalStateException("Method 'isAsyncStarted' not yet implemented!");
  }

  @Override
  public boolean isAsyncSupported() {
    throw new IllegalStateException("Method 'isAsyncSupported' not yet implemented!");
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new IllegalStateException("Method 'getRequestDispatcher' not yet implemented!");
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new IllegalStateException("Method 'getDispatcherType' not yet implemented!");
  }

  @Override
  public String changeSessionId() {
    throw new IllegalStateException("Method 'changeSessionId' not yet implemented!");
  }

  @Override
  public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    throw new IllegalStateException("Method 'authenticate' not yet implemented!");
  }

  @Override
  public void login(String username, String password) throws ServletException {
    throw new IllegalStateException("Method 'login' not yet implemented!");

  }

  @Override
  public void logout() throws ServletException {
    throw new IllegalStateException("Method 'logout' not yet implemented!");
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    throw new IllegalStateException("Method 'getParts' not yet implemented!");
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    throw new IllegalStateException("Method  'getPart' not yet implemented!");
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
      throws IOException, ServletException {
    throw new IllegalStateException("Method not yet implemented!");
  }
}
