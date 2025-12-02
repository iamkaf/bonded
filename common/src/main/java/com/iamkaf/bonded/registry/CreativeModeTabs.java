package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.CREATIVE_MODE_TAB);

    private static final List<Supplier<Item>> ITEMS = new ArrayList<>();

    public static final RegistrySupplier<CreativeModeTab> BONDED = TABS.register(
            Bonded.MOD_ID,
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(Blocks.TOOL_BENCH_ITEM.get()))
                    .title(Component.translatable("creativetab." + Bonded.MOD_ID + "." + Bonded.MOD_ID))
                    .displayItems((itemDisplayParameters, output) -> {
                        ITEMS.forEach(item -> output.accept(item.get()));
                    })
                    .build()
    );

    public static void addItem(Supplier<Item> item) {
        ITEMS.add(item);
    }

    public static void init() {
        TABS.register();
    }
}