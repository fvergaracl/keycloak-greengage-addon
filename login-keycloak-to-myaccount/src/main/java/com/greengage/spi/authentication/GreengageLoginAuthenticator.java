package com.greengage.spi.authentication;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.authentication.AuthenticationFlowError;

import javax.ws.rs.core.Response;
import java.net.URI;

public class GreengageLoginAuthenticator implements Authenticator {

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
        if (user == null) {
            // Usuario no encontrado, falla la autenticación
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

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
            if (userHasLoggedInBefore(user)) {
                context.success();
            } else {
                redirectAfterLogin(context);
            }
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // No se requiere acción específica aquí
        context.success();
    }

    private boolean userHasLoggedInBefore(UserModel user) {
        // Aquí usamos un atributo personalizado para almacenar la última vez que el usuario inició sesión
        String lastLoginTimeStr = user.getFirstAttribute("lastLoginTime");
        if (lastLoginTimeStr != null) {
            try {
                Long lastLoginTime = Long.parseLong(lastLoginTimeStr);
                return lastLoginTime != null;
            } catch (NumberFormatException e) {
                // Maneja la excepción según sea necesario
                return false;
            }
        }
        return false;
    }

    private String buildRedirectUrl(AuthenticationFlowContext context) {
        String originalRedirectUrl = context.getAuthenticationSession().getRedirectUri();
        return String.format(REDIRECT_URL_TEMPLATE, originalRedirectUrl);
    }

    private void redirectAfterLogin(AuthenticationFlowContext context) {
        String redirectUrl = buildRedirectUrl(context);
        Response response = Response.status(Response.Status.FOUND)
                .location(URI.create(redirectUrl))
                .build();
        context.getEvent().detail("redirect_after_login", redirectUrl);
        context.forceChallenge(response);
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
        // No se requieren acciones específicas aquí
    }

    @Override
    public void close() {
        // No se requieren acciones específicas aquí
    }
}
