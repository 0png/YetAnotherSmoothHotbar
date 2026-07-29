package dev.yash.fabric.v26_2.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.yash.mc.v26_2.YashMinecraftClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric-owned hooks that delegate all selector behavior to the Minecraft 26.2 layer. */
@Mixin(Hud.class)
abstract class HudMixin {
    @WrapOperation(
            method = "extractItemHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite("
                            + "Lcom/mojang/blaze3d/pipeline/RenderPipeline;"
                            + "Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 1),
            require = 1,
            allow = 1)
    private void yash$wrapHotbarSelector(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier sprite,
            int x,
            int y,
            int width,
            int height,
            Operation<Void> original) {
        if (!YashMinecraftClient.renderHotbarSelector(
                graphics, pipeline, sprite, x, y, width, height)) {
            original.call(graphics, pipeline, sprite, x, y, width, height);
        }
    }

    @Inject(method = {"toggle", "onDisconnected"}, at = @At("TAIL"))
    private void yash$resetAfterHudLifecycleChange(CallbackInfo callbackInfo) {
        YashMinecraftClient.resetAnimation();
    }
}
