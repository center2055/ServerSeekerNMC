package de.damcraft.serverseeker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static de.damcraft.serverseeker.ServerSeeker.LOG;

public class SmallHttp {
    private static volatile String lastError = null;

    public static String getLastError() {
        return lastError;
    }

    public record StringResponse(int status, String body) {}

    public static String post(String url, String json) {
        try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()) {
            HttpResponse<String> res = client.send(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "ServerSeeker/" + (ServerSeeker.gson != null ? "4.5.5" : "unknown"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return res.body();
        } catch (IOException | InterruptedException e) {
            lastError = e.getMessage();
            LOG.error(e.toString());
            return null;
        }
    }

    public static StringResponse postResp(String url, String json) {
        try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()) {
            HttpResponse<String> res = client.send(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "ServerSeeker/4.5.5")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return new StringResponse(res.statusCode(), res.body());
        } catch (IOException | InterruptedException e) {
            lastError = e.getMessage();
            LOG.error(e.toString());
            return new StringResponse(-1, null);
        }
    }

    public static String get(String url) {
        try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()) {
            return client.send(HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "ServerSeeker/4.5.5")
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            ).body();
        } catch (IOException | InterruptedException e) {
            lastError = e.getMessage();
            LOG.error(e.toString());
            return null;
        }
    }

    public static StringResponse getResp(String url) {
        try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()) {
            HttpResponse<String> res = client.send(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("User-Agent", "ServerSeeker/4.5.5")
                .GET()
                .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            return new StringResponse(res.statusCode(), res.body());
        } catch (IOException | InterruptedException e) {
            lastError = e.getMessage();
            LOG.error(e.toString());
            return new StringResponse(-1, null);
        }
    }

    public static HttpResponse<InputStream> download(String url) {
        try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()) {
            HttpResponse<InputStream> req = client.send(HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "ServerSeeker/4.5.5")
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofInputStream()
            );
            if (req.headers().firstValue("location").isPresent()) {
                return download(req.headers().firstValue("location").get());
            }
            return req;
        } catch (IOException | InterruptedException e) {
            lastError = e.getMessage();
            LOG.error(e.toString());
            return null;
        }
    }
}
