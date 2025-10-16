package de.damcraft.serverseeker.gui;

import com.google.common.net.HostAndPort;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.damcraft.serverseeker.ServerSeeker;
import de.damcraft.serverseeker.SmallHttp;
import de.damcraft.serverseeker.ssapi.requests.ServersRequest;
import de.damcraft.serverseeker.ssapi.responses.ServersResponse;
import de.damcraft.serverseeker.utils.MultiplayerScreenUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.List;

public class FindServersScreen extends Screen {
    private final MultiplayerScreen parent;
    private String status = "";
    private List<ServersResponse.Server> servers;
    private int page = 0;
    private static final int PER_PAGE = 10;

    public FindServersScreen(MultiplayerScreen parent) {
        super(Text.literal("Find servers"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();

        // Back
        this.addDrawableChild(new ButtonWidget.Builder(Text.of("Back"), b -> this.close()).dimensions(10, this.height - 30, 60, 20).build());

        // Find button
        this.addDrawableChild(new ButtonWidget.Builder(Text.of("Find"), b -> fetch()).dimensions(10, 10, 60, 20).build());
        // Settings button
        this.addDrawableChild(new ButtonWidget.Builder(Text.of("Settings"), b -> this.client.setScreen(new SettingsScreen(this.parent, this))).dimensions(75, 10, 70, 20).build());

        if (servers == null) {
            if (status != null && !status.isEmpty()) {
                this.addDrawableChild(new TextWidget(80, 12, 400, 20, Text.of(status), this.textRenderer));
            }
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil(servers.size() / (double) PER_PAGE));
        int from = page * PER_PAGE;
        int to = Math.min(servers.size(), from + PER_PAGE);

        this.addDrawableChild(new TextWidget(80, 12, 300, 20, Text.of("Found " + servers.size() + " servers (page " + (page + 1) + "/" + totalPages + ")"), this.textRenderer));

        // Prev/Next
        if (page > 0) this.addDrawableChild(new ButtonWidget.Builder(Text.of("Prev"), b -> { page--; init(); }).dimensions(10, 40, 60, 20).build());
        if (page < totalPages - 1) this.addDrawableChild(new ButtonWidget.Builder(Text.of("Next"), b -> { page++; init(); }).dimensions(75, 40, 60, 20).build());

        // Add all
        this.addDrawableChild(new ButtonWidget.Builder(Text.of("Add all"), b -> addAll()).dimensions(140, 40, 70, 20).build());

        int y = 70;
        for (int i = from; i < to; i++) {
            ServersResponse.Server s = servers.get(i);
            String ip = s.server;
            String ver = s.version;
            this.addDrawableChild(new TextWidget(10, y, 330, 20, Text.of(ip + "  (" + ver + ")"), this.textRenderer));

            final int rowY = y;
            this.addDrawableChild(new ButtonWidget.Builder(Text.of("Add"), b -> addServer(ip)).dimensions(350, rowY, 45, 20).build());
            this.addDrawableChild(new ButtonWidget.Builder(Text.of("Join"), b -> joinServer(ip)).dimensions(400, rowY, 50, 20).build());

            y += 22;
        }
    }

    private void fetch() {
        this.status = "Loading...";
        this.servers = null;
        this.page = 0;
        this.init();

        new Thread(() -> {
            try {
                ServersRequest req = new ServersRequest();
                req.setProtocolVersion(SharedConstants.getProtocolVersion());
                req.setIgnoreModded(true);

                Gson gson = new Gson();
                String[] endpoints = new String[] {
                    "https://api.serverseeker.net/servers",
                    "https://api.serverseeker.net/servers/",
                    "https://api.serverseeker.net/v1/servers"
                };

                SmallHttp.StringResponse http = null;
                for (String ep : endpoints) {
                    http = SmallHttp.postResp(ep, gson.toJson(req));
                    if (http.status() != 404 && http.status() != -1) break;
                }
                ServersResponse resp = null;
                if (http.body() != null) {
                    try {
                        resp = gson.fromJson(http.body(), ServersResponse.class);
                    } catch (JsonSyntaxException ex) {
                        status = "Unexpected response: " + http.body().substring(0, Math.min(140, http.body().length()));
                        MinecraftClient.getInstance().execute(this::init);
                        return;
                    }
                }
                if (http.status() == 403) {
                    status = "HTTP 403 (Forbidden) - API key missing/invalid";
                } else if (http.status() > 0 && (http.status() < 200 || http.status() >= 300)) {
                    status = "HTTP " + http.status() + " - unexpected response";
                } else if (resp == null) {
                    String err = SmallHttp.getLastError();
                    status = err != null ? ("Network error: " + err) : "Network error";
                } else if (resp.isError()) {
                    status = resp.error;
                } else {
                    servers = resp.data;
                    status = servers.isEmpty() ? "No servers found" : null;
                }
            } catch (Exception e) {
                status = "Network error: " + e.getMessage();
            }
            MinecraftClient.getInstance().execute(this::init);
        }).start();
    }

    private void addServer(String ip) {
        ServerInfo info = new ServerInfo("ServerSeeker " + ip, ip, ServerInfo.ServerType.OTHER);
        MultiplayerScreenUtil.addInfoToServerList(parent, info);
    }

    private void addAll() {
        if (servers == null || servers.isEmpty()) return;
        for (ServersResponse.Server s : servers) {
            ServerInfo info = new ServerInfo("ServerSeeker " + s.server, s.server, ServerInfo.ServerType.OTHER);
            MultiplayerScreenUtil.addInfoToServerList(parent, info, false);
        }
        MultiplayerScreenUtil.saveList(parent);
        MultiplayerScreenUtil.reloadServerList(parent);
    }

    private void joinServer(String ip) {
        HostAndPort hap = HostAndPort.fromString(ip);
        ConnectScreen.connect(new TitleScreen(), MinecraftClient.getInstance(), new ServerAddress(hap.getHost(), hap.getPort()), new ServerInfo("a", hap.toString(), ServerInfo.ServerType.OTHER), false, null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        // column headers
        if (servers != null && status == null) {
            context.drawTextWithShadow(this.textRenderer, "Server IP", 10, 58, 0xAAAAAA);
            context.drawTextWithShadow(this.textRenderer, "Actions", 350, 58, 0xAAAAAA);
        }
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }
}


