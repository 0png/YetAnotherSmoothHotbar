package dev.yash.fabric.v26_2;

import dev.yash.core.YashCore;
import dev.yash.mc.v26_2.YashMinecraftClient;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/** Resets selector coordinates after client textures have been reloaded. */
final class YashResourceReloadListener implements ResourceManagerReloadListener {
    static final Identifier ID = Identifier.fromNamespaceAndPath(
            YashCore.MOD_ID, "selector_state");

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        YashMinecraftClient.resetAnimation();
    }
}
