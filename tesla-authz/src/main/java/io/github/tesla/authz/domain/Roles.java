package io.github.tesla.authz.domain;

import java.io.Serializable;

public class Roles implements Serializable {

  private static final long serialVersionUID = -7623030531700176287L;

  private Long roleId;

  private String roleName;

  public Long roleId() {
    return roleId;
  }

  public Roles roleId(Long id) {
    this.roleId = id;
    return this;
  }


  public String roleName() {
    return roleName;
  }

  public Roles roleName(String roleName) {
    this.roleName = roleName;
    return this;
  }

  @Override
  public String toString() {
    return "Roles [roleId=" + roleId + ", roleName=" + roleName + "]";
  }



}
