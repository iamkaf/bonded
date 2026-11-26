package com.iamkaf.bonded.platform;

import com.iamkaf.bonded.platform.services.IPlatformHelper;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        //? if >=1.21.2
        return LoadingModList.getModFileById(modId) != null;
        //? if <1.21.2
        /*return LoadingModList.get().getModFileById(modId) != null;*/
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
