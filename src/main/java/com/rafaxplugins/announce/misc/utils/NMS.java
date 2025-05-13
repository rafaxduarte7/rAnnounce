package com.rafaxplugins.announce.misc.utils;

import com.rafaxplugins.announce.misc.message.MessageUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("all")
public class NMS {

    public static void sendBreakAnimationPacket(Player player, Location location, int data) {

        Block block = location.getBlock();
        BlockPosition bp = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(block.getTypeId(), bp, data);
        sendPacket(player, packet);

    }

    @SuppressWarnings("deprecation")
    public static void setBlockFast(Block block, Material material, short data) {
        Chunk chunk = ((CraftChunk) block.getChunk()).getHandle();
        chunk.a(new BlockPosition(block.getX(), block.getY(), block.getZ()), net.minecraft.server.v1_8_R3.Block.getById(material.getId()).fromLegacyData(data));
    }

    //
    public static WorldServer getWorld(org.bukkit.World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static EntityPlayer getPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static String serializeItemToNBT(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = new NBTTagCompound();
        compound = nmsItemStack.save(compound);

        return compound.toString();
    }

    public static void sendMusicText(String text, Player... players) {
        if (text != null) {
            text = MessageUtils.translateColorCodes(text);
        }

        IChatBaseComponent comp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + text + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(comp, (byte) 2);

        for (Player p : players) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void sendPacketNearby(Player from, Location location, Packet packet) {
        NMS.sendPacketsNearby(from, location, Arrays.asList(packet), 64);
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet> packets) {
        NMS.sendPacketsNearby(from, location, packets, 64);
    }

    public static void sendPacketsNearby(Player from, Location location, Collection<Packet> packets, double radius) {
        radius *= radius;
        double distance;
        final org.bukkit.World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld() || (from != null && !ply.canSee(from))) {
                continue;
            }

            try {
                distance = location.distanceSquared(ply.getLocation(PACKET_CACHE_LOCATION));
            } catch (IllegalArgumentException e) {
                continue;
            }

            if (ply.getWorld() != world || distance > radius) {
                continue;
            }
            for (Packet packet : packets) {
                sendPacket(ply, packet);
            }
        }
    }

    public static void sendPacketsNearby(Player from, Location location, Packet... packets) {
        NMS.sendPacketsNearby(from, location, Arrays.asList(packets), 64);
    }

    public static void sendPacket(Player player, Packet packet) {
        if (packet == null) {
            return;
        }
        ((EntityPlayer) ((CraftPlayer) player).getHandle()).playerConnection.sendPacket(packet);
    }

    //
    public static void clearEntitySelectors(EntityInsentient entity) {
        clearGoalSelector(entity);
        clearTargetSelector(entity);
    }

    public static void clearGoalSelector(EntityInsentient entity) {
        try {
            ((UnsafeList<?>) PATHFINDER_GOAL_1.get(entity.goalSelector)).clear();
            ((UnsafeList<?>) PATHFINDER_GOAL_2.get(entity.goalSelector)).clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void clearTargetSelector(EntityInsentient entity) {
        try {
            ((UnsafeList<?>) PATHFINDER_GOAL_1.get(entity.targetSelector)).clear();
            ((UnsafeList<?>) PATHFINDER_GOAL_2.get(entity.targetSelector)).clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Class<?> getNMSClassByName(String inName) {
        try {
            return Class.forName("net.minecraft.server." + getMinecraftRevision() + "." + inName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets the current minecraft revision
     *
     * @return The revision as string in the format "X.X_RX"
     */
    public static String getMinecraftRevision() {
        Class serverClass = Bukkit.getServer().getClass();
        String remaining = serverClass.getPackage().getName().replace("org.bukkit.craftbukkit.", "");
        return remaining.split("\\.")[0];
    }

    /**
     * Gets a declared field of the given class and caches it. If a field is not
     * cached it will attempt to get it from the given class.
     *
     * @param inSource The class which has the field
     * @param inField The field name
     *
     * @return The field
     */
    public static Field getOrRegisterField(Class<?> inSource, String inField) {
        Field field;
        try {
            String id = inSource.getName() + "_" + inField;
            if (s_cachedFields.containsKey(id)) {
                field = s_cachedFields.get(id);
            } else {
                field = inSource.getDeclaredField(inField);
                field.setAccessible(true);
                s_cachedFields.put(id, field);
            }

            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // We split up in two methods so we don't cause much overhead because otherwise we'd need to search for the nms class every time when calling the method
    // instead of just once when we need it
    public static Field getOrRegisterNMSField(String inNMSClass, String inField) {
        Field field;
        try {
            String id = inNMSClass + "_" + inField;
            if (s_cachedFields.containsKey(id)) {
                field = s_cachedFields.get(id);
            } else {
                field = getNMSClassByName(inNMSClass).getDeclaredField(inField);
                field.setAccessible(true);
                s_cachedFields.put(id, field);
            }

            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registra e spawna uma entidade custom no mundo e logo em seguida registra
     * de volta a entidade padrão na qual a entidade custom é baseada.
     *
     * @param <T>
     * @param customEntityClass Classe da entidade custom
     * @param entityClass Classe na qual a entidade custom é baseada
     * @param location Local onde a entidade deve ser spawnada
     *
     * @return Retorna uma instancia de customEntityClass já spawnada no mundo
     */
    public static <T extends Entity> T spawnCustomEntity(Class<T> customEntityClass, Class<? extends Entity> entityClass, Location location) {
        return spawnCustomEntity(customEntityClass, entityClass, location, null);
    }

    /**
     * Registra e spawna uma entidade custom no mundo e logo em seguida registra
     * de volta a entidade padrão na qual a entidade custom é baseada.
     *
     * @param <T>
     * @param customEntityClass Classe da entidade custom
     * @param entityClass Classe na qual a entidade custom é baseada
     * @param location Local onde a entidade deve ser spawnada
     * @param preSpawn
     *
     * @return Retorna uma instancia de customEntityClass já spawnada no mundo
     */
    public static <T extends Entity> T spawnCustomEntity(Class<T> customEntityClass, Class<? extends Entity> entityClass, Location location, Consumer<Entity> preSpawn) {
        try {
            WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
            Class entityTypes = getNMSClassByName("EntityTypes");
            Field nameMap = getOrRegisterField(entityTypes, "d");
            Field idMap = getOrRegisterField(entityTypes, "f");

            Integer id = (Integer) ((Map) idMap.get(null)).get(entityClass);
            String name = (String) ((Map) nameMap.get(null)).get(entityClass);

            registerEntityType(customEntityClass, name, id);
            T customEntity = customEntityClass.getConstructor(World.class).newInstance(worldServer);

            customEntity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
            customEntity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

            if (preSpawn != null) {
                preSpawn.accept(customEntity);
            }

            if (!worldServer.addEntity(customEntity, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
                System.out.println("FALHA AO ADICIONAR ENTIDADE AO MUNDO - " + customEntityClass.getName());
            }

            customEntity.getBukkitEntity();
            registerEntityType(entityClass, name, id);
            return customEntity;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registra e spawna uma entidade custom no mundo
     *
     * @param customEntityClass Classe da entidade custom
     * @param entityClass Classe na qual a entidade custom é baseada
     *
     */
    public static void registerCustomEntity(Class<? extends Entity> customEntityClass, Class<? extends Entity> entityClass) {
        try {
            Class entityTypes = getNMSClassByName("EntityTypes");
            Field nameMap = getOrRegisterField(entityTypes, "d");
            Field idMap = getOrRegisterField(entityTypes, "f");

            Integer id = (Integer) ((Map) idMap.get(null)).get(entityClass);
            String name = (String) ((Map) nameMap.get(null)).get(entityClass);

            registerEntityType(customEntityClass, name, id);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers custom entity class at the native minecraft entity enum.
     * Automatically clears internal maps first @see
     * ReflectionUtil#clearEntityType(String, int)
     *
     * @param inClass	class of the entity
     * @param inName	minecraft entity name
     * @param inID	minecraft entity id
     */
    public static void registerEntityType(Class<?> inClass, String inName, int inID) {
        try {
            clearEntityType(inName, inID);
            @SuppressWarnings("rawtypes")
            Class[] args = new Class[3];
            args[0] = Class.class;
            args[1] = String.class;
            args[2] = int.class;

            Method a = getNMSClassByName("EntityTypes").getDeclaredMethod("a", args);
            a.setAccessible(true);

            a.invoke(a, inClass, inName, inID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <E extends EntityInsentient> void registerEntityType(Class<E> entityClass, String findName, String customName) {
        try {
            Class entityTypes = NMS.getNMSClassByName("EntityTypes");
            Field classMap = NMS.getOrRegisterField(entityTypes, "c");
            Field idMap = NMS.getOrRegisterField(entityTypes, "f");
            Class innerEntityClass = (Class) ((Map) classMap.get(null)).get(findName);
            Integer id = (Integer) ((Map) idMap.get(null)).get(innerEntityClass);

            NMS.registerEntityType(entityClass, customName, id);
        } catch (Exception e) {
        }
    }

    /**
     * Clears the entity name and entity id from the EntityTypes internal c and
     * e map to allow registering of those names with different values. The
     * other maps are not touched and stay as they are.
     *
     * @param inName The internal name of the entity
     * @param inID The internal id of the entity
     */
    public static void clearEntityType(String inName, int inID) {
        try {
            Field cMap = getOrRegisterNMSField("EntityTypes", "c");
            Field eMap = getOrRegisterNMSField("EntityTypes", "e");
            ((Map) cMap.get(null)).remove(inName);
            ((Map) eMap.get(null)).remove(inID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Map<String, Field> s_cachedFields = new HashMap<>();

    public static <E extends EntityInsentient> void registerEntity(int id, String name, Class<E> entityClass) {
        try {
            ((Map<String, Class<E>>) ENTITY_NAME_TO_CLASS.get(null)).put(name, entityClass);
            ((Map<Class<E>, String>) ENTITY_CLASS_TO_NAME.get(null)).put(entityClass, name);
            ((Map<Class<E>, Integer>) ENTITY_CLASS_TO_ID.get(null)).put(entityClass, id);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    private static final Location PACKET_CACHE_LOCATION = new Location(null, 0, 0, 0);

    private static Field ENTITY_NAME_TO_CLASS;
    private static Field ENTITY_CLASS_TO_NAME;
    private static Field ENTITY_CLASS_TO_ID;

    public static Field PATHFINDER_GOAL_1, PATHFINDER_GOAL_2;

    static {
        try {
            PATHFINDER_GOAL_1 = PathfinderGoalSelector.class.getDeclaredField("b");
            PATHFINDER_GOAL_1.setAccessible(true);

            PATHFINDER_GOAL_2 = PathfinderGoalSelector.class.getDeclaredField("c");
            PATHFINDER_GOAL_2.setAccessible(true);

            ENTITY_NAME_TO_CLASS = EntityTypes.class.getDeclaredField("c");
            ENTITY_NAME_TO_CLASS.setAccessible(true);

            ENTITY_CLASS_TO_NAME = EntityTypes.class.getDeclaredField("d");
            ENTITY_CLASS_TO_NAME.setAccessible(true);

            ENTITY_CLASS_TO_ID = EntityTypes.class.getDeclaredField("f");
            ENTITY_CLASS_TO_ID.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void sendActionbarRaw(Object sendeeObject, String raw)
    {

    }

    public static void sendActionbarMessage(Object sendeeObject, String message)
    {
        message = messageToRaw(message);

        sendActionbarRaw(sendeeObject, message);
    }

    public static String messageToRaw(String message)
    {
        message = JSONObject.escape(message);
        message = "{\"text\": \"" + message + "\"}";
        return message;
    }

}
