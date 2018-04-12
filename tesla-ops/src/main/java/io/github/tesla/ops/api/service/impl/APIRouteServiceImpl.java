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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import io.github.tesla.ops.api.dto.APIRouteDto;
import io.github.tesla.ops.api.service.APIRouteService;
import io.github.tesla.ops.common.CommonResponse;
import io.github.tesla.ops.system.domain.PageDO;
import io.github.tesla.ops.utils.Query;
import io.github.tesla.filter.dao.ApiDao;
import io.github.tesla.filter.dao.ApiRpcDao;
import io.github.tesla.filter.domain.ApiDO;
import io.github.tesla.filter.domain.ApiRpcDO;

/**
 * @author liushiming
 * @version routeServiceImpl.java, v 0.0.1 2018年1月8日 上午11:38:49 liushiming
 */
@Service
public class APIRouteServiceImpl implements APIRouteService {

  @Autowired
  private ApiDao routeDao;

  @Autowired
  private ApiRpcDao rpcDao;

  @Override
  public PageDO<APIRouteDto> queryList(Query query) {
    int total = routeDao.count(query);
    List<ApiDO> routes = routeDao.list(query);
    List<APIRouteDto> dtos = Lists.newArrayListWithCapacity(routes.size());
    for (ApiDO routeDo : routes) {
      ApiRpcDO rpcDO = rpcDao.get(routeDo.getId());
      APIRouteDto dto = APIRouteDto.buildRouteDto(routeDo, rpcDO);
      dtos.add(dto);
    }
    PageDO<APIRouteDto> page = new PageDO<>();
    page.setTotal(total);
    page.setRows(dtos);
    return page;
  }

  @Override
  public APIRouteDto get(Long routeId) {
    ApiDO route = routeDao.get(routeId);
    ApiRpcDO rpc = rpcDao.get(routeId);
    APIRouteDto routeDto = APIRouteDto.buildRouteDto(route, rpc);
    return routeDto;
  }

  @Override
  public List<APIRouteDto> list(Map<String, Object> map) {
    List<ApiDO> routes = routeDao.list(map);
    List<APIRouteDto> routeDtos = Lists.newArrayList();
    for (ApiDO route : routes) {
      ApiRpcDO rpc = rpcDao.get(route.getId());
      APIRouteDto routeDto = APIRouteDto.buildRouteDto(route, rpc);
      routeDtos.add(routeDto);
    }
    return routeDtos;
  }

  @Override
  public int count(Map<String, Object> map) {
    int total = routeDao.count(map);
    return total;
  }

  @Override
  public int save(APIRouteDto routeDto) {
    ApiDO routeDo = routeDto.buildRoute();
    ApiRpcDO rpcDo = routeDto.buildRpc();
    int success2 = routeDao.save(routeDo);
    Long routeId = routeDo.getId();
    rpcDo.setRouteId(routeId);
    if (routeDo.getRpc()) {
      int success1 = rpcDao.save(rpcDo);
      if (success1 > 0 && success2 > 0) {
        return CommonResponse.SUCCESS;
      } else {
        return CommonResponse.ERROR;
      }
    }
    return success2;

  }

  @Override
  public int update(APIRouteDto routeDto) {
    ApiDO routeDo = routeDto.buildRoute();
    ApiRpcDO rpcDo = routeDto.buildRpc();
    int success2 = routeDao.update(routeDo);
    if (routeDo.getRpc()) {
      int success1 = rpcDao.update(rpcDo);
      if (success1 > 0 && success2 > 0) {
        return CommonResponse.SUCCESS;
      } else {
        return CommonResponse.ERROR;
      }
    }
    return success2;
  }

  @Override
  public int remove(Long routeId) {
    int success1 = routeDao.remove(routeId);
    int success2 = rpcDao.removeByRouteId(routeId);
    if (success1 > 0 && success2 > 0) {
      return CommonResponse.SUCCESS;
    } else {
      return CommonResponse.ERROR;
    }
  }

  @Override
  public int batchRemove(Long[] routeIds) {
    int success1 = routeDao.batchRemove(routeIds);
    int success2 = rpcDao.batchRemove(routeIds);
    if (success1 > 0 && success2 > 0) {
      return CommonResponse.SUCCESS;
    } else {
      return CommonResponse.ERROR;
    }
  }
}
