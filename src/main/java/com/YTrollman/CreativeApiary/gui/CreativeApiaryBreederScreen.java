package com.YTrollman.CreativeApiary.gui;

import com.YTrollman.CreativeApiary.container.CreativeApiaryBreederContainer;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryBreederTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.resourcefulbees.resourcefulbees.client.gui.widget.TabImageButton;
import com.resourcefulbees.resourcefulbees.container.ApiaryBreederContainer;
import com.resourcefulbees.resourcefulbees.lib.ApiaryTabs;
import com.resourcefulbees.resourcefulbees.network.NetPacketHandler;
import com.resourcefulbees.resourcefulbees.network.packets.ApiaryTabMessage;
import com.resourcefulbees.resourcefulbees.registry.ModItems;
import com.resourcefulbees.resourcefulbees.utils.MathUtils;
import java.util.Iterator;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CreativeApiaryBreederScreen extends ContainerScreen<CreativeApiaryBreederContainer> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("creativeapiary", "textures/gui/creative_apiary_breeder_gui.png");
    private static final ResourceLocation TABS_BG = new ResourceLocation("resourcefulbees", "textures/gui/apiary/apiary_gui_tabs.png");
    private CreativeApiaryBreederTileEntity apiaryBreederTileEntity;
    private TabImageButton mainTabButton;
    private TabImageButton storageTabButton;
    private final int breedersize = 5;

    public CreativeApiaryBreederScreen(CreativeApiaryBreederContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.preInit();
    }

    protected void preInit() {
        this.imageWidth = 226;
        this.imageHeight = 110 + this.breedersize * 20;
    }

    protected void init() {
        super.init();
        this.buttons.clear();
        this.apiaryBreederTileEntity = this.menu.getApiaryBreederTileEntity();
        int i = this.leftPos;
        int j = this.topPos;
        int t = i + this.imageWidth - 24;
        this.mainTabButton = this.addButton(new TabImageButton(t + 1, j + 17, 18, 18, 110, 0, 18, TABS_BG, new ItemStack((IItemProvider)ModItems.BEE_JAR.get()), 1, 1, (onPress) -> {
            this.changeScreen(ApiaryTabs.MAIN);
        }, 128, 128) {
            public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
                StringTextComponent s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.main_screen"));
                CreativeApiaryBreederScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
            }
        });
        this.storageTabButton = this.addButton(new TabImageButton(t + 1, j + 37, 18, 18, 110, 0, 18, TABS_BG, new ItemStack(Items.HONEYCOMB), 2, 1, (onPress) -> {
            this.changeScreen(ApiaryTabs.STORAGE);
        }, 128, 128) {
            public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
                StringTextComponent s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.storage_screen"));
                CreativeApiaryBreederScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
            }
        });

        class NamelessClass_1 extends TabImageButton {
            NamelessClass_1(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, ItemStack displayItem, int itemX, int itemY, IPressable onPressIn, int textureWidth, int textureHeight) {
                super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, displayItem, itemX, itemY, onPressIn, textureWidth, textureHeight);
            }

            public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
                StringTextComponent s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.breed_screen"));
                CreativeApiaryBreederScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
            }
        }

        (this.addButton(new NamelessClass_1(t + 1, j + 57, 18, 18, 110, 0, 18, TABS_BG, new ItemStack(ModItems.GOLD_FLOWER_ITEM.get()), 1, 1, (onPress) -> {
            this.changeScreen(ApiaryTabs.BREED);
        }, 128, 128))).active = false;
    }

    private void changeScreen(ApiaryTabs tab) {
        switch(tab) {
            case BREED:
            default:
                break;
            case STORAGE:
                if (this.storageTabButton.active) {
                    NetPacketHandler.sendToServer(new ApiaryTabMessage(this.getApiaryBreederTileEntity().getBlockPos(), ApiaryTabs.STORAGE));
                }
                break;
            case MAIN:
                if (this.mainTabButton.active) {
                    NetPacketHandler.sendToServer(new ApiaryTabMessage(this.getApiaryBreederTileEntity().getBlockPos(), ApiaryTabs.MAIN));
                }
        }

    }

    public void render(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrix, mouseX, mouseY);
    }

    protected void renderBg(@Nonnull MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        if (this.menu.isRebuild()) {
            this.preInit();
            this.init();
            this.menu.setRebuild(false);
        }

        this.mainTabButton.active = this.getApiaryBreederTileEntity().getApiary() != null;
        this.storageTabButton.active = this.getApiaryBreederTileEntity().getApiary() != null && this.getApiaryBreederTileEntity().getApiary().getStoragePos() != null;
        Minecraft client = this.minecraft;
        if (client != null) {
            client.getTextureManager().bind(BACKGROUND);
            int i = this.leftPos;
            int j = this.topPos;
            this.blit(matrix, i, j + 16, 0, 16, 25, 82);
            this.blit(matrix, i + 25, j, 25, 0, 176, 15);

            int t;
            for(t = 0; t < this.breedersize; ++t) {
                this.blit(matrix, i + 25, j + 15 + t * 20, 25, 15, 176, 20);
            }

            if (!(this.menu.getApiaryBreederTileEntity().getTotalTime() == 0))
            {
                for(t = 0; t < this.breedersize; ++t) {
                    int scaledprogress = MathUtils.clamp(118 * this.menu.times.get(t) / this.menu.getApiaryBreederTileEntity().getTotalTime(), 0, this.menu.getApiaryBreederTileEntity().getTotalTime());
                    this.blit(matrix, i + 54, j + 21 + t * 20, 0, 246, scaledprogress, 10);
                }
            }

            this.blit(matrix, i + 25, j + 15 + 20 * 5, 25, 95, 176, 95);
            t = i + this.imageWidth - 24;
            this.minecraft.getTextureManager().bind(TABS_BG);
            blit(matrix, t - 1, j + 12, 0.0F, 0.0F, 25, 68, 128, 128);
        }
    }

    protected void renderLabels(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        Iterator var4 = this.buttons.iterator();

        while(var4.hasNext()) {
            Widget widget = (Widget)var4.next();
            if (widget.isHovered()) {
                widget.renderToolTip(matrix, mouseX - this.leftPos, mouseY - this.topPos);
                break;
            }
        }
    }

    public CreativeApiaryBreederTileEntity getApiaryBreederTileEntity() {
        return this.apiaryBreederTileEntity;
    }
}
