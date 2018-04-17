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
package io.github.tesla.ops.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.tesla.filter.domain.ApiGroupDO;
import io.github.tesla.ops.api.service.ApiGroupService;

/**
 * @author liushiming
 * @version APIGroupServiceImpl.java, v 0.0.1 2018年4月11日 下午4:06:23 liushiming
 */
@Service
public class ApiGroupServiceImpl implements ApiGroupService {

  @Override
  public ApiGroupDO get(Long id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ApiGroupDO> list() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int save(ApiGroupDO role) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int update(ApiGroupDO role) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int remove(Long id) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<ApiGroupDO> list(Long userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int batchremove(Long[] ids) {
    // TODO Auto-generated method stub
    return 0;
  }

}
