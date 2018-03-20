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
package io.github.tesla.filter.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.tesla.filter.domain.FilterRpcDO;

/**
 * @author liushiming
 * @version GrpcDao.java, v 0.0.1 2018年1月4日 上午10:48:12 liushiming
 */
@Mapper
public interface FilterRpcDao {
  FilterRpcDO get(@Param("routeId") Long routeId);

  FilterRpcDO getByService(@Param("serviceName") String serviceName,
      @Param("methodName") String methodName, @Param("group") String group,
      @Param("version") String version);

  List<FilterRpcDO> list(Map<String, Object> map);

  int save(FilterRpcDO route);

  int update(FilterRpcDO route);

  int remove(Long id);

  int removeByRouteId(Long routeId);

  int batchRemove(Long[] routeIds);

}
