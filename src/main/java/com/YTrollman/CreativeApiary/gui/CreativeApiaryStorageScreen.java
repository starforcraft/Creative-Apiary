package com.YTrollman.CreativeApiary.gui;

import com.YTrollman.CreativeApiary.CreativeApiary;
import com.YTrollman.CreativeApiary.container.CreativeApiaryStorageContainer;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryStorageTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.client.gui.widget.TabImageButton;
import com.resourcefulbees.resourcefulbees.lib.ApiaryTabs;
import com.resourcefulbees.resourcefulbees.network.NetPacketHandler;
import com.resourcefulbees.resourcefulbees.network.packets.ApiaryTabMessage;
import com.resourcefulbees.resourcefulbees.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

public class CreativeApiaryStorageScreen extends ContainerScreen<CreativeApiaryStorageContainer> {

    private static final ResourceLocation BACKGROUND_9X12 = new ResourceLocation(CreativeApiary.MOD_ID, "textures/gui/creative_apiary_storage.png");
    private static final ResourceLocation TABS_BG = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/gui/apiary/apiary_gui_tabs.png");

    private CreativeApiaryStorageTileEntity apiaryStorageTileEntity;

    private ResourceLocation background;

    private TabImageButton mainTabButton;
    private TabImageButton breedTabButton;

    public CreativeApiaryStorageScreen(CreativeApiaryStorageContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        apiaryStorageTileEntity = this.menu.getApiaryStorageTileEntity();
        preInit();
    }

    protected void preInit(){
        this.imageWidth = 281;
        this.imageHeight = 276;
        background = BACKGROUND_9X12;
    }

    @Override
    protected void init() {
        super.init();
        this.buttons.clear();

        int i = this.leftPos;
        int j = this.topPos;
        int t = i + this.imageWidth - 23;

        mainTabButton = this.addButton(new TabImageButton(t+1, j+17, 18, 18, 110, 0, 18, TABS_BG, new ItemStack(ModItems.BEE_JAR.get()), 1, 1,
                onPress -> this.changeScreen(ApiaryTabs.MAIN), 128, 128) {

            @Override
            public void renderToolTip(@NotNull MatrixStack matrix, int mouseX, int mouseY) {
                StringTextComponent s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.main_screen"));
                CreativeApiaryStorageScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
            }
        });

        this.addButton(new TabImageButton(t + 1, j + 37, 18, 18, 110, 0, 18, TABS_BG, new ItemStack(Items.HONEYCOMB), 2, 1,
                onPress -> this.changeScreen(ApiaryTabs.STORAGE), 128, 128) {

            @Override
            public void renderToolTip(@NotNull MatrixStack matrix,int mouseX, int mouseY) {
                StringTextComponent s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.storage_screen"));
                CreativeApiaryStorageScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
            }
        }).active = false;

        breedTabButton = this.addButton(new TabImageButton(t + 1, j + 57, 18, 18, 110, 0, 18, TABS_BG, new ItemStack(ModItems.GOLD_FLOWER_ITEM.get()), 1, 1,
                onPress -> this.changeScreen(ApiaryTabs.BREED), 128, 128) {

            @Override
            public void renderToolTip(@NotNull MatrixStack matrix,int mouseX, int mouseY) {
                StringTextComponent s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.breed_screen"));
                CreativeApiaryStorageScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
            }
        });
    }

    private void changeScreen(ApiaryTabs tab) {
        switch (tab) {
            case BREED:
                if (breedTabButton.active)
                    NetPacketHandler.sendToServer(new ApiaryTabMessage(getApiaryStorageTileEntity().getBlockPos(), ApiaryTabs.BREED));
                break;
            case STORAGE:
                break;
            case MAIN:
                if (mainTabButton.active)
                    NetPacketHandler.sendToServer(new ApiaryTabMessage(getApiaryStorageTileEntity().getBlockPos(), ApiaryTabs.MAIN));
        }
    }

    @Override
    protected void renderBg(@NotNull MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        if (this.menu.isRebuild()) {
            preInit();
            init();
            this.menu.setRebuild(false);
        }

        mainTabButton.active = getApiaryStorageTileEntity().getApiary() != null;
        breedTabButton.active = getApiaryStorageTileEntity().getApiary() != null && getApiaryStorageTileEntity().getApiary().getBreederPos() != null;

        Minecraft client = this.minecraft;
        if (client != null) {
            this.minecraft.getTextureManager().bind(getBackground());
            int i = this.leftPos;
            int j = this.topPos;
            blit(matrix, i + 26, j, 0, 0, this.imageWidth, this.imageHeight, 384, 384);
            blit(matrix, i + 1, j + 12, 359, 0, 25, 28, 384, 384);
            int t = i + this.imageWidth - 23;
            this.minecraft.getTextureManager().bind(TABS_BG);
            blit(matrix, t -1, j + 12, 0,0, 25, 68, 128, 128);
        }
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (getApiaryStorageTileEntity() != null) {
            this.renderBackground(matrix);
            super.render(matrix, mouseX, mouseY, partialTicks);
            this.renderTooltip(matrix, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(@NotNull MatrixStack matrix, int mouseX, int mouseY) {
        for (Widget widget : this.buttons) {
            if (widget.isHovered()) {
                widget.renderToolTip(matrix, mouseX - this.leftPos, mouseY - this.topPos);
                break;
            }
        }
    }

    public CreativeApiaryStorageTileEntity getApiaryStorageTileEntity() {
        return apiaryStorageTileEntity;
    }

    public ResourceLocation getBackground() {
        return background;
    }
}
