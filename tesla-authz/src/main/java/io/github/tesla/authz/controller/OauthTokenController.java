package io.github.tesla.authz.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.github.tesla.authz.controller.oauth2.OAuthTokenxRequest;
import io.github.tesla.authz.controller.oauth2.WebUtils;
import io.github.tesla.authz.controller.oauth2.token.OAuthTokenHandleDispatcher;


@Controller
@RequestMapping("oauth/")
public class OauthTokenController {


  @RequestMapping("token")
  public void authorize(HttpServletRequest request, HttpServletResponse response)
      throws OAuthSystemException {
    try {
      OAuthTokenxRequest tokenRequest = new OAuthTokenxRequest(request);
      OAuthTokenHandleDispatcher tokenHandleDispatcher =
          new OAuthTokenHandleDispatcher(tokenRequest, response);
      tokenHandleDispatcher.dispatch();
    } catch (OAuthProblemException e) {
      OAuthResponse oAuthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
          .location(e.getRedirectUri()).error(e).buildJSONMessage();
      WebUtils.writeOAuthJsonResponse(response, oAuthResponse);
    }

  }
}
