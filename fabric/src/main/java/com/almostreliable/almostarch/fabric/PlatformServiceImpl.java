package com.almostreliable.almostarch.fabric;

import com.almostreliable.almostarch.PlatformService;
import com.google.auto.service.AutoService;
import net.fabricmc.loader.api.FabricLoader;

@AutoService(PlatformService.class)
public class PlatformServiceImpl implements PlatformService {

    @Override
    public Platform getPlatform() {
        return Platform.Fabric;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
