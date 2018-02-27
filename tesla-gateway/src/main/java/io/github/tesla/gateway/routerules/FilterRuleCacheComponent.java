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
package io.github.tesla.gateway.routerules;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import io.github.tesla.gateway.netty.filter.request.HttpRequestFilter;
import io.github.tesla.rule.FilterTypeEnum;
import io.github.tesla.rule.dao.FilterRuleDao;

/**
 * @author liushiming
 * @version FilterRuleCacheComponent.java, v 0.0.1 2018年1月29日 下午6:08:08 liushiming
 */
public class FilterRuleCacheComponent {


  @Autowired
  private FilterRuleDao rilterRuleDao;


  public List<Pattern> getFilterRuleByClass(HttpRequestFilter filter) {
    FilterTypeEnum type = filter.filterType();
    rilterRuleDao.getByFilterType(type);
    return null;
  }


  public Map<String, Double> getRateLimit(HttpRequestFilter filter) {
    return null;
  }
}
