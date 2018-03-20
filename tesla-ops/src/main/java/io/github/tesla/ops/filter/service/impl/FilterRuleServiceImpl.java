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
package io.github.tesla.ops.filter.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.tesla.filter.dao.FilterRuleDao;
import io.github.tesla.filter.domain.FilterRuleDO;
import io.github.tesla.ops.filter.service.FilterRuleService;
import io.github.tesla.ops.system.domain.PageDO;
import io.github.tesla.ops.utils.Query;

/**
 * @author liushiming
 * @version FilterRuleServiceImpl.java, v 0.0.1 2018年3月20日 上午11:02:55 liushiming
 */
@Service
public class FilterRuleServiceImpl implements FilterRuleService {

  private FilterRuleDao ruleDao;

  @Override
  public PageDO<FilterRuleDO> queryList(Query query) {
    int total = ruleDao.count(query);
    List<FilterRuleDO> rules = ruleDao.list(query);
    PageDO<FilterRuleDO> page = new PageDO<>();
    page.setTotal(total);
    page.setRows(rules);
    return page;
  }

  @Override
  public FilterRuleDO get(Long routeId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<FilterRuleDO> list(Map<String, Object> map) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int count(Map<String, Object> map) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int save(FilterRuleDO zuulDto) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int update(FilterRuleDO zuulDto) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int remove(Long routeId) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int batchRemove(Long[] routeIds) {
    // TODO Auto-generated method stub
    return 0;
  }

}
