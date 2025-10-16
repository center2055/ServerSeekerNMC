package de.damcraft.serverseeker.ssapi.requests;

import com.google.gson.annotations.SerializedName;

public record ServerInfoRequest(@SerializedName("api_key") String apiKey, String ip, int port) {}
