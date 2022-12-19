package com.almostreliable.almostarch.forge;

import com.almostreliable.almostarch.PlatformService;
import com.google.auto.service.AutoService;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

@AutoService(PlatformService.class)
public class PlatformServiceImpl implements PlatformService {

    @Override
    public Platform getPlatform() {
        return Platform.Forge;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
}
