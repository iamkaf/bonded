package com.iamkaf.bonded.fabric.datagen;

import com.iamkaf.bonded.registry.Tags;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        valueLookupBuilder(Tags.ARMORS)
                .forceAddTag(ItemTags.HEAD_ARMOR)
                .forceAddTag(ItemTags.CHEST_ARMOR)
                .forceAddTag(ItemTags.LEG_ARMOR)
                .forceAddTag(ItemTags.FOOT_ARMOR)
                .add(Items.ELYTRA);

        valueLookupBuilder(Tags.MELEE_WEAPONS)
                .forceAddTag(ItemTags.SWORDS)
                .forceAddTag(ItemTags.SPEARS)
                .add(Items.MACE);

        valueLookupBuilder(Tags.RANGED_WEAPONS)
                .add(Items.BOW)
                .add(Items.CROSSBOW)
                .add(Items.TRIDENT);

        valueLookupBuilder(Tags.DIGGING_EQUIPMENT)
                .forceAddTag(ItemTags.PICKAXES)
                .forceAddTag(ItemTags.AXES)
                .forceAddTag(ItemTags.SHOVELS)
                .forceAddTag(ItemTags.HOES);

        valueLookupBuilder(Tags.UTILITY_EQUIPMENT)
                .forceAddTag(Tags.FISHING_RODS)
                .forceAddTag(Tags.BRUSHES)
                .forceAddTag(Tags.IGNITERS)
                .forceAddTag(Tags.SHEARS);
    }
}
