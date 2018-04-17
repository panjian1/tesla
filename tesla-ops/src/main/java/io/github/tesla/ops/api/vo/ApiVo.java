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
package io.github.tesla.ops.api.vo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import io.github.tesla.filter.RequestFilterTypeEnum;
import io.github.tesla.filter.ResponseFilterTypeEnum;
import io.github.tesla.filter.domain.ApiDO;
import io.github.tesla.filter.domain.ApiRpcDO;
import io.github.tesla.filter.domain.ApiSpringCloudDO;

/**
 * @author liushiming
 * @version ApiVo.java, v 0.0.1 2018年4月17日 下午2:17:58 liushiming
 */
public class ApiVo implements Serializable {

  private static final long serialVersionUID = 8303012923548625829L;

  private Long id;

  private String name;

  private String describe;

  private String url;

  private String path;

  private Boolean rpc;

  private Boolean springCloud;

  private Long groupId;

  private String groupName;

  private Timestamp gmtCreate;

  private Timestamp gmtModified;

  // RPC
  private String serviceName;

  private String methodName;

  private String serviceGroup;

  private String serviceVersion;

  private byte[] protoContext;

  private String inputParam;

  // Spring Cloud
  private String instanceId;

  private String scPath;

  // Filter

  private List<RequestFilterTypeEnum> requestFilterType;

  private List<ResponseFilterTypeEnum> responseFilterType;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescribe() {
    return describe;
  }

  public void setDescribe(String describe) {
    this.describe = describe;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Boolean getRpc() {
    return rpc;
  }

  public void setRpc(Boolean rpc) {
    this.rpc = rpc;
  }

  public Boolean getSpringCloud() {
    return springCloud;
  }

  public void setSpringCloud(Boolean springCloud) {
    this.springCloud = springCloud;
  }

  public Long getGroupId() {
    return groupId;
  }

  public void setGroupId(Long groupId) {
    this.groupId = groupId;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public Timestamp getGmtCreate() {
    return gmtCreate;
  }

  public void setGmtCreate(Timestamp gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  public Timestamp getGmtModified() {
    return gmtModified;
  }

  public void setGmtModified(Timestamp gmtModified) {
    this.gmtModified = gmtModified;
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

  public String getInputParam() {
    return inputParam;
  }

  public void setInputParam(String inputParam) {
    this.inputParam = inputParam;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getScPath() {
    return scPath;
  }

  public void setScPath(String scPath) {
    this.scPath = scPath;
  }

  public ApiDO buildApiDO() {
    ApiDO apiDO = new ApiDO();
    return apiDO;
  }

  public ApiRpcDO buildApiRpcDO() {
    ApiRpcDO rpcDO = new ApiRpcDO();
    return rpcDO;
  }

  public ApiSpringCloudDO buildApiSpringCloudDO() {
    ApiSpringCloudDO springCloudDO = new ApiSpringCloudDO();
    return springCloudDO;
  }


  public static ApiVo buildApiVO(ApiDO apiDO, ApiRpcDO rpcDO, ApiSpringCloudDO scDO) {
    if (apiDO != null) {
      ApiVo apiVO = new ApiVo();
      apiVO.setId(apiDO.getId());
      apiVO.setName(apiDO.getName());
      apiVO.setDescribe(apiDO.getDescribe());
      apiVO.setUrl(apiDO.getUrl());
      apiVO.setPath(apiDO.getPath());
      apiVO.setRpc(apiDO.getRpc());
      apiVO.setSpringCloud(apiDO.getSpringCloud());
      apiVO.setGmtCreate(apiDO.getGmtCreate());
      apiVO.setGmtModified(apiDO.getGmtModified());
      apiVO.setGroupId(apiDO.getApiGroup().getId());
      apiVO.setGroupName(apiDO.getApiGroup().getName());
      // RPC
      apiVO.setServiceName(rpcDO.getServiceName());
      apiVO.setMethodName(rpcDO.getMethodName());
      apiVO.setServiceGroup(rpcDO.getServiceGroup());
      apiVO.setServiceVersion(rpcDO.getServiceVersion());
      apiVO.setProtoContext(rpcDO.getProtoContext());
      apiVO.setInputParam(rpcDO.getInputParam());
      // Spring Cloud
      apiVO.setInstanceId(scDO.getInstanceId());
      apiVO.setScPath(scDO.getPath());
      return apiVO;
    }
    return null;
  }


}
