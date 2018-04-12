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
package io.github.tesla.filter.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import org.springframework.beans.BeanUtils;

/**
 * @author liushiming
 * @version ApiDO.java, v 0.0.1 2018年1月4日 上午10:28:15 liushiming
 */
public class ApiDO implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String name;

  private String describe;

  private String url;

  private String path;

  private Boolean rpc;

  private Boolean springCloud;

  private ApiGroupDO apiGroup;

  private Timestamp gmtCreate;

  private Timestamp gmtModified;

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

  public ApiGroupDO getApiGroup() {
    return apiGroup;
  }

  public void setApiGroup(ApiGroupDO apiGroup) {
    this.apiGroup = apiGroup;
  }

  public void setSpringCloud(Boolean springCloud) {
    this.springCloud = springCloud;
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

  public ApiDO copy(ApiDO source) {
    ApiDO target = new ApiDO();
    BeanUtils.copyProperties(source, target);
    return target;
  }


}
