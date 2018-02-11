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
package io.github.tesla.rule.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.tesla.rule.RequestFilterType;
import io.github.tesla.rule.domain.FilterRuleDO;

/**
 * @author liushiming
 * @version FilterRuleDao.java, v 0.0.1 2018年2月11日 下午2:36:34 liushiming
 */
@Mapper
public interface FilterRuleDao {

  FilterRuleDO get(Long ruleId);

  List<FilterRuleDO> getByFilterType(@Param("filterType") RequestFilterType type);

  List<FilterRuleDO> list(Map<String, Object> map);

  int save(FilterRuleDO route);

  int update(FilterRuleDO route);

  int remove(Long id);

  int removeByRouteId(Long routeId);

  int batchRemove(Long[] routeIds);
}
