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
package io.github.tesla.gateway.protocol;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.JSONOutputFormat;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import io.github.tesla.filter.domain.ApiRpcDO;
import io.github.tesla.gateway.mapping.MappingHeader;
import io.github.tesla.gateway.mapping.MappingInput;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author liushiming
 * @version RpcDynamicClient.java, v 0.0.1 2018年1月29日 下午3:59:33 liushiming
 */
public abstract class RpcDynamicClient {

  private final Configuration configuration;

  private final StringTemplateLoader templateHolder = new StringTemplateLoader();

  public abstract String doRemoteCall(final ApiRpcDO rpcDo, final FullHttpRequest jsonInput);

  public RpcDynamicClient() {
    Configuration configuration_ = new Configuration(Configuration.VERSION_2_3_26);
    configuration_.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_26));
    configuration_.setOutputFormat(JSONOutputFormat.INSTANCE);
    configuration_.setTemplateLoader(templateHolder);
    this.configuration = configuration_;

  }

  protected String cacheTemplate(final ApiRpcDO rpcDo) {
    final String templateContent = rpcDo.getInputTemplate();
    final String templateKey = rpcDo.getServiceName() + "_" + rpcDo.getMethodName();
    templateHolder.putTemplate(templateKey, templateContent);
    return templateKey;
  }

  protected String doDataMapping(final String templateKey, final FullHttpRequest httpRequest)
      throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
      TemplateException {
    Map<String, Object> templateContext = new HashMap<String, Object>();
    templateContext.put("header", new MappingHeader(httpRequest));
    templateContext.put("input", new MappingInput(httpRequest));
    Template template = configuration.getTemplate(templateKey);
    StringWriter outPutWrite = new StringWriter();
    template.process(templateContext, outPutWrite);
    String outPutJson = outPutWrite.toString();
    return outPutJson;
  }
}
