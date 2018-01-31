package io.github.tesla.ops.common;

import org.springframework.stereotype.Controller;

import io.github.tesla.ops.system.domain.UserDO;
import io.github.tesla.ops.utils.ShiroUtils;

@Controller
public class BaseController {
  public UserDO getUser() {
    return ShiroUtils.getUser();
  }

  public Long getUserId() {
    return getUser().getUserId();
  }

  public String getUsername() {
    return getUser().getUsername();
  }
}
