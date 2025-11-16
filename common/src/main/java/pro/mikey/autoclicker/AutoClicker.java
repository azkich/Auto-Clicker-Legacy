package pro.mikey.autoclicker;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class AutoClicker {
    public static final String MOD_ID = "autoclicker";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final KeyMapping openConfig =
            new KeyMapping("keybinding.open-gui", GLFW.GLFW_KEY_O, "key.categories." + MOD_ID);
    public static final KeyMapping toggleHolding =
            new KeyMapping("keybinding.toggle-hold", GLFW.GLFW_KEY_I, "key.categories." + MOD_ID);

    private static final Supplier<Pair<Path, Path>> CONFIG_PATHS = Suppliers.memoize(() -> {
        Path configDir = Paths.get(Minecraft.getInstance().gameDirectory.getPath() + "/config");
        Path configFile = Paths.get(configDir + "/auto-clicker-fabric.json");
        return Pair.of(configDir, configFile);
    });

    public static Holding.AttackHolding leftHolding;
    public static Holding rightHolding;
    public static Holding jumpHolding;
    public static Config.HudConfig hudConfig;
    private static AutoClicker INSTANCE;
    private boolean isActive = false;
    private Config config = new Config(
            new Config.LeftMouseConfig(false, false, 0),
            new Config.RightMouseConfig(false, false, 0),
            new Config.JumpConfig(false, false, 0),
            new Config.HudConfig(true, "top-left", false, false, false)
    );

    public AutoClicker() {
        INSTANCE = this;
    }

    public static AutoClicker getInstance() {
        return INSTANCE;
    }

    public void onInitialize() {
        LOGGER.info("Auto Clicker Initialised");
    }

    public void clientReady(Minecraft client) {
        var configPaths = CONFIG_PATHS.get();
        if (!Files.exists(configPaths.value())) {
            try {
                Files.createDirectories(configPaths.key());
                Files.createFile(configPaths.value());
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.saveConfig();
        } else {
            try {
                FileReader json = new FileReader(configPaths.value().toFile());
                Config config = new Gson().fromJson(json, Config.class);
                json.close();
                if (config != null && config.getHudConfig() != null) {
                    this.config = config;
                }
            } catch (Exception e){
                e.printStackTrace();
                this.saveConfig();
            }
        }

        leftHolding = new Holding.AttackHolding(client.options.keyAttack, this.config.getLeftClick());
        rightHolding = new Holding(client.options.keyUse, this.config.getRightClick());
        jumpHolding = new Holding(client.options.keyJump, this.config.getJump());
        hudConfig = this.config.getHudConfig();
    }

    public void saveConfig() {
        var configPaths = CONFIG_PATHS.get();
        try {
            FileWriter writer = new FileWriter(configPaths.value().toFile());

            new Gson().toJson(this.config, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renderGameOverlayEvent(GuiGraphics context, DeltaTracker delta) {

        if ((!leftHolding.isActive() && !rightHolding.isActive() && !jumpHolding.isActive()) || !this.isActive || !config.getHudConfig().isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        if (leftHolding.isActive()) {
            Component text = Language.HUD_HOLDING.getText(I18n.get(leftHolding.getKey().getName()));
            int y = getHudY() + (15 * 0);
            int x = getHudX(text);
            context.drawString(client.font, text.getVisualOrderText(), x, y, 0xFFffffff);
        }

        if (rightHolding.isActive()) {
            Component text = Language.HUD_HOLDING.getText(I18n.get(rightHolding.getKey().getName()));
            int y = getHudY() + (15 * 1);
            int x = getHudX(text);
            context.drawString(client.font, text.getVisualOrderText(), x, y, 0xFFffffff);
        }

        if (jumpHolding.isActive()) {
            Component text = Language.HUD_HOLDING.getText(I18n.get(jumpHolding.getKey().getName()));
            int y = getHudY() + (15 * 2);
            int x = getHudX(text);
            context.drawString(client.font, text.getVisualOrderText(), x, y, 0xFFffffff);
        }
    }

    public int getHudX(Component text){
        Minecraft client = Minecraft.getInstance();

        String location = this.config.getHudConfig().getLocation();
        return switch (location) {
            case "top-left", "bottom-left" -> 10;
            case "top-right", "bottom-right" -> (Minecraft.getInstance().getWindow().getGuiScaledWidth()) - 10 - client.font.width(text);
            default -> 10;
        };
    }
    public int getHudY(){
        String location = this.config.getHudConfig().getLocation();
        return switch (location) {
            case "top-left", "top-right" -> 10;
            case "bottom-left", "bottom-right" -> (Minecraft.getInstance().getWindow().getGuiScaledHeight()) - 50;
            default -> 10;
        };
    }

    public void clientTickEvent(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            // Check for disconnect
            if (hudConfig.isDisableAfterDisconnect() && this.isActive) {
                this.isActive = false;
                if(leftHolding.isActive()) leftHolding.getKey().setDown(false);
                if(rightHolding.isActive()) rightHolding.getKey().setDown(false);
                if(jumpHolding.isActive()) jumpHolding.getKey().setDown(false);
            }
            return;
        }
        
        // Check for death
        if (!mc.player.isAlive() && hudConfig.isDisableAfterDeath() && this.isActive) {
            this.isActive = false;
            if(leftHolding.isActive()) leftHolding.getKey().setDown(false);
            if(rightHolding.isActive()) rightHolding.getKey().setDown(false);
            if(jumpHolding.isActive()) jumpHolding.getKey().setDown(false);
        }
        
        // Check for reload screen (Downloading terrain or similar)
        if (hudConfig.isDisableAfterReloadScreen() && this.isActive && mc.screen != null) {
            String screenName = mc.screen.getClass().getSimpleName();
            if (screenName.contains("Downloading") || screenName.contains("Receiving") || screenName.contains("Loading")) {
                this.isActive = false;
                if(leftHolding.isActive()) leftHolding.getKey().setDown(false);
                if(rightHolding.isActive()) rightHolding.getKey().setDown(false);
                if(jumpHolding.isActive()) jumpHolding.getKey().setDown(false);
            }
        }

        if (this.isActive) {
            if (leftHolding.isActive()) {
                this.handleActiveHolding(mc, leftHolding);
            }

            if (rightHolding.isActive()) {
                this.handleActiveHolding(mc, rightHolding);
            }

            if (jumpHolding.isActive()) {
                this.handleActiveHolding(mc, jumpHolding);
            }
        }

        this.keyInputEvent(mc);
    }

    private void handleActiveHolding(Minecraft mc, Holding key) {
        assert mc.player != null;
        if (!key.isActive()) {
            return;
        }

        if (key.isSpamming()) {
            // How to handle the click if it's done by spamming
            if (key.getSpeed() > 0) {
                // Decrease timeout first
                key.decreaseTimeout();
                
                // When timeout reaches 0, perform click and reset
                if (key.getTimeout() <= 0) {
                    // For right click, check if we should allow the click
                    boolean shouldClick = true;
                    if (key.getKey() == rightHolding.getKey()) {
                        HitResult rayTrace = mc.hitResult;
                        ItemStack itemInHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
                        boolean isBlockItem = itemInHand.getItem() instanceof BlockItem;
                        
                        // Don't allow clicking if holding block and looking at air
                        if (isBlockItem && (rayTrace == null || rayTrace.getType() == HitResult.Type.MISS)) {
                            shouldClick = false;
                        }
                    }
                    
                    if (shouldClick) {
                        // Press the button twice by toggling 1 and 0
                        key.getKey().setDown(true);
                        // Perform action based on key type
                        if (key.getKey() == leftHolding.getKey()) {
                            this.performLeftClickAction(mc);
                        } else if (key.getKey() == rightHolding.getKey()) {
                            // For right click, only perform action if looking at block/entity
                            // Don't allow placing blocks in air
                            this.performRightClickAction(mc);
                        }
                        key.getKey().setDown(false);
                    }
                    key.resetTimeout();
                }
            } else {
                // Handle the click if it's done normally (speed = 0, no delay)
                // For right click, check if we should allow the click
                boolean shouldClick = true;
                if (key.getKey() == rightHolding.getKey()) {
                    HitResult rayTrace = mc.hitResult;
                    ItemStack itemInHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
                    boolean isBlockItem = itemInHand.getItem() instanceof BlockItem;
                    
                    // Don't allow clicking if holding block and looking at air
                    if (isBlockItem && (rayTrace == null || rayTrace.getType() == HitResult.Type.MISS)) {
                        shouldClick = false;
                    }
                }
                
                if (shouldClick) {
                    key.getKey().setDown(!key.getKey().isDown());
                    // Perform action based on key type when pressed
                    if (key.getKey().isDown()) {
                        if (key.getKey() == leftHolding.getKey()) {
                            this.performLeftClickAction(mc);
                        } else if (key.getKey() == rightHolding.getKey()) {
                            // For right click, only perform action if looking at block/entity
                            // Don't allow placing blocks in air
                            this.performRightClickAction(mc);
                        }
                    }
                }
            }

            return;
        }

        // Normal holding behaviour
        // For right click, only hold if looking at block/entity or holding non-block item
        if (key.getKey() == rightHolding.getKey()) {
            HitResult rayTrace = mc.hitResult;
            ItemStack itemInHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
            boolean isBlockItem = itemInHand.getItem() instanceof BlockItem;
            
            // Only hold right click if:
            // 1. Looking at block/entity, OR
            // 2. Holding non-block item (food, potions, etc.)
            if (rayTrace instanceof EntityHitResult || rayTrace instanceof BlockHitResult || !isBlockItem) {
                key.getKey().setDown(true);
            }
            // If holding block and looking at air, don't hold - prevents placing blocks in air
        } else {
            // For left click and jump, always hold
            key.getKey().setDown(true);
        }
    }

    private void performLeftClickAction(Minecraft mc) {
        // Attack by air - always swing hand
        if (mc.player != null) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
        
        // Attack entity if present, but always swing regardless
        HitResult rayTrace = mc.hitResult;
        if (rayTrace instanceof EntityHitResult && mc.gameMode != null) {
            mc.gameMode.attack(mc.player, ((EntityHitResult) rayTrace).getEntity());
        }
    }

    private void performRightClickAction(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        HitResult rayTrace = mc.hitResult;
        ItemStack itemInHand = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean isBlockItem = itemInHand.getItem() instanceof BlockItem;
        
        // Only perform action if we're looking at something (block or entity)
        // For blocks: only allow placement on existing blocks, not in air
        // For consumables (food, potions): allow use in air
        if (rayTrace instanceof EntityHitResult entityHit) {
            // Swing hand and interact with entity
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.gameMode.interact(mc.player, entityHit.getEntity(), InteractionHand.MAIN_HAND);
        } else if (rayTrace instanceof BlockHitResult blockHit) {
            // Swing hand and interact with block (placing blocks on blocks is allowed)
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHit);
        } else if (rayTrace == null || rayTrace.getType() == HitResult.Type.MISS) {
            // Looking at air
            if (!isBlockItem) {
                // Allow using consumables (food, potions) in air
                mc.player.swing(InteractionHand.MAIN_HAND);
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            }
            // If holding a block, do nothing - don't allow placing blocks in air
        }
    }


    private void keyInputEvent(Minecraft mc) {
        assert mc.player != null;
        while (toggleHolding.consumeClick()) {
            this.isActive = !this.isActive;
            mc.player.displayClientMessage(
                    (this.isActive ? Language.MSG_HOLDING_KEYS : Language.MSG_RELEASED_KEYS)
                    .getText()
                            .withStyle(this.isActive ? ChatFormatting.GREEN : ChatFormatting.RED),
                    true
                    );

            if (!this.isActive) {
                if(leftHolding.isActive()) leftHolding.getKey().setDown(false);
                if(rightHolding.isActive()) rightHolding.getKey().setDown(false);
                if(jumpHolding.isActive()) jumpHolding.getKey().setDown(false);
            }
        }

        while (openConfig.consumeClick()) {
            mc.setScreen(getConfigScreen());
        }
    }

    public OptionsScreen getConfigScreen(){
        return new OptionsScreen();
    }
}

