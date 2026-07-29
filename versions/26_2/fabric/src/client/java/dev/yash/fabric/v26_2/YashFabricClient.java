package dev.yash.fabric.v26_2;

import dev.yash.core.YashCore;
import dev.yash.core.config.ConfigLoadResult;
import dev.yash.mc.v26_2.YashMinecraftClient;
import dev.yash.mc.v26_2.YashMinecraftVersion;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
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
        ConfigLoadResult loaded = YashMinecraftClient.initialize(
                FabricLoader.getInstance().getConfigDir().resolve("yash.json"),
                LOGGER::warn);
        ResourceLoader resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
        resourceLoader.registerReloadListener(
                YashResourceReloadListener.ID, new YashResourceReloadListener());
        resourceLoader.addListenerOrdering(
                ResourceReloaderKeys.Client.TEXTURES, YashResourceReloadListener.ID);
        LOGGER.info(
                "{} initialized for Minecraft {} with configuration source {}",
                YashCore.MOD_NAME,
                YashMinecraftVersion.VERSION,
                loaded.source());
    }
}
