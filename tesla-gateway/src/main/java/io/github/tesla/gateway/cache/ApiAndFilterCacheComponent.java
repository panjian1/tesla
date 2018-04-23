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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.github.tesla.filter.dao.ApiDao;
import io.github.tesla.filter.dao.ApiRpcDao;
import io.github.tesla.filter.dao.ApiSpringCloudDao;
import io.github.tesla.filter.dao.FilterDao;
import io.github.tesla.filter.domain.ApiDO;
import io.github.tesla.filter.domain.ApiGroupDO;
import io.github.tesla.filter.domain.ApiRpcDO;
import io.github.tesla.filter.domain.ApiSpringCloudDO;
import io.github.tesla.filter.domain.FilterDO;
import io.github.tesla.gateway.netty.filter.request.HttpRequestFilter;

/**
 * @author liushiming
 * @version RouteCacheComponent.java, v 0.0.1 2018年1月26日 上午11:25:08 liushiming
 */
@Component
public class ApiAndFilterCacheComponent extends AbstractScheduleCache {

  private static final PathMatcher pathMatcher = new AntPathMatcher();

  // 直接路由
  private static final Map<String, Pair<String, String>> REDIRECT_ROUTE =
      Collections.synchronizedMap(new WeakHashMap<String, Pair<String, String>>());

  // RPC服务发现
  private static final Map<String, ApiRpcDO> RPC_ROUTE =
      Collections.synchronizedMap(new WeakHashMap<String, ApiRpcDO>());

  // SpringCloud服务发现
  private static final Map<String, ApiSpringCloudDO> SPRINGCLOUD_ROUTE =
      Collections.synchronizedMap(new WeakHashMap<String, ApiSpringCloudDO>());

  // 针对所有url的过滤规则,Key是Filter类型
  private static final Map<String, Set<String>> COMMUNITY_RULE_CACHE =
      Collections.synchronizedMap(new WeakHashMap<String, Set<String>>());

  // 针对特定url的过滤规则，外部的Key是Filter类型，内部的key是url
  private static final Map<String, Map<String, Set<String>>> URL_RULE_CACHE =
      Collections.synchronizedMap(new WeakHashMap<String, Map<String, Set<String>>>());

  private static final String LINE_SEPARATOR_UNIX = "\n";

  private static final String LINE_SEPARATOR_WINDOWS = "\r\n";

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  @Autowired
  private ApiDao apiDao;

  @Autowired
  private ApiRpcDao rpcDao;

  @Autowired
  private ApiSpringCloudDao springCloudDao;

  @Autowired
  private FilterDao filterDao;


  @Override
  protected void doCache() {
    try {
      readWriteLock.writeLock().lock();
      List<ApiDO> apis = apiDao.list(Maps.newHashMap());
      for (ApiDO api : apis) {
        ApiDO apiClone = api.copy();
        String url = apiClone.getUrl();
        Long apiId = apiClone.getId();
        ApiGroupDO group = apiClone.getApiGroup();
        this.doCacheRoute(apiClone, url, apiId, group);
        this.doCacheFilter(apiClone, url, apiId, group);
      }
      List<FilterDO> filterDOs = filterDao.loadCommon();
      for (FilterDO filterDO : filterDOs) {
        String type = filterDO.getFilterType().name();
        String rule = filterDO.getRule();
        Set<String> rules = COMMUNITY_RULE_CACHE.get(type);
        if (rules == null) {
          rules = Sets.newHashSet();
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
      }

    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  private void doCacheFilter(ApiDO apiClone, String url, Long apiId, ApiGroupDO group) {
    List<FilterDO> filterDO1 = filterDao.loadByApiId(apiId);
    List<FilterDO> filterDO2 = filterDao.loadByGroupId(group.getId());
    Set<FilterDO> filterDOs = Sets.newHashSet();
    filterDOs.addAll(filterDO1);
    filterDOs.addAll(filterDO2);
    for (FilterDO filterDO : filterDOs) {
      String type = filterDO.getFilterType().name();
      String rule = filterDO.getRule();
      Map<String, Set<String>> maprules = URL_RULE_CACHE.get(type);
      if (maprules == null) {
        maprules = Maps.newConcurrentMap();
        URL_RULE_CACHE.put(type, maprules);
      }
      Set<String> rules = maprules.get(url);
      if (rules == null) {
        rules = Sets.newHashSet();
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

  private void doCacheRoute(ApiDO apiClone, String url, Long apiId, ApiGroupDO group) {
    // 直接路由
    String backEndHost = group.getBackendHost();
    String backEndPort = group.getBackendPort();
    if (backEndHost != null && backEndPort != null) {
      String backEndPath = group.getBackendPath();
      String urlPath = "";
      if (backEndPath != null) {
        urlPath = path(backEndPath) + path(apiClone.getPath());
      } else {
        urlPath = apiClone.getPath();
      }
      REDIRECT_ROUTE.put(url,
          new MutablePair<String, String>(backEndHost + ":" + backEndPort, urlPath));
    } // RPC路由
    else if (apiClone.isRpc()) {
      ApiRpcDO rpc = rpcDao.get(apiId);
      RPC_ROUTE.put(url, rpc);
    } // SpringCloud路由
    else if (apiClone.isSpringCloud()) {
      ApiSpringCloudDO springCloud = springCloudDao.get(apiId);
      SPRINGCLOUD_ROUTE.put(url, springCloud);
    }
  }

  private String path(String path) {
    if (path.startsWith("/")) {
      return path;
    } else {
      return "/" + path;
    }
  }


  public Pair<String, String> getDirectRoute(String actorPath) {
    try {
      readWriteLock.readLock().lock();
      Set<String> allRoutePath = REDIRECT_ROUTE.keySet();
      for (String path : allRoutePath) {
        if (path.equals(actorPath) || pathMatcher.match(path, actorPath)) {
          try {
            return REDIRECT_ROUTE.get(path);
          } catch (Throwable e) {
            return null;
          }
        }
      }
      return null;
    } finally {
      readWriteLock.readLock().unlock();
    }

  }

  public ApiRpcDO getRpcRoute(String actorPath) {
    try {
      readWriteLock.readLock().lock();
      return RPC_ROUTE.get(actorPath);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public ApiSpringCloudDO getSpringCloudRoute(String actorPath) {
    try {
      readWriteLock.readLock().lock();
      return SPRINGCLOUD_ROUTE.get(actorPath);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public Set<Pattern> getPubicFilterRule(HttpRequestFilter filter) {
    try {
      readWriteLock.readLock().lock();
      String type = filter.filterType().name();
      Set<String> patterns = COMMUNITY_RULE_CACHE.get(type);
      Set<Pattern> compilePatterns = Sets.newHashSet();
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


  public Map<String, Set<String>> getUrlFilterRule(HttpRequestFilter filter) {
    try {
      readWriteLock.readLock().lock();
      String type = filter.filterType().name();
      Map<String, Set<String>> patterns = URL_RULE_CACHE.get(type);
      if (patterns == null) {
        patterns = Maps.newConcurrentMap();
      }
      return patterns;
    } finally {
      readWriteLock.readLock().unlock();
    }

  }


}
