package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;

@Data
public class StoredSession implements Session {
	private String uuid;

	private String name;

	private String clientToken, accessToken;

	private Map<String, String> userProperties;

	private UserType userType;

	@Override
	public String getUuid() {
		return this.uuid;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getClientToken() {
		return this.clientToken;
	}

	@Override
	public String getAccessToken() {
		return this.accessToken;
	}

	@Override
	public Map<String, String> getUserProperties() {
		return this.userProperties;
	}

	@Override
	@JsonIgnore
	public String getSessionToken() {
		return String.format("token:%s:%s", this.getAccessToken(), this.getUuid());
	}

	@Override
	public UserType getUserType() {
		return this.userType;
	}

	@Override
	@JsonIgnore
	public boolean isOnline() {
		return true;
	}
}
