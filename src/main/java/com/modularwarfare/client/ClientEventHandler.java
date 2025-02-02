package com.modularwarfare.client;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.model.InstantBulletRenderer;
import com.modularwarfare.common.init.ModSounds;
import com.modularwarfare.common.network.PacketOpenGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientEventHandler {

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent event) {
        InstantBulletRenderer.RenderAllTrails(event.getPartialTicks());
    }

    @SubscribeEvent
    public void onGuiLaunch(GuiOpenEvent event) {
        if (ModConfig.INSTANCE.enableModifiedInventory) {
            if (event.getGui() != null && event.getGui().getClass() == GuiInventory.class) {
                final EntityPlayer player = Minecraft.getMinecraft().player;
                if (!player.isCreative()) {
                    event.setCanceled(true);
                    ModularWarfare.NETWORK.sendToServer(new PacketOpenGui(0));
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (ModConfig.INSTANCE.enableWalkSounds && !Loader.isModLoaded("dsurround")) {
            float soundVolume = ModConfig.INSTANCE.walkSoundsVolume;
            Minecraft mc = Minecraft.getMinecraft();
            final ResourceLocation currentSound = event.getSound().getSoundLocation();

            if (currentSound.toString().equals("minecraft:entity.generic.explode")) {
                event.setResultSound(null);
            }
            if (currentSound.toString().equals("minecraft:block.grass.step")) {
                if (mc.player.isSprinting()) {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_GRASS_SPRINT, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                } else {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_GRASS_WALK, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                }
            } else if (currentSound.toString().equals("minecraft:block.stone.step")) {
                if (mc.player.isSprinting()) {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_STONE_SPRINT, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                } else {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_STONE_WALK, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                }
            } else if (currentSound.toString().equals("minecraft:block.gravel.step")) {
                if (mc.player.isSprinting()) {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_GRAVEL_SPRINT, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                } else {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_GRAVEL_WALK, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                }
            } else if (currentSound.toString().equals("minecraft:block.metal.step")) {
                if (mc.player.isSprinting()) {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_METAL_SPRINT, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                } else {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_METAL_WALK, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                }
            } else if (currentSound.toString().equals("minecraft:block.wood.step")) {
                if (mc.player.isSprinting()) {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_WOOD_SPRINT, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                } else {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_WOOD_WALK, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                }
            } else if (currentSound.toString().equals("minecraft:block.sand.step")) {
                if (mc.player.isSprinting()) {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_SAND_SPRINT, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                } else {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_SAND_WALK, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                }
            } else if (currentSound.toString().equals("minecraft:block.snow.step")) {
                if (mc.player.isSprinting()) {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_SNOW_SPRINT, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                } else {
                    event.setResultSound(new PositionedSoundRecord(ModSounds.STEP_SNOW_WALK, SoundCategory.BLOCKS, soundVolume, 1f, event.getSound().getXPosF(), event.getSound().getYPosF(), event.getSound().getZPosF()));
                }
            }
        }
    }

}

