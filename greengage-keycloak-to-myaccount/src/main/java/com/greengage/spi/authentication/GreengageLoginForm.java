package com.greengage.spi.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.Response;
import java.net.URI;

public class GreengageLoginForm implements Authenticator {

    private static final String FIRST_LOGIN_GREENGAGE = "first-login-greegage.ftl";
    private static final String REDIRECT_URL_TEMPLATE = "https://me.greengage-project.eu/?redirect=%s";

    private static final String[] ATTRIBUTES_TO_CHECK = {
        "userAttribute_ageRange", "userAttribute_digitalToolsAcquaintance", 
        "userAttribute_disadvantagedGroup", "userAttribute_educationLevel", 
        "userAttribute_gender",
        "userAttribute_organizationType", "userAttribute_thematicRole", 
        "userAttribute_workstatus", "userAttribute_zipCode"
    };

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        boolean allAttributesExist = true;

        for (String attribute : ATTRIBUTES_TO_CHECK) {
            if (user.getFirstAttribute(attribute) == null) {
                allAttributesExist = false;
                break;
            }
        }

        if (!allAttributesExist) {
            String redirectUrl = buildRedirectUrl(context);
            Response response = Response.status(Response.Status.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
            context.forceChallenge(response);
        } else {
            context.success();
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        boolean firstLogin = isFirstLogin(user);

        if (firstLogin) {
            for (String attribute : ATTRIBUTES_TO_CHECK) {
                String value = context.getHttpRequest().getDecodedFormParameters().getFirst(attribute);
                if (value != null) {
                    user.setSingleAttribute(attribute, value);
                }
            }

            String redirectUrl = buildRedirectUrl(context);
            Response response = Response.status(Response.Status.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
            context.forceChallenge(response);
        } else {
            context.success();
        }
    }

    private boolean isFirstLogin(UserModel user) {
        // Assuming that a user without a last login timestamp is a first-time login
        return user.getAttribute("lastLoginTime").isEmpty();
    }

    private String buildRedirectUrl(AuthenticationFlowContext context) {
        // Get the original redirect URL from the session note
        String originalRedirectUrl = context.getAuthenticationSession().getRedirectUri();
        return String.format(REDIRECT_URL_TEMPLATE, originalRedirectUrl);
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    }

    @Override
    public void close() {
    }
}
