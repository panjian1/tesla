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
package io.github.tesla.gateway.protocol.dubbo;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import io.github.tesla.gateway.mapping.HeaderMapping;
import io.github.tesla.gateway.mapping.BodyMapping;
import io.github.tesla.gateway.protocol.RpcDynamicClient;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author liushiming
 * @version DynamicDubboClient.java, v 0.0.1 2018年1月29日 下午2:38:28 liushiming
 */
public class DynamicDubboClient extends RpcDynamicClient {

  private final ApplicationConfig applicationConfig;

  private final RegistryConfig registryConfig;

  private final StringTemplateLoader templateHolder = new StringTemplateLoader();

  private final Configuration configuration;

  public DynamicDubboClient(final ApplicationConfig applicationConfig,
      RegistryConfig registryConfig) {
    super();
    this.applicationConfig = applicationConfig;
    this.registryConfig = registryConfig;
    Configuration configuration_ = new Configuration(Configuration.VERSION_2_3_26);
    configuration_.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_26));
    configuration_.setOutputFormat(JSONOutputFormat.INSTANCE);
    configuration_.setTemplateLoader(templateHolder);
    this.configuration = configuration_;
  }

  private String cacheTemplate(final ApiRpcDO rpcDo) {
    final String templateContent = rpcDo.getDubboParamTemplate();
    final String templateKey = rpcDo.getServiceName() + "_" + rpcDo.getMethodName();
    templateHolder.putTemplate(templateKey, templateContent);
    return templateKey;
  }

  private String doDataMapping(final String templateKey, final FullHttpRequest httpRequest)
      throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
      TemplateException {
    Map<String, Object> templateContext = new HashMap<String, Object>();
    templateContext.put("header", new HeaderMapping(httpRequest));
    templateContext.put("input", new BodyMapping(httpRequest));
    Template template = configuration.getTemplate(templateKey);
    StringWriter outPutWrite = new StringWriter();
    template.process(templateContext, outPutWrite);
    String outPutJson = outPutWrite.toString();
    return outPutJson;
  }

  @Override
  public String doRemoteCall(final ApiRpcDO rpcDo, final FullHttpRequest httpRequest) {
    try {
      final String serviceName = rpcDo.getServiceName();
      final String methodName = rpcDo.getMethodName();
      final String group = rpcDo.getServiceGroup();
      final String version = rpcDo.getServiceVersion();
      ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
      reference.setApplication(applicationConfig);
      reference.setRegistry(registryConfig);
      reference.setInterface(serviceName);
      reference.setGroup(group);
      reference.setGeneric(true);
      reference.setCheck(false);
      reference.setVersion(version);
      ReferenceConfigCache cache = ReferenceConfigCache.getCache();
      GenericService genericService = cache.get(reference);
      String templateKey = this.cacheTemplate(rpcDo);
      Pair<String[], Object[]> typeAndValue = this.transformerData(templateKey, httpRequest);
      Object response =
          genericService.$invoke(methodName, typeAndValue.getLeft(), typeAndValue.getRight());
      return JSON.toJSONString(response);
    } catch (Throwable e) {
      throw new IllegalArgumentException(String.format(
          "service definition is wrong,please check the proto file you update,service is %s, method is %s",
          rpcDo.getServiceName(), rpcDo.getMethodName()), e);
    }

  }


  private Pair<String[], Object[]> transformerData(String templateKey,
      final FullHttpRequest httpRequest) throws TemplateNotFoundException,
      MalformedTemplateNameException, ParseException, IOException, TemplateException {
    String outPutJson = this.doDataMapping(templateKey, httpRequest);
    Map<String, Object> dubboParamters = com.alibaba.fastjson.JSON.parseObject(outPutJson);
    List<String> type = Lists.newArrayList();
    List<Object> value = Lists.newArrayList();
    type.addAll(dubboParamters.keySet());
    value.addAll(dubboParamters.values());
    String[] typeArray = new String[type.size()];
    type.toArray(typeArray);
    return new ImmutablePair<String[], Object[]>(typeArray, value.toArray());
  }

  public static void main(String[] args) {
    Map<String, String> dataMapping = Maps.newHashMap();
    dataMapping.put("java.lang.String", "${item.title}");
    dataMapping.put("java.lang.Lang", "${item.title}");
    dataMapping.put("com.data.pojo.bean", "${item.title}");
    System.out.println(JSON.toJSON(dataMapping));
  }



}
