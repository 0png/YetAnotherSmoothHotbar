package dev.yash.fabric.v26_2;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yash.core.YashCore;
import dev.yash.core.i18n.TranslationKeys;
import dev.yash.mc.v26_2.YashMinecraftClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Fabric key registration kept outside the Minecraft-version implementation. */
final class YashKeyBindings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(YashCore.MOD_ID, "controls"));

    private YashKeyBindings() {
    }

    static void register() {
        KeyMapping openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                TranslationKeys.OPEN_CONFIG,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_Y,
                CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettings.consumeClick()) {
                if (client.player != null && client.level != null) {
                    client.setScreenAndShow(YashMinecraftClient.createConfigScreen(null));
                }
            }
        });
    }
}
