package com.almostreliable.almostarch.fabric;

import com.almostreliable.almostarch.AlmostArchMod;
import net.fabricmc.api.ModInitializer;

public class FabricInitializer implements ModInitializer {

    @Override
    public void onInitialize() {
        AlmostArchMod.init();
    }
}
