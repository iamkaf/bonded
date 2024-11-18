package com.iamkaf.bonded.neoforge.datagen;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.registry.Tags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput arg, CompletableFuture<HolderLookup.Provider> completableFuture,
            CompletableFuture<TagLookup<Block>> completableFuture2,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(arg, completableFuture, completableFuture2, Bonded.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.DIGGING_EQUIPMENT).addTag(ItemTags.PICKAXES)
                .addTag(ItemTags.AXES)
                .addTag(ItemTags.SHOVELS)
                .addTag(ItemTags.HOES);
        tag(Tags.UTILITY_EQUIPMENT).addTag(Tags.FISHING_RODS)
                .addTag(Tags.BRUSHES)
                .addTag(Tags.IGNITERS)
                .addTag(Tags.SHEARS);
    }
}
