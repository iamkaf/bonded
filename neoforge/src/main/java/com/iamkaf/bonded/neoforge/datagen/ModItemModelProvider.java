package com.iamkaf.bonded.neoforge.datagen;

import com.iamkaf.bonded.Bonded;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Bonded.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }

    public void buttonItem(String id, String baseBlockId) {
        this.withExistingParent(id, mcLoc("block/button_inventory"))
                .texture("texture", modLoc("block/" + baseBlockId));
    }

    public void fenceItem(String id, String baseBlockId) {
        this.withExistingParent(id, mcLoc("block/fence_inventory"))
                .texture("texture", modLoc("block/" + baseBlockId));
    }

    public void wallItem(String id, String baseBlockId) {
        this.withExistingParent(id, mcLoc("block/wall_inventory"))
                .texture("wall", modLoc("block/" + baseBlockId));
    }

    private ItemModelBuilder handheldItem(String id) {
        return this.withExistingParent(modLoc(id).getPath(), ResourceLocation.parse("item/handheld"))
                .texture("layer0", modLoc("item/" + id));
    }
}
