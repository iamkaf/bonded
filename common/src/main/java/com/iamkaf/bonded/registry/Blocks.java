package com.iamkaf.bonded.registry;

import com.iamkaf.bonded.Bonded;
import com.iamkaf.bonded.block.RepairBenchBlock;
import com.iamkaf.bonded.block.ToolBenchBlock;
import dev.architectury.registry.registries.DeferredRegister;
//import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Bonded.MOD_ID, Registries.BLOCK);

    public static Supplier<Block> TOOL_BENCH = register("tool_bench",
            () -> new ToolBenchBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.CRAFTING_TABLE)
                    .noOcclusion()
                    .setId(id2("tool_bench")))
    );
    public static Supplier<Item> TOOL_BENCH_ITEM =
            thisShouldBeDoneAutomaticallyAndInternallyByArchitectury("tool_bench", TOOL_BENCH);

    public static Supplier<Block> REPAIR_BENCH = register("repair_bench",
            () -> new RepairBenchBlock(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.COBBLESTONE)
                    .noOcclusion()
                    .setId(id2("repair_bench")))
    );
    public static Supplier<Item> REPAIR_BENCH_ITEM =
            thisShouldBeDoneAutomaticallyAndInternallyByArchitectury("repair_bench", REPAIR_BENCH);

    private static Supplier<Block> register(String id, Supplier<Block> block) {
        return BLOCKS.register(id, block);
    }

    private static Supplier<Item> thisShouldBeDoneAutomaticallyAndInternallyByArchitectury(String id, Supplier<Block> block) {
        return Items.ITEMS.register(id,
                () -> new BlockItem(block.get(),
                        new Item.Properties().setId(id(id)).arch$tab(CreativeModeTabs.BONDED)
                )
        );
    }

    private static ResourceKey<Item> id(String id) {
        return ResourceKey.create(Registries.ITEM, Bonded.resource(id));
    }

    private static ResourceKey<Block> id2(String id) {
        return ResourceKey.create(Registries.BLOCK, Bonded.resource(id));
    }

    public static void init() {
        BLOCKS.register();
    }
}