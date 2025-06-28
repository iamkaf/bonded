package com.iamkaf.bonded.fabric.datagen;

import com.iamkaf.bonded.registry.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        valueLookupBuilder(Tags.DIGGING_EQUIPMENT).forceAddTag(ItemTags.PICKAXES)
                .forceAddTag(ItemTags.AXES)
                .forceAddTag(ItemTags.SHOVELS)
                .forceAddTag(ItemTags.HOES);
        valueLookupBuilder(Tags.UTILITY_EQUIPMENT).forceAddTag(Tags.FISHING_RODS)
                .forceAddTag(Tags.BRUSHES)
                .forceAddTag(Tags.IGNITERS)
                .forceAddTag(Tags.SHEARS)
                .add(Items.ELYTRA);
    }
}
