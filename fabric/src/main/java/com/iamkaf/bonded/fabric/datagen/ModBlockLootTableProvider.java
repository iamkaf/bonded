package com.iamkaf.bonded.fabric.datagen;

import com.iamkaf.bonded.registry.Blocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {
    public ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(Blocks.TOOL_BENCH.get());
        dropSelf(Blocks.REPAIR_BENCH.get());
    }

//    @Override
//    protected Iterable<Block> getKnownBlocks() {
//        return List.of(Blocks.TOOL_BENCH.get(), Blocks.REPAIR_BENCH.get());
//    }
}
