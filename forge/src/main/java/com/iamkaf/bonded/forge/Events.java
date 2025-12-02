package com.iamkaf.bonded.forge;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraftforge.event.ModifyDefaultComponentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Bonded.MOD_ID)
public class Events {
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onDefaultItemComponent(ModifyDefaultComponentsEvent event) {
        TierMap.getRepairMaterialMap().forEach((tier, material) -> {
            event.modify(tier, builder -> {
                builder.set(
                        DataComponents.REPAIRABLE,
                        new Repairable(HolderSet.direct(material.builtInRegistryHolder()))
                );
            });
        });
    }
}