package com.iamkaf.bonded.neoforge.datagen;

import com.iamkaf.bonded.Bonded;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Bonded.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }

    private void blockWithItem(Supplier<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    private <T extends Block> void blockItem(String id, Supplier<T> block) {
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(modLoc("block/" + id)));
    }
}
