package com.YTrollman.CreativeApiary.gui;

import com.YTrollman.CreativeApiary.container.UnvalidatedCreativeApiaryContainer;
import com.YTrollman.CreativeApiary.network.CreativeBuildApiaryMessage;
import com.YTrollman.CreativeApiary.network.CreativeNetPacketHandler;
import com.YTrollman.CreativeApiary.network.CreativeValidateApiaryMessage;
import com.YTrollman.CreativeApiary.tileentity.CreativeApiaryTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.client.gui.widget.ArrowButton;
import com.resourcefulbees.resourcefulbees.utils.MathUtils;
import com.resourcefulbees.resourcefulbees.utils.PreviewHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class UnvalidatedCreativeApiaryScreen extends ContainerScreen<UnvalidatedCreativeApiaryContainer> {

    private static final ResourceLocation unvalidatedTexture = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/gui/apiary/unvalidated.png");
    private static final ResourceLocation arrowButtonTexture = new ResourceLocation(ResourcefulBees.MOD_ID, "textures/gui/apiary/arrow_button.png");
    private final CreativeApiaryTileEntity apiaryTileEntity;
    private final PlayerEntity player;
    private int verticalOffset;
    private int horizontalOffset;
    private ArrowButton upButton;
    private ArrowButton downButton;
    private ArrowButton leftButton;
    private ArrowButton rightButton;
    private PreviewButton previewButton;

    public UnvalidatedCreativeApiaryScreen(UnvalidatedCreativeApiaryContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.player = inv.player;
        this.verticalOffset = screenContainer.getApiaryTileEntity().getVerticalOffset();
        this.horizontalOffset = screenContainer.getApiaryTileEntity().getHorizontalOffset();
        this.apiaryTileEntity = this.menu.getApiaryTileEntity();
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(getGuiLeft() + 116, getGuiTop() + 10, 50, 20, new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.validate")), onPress -> this.validate()));
        BuildButton buildStructureButton = this.addButton(new BuildButton(getGuiLeft() + 116, getGuiTop() + 35, 50, 20, new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.build")), onPress -> this.build()));
        if (!this.player.isCreative()) {
            buildStructureButton.active = false;
        }
        this.previewButton = this.addButton(new PreviewButton(getGuiLeft() + 22, getGuiTop() + 25, 12, 12, 0, 24, 12, arrowButtonTexture, this.menu.getApiaryTileEntity().isPreviewed(), onPress -> {
            setPreviewToggle();
            previewSetToggle(this.previewButton.isTriggered());
        }));
        previewSetToggle(this.previewButton.isTriggered());
        this.upButton = this.addButton(new ArrowButton(getGuiLeft() + 22, getGuiTop() + 12, ArrowButton.Direction.UP, onPress -> this.offsetPosition(Direction.UP)));
        this.downButton = this.addButton(new ArrowButton(getGuiLeft() + 22, getGuiTop() + 38, ArrowButton.Direction.DOWN, onPress -> this.offsetPosition(Direction.DOWN)));
        this.leftButton = this.addButton(new ArrowButton(getGuiLeft() + 9, getGuiTop() + 25, ArrowButton.Direction.LEFT, onPress -> this.offsetPosition(Direction.LEFT)));
        this.rightButton = this.addButton(new ArrowButton(getGuiLeft() + 35, getGuiTop() + 25, ArrowButton.Direction.RIGHT, onPress -> this.offsetPosition(Direction.RIGHT)));
    }

    private void previewSetToggle(boolean toggled) {
        if (!toggled)
            this.previewButton.setTrigger(false);

        PreviewHandler.setPreview(getMenu().getPos(), this.menu.getApiaryTileEntity().buildStructureBounds(this.horizontalOffset, this.verticalOffset), toggled);
    }

	private void setPreviewToggle() {
        if (this.previewButton.active)
            this.previewButton.setTrigger(!this.previewButton.isTriggered());
    }

    private void offsetPosition(Direction direction) {
        previewSetToggle(false);
        switch (direction) {
            case UP:
                verticalOffset++;
                break;
            case DOWN:
                verticalOffset--;
                break;
            case LEFT:
                horizontalOffset--;
                break;
            default:
                horizontalOffset++;
        }
        verticalOffset = MathUtils.clamp(verticalOffset, -1, 2);
        horizontalOffset = MathUtils.clamp(horizontalOffset, -2, 2);

        apiaryTileEntity.setVerticalOffset(verticalOffset);
        apiaryTileEntity.setHorizontalOffset(horizontalOffset);
    }

    private void build() {
        previewSetToggle(false);
        UnvalidatedCreativeApiaryContainer container = getMenu();
        BlockPos pos = container.getPos();
        CreativeNetPacketHandler.sendToServer(new CreativeBuildApiaryMessage(pos, verticalOffset, horizontalOffset));
    }

    @Override
    public void render(@NotNull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (apiaryTileEntity != null) {
            this.upButton.active = verticalOffset != 2;
            this.downButton.active = verticalOffset != -1;
            this.leftButton.active = horizontalOffset != -2;
            this.rightButton.active = horizontalOffset != 2;
            this.renderBackground(matrix);
            super.render(matrix, mouseX, mouseY, partialTicks);
            this.renderTooltip(matrix, mouseX, mouseY);
        }
    }

    private void validate() {
        previewSetToggle(false);
        UnvalidatedCreativeApiaryContainer container = getMenu();
        BlockPos pos = container.getPos();
        CreativeNetPacketHandler.sendToServer(new CreativeValidateApiaryMessage(pos, verticalOffset, horizontalOffset));
    }

    @Override
    protected void renderBg(@NotNull MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        Minecraft client = this.minecraft;
        if (client != null) {
            this.minecraft.getTextureManager().bind(unvalidatedTexture);
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            this.blit(matrix, i, j, 0, 0, this.imageWidth, this.imageHeight);
        }
    }

    @Override
    protected void renderLabels(@NotNull MatrixStack matrix, int mouseX, int mouseY) {
        this.font.draw(matrix,  "Offset", 65, 13, 0x404040);
        this.font.draw(matrix, "Vert.", 75, 26, 0x404040);
        this.font.draw(matrix, "Horiz.", 75, 39, 0x404040);
        this.drawRightAlignedString(matrix, font, String.valueOf(verticalOffset), 70, 26, 0x404040);
        this.drawRightAlignedString(matrix, font, String.valueOf(horizontalOffset), 70, 39, 0x404040);

        for (Widget widget : this.buttons) {
            if (widget.isHovered()) {
                widget.renderToolTip(matrix, mouseX - this.leftPos, mouseY - this.topPos);
                break;
            }
        }
    }

    public void drawRightAlignedString(@NotNull MatrixStack matrix, FontRenderer fontRenderer, @NotNull String s, int posX, int posY, int color) {
        fontRenderer.draw(matrix, s, (float) (posX - fontRenderer.width(s)), (float) posY, color);
    }

    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    @OnlyIn(Dist.CLIENT)
    public class BuildButton extends Button {
        public BuildButton(int widthIn, int heightIn, int width, int height, StringTextComponent text, IPressable onPress) {
            super(widthIn, heightIn, width, height, text, onPress);
        }

        @Override
        public void renderToolTip(@NotNull MatrixStack matrix, int mouseX, int mouseY) {
            if (!this.active) {
                StringTextComponent s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.build.creative"));
                UnvalidatedCreativeApiaryScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class PreviewButton extends ImageButton {
        private final ResourceLocation resourceLocation;
        private final int xTexStart;
        private final int yTexStart;
        private final int yDiffText;
        private boolean triggered;

        public PreviewButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, boolean triggered, IPressable onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
            this.triggered = triggered;
            this.xTexStart = xTexStartIn;
            this.yTexStart = yTexStartIn;
            this.yDiffText = yDiffTextIn;
            this.resourceLocation = resourceLocationIn;
        }

        @Override
        public void renderButton(@NotNull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getTextureManager().bind(this.resourceLocation);
            RenderSystem.disableDepthTest();
            int i = this.yTexStart;
            int j = this.xTexStart;
            if (!this.active) {
                j += 24;
            } else if (this.isTriggered()) {
                j += 12;
                if (this.isHovered()) {
                    i += this.yDiffText;
                }
            } else {
                if (this.isHovered()) {
                    i += this.yDiffText;
                }
            }
            blit(matrix, this.x, this.y, (float) j, (float) i, this.width, this.height, 64, 64);
            RenderSystem.enableDepthTest();
        }

        @Override
        public void renderToolTip(@NotNull MatrixStack matrix, int mouseX, int mouseY) {
            StringTextComponent s;
            if (!isTriggered()) {
                s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.preview.enable"));
            }
            else {
                s = new StringTextComponent(I18n.get("gui.resourcefulbees.apiary.button.preview.disable"));
            }
            UnvalidatedCreativeApiaryScreen.this.renderTooltip(matrix, s, mouseX, mouseY);
        }

        public void setTrigger(boolean triggered) {
            menu.getApiaryTileEntity().setPreviewed(triggered);
            this.triggered = triggered;
        }

        public boolean isTriggered() {
            return this.triggered;
        }
    }
}
