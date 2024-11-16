package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.block.RepairBenchBlock;
import com.iamkaf.bonded.block.ToolBenchBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.BLOCK);

    public static RegistrySupplier<Block> TOOL_BENCH = register("tool_bench",
            () -> new ToolBenchBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE)
                    .noOcclusion())
    );
    public static RegistrySupplier<Item> TOOL_BENCH_ITEM =
            thisShouldBeDoneAutomaticallyAndInternallyByArchitectury("tool_bench", TOOL_BENCH);

    public static RegistrySupplier<Block> REPAIR_BENCH = register("repair_bench",
            () -> new RepairBenchBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.COBBLESTONE)
                    .noOcclusion())
    );
    public static RegistrySupplier<Item> REPAIR_BENCH_ITEM =
            thisShouldBeDoneAutomaticallyAndInternallyByArchitectury("repair_bench", REPAIR_BENCH);

    private static RegistrySupplier<Block> register(String id, Supplier<Block> block) {
        return BLOCKS.register(id, block);
    }

    private static RegistrySupplier<Item> thisShouldBeDoneAutomaticallyAndInternallyByArchitectury(String id, Supplier<Block> block) {
        return Items.ITEMS.register(
                id,
                () -> new BlockItem(block.get(), new Item.Properties().arch$tab(CreativeModeTabs.BONDED))
        );
    }

    public static void init() {
        BLOCKS.register();
    }
}