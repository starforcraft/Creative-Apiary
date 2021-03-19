package com.YTrollman.CreativeApiary.registry;

import javax.annotation.Nonnull;

import com.YTrollman.CreativeApiary.CreativeApiary;
import com.resourcefulbees.resourcefulbees.compat.jei.ApiaryCategory;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class CreativeApiaryPlugin implements IModPlugin {
    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CreativeApiary.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TCREATIVE_APIARY_BLOCK.get()), ApiaryCategory.ID);
    }
}