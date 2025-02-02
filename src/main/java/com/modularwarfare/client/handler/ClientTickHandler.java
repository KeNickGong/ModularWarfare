package com.modularwarfare.client.handler;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.anim.AnimStateMachine;
import com.modularwarfare.client.anim.StateEntry;
import com.modularwarfare.client.model.InstantBulletRenderer;
import com.modularwarfare.client.model.ModelGun;
import com.modularwarfare.client.model.renders.RenderParameters;
import com.modularwarfare.common.grenades.ItemGrenade;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.ItemSpray;
import com.modularwarfare.utility.MWSound;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.modularwarfare.client.model.renders.RenderParameters.*;

public class ClientTickHandler extends ForgeEvent {

    public static ConcurrentHashMap<UUID, Integer> playerShootCooldown = new ConcurrentHashMap<UUID, Integer>();
    public static ConcurrentHashMap<UUID, Integer> playerReloadCooldown = new ConcurrentHashMap<UUID, Integer>();
    private static Item oldItem;
    private static Field sprintToggleTimer = null;
    int i = 0;

    public ClientTickHandler() {
        super();
        try {
            sprintToggleTimer = ReflectionHelper.findField(EntityPlayerSP.class, "sprintToggleTimer", "field_71156_d");
        } catch (Exception e) {
            ModularWarfare.LOGGER.error("Unable to find field 'sprintToggleTimer'");
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                onClientTickStart(Minecraft.getMinecraft());
                ModularWarfare.NETWORK.handleClientPackets();

                // Player shoot cooldown
                for (UUID uuid : playerShootCooldown.keySet()) {
                    i += 1;
                    int value = playerShootCooldown.get(uuid) - 1;
                    if (value <= 0) {
                        playerShootCooldown.remove(uuid);
                    } else {
                        playerShootCooldown.replace(uuid, value);
                    }
                }

                // Player reload cooldown
                for (UUID uuid : playerReloadCooldown.keySet()) {
                    i += 1;
                    int value = playerReloadCooldown.get(uuid) - 1;
                    if (value <= 0) {
                        playerReloadCooldown.remove(uuid);
                    } else {
                        playerReloadCooldown.replace(uuid, value);
                    }
                }
                break;
            case END:
                onClientTickEnd(Minecraft.getMinecraft());
                ModularWarfare.PLAYERHANDLER.clientTick();

        }
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        switch (event.phase) {
            case START: {
                float renderTick = event.renderTickTime;
                renderTick *= 60d / (double) Minecraft.getDebugFPS();
                StateEntry.smoothing = renderTick;
                onRenderTickStart(Minecraft.getMinecraft(), renderTick);
                break;
            }
        }
    }

    public void onRenderTickStart(Minecraft minecraft, float renderTick) {
        if (minecraft.player == null || minecraft.world == null)
            return;

        EntityPlayerSP player = minecraft.player;

        if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemGun) {
            ModelGun model = (ModelGun) ((ItemGun) player.getHeldItemMainhand().getItem()).type.model;
            if (!RenderParameters.lastModel.equalsIgnoreCase(model.getClass().getName())) {
                RenderParameters.resetRenderMods();
                RenderParameters.lastModel = model.getClass().getName();
            }

            AnimStateMachine anim = ClientRenderHooks.getAnimMachine(player);

            float adsSpeed = (0.10f + model.config.extra.adsSpeed) * renderTick;
            boolean aimChargeMisc = !anim.reloading;
            float value = (Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(1) && aimChargeMisc && !ClientRenderHooks.getAnimMachine(player).attachmentMode) ? RenderParameters.adsSwitch + adsSpeed : RenderParameters.adsSwitch - adsSpeed;
            RenderParameters.adsSwitch = Math.max(0, Math.min(1, value));
            ;

            float sprintSpeed = 0.15f * renderTick;
            float sprintValue = (player.isSprinting() && !ClientRenderHooks.getAnimMachine(player).attachmentMode) ? RenderParameters.sprintSwitch + sprintSpeed : RenderParameters.sprintSwitch - sprintSpeed;
            RenderParameters.sprintSwitch = Math.max(0, Math.min(1, sprintValue));
            ;

            float attachmentSpeed = 0.15f * renderTick;
            float attachmentValue = ClientRenderHooks.getAnimMachine(player).attachmentMode ? RenderParameters.attachmentSwitch + attachmentSpeed : RenderParameters.attachmentSwitch - attachmentSpeed;
            RenderParameters.attachmentSwitch = Math.max(0, Math.min(1, attachmentValue));
            ;

            float crouchSpeed = 0.15f * renderTick;
            float crouchValue = player.isSneaking() ? RenderParameters.crouchSwitch + crouchSpeed : RenderParameters.crouchSwitch - crouchSpeed;
            RenderParameters.crouchSwitch = Math.max(0, Math.min(1, crouchValue));
            ;

            float reloadSpeed = 0.15f * renderTick;
            float reloadValue = anim.reloading ? RenderParameters.reloadSwitch - reloadSpeed : RenderParameters.reloadSwitch + reloadSpeed;
            RenderParameters.reloadSwitch = Math.max(0, Math.min(1, reloadValue));
            ;

            float triggerPullSpeed = 0.03f * renderTick;
            float triggerPullValue = Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(0) && !ClientRenderHooks.getAnimMachine(player).attachmentMode ? RenderParameters.triggerPullSwitch + triggerPullSpeed : RenderParameters.triggerPullSwitch - triggerPullSpeed;
            RenderParameters.triggerPullSwitch = Math.max(0, Math.min(model.triggerDistance, triggerPullValue));

            float modeSwitchSpeed = 0.03f * renderTick;
            float modeSwitchValue = Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(0) ? RenderParameters.triggerPullSwitch + triggerPullSpeed : RenderParameters.triggerPullSwitch - triggerPullSpeed;
            RenderParameters.triggerPullSwitch = Math.max(0, Math.min(model.triggerDistance, triggerPullValue));

            //Guns animations
            float maxHorizontal = 1.5f;
            float maxVertical = 0.75f;
            float swaySpeed = (player.isSprinting()) ? 0.005f : (0.006f * renderTick);

            RenderParameters.swayHorizontal = 0f;
            RenderParameters.swayVertical = 0f;
            RenderParameters.swayHorizontalEP = 0f;
            RenderParameters.swayVerticalEP = 0f;
            //if(RenderParameters.swayHorizontalEP == null || Float.isNaN(RenderParameters.swayHorizontalEP)) RenderParameters.swayHorizontalEP = NumberHelper.generateInRange(maxHorizontal);
            //if(RenderParameters.swayVerticalEP == null || Float.isNaN(RenderParameters.swayVerticalEP)) RenderParameters.swayVerticalEP = NumberHelper.generateInRange(maxVertical);
            //RenderParameters.swayHorizontal = !Float.isNaN(RenderParameters.swayHorizontal) ? NumberHelper.isInRange(maxHorizontal, RenderParameters.swayHorizontal) ? NumberHelper.addTowards(RenderParameters.swayHorizontalEP, RenderParameters.swayHorizontal, swaySpeed) : 0 : 0;
            //RenderParameters.swayVertical = !Float.isNaN(RenderParameters.swayVertical) ? NumberHelper.isInRange(maxVertical, RenderParameters.swayVertical) ? NumberHelper.addTowards(RenderParameters.swayVerticalEP, RenderParameters.swayVertical, swaySpeed/2) : 0 : 0;
            //RenderParameters.swayHorizontalEP = NumberHelper.isTargetMet(RenderParameters.swayHorizontalEP, RenderParameters.swayHorizontal) ? NumberHelper.generateInRange(maxHorizontal) : RenderParameters.swayHorizontalEP;
            //RenderParameters.swayVerticalEP = NumberHelper.isTargetMet(RenderParameters.swayVerticalEP, RenderParameters.swayVertical) ? NumberHelper.generateInRange(maxVertical) : RenderParameters.swayVerticalEP;

            for (AnimStateMachine stateMachine : ClientRenderHooks.weaponAnimations.values()) {
                stateMachine.onRenderTickUpdate();
            }
        } else {
            RenderParameters.resetRenderMods();
        }
    }

    public void onClientTickStart(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.world == null)
            return;

        GUN_ROT_X_LAST = GUN_ROT_X;
        GUN_ROT_Y_LAST = GUN_ROT_Y;
        GUN_ROT_Z_LAST = GUN_ROT_Z;

        Minecraft mc = FMLClientHandler.instance().getClient();

        if (mc.getRenderViewEntity() != null) {
            if (mc.getRenderViewEntity().getRotationYawHead() > mc.getRenderViewEntity().prevRotationYaw) {
                GUN_ROT_X += (mc.getRenderViewEntity().getRotationYawHead() - mc.getRenderViewEntity().prevRotationYaw) / 1.5;
            } else if (mc.getRenderViewEntity().getRotationYawHead() < mc.getRenderViewEntity().prevRotationYaw) {
                GUN_ROT_X -= (mc.getRenderViewEntity().prevRotationYaw - mc.getRenderViewEntity().getRotationYawHead()) / 1.5;
            }
            if (mc.getRenderViewEntity().rotationPitch > prevPitch) {
                GUN_ROT_Y += (mc.getRenderViewEntity().rotationPitch - prevPitch) / 1.5;
            } else if (mc.getRenderViewEntity().rotationPitch < prevPitch) {
                GUN_ROT_Y -= (prevPitch - mc.getRenderViewEntity().rotationPitch) / 1.5;
            }
            prevPitch = mc.getRenderViewEntity().rotationPitch;
        }

        GUN_ROT_X *= .2F;
        GUN_ROT_Y *= .2F;
        GUN_ROT_Z *= .2F;

        if (GUN_ROT_X > 20) {
            GUN_ROT_X = 20;
        } else if (GUN_ROT_X < -20) {
            GUN_ROT_X = -20;
        }

        if (GUN_ROT_Y > 20) {
            GUN_ROT_Y = 20;
        } else if (GUN_ROT_Y < -20) {
            GUN_ROT_Y = -20;
        }

        if (ClientProxy.gunUI.bulletSnapFade > 0) {
            ClientProxy.gunUI.bulletSnapFade -= 0.01F;
        }

        this.processGunChange();
        ItemGun.fireButtonHeld = Mouse.isButtonDown(0);
    }

    public void onClientTickEnd(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.world == null)
            return;

        EntityPlayerSP player = minecraft.player;

        if (playerRecoilPitch > 0)
            playerRecoilPitch *= 0.8F;

        if (playerRecoilYaw > 0)
            playerRecoilYaw *= 0.8F;

        player.rotationPitch -= playerRecoilPitch;
        player.rotationYaw -= playerRecoilYaw;
        antiRecoilPitch += playerRecoilPitch;
        antiRecoilYaw += playerRecoilYaw;

        player.rotationPitch += antiRecoilPitch * 0.2F;
        player.rotationYaw += antiRecoilYaw * 0.2F;
        antiRecoilPitch *= 0.8F;
        antiRecoilYaw *= 0.8F;

        for (AnimStateMachine stateMachine : ClientRenderHooks.weaponAnimations.values()) {
            stateMachine.onTickUpdate();
        }

        AnimStateMachine anim = ClientRenderHooks.getAnimMachine(player);
        if (anim.reloading) {
            try {
                sprintToggleTimer.setInt(player, 7);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        InstantBulletRenderer.UpdateAllTrails();
    }


    public void processGunChange() {
        final EntityPlayer player = Minecraft.getMinecraft().player;
        if (player.getHeldItemMainhand().getItem() != this.oldItem) {
            if (player.getHeldItemMainhand().getItem() instanceof ItemGun) {
                ModularWarfare.PROXY.playSound(new MWSound(player.getPosition(), "human.equip.gun", 1f, 1f));
                RenderParameters.switchDelay = 20;
            } else if (player.getHeldItemMainhand().getItem() instanceof ItemSpray) {
                ModularWarfare.PROXY.playSound(new MWSound(player.getPosition(), "shake", 1f, 1f));
                RenderParameters.switchDelay = 20;
            } else if (player.getHeldItemMainhand().getItem() instanceof ItemGrenade) {
                ModularWarfare.PROXY.playSound(new MWSound(player.getPosition(), "human.equip.extra", 1f, 1f));
                RenderParameters.switchDelay = 20;
            }
        }
        if (this.oldItem != player.getHeldItemMainhand().getItem()) {
            ClientRenderHooks.getAnimMachine(player).attachmentMode = false;
            this.oldItem = player.getHeldItemMainhand().getItem();
        }
    }
}
