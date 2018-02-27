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
package io.github.tesla.rule.domain;

import java.io.Serializable;

import io.github.tesla.rule.FilterTypeEnum;

/**
 * @author liushiming
 * @version FilterRule.java, v 0.0.1 2018年2月11日 下午2:00:13 liushiming
 */
public class FilterRuleDO implements Serializable {

  private static final long serialVersionUID = 8473084736935164540L;

  private Long id;

  private FilterTypeEnum filterType;

  private String rule;

  public FilterTypeEnum getFilterType() {
    return filterType;
  }

  public void setFilterType(FilterTypeEnum filterType) {
    this.filterType = filterType;
  }

  public void setFilterType(String filterType) {
    FilterTypeEnum type = FilterTypeEnum.fromTypeName(filterType);
    if (type != null) {
      this.filterType = type;
    } else {
      throw new java.lang.IllegalArgumentException(
          "no type found in defination,[" + filterType + "]");
    }
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }



}
