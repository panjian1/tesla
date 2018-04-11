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
package io.github.tesla.ops.api.dto;

import java.io.Serializable;

import io.github.tesla.filter.domain.FilterRouteDO;
import io.github.tesla.filter.domain.FilterRpcDO;

/**
 * @author liushiming
 * @version GateWayRouteDto.java, v 0.0.1 2018年1月5日 上午10:47:04 liushiming
 */
public class APIRouteDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long routeId;

  private String fromPath;

  private String toHostport;

  private String toPath;

  private String serviceId;

  private Boolean rpc = false;

  private String serviceName;

  private String methodName;

  private String serviceGroup;

  private String serviceVersion;

  private byte[] protoContext;

  private String inputParam;

  public String getFromPath() {
    return fromPath;
  }

  public void setFromPath(String fromPath) {
    this.fromPath = fromPath;
  }


  public String getToHostport() {
    return toHostport;
  }

  public void setToHostport(String toHostport) {
    this.toHostport = toHostport;
  }

  public String getToPath() {
    return toPath;
  }

  public void setToPath(String toPath) {
    this.toPath = toPath;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public Boolean getRpc() {
    return rpc;
  }

  public void setRpc(Boolean rpc) {
    this.rpc = rpc;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getServiceGroup() {
    return serviceGroup;
  }

  public void setServiceGroup(String serviceGroup) {
    this.serviceGroup = serviceGroup;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = serviceVersion;
  }

  public byte[] getProtoContext() {
    return protoContext;
  }

  public void setProtoContext(byte[] protoContext) {
    this.protoContext = protoContext;
  }

  public Long getRouteId() {
    return routeId;
  }

  public void setRouteId(Long routeId) {
    this.routeId = routeId;
  }

  public String getInputParam() {
    return inputParam;
  }

  public void setInputParam(String inputParam) {
    this.inputParam = inputParam;
  }

  public FilterRouteDO buildRoute() {
    FilterRouteDO routeDo = new FilterRouteDO();
    if (this.routeId != null && this.routeId != 0) {
      routeDo.setId(this.routeId);
    }
    routeDo.setFromPath(this.fromPath);
    routeDo.setServiceId(this.serviceId);
    routeDo.setToHostport(this.toHostport);
    routeDo.setToPath(this.toPath);
    routeDo.setRpc(this.rpc);
    return routeDo;
  }

  public FilterRpcDO buildRpc() {
    FilterRpcDO rpcDO = new FilterRpcDO();
    rpcDO.setServiceName(this.serviceName);
    rpcDO.setMethodName(this.methodName);
    rpcDO.setServiceGroup(this.serviceGroup);
    rpcDO.setServiceVersion(this.serviceVersion);
    rpcDO.setProtoContext(this.protoContext);
    rpcDO.setInputParam(this.inputParam);
    rpcDO.setRouteId(this.routeId);
    return rpcDO;
  }

  public static APIRouteDto buildRouteDto(FilterRouteDO route, FilterRpcDO rpc) {
    APIRouteDto routeDto = new APIRouteDto();
    routeDto.setRouteId(route.getId());
    routeDto.setFromPath(route.getFromPath());
    routeDto.setServiceId(route.getServiceId());
    routeDto.setRpc(route.getRpc());
    routeDto.setToHostport(route.getToHostport());
    routeDto.setToPath(route.getToPath());

    if (rpc != null) {
      routeDto.setServiceName(rpc.getServiceName());
      routeDto.setMethodName(rpc.getMethodName());
      routeDto.setServiceGroup(rpc.getServiceName());
      routeDto.setServiceVersion(rpc.getServiceVersion());
      routeDto.setProtoContext(rpc.getProtoContext());
      routeDto.setInputParam(rpc.getInputParam());
    }
    return routeDto;
  }

  public static APIRouteDto buildRouteDto(FilterRouteDO route) {
    APIRouteDto routeDto = new APIRouteDto();
    routeDto.setRouteId(route.getId());
    routeDto.setFromPath(route.getFromPath());
    routeDto.setServiceId(route.getServiceId());
    routeDto.setRpc(route.getRpc());
    routeDto.setToHostport(route.getToHostport());
    routeDto.setToPath(route.getToPath());
    return routeDto;
  }



}
