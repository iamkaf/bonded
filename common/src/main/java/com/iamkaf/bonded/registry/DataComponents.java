package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.component.AppliedBonusesContainer;
import com.iamkaf.bonded.component.ItemLevelContainer;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

public class DataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Bonded.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final DeferredSupplier<DataComponentType<ItemLevelContainer>> ITEM_LEVEL_CONTAINER =
            DATA_COMPONENT_TYPES.register(
                    Bonded.resource("item_level"),
                    () -> DataComponentType.<ItemLevelContainer>builder()
                            .persistent(ItemLevelContainer.CODEC)
                            .build()
            );
    public static final DeferredSupplier<DataComponentType<AppliedBonusesContainer>> APPLIED_BONUSES_CONTAINER =
            DATA_COMPONENT_TYPES.register(
                    Bonded.resource("applied_bonuses"),
                    () -> DataComponentType.<AppliedBonusesContainer>builder()
                            .persistent(AppliedBonusesContainer.CODEC)
                            .build()
            );

    public static void init() {
        DATA_COMPONENT_TYPES.register();
    }
}
