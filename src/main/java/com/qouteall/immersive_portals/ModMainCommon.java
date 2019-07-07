package com.qouteall.immersive_portals;

import com.qouteall.immersive_portals.my_util.Signal;
import com.qouteall.immersive_portals.portal_entity.PortalEntity;
import net.fabricmc.api.ModInitializer;

public class ModMainCommon implements ModInitializer {
    public static Signal clientTickSignal = new Signal();
    public static Signal serverTickSignal = new Signal();
    
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        PortalEntity.init();
        
        MyNetwork.init();
        
    }
}