package com.iamkaf.bonded.neoforge.datagen;

import com.iamkaf.bonded.registry.Blocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.TOOL_BENCH.get())
                .pattern("AAA")
                .pattern("A A")
                .pattern("B B")
                .define('A', ItemTags.PLANKS)
                .define('B', ItemTags.LOGS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Blocks.REPAIR_BENCH.get())
                .pattern("AAA")
                .pattern("A A")
                .pattern("B B")
                .define('A', net.minecraft.world.item.Items.COBBLESTONE)
                .define('B', net.minecraft.world.item.Items.STONE_BRICKS)
                .unlockedBy("has_cobby", has(net.minecraft.world.item.Items.COBBLESTONE))
                .save(recipeOutput);
    }
}
