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
package io.github.tesla.ops.api.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;

import io.github.tesla.ops.api.dto.APIRouteDto;
import io.github.tesla.ops.api.service.ApiService;
import io.github.tesla.ops.api.service.ProtobufService;
import io.github.tesla.ops.api.vo.APIRouteVo;
import io.github.tesla.ops.common.BaseController;
import io.github.tesla.ops.common.CommonResponse;
import io.github.tesla.ops.common.Log;
import io.github.tesla.ops.common.TeslaException;
import io.github.tesla.ops.system.domain.PageDO;
import io.github.tesla.ops.utils.FileType;
import io.github.tesla.ops.utils.Query;

/**
 * @author liushiming
 * @version ZuulController.java, v 0.0.1 2018年1月9日 上午11:19:14 liushiming
 */
@Controller
@RequestMapping("gateway/api")
public class ApiController extends BaseController {

  private final String prefix = "gateway/api";
  @Autowired
  private ProtobufService protobufService;

  @Autowired
  private ApiService routeService;

  @RequiresPermissions("gateway:api:api")
  @GetMapping()
  public String route() {
    return prefix + "/route";
  }

  @RequiresPermissions("gateway:api:add")
  @GetMapping("/add")
  public String add() {
    return prefix + "/add";
  }

  @RequiresPermissions("gateway:api:edit")
  @GetMapping("/edit/{id}")
  public String edit(@PathVariable("id") Long id, Model model) {
    
    model.addAttribute("route", zuulVo);
    return prefix + "/edit";
  }

  @Log("保存路由")
  @RequiresPermissions("filter:route:add")
  @PostMapping("/save")
  @ResponseBody()
  public CommonResponse save(APIRouteVo zuulVo,
      @RequestParam(name = "zipFile", required = false) MultipartFile zipFile) {
    try {
      // grpc路由
      if (zipFile != null) {
        InputStream directoryZipStream = zipFile.getInputStream();
        CommonResponse response = judgeFileType(directoryZipStream, "zip");
        if (response != null) {
          return response;
        } else {
          String serviceFileName = zuulVo.getServiceFileName();
          byte[] protoContext = protobufService.compileDirectoryProto(zipFile, serviceFileName);
          APIRouteDto zuulDto = zuulVo.buildRouteDto();
          zuulDto.setProtoContext(protoContext);
          routeService.save(zuulDto);
        }
      } else {
        APIRouteDto zuulDto = zuulVo.buildRouteDto();
        routeService.save(zuulDto);
      }
    } catch (IOException e) {
      throw new TeslaException("保存路由失败", e);
    }
    return CommonResponse.ok();
  }

  @Log("查询路由")
  @RequiresPermissions("filter:route:route")
  @GetMapping("/list")
  @ResponseBody
  public PageDO<APIRouteVo> list(@RequestParam Map<String, Object> params) {
    Query query = new Query(params);
    PageDO<APIRouteDto> pageDto = routeService.queryList(query);
    PageDO<APIRouteVo> pageVo = new PageDO<>();
    pageVo.setTotal(pageDto.getTotal());
    List<APIRouteDto> zuulDtos = pageDto.getRows();
    List<APIRouteVo> vos = Lists.newArrayListWithCapacity(zuulDtos.size());
    for (APIRouteDto zuulDto : zuulDtos) {
      vos.add(APIRouteVo.buildRouteVo(zuulDto));
    }
    pageVo.setRows(vos);
    return pageVo;
  }


  @Log("更新路由")
  @RequiresPermissions("filter:route:edit")
  @PostMapping("/update")
  @ResponseBody()
  public CommonResponse update(APIRouteVo zuulVo,
      @RequestParam(name = "zipFile", required = false) MultipartFile zipFile) {
    try {
      // grpc路由
      if (zipFile != null) {
        InputStream directoryZipStream = zipFile.getInputStream();
        CommonResponse response = judgeFileType(directoryZipStream, "zip");
        if (response != null) {
          return response;
        } else {
          String serviceFileName = zuulVo.getServiceFileName();
          byte[] protoContext = protobufService.compileDirectoryProto(zipFile, serviceFileName);
          APIRouteDto zuulDto = zuulVo.buildRouteDto();
          zuulDto.setProtoContext(protoContext);
          routeService.update(zuulDto);
        }
      } else {
        APIRouteDto zuulDto = zuulVo.buildRouteDto();
        routeService.update(zuulDto);
      }
    } catch (IOException e) {
      throw new TeslaException("保存路由失败", e);
    }
    return CommonResponse.ok();
  }

  @Log("删除路由")
  @RequiresPermissions("filter:route:remove")
  @PostMapping("/remove")
  @ResponseBody()
  public CommonResponse save(Long id) {
    if (routeService.remove(id) > 0) {
      return CommonResponse.ok();
    } else {
      return CommonResponse.error(1, "删除失败");
    }
  }

  @RequiresPermissions("filter:route:batchRemove")
  @Log("批量删除路由")
  @PostMapping("/batchRemove")
  @ResponseBody
  public CommonResponse batchRemove(@RequestParam("ids[]") Long[] ids) {
    int response = routeService.batchRemove(ids);
    if (response > 0) {
      return CommonResponse.ok();
    }
    return CommonResponse.error();
  }


  private CommonResponse judgeFileType(InputStream inpustream, String type) throws IOException {
    String fileType = FileType.calculateFileHexString(inpustream);
    if (!type.equals(fileType)) {
      return CommonResponse.error(1, "只能上传" + type + "类型文件");
    } else {
      return null;
    }
  }

}
