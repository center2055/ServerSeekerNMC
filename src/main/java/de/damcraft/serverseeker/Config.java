package de.damcraft.serverseeker;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private static final Gson GSON = new Gson();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("serverseeker.json");

    private static volatile Data data = new Data();

    public static final class Data {
        @SerializedName("base_url")
        public String baseUrl = "https://api.serverseeker.net";

        @SerializedName("api_key")
        public String apiKey = ServerSeeker.API_KEY;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE, StandardCharsets.UTF_8);
                Data loaded = GSON.fromJson(json, Data.class);
                if (loaded != null) data = loaded;
            } else {
                save();
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    public static String getBaseUrl() { return data.baseUrl; }
    public static void setBaseUrl(String url) { data.baseUrl = url; }

    public static String getApiKey() { return data.apiKey; }
    public static void setApiKey(String key) { data.apiKey = key; }

    public static String serversEndpoint() { return normalize(data.baseUrl) + "/servers"; }
    public static String serverInfoEndpoint() { return normalize(data.baseUrl) + "/server_info"; }
    public static String whereisEndpoint() { return normalize(data.baseUrl) + "/whereis"; }

    private static String normalize(String base) {
        if (base.endsWith("/")) return base.substring(0, base.length() - 1);
        return base;
    }
}



