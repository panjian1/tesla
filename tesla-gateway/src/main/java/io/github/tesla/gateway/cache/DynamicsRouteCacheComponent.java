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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.google.common.collect.Maps;

import io.github.tesla.rule.dao.RouteDao;
import io.github.tesla.rule.dao.RpcDao;
import io.github.tesla.rule.domain.RouteDO;
import io.github.tesla.rule.domain.RpcDO;

/**
 * @author liushiming
 * @version RouteCacheComponent.java, v 0.0.1 2018年1月26日 上午11:25:08 liushiming
 */
@Component
public class DynamicsRouteCacheComponent extends AbstractScheduleCache {

  private static final PathMatcher pathMatcher = new AntPathMatcher();

  private static final Map<String, RouteDO> ROUTE_CACHE = Maps.newConcurrentMap();

  private static final Map<Long, RpcDO> RPC_CACHE = Maps.newConcurrentMap();

  @Autowired
  private RouteDao routeDao;

  @Autowired
  private RpcDao rpcDao;

  public DynamicsRouteCacheComponent() {
    super();
  }

  @Override
  protected void doPoller() {
    // clear data
    ROUTE_CACHE.clear();
    RPC_CACHE.clear();
    // cache all data
    List<RouteDO> routes = routeDao.list(Maps.newHashMap());
    for (RouteDO route : routes) {
      RouteDO routeCopy = route.copy();
      String path = routeCopy.getFromPath();
      ROUTE_CACHE.put(path, routeCopy);
    }
    List<RpcDO> rpcs = rpcDao.list(Maps.newHashMap());
    for (RpcDO rpc : rpcs) {
      RpcDO rpcCopy = rpc.copy();
      RPC_CACHE.put(rpcCopy.getRouteId(), rpcCopy);
    }
  }


  public RouteDO getRoute(String actorPath) {
    Set<String> allRoutePath = ROUTE_CACHE.keySet();
    for (String path : allRoutePath) {
      if (path.equals(actorPath) || pathMatcher.match(path, actorPath)) {
        try {
          return ROUTE_CACHE.get(path);
        } catch (Throwable e) {
          return null;
        }
      }
    }
    return null;

  }

  public RpcDO getRpc(String actorPath) {
    RouteDO route = getRoute(actorPath);
    if (route != null) {
      Long routeId = route.getId();
      try {
        return RPC_CACHE.get(routeId);
      } catch (Throwable e) {
        return null;
      }
    }
    return null;
  }



}
