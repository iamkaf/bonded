package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> BONDED = TABS.register(
            Bonded.MOD_ID,
            () -> CreativeTabRegistry.create(Component.translatable("creativetab." + Bonded.MOD_ID + "." + Bonded.MOD_ID),
                    () -> new ItemStack(Blocks.TOOL_BENCH_ITEM.get())
            )
    );

    public static void init() {
        TABS.register();
    }
}