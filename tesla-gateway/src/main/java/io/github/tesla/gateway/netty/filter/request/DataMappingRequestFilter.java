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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.JSONOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import io.github.tesla.filter.RequestFilterTypeEnum;
import io.github.tesla.gateway.mapping.MappingHeader;
import io.github.tesla.gateway.mapping.MappingInput;
import io.github.tesla.gateway.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

/**
 * @author liushiming
 * @version DataMappingRequestFilter.java, v 0.0.1 2018年4月24日 上午9:50:22 liushiming
 */
public class DataMappingRequestFilter extends HttpRequestFilter {

  private final StringTemplateLoader templateHolder = new StringTemplateLoader();

  private final Configuration configuration;

  private DataMappingRequestFilter() {
    Configuration configuration_ = new Configuration(Configuration.VERSION_2_3_26);
    configuration_.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_26));
    configuration_.setOutputFormat(JSONOutputFormat.INSTANCE);
    configuration_.setTemplateLoader(templateHolder);
    this.configuration = configuration_;
  }

  public static HttpRequestFilter newFilter() {
    return new DataMappingRequestFilter();
  }

  @Override
  public HttpResponse doFilter(HttpRequest httpRequest, HttpObject httpObject,
      ChannelHandlerContext channelHandlerContext) {
    if (httpObject instanceof HttpContent) {
      HttpContent httpContent = (HttpContent) httpObject;
      String url = httpRequest.uri();
      int index = url.indexOf("?");
      if (index > -1) {
        url = url.substring(0, index);
      }
      ByteBuf contentBuf = httpContent.content();
      Boolean canDataMapping = isCanDataMapping(contentBuf);
      if (canDataMapping) {
        Map<String, Set<String>> rules = super.getUrlRule(DataMappingRequestFilter.this);
        Set<String> urlRules = rules.get(url);
        if (urlRules != null && urlRules.size() == 1) {
          String tempalteContent = urlRules.iterator().next();
          try {
            templateHolder.putTemplate("template" + url, tempalteContent);
            Map<String, Object> templateContext = new HashMap<String, Object>();
            templateContext.put("header", new MappingHeader(httpRequest));
            templateContext.put("input", new MappingInput(httpContent));
            Template template = configuration.getTemplate("template" + url);
            StringWriter outPutWrite = new StringWriter();
            template.process(templateContext, outPutWrite);
            String outPutJson = outPutWrite.toString();
            ByteBuf bodyContent = Unpooled.copiedBuffer(outPutJson, CharsetUtil.UTF_8);
            contentBuf.clear().writeBytes(bodyContent);
            HttpUtil.setContentLength(httpRequest, outPutJson.length());
          } catch (Throwable e) {
            e.printStackTrace();
            super.writeFilterLog(tempalteContent, this.getClass(), "dataMapping");
          }
        }
      }

    }

    return null;
  }


  private Boolean isCanDataMapping(ByteBuf contentBuf) {
    try {
      String contentStr = contentBuf.toString(CharsetUtil.UTF_8);
      JsonUtils.parse(contentStr);
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  @Override
  public RequestFilterTypeEnum filterType() {
    return RequestFilterTypeEnum.DataMappingRequestFilter;
  }

}
