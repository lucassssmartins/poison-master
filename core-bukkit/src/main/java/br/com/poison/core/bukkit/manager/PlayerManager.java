package br.com.poison.core.bukkit.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.resources.skin.Skin;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PlayerManager {

    public static void handleTeleport(Location location) {
        Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());

        Iterator<Player> iterator = players.iterator();

        // Teleport Scheduler
        new BukkitRunnable() {
            @Override
            public void run() {
                if (iterator.hasNext()) {
                    Player player = iterator.next();

                    if (player != null) {
                        iterator.remove();

                        player.teleport(location);
                    }
                } else
                    cancel();
            }
        }.runTaskTimer(BukkitCore.getPlugin(BukkitCore.class), 0, 3);
    }

    public static void sendBar(Player player, String text) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(text));
        packet.getBytes().write(0, (byte) 2);

        try {
            BukkitCore.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openBook(ItemStack book, Player p) {
        int slot = p.getInventory().getHeldItemSlot();
        org.bukkit.inventory.ItemStack old = p.getInventory().getItem(slot);

        p.getInventory().setItem(slot, book);

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte) 0);
        buf.writerIndex(1);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

        p.getInventory().setItem(slot, old);
    }

    private static void respawn(EntityPlayer ep) {

        double x = ep.locX;
        double y = ep.locY;
        double z = ep.locZ;

        WorldServer worldserver = (WorldServer) ep.getWorld();
        DedicatedPlayerList playerList = ((CraftServer) Bukkit.getServer()).getHandle();

        PacketPlayOutEntityDestroy destroyEntity = new PacketPlayOutEntityDestroy(ep.getId());
        PacketPlayOutPlayerInfo removePlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep);
        PacketPlayOutPlayerInfo addPlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep);
        PacketPlayOutNamedEntitySpawn spawnEntity = new PacketPlayOutNamedEntitySpawn(ep);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(ep.getId(), ep.getDataWatcher(), true);
        PacketPlayOutHeldItemSlot heldItemSlot = new PacketPlayOutHeldItemSlot(ep.inventory.itemInHandIndex);
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(ep, (byte) 28);
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(worldserver.worldProvider.getDimension(), worldserver.getDifficulty(), worldserver.getWorldData().getType(), ep.playerInteractManager.getGameMode());
        PacketPlayOutPosition position = new PacketPlayOutPosition(ep.locX, ep.locY, ep.locZ, ep.yaw, ep.pitch, Collections.emptySet());
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(ep, (byte) MathHelper.d(ep.getHeadRotation() * 256.0F / 360.0F));

        Bukkit.getScheduler().runTask(BukkitCore.getPlugin(BukkitCore.class), () -> {
            for (int i = 0; i < playerList.players.size(); i++) {
                EntityPlayer ep1 = playerList.players.get(i);

                if (ep1.getBukkitEntity().canSee(ep.getBukkitEntity())) {
                    PlayerConnection playerConnection = ep1.playerConnection;
                    playerConnection.sendPacket(removePlayer);
                    playerConnection.sendPacket(addPlayer);

                    if (ep1.getId() != ep.getId()) {
                        playerConnection.sendPacket(destroyEntity);
                        playerConnection.sendPacket(spawnEntity);
                    }

                    playerConnection.sendPacket(headRotation);
                }
            }

            PlayerConnection con = ep.playerConnection;
            con.sendPacket(metadata);
            con.sendPacket(respawn);
            con.sendPacket(position);
            con.sendPacket(heldItemSlot);
            con.sendPacket(status);
            ep.updateAbilities();
            ep.triggerHealthUpdate();
            ep.updateInventory(ep.activeContainer);
            ep.updateInventory(ep.defaultContainer);
        });

        CraftPlayer player = ep.getBukkitEntity();

        player.getInventory().setArmorContents(player.getInventory().getArmorContents());
        player.setExp(player.getExp());
        player.setHealth(player.getHealth());
        player.setSneaking(player.isSneaking());

        if (player.getPassenger() != null)
            player.setPassenger(player.getPassenger());

        if (player.isInsideVehicle())
            player.getVehicle().setPassenger(player);

        ep.locX = x;
        ep.locY = y;
        ep.locZ = z;
        ep.lastX = x;
        ep.lastY = y;
        ep.lastZ = z;

        ep.setPosition(x, y, z);
    }

    public static void changePlayerSkin(Player player, Skin skin) {
        changePlayerSkin(player, skin, true);
    }

    public static void changePlayerSkin(Player player, Skin skin, boolean respawn) {
        WrappedGameProfile playerProfile = WrappedGameProfile.fromPlayer(player);

        WrappedGameProfile skinProfile = new WrappedGameProfile(skin.getId(), skin.getName());

        skinProfile.getProperties().clear();

        skinProfile.getProperties().put("textures", new WrappedSignedProperty("textures",
                skin.getTexture().getValue(),
                skin.getTexture().getSignature()));

        WrappedSignedProperty property = skinProfile.getProperties().get("textures").stream().findFirst().orElse(null);

        playerProfile.getProperties().clear();
        playerProfile.getProperties().put("textures", property);

        if (respawn)
            respawn(((CraftPlayer) player).getHandle());
    }

    public static void removePlayerSkin(Player player) {
        removePlayerSkin(player, true);
    }

    public static void removePlayerSkin(Player player, boolean respawn) {
        WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);
        profile.getProperties().clear();

        if (respawn) {
            respawn(((CraftPlayer) player).getHandle());
        }
    }
}
