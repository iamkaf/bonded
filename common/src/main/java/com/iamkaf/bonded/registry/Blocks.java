package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.block.ToolBenchBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.BLOCK);

    public static RegistrySupplier<Block> TOOL_BENCH = BLOCKS.register(
            "tool_bench",
            () -> new ToolBenchBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE)
                    .noOcclusion())
    );

    public static RegistrySupplier<Block> REPAIR_BENCH = BLOCKS.register(
            "repair_bench",
            () -> new ToolBenchBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.COBBLESTONE)
                    .noOcclusion())
    );

    public static void init() {
        BLOCKS.register();
    }
}