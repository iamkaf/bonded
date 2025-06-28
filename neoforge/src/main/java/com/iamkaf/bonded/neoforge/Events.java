package com.iamkaf.bonded.neoforge;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.TierMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Repairable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

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
