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
package io.github.tesla.authz.shiro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author liushiming
 * @version TeslaUserRealm.java, v 0.0.1 2018年1月31日 下午4:14:29 liushiming
 */
public class TeslaUserRealm extends JdbcRealm {

  private static final Logger log = LoggerFactory.getLogger(TeslaUserRealm.class);

  private static final String AUTHENTICATION_QUERY =
      "select user_id,username,password,status from sys_user where username = ?";

  private static final String PERMISSIONS_QUERY =
      "select distinct m.perms from sys_menu m left join sys_role_menu rm on m.menu_id = rm.menu_id left join sys_user_role ur on rm.role_id = ur.role_id where ur.user_id = ?";


  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    if (principals == null) {
      throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
    }
    Long userId = (Long) principals.getPrimaryPrincipal();
    Connection conn = null;
    Set<String> permissions = new LinkedHashSet<String>();
    PreparedStatement ps = null;
    try {
      try {
        conn = dataSource.getConnection();
        ps = conn.prepareStatement(PERMISSIONS_QUERY);
        ps.setLong(1, userId);
        ResultSet rs = null;
        try {
          rs = ps.executeQuery();
          while (rs.next()) {
            String permissionString = rs.getString(1);
            if (!StringUtils.isEmpty(permissionString)) {
              permissions.addAll(Arrays.asList(permissionString.trim().split(",")));
            }
          }
        } finally {
          JdbcUtils.closeResultSet(rs);
        }
      } finally {
        JdbcUtils.closeStatement(ps);
      }
    } catch (SQLException e) {
      final String message = "There was a SQL error while authorizing user [" + userId + "]";
      if (log.isErrorEnabled()) {
        log.error(message, e);
      }
      throw new AuthorizationException(message, e);
    } finally {
      JdbcUtils.closeConnection(conn);
    }
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    info.setStringPermissions(permissions);
    return info;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();
    if (username == null) {
      throw new AccountException("Null usernames are not allowed by this realm.");
    }
    Connection conn = null;
    SimpleAuthenticationInfo info = null;
    try {
      conn = dataSource.getConnection();
      Object[] queryResults = getPasswordForUser(conn, username);
      Long userId = (Long) queryResults[0];
      String password = (String) queryResults[2];
      int status = (Integer) queryResults[3];
      if (password == null) {
        throw new UnknownAccountException("No account found for user [" + username + "]");
      }
      if (!password.equals(new String((char[]) token.getCredentials()))) {
        throw new IncorrectCredentialsException(
            "Password or account is not right for user [" + username + "]");
      }
      if (status == 0) {
        throw new LockedAccountException("account is locked for user [" + username + "]");
      }
      info = new SimpleAuthenticationInfo(userId, password.toCharArray(), username);
      info.setCredentialsSalt(ByteSource.Util.bytes(username));
    } catch (SQLException e) {
      final String message = "There was a SQL error while authenticating user [" + username + "]";
      if (log.isErrorEnabled()) {
        log.error(message, e);
      }
      throw new AuthenticationException(message, e);
    } finally {
      JdbcUtils.closeConnection(conn);
    }

    return info;
  }

  private Object[] getPasswordForUser(Connection conn, String username) throws SQLException {
    Object[] result = new Object[4];
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = conn.prepareStatement(AUTHENTICATION_QUERY);
      ps.setString(1, username);
      rs = ps.executeQuery();
      boolean foundResult = false;
      while (rs.next()) {
        if (foundResult) {
          throw new AuthenticationException("More than one user row found for user [" + username
              + "]. Usernames must be unique.");
        }
        result[0] = rs.getLong(1);
        result[1] = rs.getString(2);
        result[2] = rs.getString(3);
        result[3] = rs.getInt(4);
        foundResult = true;
      }
    } finally {
      JdbcUtils.closeResultSet(rs);
      JdbcUtils.closeStatement(ps);
    }
    return result;
  }



}
