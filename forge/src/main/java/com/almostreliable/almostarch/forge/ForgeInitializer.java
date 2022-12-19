package com.almostreliable.almostarch.forge;

import com.almostreliable.almostarch.AlmostArchMod;
import com.almostreliable.almostarch.BuildConfig;
import net.minecraftforge.fml.common.Mod;

@Mod(BuildConfig.MOD_ID)
public class ForgeInitializer {

    public ForgeInitializer() {
        AlmostArchMod.init();
    }
}
