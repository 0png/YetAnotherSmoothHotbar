package dev.yash.fabric.v26_2;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.yash.mc.v26_2.YashMinecraftClient;

/** Loaded only when Mod Menu is present through its optional Fabric entrypoint. */
public final class YashModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return YashMinecraftClient::createConfigScreen;
    }
}
