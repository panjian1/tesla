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
package io.github.tesla.gateway.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.github.tesla.gateway.netty.filter.request.HttpRequestFilter;
import io.github.tesla.rule.dao.FilterRuleDao;
import io.github.tesla.rule.domain.FilterRuleDO;

/**
 * @author liushiming
 * @version FilterRuleCacheComponent.java, v 0.0.1 2018年1月29日 下午6:08:08 liushiming
 */
@Component
public class FilterRuleCacheComponent extends AbstractScheduleCache {

  @Autowired
  private FilterRuleDao rilterRuleDao;

  // 针对所有url的过滤规则
  private static final Map<String, List<String>> COMMUNITY_RULE_CACHE = Maps.newConcurrentMap();

  // 针对特定url的过滤规则
  private static final Map<String, Map<String, List<String>>> URL_RULE_CACHE =
      Maps.newConcurrentMap();

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private static final String LINE_SEPARATOR_UNIX = "\n";

  private static final String LINE_SEPARATOR_WINDOWS = "\r\n";


  @Override
  protected void doCache() {
    try {
      readWriteLock.writeLock().lock();
      COMMUNITY_RULE_CACHE.clear();
      URL_RULE_CACHE.clear();
      List<FilterRuleDO> filterRuleDOs = rilterRuleDao.list(Maps.newHashMap());
      for (FilterRuleDO ruleDO : filterRuleDOs) {
        String type = ruleDO.getFilterType().name();
        String rule = ruleDO.getRule();
        String url = ruleDO.getUrl();
        if (StringUtils.isEmpty(url)) {
          List<String> rules = COMMUNITY_RULE_CACHE.get(type);
          if (rules == null) {
            rules = Lists.newLinkedList();
            COMMUNITY_RULE_CACHE.put(type, rules);
          }
          if (StringUtils.contains(rule, LINE_SEPARATOR_UNIX)) {
            String[] rulesSplits = StringUtils.split(rule, LINE_SEPARATOR_UNIX);
            rules.addAll(Arrays.asList(rulesSplits));
          } else if (StringUtils.contains(rule, LINE_SEPARATOR_WINDOWS)) {
            String[] rulesSplits = StringUtils.split(rule, LINE_SEPARATOR_UNIX);
            rules.addAll(Arrays.asList(rulesSplits));
          } else {
            rules.add(rule);
          }
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
          if (StringUtils.contains(rule, LINE_SEPARATOR_UNIX)) {
            String[] rulesSplits = StringUtils.split(rule, LINE_SEPARATOR_UNIX);
            rules.addAll(Arrays.asList(rulesSplits));
          } else if (StringUtils.contains(rule, LINE_SEPARATOR_WINDOWS)) {
            String[] rulesSplits = StringUtils.split(rule, LINE_SEPARATOR_UNIX);
            rules.addAll(Arrays.asList(rulesSplits));
          } else {
            rules.add(rule);
          }
        }
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }



  public List<Pattern> getPubicFilterRule(HttpRequestFilter filter) {
    try {
      readWriteLock.readLock().lock();
      String type = filter.filterType().name();
      List<String> patterns = COMMUNITY_RULE_CACHE.get(type);
      List<Pattern> compilePatterns = Lists.newArrayList();
      if (patterns != null) {
        for (String pattern : patterns) {
          try {
            Pattern compilePattern = Pattern.compile(pattern);
            compilePatterns.add(compilePattern);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
      }
      return compilePatterns;
    } finally {
      readWriteLock.readLock().unlock();
    }

  }


  public Map<String, List<String>> getUrlFilterRule(HttpRequestFilter filter) {
    try {
      readWriteLock.readLock().lock();
      String type = filter.filterType().name();
      Map<String, List<String>> patterns = URL_RULE_CACHE.get(type);
      if (patterns == null) {
        patterns = Maps.newConcurrentMap();
      }
      return patterns;
    } finally {
      readWriteLock.readLock().unlock();
    }

  }



}
