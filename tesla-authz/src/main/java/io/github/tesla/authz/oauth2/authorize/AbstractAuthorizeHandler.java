package io.github.tesla.authz.oauth2.authorize;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import static io.github.tesla.authz.oauth2.Constants.*;
import io.github.tesla.authz.oauth2.OAuthAuthxRequest;
import io.github.tesla.authz.oauth2.OAuthHandler;
import io.github.tesla.authz.oauth2.WebUtils;
import io.github.tesla.authz.oauth2.validator.AbstractClientDetailsValidator;


public abstract class AbstractAuthorizeHandler extends OAuthHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthorizeHandler.class);


  protected OAuthAuthxRequest oauthRequest;
  protected HttpServletResponse response;
  protected boolean userFirstLogged = false;
  protected boolean userFirstApproved = false;


  public AbstractAuthorizeHandler(OAuthAuthxRequest oauthRequest, HttpServletResponse response) {
    this.oauthRequest = oauthRequest;
    this.response = response;
  }


  protected boolean validateFailed() throws OAuthSystemException {
    AbstractClientDetailsValidator validator = getValidator();
    LOG.debug("Use [{}] validate client: {}", validator, oauthRequest.getClientId());
    final OAuthResponse oAuthResponse = validator.validate();
    return checkAndResponseValidateFailed(oAuthResponse);
  }

  protected abstract AbstractClientDetailsValidator getValidator();

  protected boolean checkAndResponseValidateFailed(OAuthResponse oAuthResponse) {
    if (oAuthResponse != null) {
      LOG.debug("Validate OAuthAuthzRequest(client_id={}) failed", oauthRequest.getClientId());
      WebUtils.writeOAuthJsonResponse(response, oAuthResponse);
      return true;
    }
    return false;
  }

  protected String clientId() {
    return oauthRequest.getClientId();
  }

  protected boolean isUserAuthenticated() {
    final Subject subject = SecurityUtils.getSubject();
    return subject.isAuthenticated();
  }

  protected boolean isNeedUserLogin() {
    return !isUserAuthenticated() && !isPost();
  }

  protected boolean goApproval() throws ServletException, IOException {
    if (userFirstLogged && !clientDetails().trusted()) {
      LOG.debug("Go to oauth_approval, clientId: '{}'", clientDetails().getClientId());
      final HttpServletRequest request = oauthRequest.request();
      request.getRequestDispatcher(OAUTH_APPROVAL_VIEW).forward(request, response);
      return true;
    }
    return false;
  }

  protected boolean submitApproval() throws IOException, OAuthSystemException {
    if (isPost() && !clientDetails().trusted()) {
      final HttpServletRequest request = oauthRequest.request();
      final String oauthApproval = request.getParameter(REQUEST_USER_OAUTH_APPROVAL);
      if (!"true".equalsIgnoreCase(oauthApproval)) {
        LOG.debug("User '{}' deny access", SecurityUtils.getSubject().getPrincipal());
        responseApprovalDeny();
        return true;
      } else {
        userFirstApproved = true;
        return false;
      }
    }
    return false;
  }

  protected void responseApprovalDeny() throws IOException, OAuthSystemException {
    final OAuthResponse oAuthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
        .setError(OAuthError.CodeResponse.ACCESS_DENIED).setErrorDescription("User denied access")
        .location(clientDetails().getRedirectUri()).setState(oauthRequest.getState())
        .buildQueryMessage();
    LOG.debug("'ACCESS_DENIED' response: {}", oAuthResponse);
    WebUtils.writeOAuthQueryResponse(response, oAuthResponse);
    final Subject subject = SecurityUtils.getSubject();
    subject.logout();
    LOG.debug("After 'ACCESS_DENIED' call logout. user: {}", subject.getPrincipal());
  }


  protected boolean goLogin() throws ServletException, IOException {
    if (isNeedUserLogin()) {
      LOG.debug("Forward to Oauth login by client_id '{}'", oauthRequest.getClientId());
      final HttpServletRequest request = oauthRequest.request();
      request.getRequestDispatcher(OAUTH_LOGIN_VIEW).forward(request, response);
      return true;
    }
    return false;
  }


  protected boolean submitLogin() throws ServletException, IOException {
    if (isSubmitLogin()) {
      try {
        UsernamePasswordToken token = createUsernamePasswordToken();
        SecurityUtils.getSubject().login(token);
        LOG.debug("Submit login successful");
        this.userFirstLogged = true;
        return false;
      } catch (Exception ex) {
        LOG.debug("Login failed, back to login page too", ex);
        final HttpServletRequest request = oauthRequest.request();
        request.setAttribute("oauth_login_error", true);
        request.getRequestDispatcher(OAUTH_LOGIN_VIEW).forward(request, response);
        return true;
      }
    }
    return false;
  }

  private UsernamePasswordToken createUsernamePasswordToken() {
    final HttpServletRequest request = oauthRequest.request();
    final String username = request.getParameter(REQUEST_USERNAME);
    final String password = request.getParameter(REQUEST_PASSWORD);
    return new UsernamePasswordToken(username, password);
  }

  private boolean isSubmitLogin() {
    return !isUserAuthenticated() && isPost();
  }

  protected boolean isPost() {
    return RequestMethod.POST.name().equalsIgnoreCase(oauthRequest.request().getMethod());
  }

  public void handle() throws OAuthSystemException, ServletException, IOException {
    if (validateFailed()) {
      return;
    }
    if (goLogin()) {
      return;
    }
    if (submitLogin()) {
      return;
    }
    if (goApproval()) {
      return;
    }
    if (submitApproval()) {
      return;
    }
    handleResponse();
  }

  protected abstract void handleResponse() throws OAuthSystemException, IOException;
}
