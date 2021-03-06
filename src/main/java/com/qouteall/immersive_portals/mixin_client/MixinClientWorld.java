package com.qouteall.immersive_portals.mixin_client;

import com.qouteall.immersive_portals.CGlobal;
import com.qouteall.immersive_portals.chunk_loading.MyClientChunkManager;
import com.qouteall.immersive_portals.ducks.IEClientWorld;
import com.qouteall.immersive_portals.ducks.IEWorld;
import com.qouteall.immersive_portals.portal.global_portals.GlobalTrackedPortal;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements IEClientWorld {
    @Shadow
    @Final
    @Mutable
    private ClientPlayNetworkHandler netHandler;
    
    @Mutable
    @Shadow
    @Final
    private ClientChunkManager chunkManager;
    
    @Shadow
    public abstract Entity getEntityById(int id);
    
    private List<GlobalTrackedPortal> globalTrackedPortals;
    
    @Override
    public ClientPlayNetworkHandler getNetHandler() {
        return netHandler;
    }
    
    @Override
    public void setNetHandler(ClientPlayNetworkHandler handler) {
        netHandler = handler;
    }
    
    @Override
    public List<GlobalTrackedPortal> getGlobalPortals() {
        return globalTrackedPortals;
    }
    
    @Override
    public void setGlobalPortals(List<GlobalTrackedPortal> arg) {
        globalTrackedPortals = arg;
    }
    
    //use my client chunk manager
    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    void onConstructed(
        ClientPlayNetworkHandler clientPlayNetworkHandler,
        ClientWorld.Properties properties,
        RegistryKey<World> registryKey,
        RegistryKey<DimensionType> registryKey2,
        DimensionType dimensionType,
        int chunkLoadDistance,
        Supplier<Profiler> supplier,
        WorldRenderer worldRenderer,
        boolean bl,
        long l,
        CallbackInfo ci
    ) {
        ClientWorld clientWorld = (ClientWorld) (Object) this;
        MyClientChunkManager myClientChunkManager =
            new MyClientChunkManager(clientWorld, chunkLoadDistance);
        chunkManager = myClientChunkManager;
    }
    
    //avoid entity duplicate when an entity travels
    @Inject(
        method = "addEntityPrivate",
        at = @At("TAIL")
    )
    private void onOnEntityAdded(int entityId, Entity entityIn, CallbackInfo ci) {
        CGlobal.clientWorldLoader.clientWorldMap.values().stream()
            .filter(world -> world != (Object) this)
            .forEach(world -> world.removeEntity(entityId));
    }
    
}
