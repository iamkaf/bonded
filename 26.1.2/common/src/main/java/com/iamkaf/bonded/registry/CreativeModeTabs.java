package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.amber.api.registry.v1.DeferredRegister;
import com.iamkaf.amber.api.registry.v1.RegistrySupplier;
import com.iamkaf.amber.api.registry.v1.creativetabs.CreativeModeTabRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CreativeModeTabs {
    private static final List<Supplier<Item>> ITEMS = new ArrayList<>();

    public static final RegistrySupplier<CreativeModeTab> BONDED = CreativeModeTabRegistry.register(
            CreativeModeTabRegistry.builder(Bonded.MOD_ID)
                    .row(CreativeModeTab.Row.TOP)
                    .column(0)
                    .icon(() -> new ItemStack(Blocks.TOOL_BENCH_ITEM.get()))
                    .title(Component.translatable("creativetab." + Bonded.MOD_ID + "." + Bonded.MOD_ID))
    );

    public static void addItem(Supplier<Item> item) {
        ITEMS.add(item);
    }

    public static void init() {
        ITEMS.forEach(item -> CreativeModeTabRegistry.getTabBuilder(BONDED.getId()).addItem(item::get));
    }
}
