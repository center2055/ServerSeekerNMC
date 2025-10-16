package de.damcraft.serverseeker;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;

public class ServerSeeker implements ClientModInitializer {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String API_KEY = "ZzOluD4Uj0TPrRPZuE94UtBuIVjYxNMt";
    public static final Gson gson = new Gson();

    @Override
    public void onInitializeClient() {
        LOG.info("Loaded ServerSeeker (Fabric) without Meteor.");
        Config.load();
    }
}
