package com.blockentityoutlines.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import net.minecraft.client.render.FrameGraphBuilder;
import org.joml.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private BufferBuilderStorage bufferBuilders;

    @Redirect(method = "renderBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    private void redirectBlockEntityRender(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity,
            float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        OutlineVertexConsumerProvider outlineProvider = this.bufferBuilders.getOutlineVertexConsumers();

        // outlineProvider.setColor(255, 255, 255, 255); // Default white outline

        dispatcher.render(blockEntity, tickProgress, matrices, outlineProvider); // Render with outline
        dispatcher.render(blockEntity, tickProgress, matrices, vertexConsumers); // Always render normally too
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V"), index = 6)
    private boolean forceEntityOutline(boolean renderEntityOutline) {
        return true;
    }

    @Inject(method = "getEntitiesToRender", at = @At("RETURN"), cancellable = true)
    private void forceEntityOutlineReturn(Camera camera, Frustum frustum, List<Entity> output,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}