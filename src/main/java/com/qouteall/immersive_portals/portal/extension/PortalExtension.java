package com.qouteall.immersive_portals.portal.extension;

import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.render.TransformationManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

// the additional features of a portal
public class PortalExtension {
    public double motionAffinity = 0;
    public boolean isSpecialFlippingPortal = false;
    
    public PortalExtension() {
    
    }
    
    public void readFromNbt(CompoundTag compoundTag) {
        if (compoundTag.contains("motionAffinity")) {
            motionAffinity = compoundTag.getDouble("motionAffinity");
        }
        if (compoundTag.contains("isSpecialFlippingPortal")) {
            isSpecialFlippingPortal = compoundTag.getBoolean("isSpecialFlippingPortal");
        }
        
    }
    
    public void writeToNbt(CompoundTag compoundTag) {
        if (motionAffinity != 0) {
            compoundTag.putDouble("motionAffinity", motionAffinity);
        }
        if (isSpecialFlippingPortal) {
            compoundTag.putBoolean("isSpecialFlippingPortal", isSpecialFlippingPortal);
        }
    }
    
    public void tick(Portal portal) {
        if (portal.world.isClient()) {
            tickClient(portal);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void tickClient(Portal portal) {
    
    }
}
