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
package io.github.tesla.ops.filter.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import io.github.tesla.ops.common.CommonResponse;
import io.github.tesla.ops.filter.dto.FilterRouteDto;
import io.github.tesla.ops.filter.service.FilterRouteService;
import io.github.tesla.ops.system.domain.PageDO;
import io.github.tesla.ops.utils.Query;
import io.github.tesla.filter.dao.FilterRouteDao;
import io.github.tesla.filter.dao.FilterRpcDao;
import io.github.tesla.filter.domain.FilterRouteDO;
import io.github.tesla.filter.domain.FilterRpcDO;

/**
 * @author liushiming
 * @version routeServiceImpl.java, v 0.0.1 2018年1月8日 上午11:38:49 liushiming
 */
@Service
public class FilterRouteServiceImpl implements FilterRouteService {

  @Autowired
  private FilterRouteDao routeDao;

  @Autowired
  private FilterRpcDao rpcDao;

  @Override
  public PageDO<FilterRouteDto> queryList(Query query) {
    int total = routeDao.count(query);
    List<FilterRouteDO> routes = routeDao.list(query);
    List<FilterRouteDto> dtos = Lists.newArrayListWithCapacity(routes.size());
    for (FilterRouteDO routeDo : routes) {
      FilterRpcDO rpcDO = rpcDao.get(routeDo.getId());
      FilterRouteDto dto = FilterRouteDto.buildRouteDto(routeDo, rpcDO);
      dtos.add(dto);
    }
    PageDO<FilterRouteDto> page = new PageDO<>();
    page.setTotal(total);
    page.setRows(dtos);
    return page;
  }

  @Override
  public FilterRouteDto get(Long routeId) {
    FilterRouteDO route = routeDao.get(routeId);
    FilterRpcDO rpc = rpcDao.get(routeId);
    FilterRouteDto routeDto = FilterRouteDto.buildRouteDto(route, rpc);
    return routeDto;
  }

  @Override
  public List<FilterRouteDto> list(Map<String, Object> map) {
    List<FilterRouteDO> routes = routeDao.list(map);
    List<FilterRouteDto> routeDtos = Lists.newArrayList();
    for (FilterRouteDO route : routes) {
      FilterRpcDO rpc = rpcDao.get(route.getId());
      FilterRouteDto routeDto = FilterRouteDto.buildRouteDto(route, rpc);
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
  public int save(FilterRouteDto routeDto) {
    FilterRouteDO routeDo = routeDto.buildRoute();
    FilterRpcDO rpcDo = routeDto.buildRpc();
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
  public int update(FilterRouteDto routeDto) {
    FilterRouteDO routeDo = routeDto.buildRoute();
    FilterRpcDO rpcDo = routeDto.buildRpc();
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
