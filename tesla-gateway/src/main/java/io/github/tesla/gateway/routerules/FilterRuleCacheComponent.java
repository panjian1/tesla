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

import org.apache.commons.lang3.StringUtils;
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

  // 针对所有url的过滤规则
  private static final Map<FilterTypeEnum, List<String>> COMMUNITY_RULE_CACHE =
      Maps.newConcurrentMap();

  // 针对特定url的过滤规则
  private static final Map<FilterTypeEnum, Map<String, List<String>>> URL_RULE_CACHE =
      Maps.newConcurrentMap();

  private boolean running = true;

  private final long INTERVAL = 30000; // 30 seconds

  private final Thread checkerThread = new Thread("TeslaFilterPoller") {
    public void run() {
      while (running) {
        try {
          List<FilterRuleDO> filterRuleDOs = rilterRuleDao.list(Maps.newHashMap());
          COMMUNITY_RULE_CACHE.clear();
          URL_RULE_CACHE.clear();
          for (FilterRuleDO ruleDO : filterRuleDOs) {
            FilterTypeEnum type = ruleDO.getFilterType();
            String rule = ruleDO.getRule();
            String url = ruleDO.getUrl();
            if (StringUtils.isEmpty(url)) {
              List<String> rules = COMMUNITY_RULE_CACHE.get(type);
              if (rules == null) {
                rules = Lists.newLinkedList();
                COMMUNITY_RULE_CACHE.put(type, rules);
              }
              rules.add(rule);
            } else {
              Map<String, List<String>> maprules = URL_RULE_CACHE.get(type);
              if (maprules == null) {
                maprules = Maps.newConcurrentMap();
                URL_RULE_CACHE.put(type, maprules);
              }
              List<String> rules = maprules.get(url);
              if (rules == null) {
                rules = Lists.newLinkedList();
                maprules.put(url, rules);
              }
              rules.add(rule);
            }
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


  public List<String> getPubicFilterRule(HttpRequestFilter filter) {
    FilterTypeEnum type = filter.filterType();
    List<String> patterns = COMMUNITY_RULE_CACHE.get(type);
    if (patterns == null) {
      patterns = Lists.newArrayList();
    }
    return patterns;
  }


  public Map<String, List<String>> getUrlFilterRule(HttpRequestFilter filter) {
    FilterTypeEnum type = filter.filterType();
    Map<String, List<String>> patterns = URL_RULE_CACHE.get(type);
    if (patterns == null) {
      patterns = Maps.newConcurrentMap();
    }
    return patterns;
  }
}
