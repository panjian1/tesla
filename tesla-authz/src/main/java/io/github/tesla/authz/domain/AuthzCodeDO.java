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
package io.github.tesla.authz.domain;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author liushiming
 * @version Oauth2AuthzCode.java, v 0.0.1 2018年2月2日 上午11:38:57 liushiming
 */
public class AuthzCodeDO implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String code;

  private String username;

  private String clientId;

  private Timestamp gmtCreate;

  private Timestamp gmtModified;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
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

  @Override
  public String toString() {
    return "AuthzCodeDO [id=" + id + ", code=" + code + ", username=" + username + ", clientId="
        + clientId + ", gmtCreate=" + gmtCreate + ", gmtModified=" + gmtModified + "]";
  }



}
