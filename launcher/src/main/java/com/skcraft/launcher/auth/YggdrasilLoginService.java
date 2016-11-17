/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.*;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Creates authenticated sessions using the Mojang Yggdrasil login protocol.
 */
public class YggdrasilLoginService implements LoginService {
    private final URL authUrl;

    /**
     * Create a new login service with the given authentication URL.
     *
     * @param authUrl the authentication URL
     */
    public YggdrasilLoginService(@NonNull URL authUrl) {
        this.authUrl = authUrl;
    }

    @Override
    public List<? extends Profile> login(String agent, String id, String password)
            throws IOException, InterruptedException, AuthenticationException {
        Object payload = new AuthenticatePayload(new Agent(agent), id, password);

        HttpRequest request = HttpRequest
                .post(HttpRequest.url(authUrl.toString() + "/authenticate"))
                .bodyJson(payload)
                .execute();

        if (request.getResponseCode() != 200) {
            ErrorResponse error = request.returnContent().asJson(ErrorResponse.class);
            throw new AuthenticationException(error.getErrorMessage(), error.getErrorMessage());
        } else {
            AuthenticateResponse response = request.returnContent().asJson(AuthenticateResponse.class);
            response.setId(id);

            return response.getAvailableProfiles();
        }
    }

    @Override
    public StoredSession refreshSession(Account account) throws IOException, AuthenticationException, InterruptedException {
        RefreshPayload payload = new RefreshPayload(account.getSession().getAccessToken(), account.getSession().getClientToken());

        HttpRequest request = HttpRequest
                .post(HttpRequest.url(authUrl.toString() + "/refresh"))
                .bodyJson(payload)
                .execute();

        if (request.getResponseCode() != 200) {
            ErrorResponse error = request.returnContent().asJson(ErrorResponse.class);

            throw new AuthenticationException(error.getErrorMessage(), error.getErrorMessage());
        } else {
            AuthenticateResponse response = request.returnContent().asJson(AuthenticateResponse.class);

            return response.getSelectedProfile().createSession();
        }
    }

    @Override
    public boolean needsRefresh(Account account) {
        ValidatePayload payload = new ValidatePayload(account.getSession().getAccessToken(), account.getSession().getClientToken());

        boolean success = false;

        try {
            HttpRequest request = HttpRequest
                    .post(HttpRequest.url(authUrl.toString() + "/validate"))
                    .bodyJson(payload)
                    .execute();

            if (request.getResponseCode() == 204) {
                success = true;
            }
        } catch (IOException ignored) {

        }

        return success;
    }

    @Data
    private static class Agent {
        private final String name;
        private final int version = 1;
    }

    @Data
    private static class AuthenticatePayload {
        private final Agent agent;
        private final String username;
        private final String password;
    }

    @Data
    private static class ValidatePayload {
        private final String accessToken;
        private final String clientToken;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AuthenticateResponse {
        private String accessToken;
        private String clientToken;
        @JsonIgnore
        private String id;
        @JsonManagedReference
        private List<Profile> availableProfiles;
        @JsonManagedReference
        private Profile selectedProfile;
    }

    @Data
    private static class RefreshPayload {
        private final String accessToken;
        private final String clientToken;
    }

    @Data
    private static class ErrorResponse {
        private String error;
        private String errorMessage;
        private String cause;
    }

    /**
     * Return in the list of available profiles.
     */
    @Data
    @ToString(exclude = "response")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        @JsonProperty("id")
        private String uuid;
        private String name;

        private boolean legacy;

        @JsonIgnore
        private final Map<String, String> userProperties = Collections.emptyMap();

        @JsonBackReference
        private AuthenticateResponse response;

        @JsonIgnore
        public UserType getUserType() {
            return legacy ? UserType.LEGACY : UserType.MOJANG;
        }

        public StoredSession createSession() {
            StoredSession session = new StoredSession();
            session.setUuid(this.uuid);
            session.setName(this.name);
            session.setClientToken(this.response.getClientToken());
            session.setAccessToken(this.response.getAccessToken());
            session.setUserProperties(this.getUserProperties());
            session.setUserType(this.getUserType());

            return session;
        }
    }
}
