package com.iamkaf.bonded.fabric.datagen;

import com.iamkaf.bonded.registry.Blocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
        super(provider, recipeOutput);
    }

    @Override
    public void buildRecipes() {
        ShapedRecipeBuilder.shaped(registries.lookupOrThrow(BuiltInRegistries.ITEM.key()),
                        RecipeCategory.BUILDING_BLOCKS,
                        Blocks.TOOL_BENCH.get()
                )
                .pattern("AAA")
                .pattern("A A")
                .pattern("B B")
                .define('A', ItemTags.PLANKS)
                .define('B', ItemTags.LOGS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output);

        ShapedRecipeBuilder.shaped(
                        registries.lookupOrThrow(BuiltInRegistries.ITEM.key()),
                        RecipeCategory.BUILDING_BLOCKS,
                        Blocks.REPAIR_BENCH.get()
                )
                .pattern("AAA")
                .pattern("A A")
                .pattern("B B")
                .define('A', net.minecraft.world.item.Items.COBBLESTONE)
                .define('B', net.minecraft.world.item.Items.STONE_BRICKS)
                .unlockedBy("has_cobby", has(net.minecraft.world.item.Items.COBBLESTONE))
                .save(output);
    }

    public static class Runner extends FabricRecipeProvider {
        public Runner(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider registries,
                @NotNull RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "Bonded Recipes";
        }
    }
}
