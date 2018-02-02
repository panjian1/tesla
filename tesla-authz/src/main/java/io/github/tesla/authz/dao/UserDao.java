package io.github.tesla.authz.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.github.tesla.authz.domain.Roles;
import io.github.tesla.authz.domain.Users;


@Repository
public class UserDao extends AuthzRowMapper {

  private static final UsersRowMapper usersRowMapper = new UsersRowMapper();
  private static final RolesRowMapper rolesRowMapper = new RolesRowMapper();

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public Users findByUserId(Long userId) {
    String sql = "select u.user_id,u.username,u.password from sys_user u where u.user_id = ?";
    final List<Users> list = this.jdbcTemplate.query(sql, usersRowMapper, userId);
    Users user = list.isEmpty() ? null : list.get(0);
    if (user != null) {
      List<Roles> roles = this.findRoleByUserId(userId);
      for (Roles role : roles) {
        user.roles(role.roleName());
      }
    }
    return user;
  }

  public Users findByUserNamed(String userName) {
    String sql = "select u.user_id,u.username,u.password from sys_user u where u.username = ?";
    final List<Users> list = this.jdbcTemplate.query(sql, usersRowMapper, userName);
    Users user = list.isEmpty() ? null : list.get(0);
    if (user != null) {
      List<Roles> roles = this.findRoleByUserId(user.userId());
      for (Roles role : roles) {
        user.roles(role.roleName());
      }
    }
    return user;
  }

  public List<Roles> findRoleByUserId(Long userId) {
    final List<Roles> list = this.jdbcTemplate.query(
        "select role.role_id,role.role_name from sys_role role left join sys_user_role urole on role.role_id = urole.role_id where urole.user_id = ?",
        rolesRowMapper, userId);
    return list.isEmpty() ? null : list;
  }

  public List<String> findPermissonByUserId(Long userId) {
    String sql = "select permlist.perms from " //
        + "(select distinct m.perms from sys_menu m "//
        + "left join sys_role_menu rm on m.menu_id = rm.menu_id "//
        + "left join sys_user_role ur on rm.role_id = ur.role_id where ur.user_id = ? ) "//
        + "as permlist  where permlist.perms is not null";
    return this.jdbcTemplate.queryForList(sql, new Object[] {userId}, String.class);
  }
}
