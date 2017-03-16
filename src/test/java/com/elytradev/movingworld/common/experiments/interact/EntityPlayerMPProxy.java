package com.elytradev.movingworld.common.experiments.interact;

import com.elytradev.movingworld.common.experiments.entity.EntityMobileRegion;
import com.elytradev.movingworld.common.experiments.network.messages.server.MessageOpenGui;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.stats.StatBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.HashMap;

public class EntityPlayerMPProxy extends EntityPlayerMP {

    public static HashMap<GameProfile, EntityPlayerMPProxy> PROXIES = new HashMap<>();

    private EntityPlayerMP parent;
    private EntityMobileRegion region;

    public EntityPlayerMPProxy(EntityPlayerMP playerMP, EntityMobileRegion region) {
        super(playerMP.getServer(), (WorldServer) region.getParentWorld(),
                playerMP.getGameProfile(), MWPlayerInteractionManager.MANAGERS.get(playerMP));

        this.connection = playerMP.connection;
        this.parent = playerMP;
        this.region = region;

        Vec3d prevPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.prevPosX, parent.prevPosY, parent.prevPosZ));
        Vec3d curPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.posX, parent.posY, parent.posZ));

        this.inventory = parent.inventory;
        this.inventoryContainer = parent.inventoryContainer;
        this.openContainer = parent.openContainer;

        this.prevPosX = prevPos.xCoord;
        this.prevPosY = prevPos.yCoord;
        this.prevPosZ = prevPos.zCoord;

        this.posX = curPos.xCoord;
        this.posY = curPos.yCoord;
        this.posZ = curPos.zCoord;

        this.motionX = parent.motionX;
        this.motionY = parent.motionY;
        this.motionZ = parent.motionZ;
    }

    @Override
    public void onUpdate() {
        Vec3d prevPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.prevPosX, parent.prevPosY, parent.prevPosZ));
        Vec3d curPos = region.region.convertRealWorldPosToRegion(new Vec3d(parent.posX, parent.posY, parent.posZ));

        this.inventory = parent.inventory;
        this.inventoryContainer = parent.inventoryContainer;

        this.prevPosX = prevPos.xCoord;
        this.prevPosY = prevPos.yCoord;
        this.prevPosZ = prevPos.zCoord;

        this.posX = curPos.xCoord;
        this.posY = curPos.yCoord;
        this.posZ = curPos.zCoord;

        this.motionX = parent.motionX;
        this.motionY = parent.motionY;
        this.motionZ = parent.motionZ;

        super.onUpdate();
    }

    @Override
    public void displayGui(IInteractionObject guiOwner) {
        parent.displayGui(guiOwner);
        ContainerChecks.checkContainer(parent);
    }

    @Override
    public void displayGUIChest(IInventory chestInventory) {
        parent.displayGUIChest(chestInventory);
        ContainerChecks.checkContainer(parent);
    }

    @Override
    public void closeScreen() {
        parent.closeScreen();
    }

    @Override
    public void addStat(StatBase stat, int amount) {
        parent.addStat(stat, amount);
    }

    @Override
    public void setPositionAndUpdate(double x, double y, double z) {
        super.setPositionAndUpdate(x, y, z);
    }

    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        ModContainer modContainer = FMLCommonHandler.instance().findContainerFor(mod);
        Container remoteGuiContainer = NetworkRegistry.INSTANCE.getRemoteGuiContainer(modContainer, this, modGuiId, world, x, y, z);
        if (remoteGuiContainer != null) {
            this.getNextWindowId();
            this.closeContainer();
            int windowId = this.currentWindowId;
            new MessageOpenGui(new BlockPos(x, y, z), modContainer.getModId(), modGuiId, windowId, region.getParentWorld().provider.getDimension()).sendTo(this);
            this.openContainer = remoteGuiContainer;
            this.openContainer.windowId = windowId;
            this.openContainer.addListener(this);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(this, this.openContainer));
        }

        ContainerChecks.checkContainer(parent);
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        parent.move(type, x, y, z);
    }

    public EntityPlayerMP getParent() {
        return parent;
    }

    public EntityMobileRegion getRegion() {
        return region;
    }

    public void setRegion(EntityMobileRegion region) {
        this.region = region;
    }
}
