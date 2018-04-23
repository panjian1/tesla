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
package io.github.tesla.filter;

/**
 * @author liushiming
 * @version RouteType.java, v 0.0.1 2018年4月23日 下午12:00:50 liushiming
 */
public enum RouteType {
  DirectRoute(0), // oauth2
  Rpc(1), // URL参数黑名单参数拦截
  SpringCloud(2); // Cookie黑名单拦截


  private int type;

  RouteType(int type) {
    this.type = type;
  }

  public int type() {
    return type;
  }

  public static Boolean isRpc(RouteType type) {
    return type == RouteType.Rpc;
  }

  public static Boolean isRpc(Integer type) {
    return type == RouteType.Rpc.type;
  }

  public static Boolean isSpringCloud(RouteType type) {
    return type == RouteType.SpringCloud;
  }

  public static Boolean isSpringCloud(Integer type) {
    return type == RouteType.SpringCloud.type;
  }

  public static RouteType fromType(Integer type) {
    for (RouteType routeType : RouteType.values()) {
      if (routeType.type == type) {
        return routeType;
      }
    }
    return null;
  }


}
