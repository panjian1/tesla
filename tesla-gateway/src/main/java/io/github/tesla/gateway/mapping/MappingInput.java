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
package io.github.tesla.gateway.mapping;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import io.github.tesla.gateway.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.CharsetUtil;

/**
 * @author liushiming
 * @version MappingInput.java, v 0.0.1 2018年4月23日 下午6:37:32 liushiming
 */
public class MappingInput {

  private final String body;

  private final Object document;

  public MappingInput(HttpContent httpConent) {
    ByteBuf jsonBuf = httpConent.content();
    this.body = jsonBuf.toString(CharsetUtil.UTF_8);;
    this.document = Configuration.builder()//
        .options(Option.DEFAULT_PATH_LEAF_TO_NULL)//
        .jsonProvider(JsonUtils.jsonProvider)//
        .build()//
        .jsonProvider().parse(body);
  }

  /**
   * <pre>
   * 此函数计算 JSONPath 表达式并以 JSON 字符串形式返回结果。 
   * 例如，$input.json('$.pets') 将返回一个表示宠物结构的 JSON 字符串。
   * </pre>
   */
  public String json(String expression) {
    Object json = JsonPath.parse(document).read(expression);
    return json.toString();
  }

  /**
   * <pre>
   *获取一个 JSONPath 表达式字符串 (x) 并返回结果的对象表示形式。
   *这样，您便可通过 FreeMarker 模板语言 (VTL) 在本机访问和操作负载的元素。
   * </pre>
   */
  public Object path(String expression) {
    Object obj = JsonPath.parse(document).read(expression, Object.class);
    return obj;
  }

  /**
   * 返回您的 API 调用的所有请求参数的映射。
   */
  public String params() {
    return body;
  }

  /**
   * 在给定参数名称字符串name的情况下，返回路径中的方法请求参数值、查询字符串或标头值 (按照该顺序)。
   */
  public String params(String name) {
    if (this.document instanceof com.google.gson.JsonObject) {
      com.google.gson.JsonObject jsonObject = (com.google.gson.JsonObject) document;
      return jsonObject.get(name).toString();
    } else {
      return null;
    }

  }

}
