package dev.yash.fabric.v26_2;

import dev.yash.core.YashCore;
import dev.yash.mc.v26_2.YashMinecraftVersion;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric-only bootstrap. Product features belong in the shared or
 * Minecraft-version module and are invoked from here.
 */
public final class YashFabricClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(YashCore.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("{} client scaffold initialized for Minecraft {}", YashCore.MOD_NAME,
                YashMinecraftVersion.VERSION);
    }
}
