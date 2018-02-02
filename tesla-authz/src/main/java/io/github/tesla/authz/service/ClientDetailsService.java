///*
// * Copyright 2014-2017 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software distributed under the License
// * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// * or implied. See the License for the specific language governing permissions and limitations under
// * the License.
// */
//package io.github.tesla.authz.service;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.monkeyk.os.service.business.ClientDetailsFormSaver;
//import com.monkeyk.os.service.dto.ClientDetailsDto;
//import com.monkeyk.os.service.dto.ClientDetailsFormDto;
//import com.monkeyk.os.service.dto.ClientDetailsListDto;
//
//import io.github.tesla.authz.repository.dao.Oauth2Dao;
//import io.github.tesla.authz.repository.dao.UserDao;
//import io.github.tesla.authz.repository.domain.ClientDetails;
//import io.github.tesla.authz.repository.domain.Roles;
//
///**
// * @author liushiming
// * @version ClientDetailsService.java, v 0.0.1 2018年2月2日 下午6:09:30 liushiming
// */
//public class ClientDetailsService {
//
//  @Autowired
//  private Oauth2Dao oauthRepository;
//
//  @Autowired
//  private UserDao usersAuthzRepository;
//
//
//  public ClientDetailsListDto loadClientDetailsListDto(String clientId) {
//    List<ClientDetails> clientDetailsList =
//        oauthRepository.findClientDetailsListByClientId(clientId);
//    return new ClientDetailsListDto(clientId, clientDetailsList);
//  }
//
//  public ClientDetailsFormDto loadClientDetailsFormDto() {
//    List<Roles> rolesList = usersAuthzRepository.findAvailableRolesList();
//    return new ClientDetailsFormDto(rolesList);
//  }
//
//  public String saveClientDetails(ClientDetailsFormDto formDto) {
//    ClientDetailsFormSaver saver = new ClientDetailsFormSaver(formDto);
//    return saver.save();
//  }
//
//  public ClientDetailsDto loadClientDetailsDto(String clientId) {
//    ClientDetails clientDetails = oauthRepository.findClientDetails(clientId);
//    return new ClientDetailsDto(clientDetails);
//  }
//}
