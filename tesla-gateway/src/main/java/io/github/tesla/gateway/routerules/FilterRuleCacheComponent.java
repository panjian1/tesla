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
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.tesla.gateway.netty.filter.request.HttpRequestFilter;
import io.github.tesla.rule.FilterTypeEnum;
import io.github.tesla.rule.dao.FilterRuleDao;
import io.github.tesla.rule.domain.FilterRuleDO;

/**
 * @author liushiming
 * @version FilterRuleCacheComponent.java, v 0.0.1 2018年1月29日 下午6:08:08 liushiming
 */
@Component
public class FilterRuleCacheComponent {

  @Autowired
  private FilterRuleDao rilterRuleDao;

  private static final Map<FilterTypeEnum, List<String>> FILTER_RULE_CACHE =
      Maps.newConcurrentMap();

  private boolean running = true;

  private final long INTERVAL = 30000; // 30 seconds

  private final Thread checkerThread = new Thread("TeslaFilterPoller") {
    public void run() {
      while (running) {
        try {
          List<FilterRuleDO> filterRuleDOs = rilterRuleDao.list(Maps.newHashMap());
          FILTER_RULE_CACHE.clear();
          for (FilterRuleDO ruleDO : filterRuleDOs) {
            FilterTypeEnum type = ruleDO.getFilterType();
            String rule = ruleDO.getRule();
            List<String> rules = FILTER_RULE_CACHE.get(type);
            if (rules == null) {
              rules = Lists.newLinkedList();
              FILTER_RULE_CACHE.put(type, rules);
            }
            rules.add(rule);
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
        try {
          TimeUnit.SECONDS.sleep(INTERVAL);
        } catch (InterruptedException e) {
          e.printStackTrace();
          running = false;
        }
      }

    }
  };

  @PostConstruct
  public void init() {
    checkerThread.setDaemon(true);
    checkerThread.start();
  }


  public List<String> getFilterRule(HttpRequestFilter filter) {
    FilterTypeEnum type = filter.filterType();
    List<String> patterns = FILTER_RULE_CACHE.get(type);
    if (patterns == null) {
      patterns = Lists.newArrayList();
    }
    return patterns;
  }


  public Map<String, Double> getRateLimit(HttpRequestFilter filter) {
    return null;
  }
}
