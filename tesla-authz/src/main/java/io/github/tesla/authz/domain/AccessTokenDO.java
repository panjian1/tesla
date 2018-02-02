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
import java.util.Date;

/**
 * @author liushiming
 * @version Oauth2AccessToken.java, v 0.0.1 2018年2月2日 上午11:37:53 liushiming
 */
public class AccessTokenDO implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * 默认的 refresh_token 的有效时长: 30天
   */
  private static final int REFRESH_TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * 30;
  /**
   * 默认的 access_token 的有效时长: 12小时
   */
  private static final int ACCESS_TOKEN_VALIDITY_SECONDS = 60 * 60 * 12;

  private static final String BEARER_TYPE = "Bearer";

  private Long id;

  private String tokenId;

  private String username;

  private String clientId;

  private String authenticationId;

  private String refreshToken;

  private String tokenType = BEARER_TYPE;

  private int tokenExpiredSeconds = ACCESS_TOKEN_VALIDITY_SECONDS;

  private int refreshTokenExpiredSeconds = REFRESH_TOKEN_VALIDITY_SECONDS;

  private Timestamp gmtCreate;

  private Timestamp gmtModified;


  public boolean tokenExpired() {
    final long time = gmtCreate.getTime() + (this.tokenExpiredSeconds * 1000L);
    return time < new Date().getTime();
  }

  public boolean refreshTokenExpired() {
    final long time = gmtCreate.getTime() + (this.refreshTokenExpiredSeconds * 1000L);
    return time < new Date().getTime();
  }

  public long currentTokenExpiredSeconds() {
    if (tokenExpired()) {
      return -1;
    }
    final long time = gmtCreate.getTime() + (this.tokenExpiredSeconds * 1000L);
    return (time - new Date().getTime()) / 1000L;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
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

  public String getAuthenticationId() {
    return authenticationId;
  }

  public void setAuthenticationId(String authenticationId) {
    this.authenticationId = authenticationId;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public int getTokenExpiredSeconds() {
    return tokenExpiredSeconds;
  }

  public void setTokenExpiredSeconds(int tokenExpiredSeconds) {
    this.tokenExpiredSeconds = tokenExpiredSeconds;
  }

  public int getRefreshTokenExpiredSeconds() {
    return refreshTokenExpiredSeconds;
  }

  public void setRefreshTokenExpiredSeconds(int refreshTokenExpiredSeconds) {
    this.refreshTokenExpiredSeconds = refreshTokenExpiredSeconds;
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "Oauth2AccessToken [id=" + id + ", tokenId=" + tokenId + ", username=" + username
        + ", clientId=" + clientId + ", authenticationId=" + authenticationId + ", refreshToken="
        + refreshToken + ", tokenType=" + tokenType + ", tokenExpiredSeconds=" + tokenExpiredSeconds
        + ", refreshTokenExpiredSeconds=" + refreshTokenExpiredSeconds + ", gmtCreate=" + gmtCreate
        + ", gmtModified=" + gmtModified + "]";
  }



}
