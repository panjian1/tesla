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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.google.common.collect.Maps;

import io.github.tesla.filter.dao.ApiDao;
import io.github.tesla.filter.dao.ApiRpcDao;
import io.github.tesla.filter.dao.ApiSpringCloudDao;
import io.github.tesla.filter.domain.ApiDO;
import io.github.tesla.filter.domain.ApiGroupDO;
import io.github.tesla.filter.domain.ApiRpcDO;
import io.github.tesla.filter.domain.ApiSpringCloudDO;

/**
 * @author liushiming
 * @version RouteCacheComponent.java, v 0.0.1 2018年1月26日 上午11:25:08 liushiming
 */
@Component
public class DynamicsRouteCacheComponent extends AbstractScheduleCache {

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

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  @Autowired
  private ApiDao apiDao;

  @Autowired
  private ApiRpcDao rpcDao;

  @Autowired
  private ApiSpringCloudDao springCloudDao;


  @Override
  protected void doCache() {
    try {
      readWriteLock.writeLock().lock();
      // cache all data
      List<ApiDO> apis = apiDao.list(Maps.newHashMap());
      for (ApiDO api : apis) {
        ApiDO apiClone = api.copy();
        String url = apiClone.getUrl();
        Long apiId = apiClone.getId();
        ApiGroupDO group = apiClone.getApiGroup();
        // 直接路由
        String backEndHost = group.getBackendHost();
        String backEndPort = group.getBackendPort();
        String backEndPath = group.getBackendPath();
        if (backEndHost != null && backEndPort != null && backEndPath != null) {
          REDIRECT_ROUTE.put(url,
              new MutablePair<String, String>(backEndHost + ":" + backEndPort, backEndPath));
        } else if (apiClone.getRpc()) {
          ApiRpcDO rpc = rpcDao.get(apiId);
          RPC_ROUTE.put(url, rpc);
        } else if (apiClone.getSpringCloud()) {
          ApiSpringCloudDO springCloud = springCloudDao.get(apiId);
          SPRINGCLOUD_ROUTE.put(url, springCloud);
        }
      }
    } finally {
      readWriteLock.writeLock().unlock();
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

}
