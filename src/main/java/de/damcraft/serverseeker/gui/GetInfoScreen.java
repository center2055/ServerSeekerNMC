package de.damcraft.serverseeker.gui;

import com.google.gson.Gson;
import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.SmallHttp;
import de.damcraft.serverseeker.ssapi.requests.ServerInfoRequest;
import de.damcraft.serverseeker.ssapi.responses.ServerInfoResponse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class GetInfoScreen extends Screen {
    private final MultiplayerScreen parent;
    private final MultiplayerServerListWidget.Entry entry;
    private String status = "";
    private List<ServerInfoResponse.Player> players;
    private Boolean cracked;

    public GetInfoScreen(MultiplayerScreen parent, MultiplayerServerListWidget.Entry entry) {
        super(Text.literal("Get players"));
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    protected void init() {
        if (entry == null || !(entry instanceof MultiplayerServerListWidget.ServerEntry)) {
            this.clearChildren();
            this.addDrawableChild(new TextWidget(10, 30, 300, 20, Text.of("No server selected"), this.textRenderer));
            this.addDrawableChild(new ButtonWidget.Builder(Text.of("Back"), b -> this.close()).dimensions(10, this.height - 30, 60, 20).build());
            return;
        }
        ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry) entry).getServer();
        String address = serverInfo.address;

        if (!address.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?::[0-9]{1,5})?$")) {
            try {
                InetAddress inetAddress = InetAddress.getByName(address);
                address = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                this.clearChildren();
                this.addDrawableChild(new TextWidget(10, 30, 500, 20, Text.of("You can only get player info for servers with an IP address"), this.textRenderer));
                this.addDrawableChild(new ButtonWidget.Builder(Text.of("Back"), b -> this.close()).dimensions(10, this.height - 30, 60, 20).build());
                return;
            }
        }

        this.clearChildren();
        this.status = "Loading...";
        this.addDrawableChild(new ButtonWidget.Builder(Text.of("Back"), b -> this.close()).dimensions(10, this.height - 30, 60, 20).build());

        final String[] parts = address.split(":");
        final String ip = parts[0];
        final int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 25565;

        new Thread(() -> {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(new ServerInfoRequest(ServerSeeker.API_KEY, ip, port));
                String base = de.damcraft.serverseeker.Config.serverInfoEndpoint();
                String[] endpoints = new String[] { base, base + "/" };

                SmallHttp.StringResponse http = null;
                for (String ep : endpoints) {
                    http = SmallHttp.postResp(ep, json);
                    if (http.status() != 404 && http.status() != -1) break;
                }
                if (http.status() == 403) {
                    status = "HTTP 403 (Forbidden) - API key missing/invalid";
                } else if (http.status() > 0 && (http.status() < 200 || http.status() >= 300)) {
                    status = "HTTP " + http.status() + " - unexpected response";
                } else if (http.body() == null) {
                    String err = SmallHttp.getLastError();
                    status = err != null ? ("Network error: " + err) : "Network error";
                } else {
                    ServerInfoResponse response;
                    try {
                        response = gson.fromJson(http.body(), ServerInfoResponse.class);
                    } catch (Exception parseEx) {
                        status = "Unexpected response: " + http.body().substring(0, Math.min(140, http.body().length()));
                        MinecraftClient.getInstance().execute(this::init);
                        return;
                    }
                    if (response == null || response.isError()) {
                        status = response == null ? "Network error" : response.error();
                    } else {
                        players = response.players();
                        cracked = response.cracked();
                        status = null;
                    }
                }
            } catch (Exception e) {
                status = "Network error";
            }
            MinecraftClient.getInstance().execute(this::init);
        }).start();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int y = 20;
        if (status != null && !status.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, status, 10, y, 0xFFFFFF);
            return;
        }

        if (players == null || players.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, "No records of players found.", 10, y, 0xFFFFFF);
            return;
        }

        if (cracked != null && !cracked) {
            context.drawTextWithShadow(this.textRenderer, "Attention: The server is NOT cracked!", 10, y, 0xFF5555);
            y += 12;
        }

        String playersLabel = players.size() == 1 ? " player:" : " players:";
        context.drawTextWithShadow(this.textRenderer, "Found " + players.size() + playersLabel, 10, y, 0xFFFFFF);
        y += 14;

        // Headers
        context.drawTextWithShadow(this.textRenderer, "Name", 10, y, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Last seen", 180, y, 0xAAAAAA);
        y += 12;

        for (ServerInfoResponse.Player p : players) {
            String lastSeenFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(Instant.ofEpochSecond(p.lastSeen()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            context.drawTextWithShadow(this.textRenderer, p.name(), 10, y, 0xFFFFFF);
            context.drawTextWithShadow(this.textRenderer, lastSeenFormatted, 180, y, 0xFFFFFF);
            y += 12;
            if (y > this.height - 20) break;
        }
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }
}


