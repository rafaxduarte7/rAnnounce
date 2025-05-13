package com.rafaxplugins.announce.misc.utils;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

@SuppressWarnings("all")
public class ItemCreator implements Cloneable {

    private final ItemStack stack;
    private ItemMeta meta;

    public ItemCreator() {
        this(Material.AIR);
    }

    public ItemCreator(Material type) {
        this(type, (short) 0);
    }

    public ItemCreator(Material type, int amount) {
        this(type, amount, (short) 0);
    }

    public ItemCreator(Material type, short damage) {
        this(type, 1, damage);
    }

    public ItemCreator(Material type, int amount, short damage) {
        this(new ItemStack(type, amount, damage));
    }

    public ItemCreator(MaterialData materialData) {
        this(new ItemStack(materialData.getItemType(), 1, (short) 0));
    }

    public ItemCreator(MaterialData materialData, int amount) {
        this(new ItemStack(materialData.getItemType(), amount, (short) 0));
    }

    public ItemCreator(MaterialData materialData, short damage) {
        this(new ItemStack(materialData.getItemType(), 1, damage));
    }

    public ItemCreator(MaterialData materialData, int amount, short damage) {
        this(new ItemStack(materialData.getItemType(), amount, damage));
    }

    public ItemCreator(ItemStack itemStack) {
        this(itemStack, false);
    }

    public ItemCreator(ItemStack stack, boolean keepOriginal) {
        this.stack = keepOriginal ? stack : stack.clone();
        this.meta = this.stack.getItemMeta();
    }

    public static ItemCreator of(Material type) {
        return new ItemCreator(type);
    }

    public static ItemCreator of(Material type, int amount) {
        return new ItemCreator(type, amount);
    }

    public static ItemCreator of(Material type, short damage) {
        return new ItemCreator(type, damage);
    }

    public static ItemCreator of(Material type, int amount, short damage) {
        return new ItemCreator(type, amount, damage);
    }

    public static ItemCreator of(ItemStack itemStack, boolean keepOriginal) {
        return new ItemCreator(itemStack, keepOriginal);
    }

    public static ItemCreator of(ItemStack itemStack) {
        return of(itemStack, false);
    }

    public ItemCreator type(final Material material) {
        create().setType(material);
        return this;
    }

    public Material type() {
        return create().getType();
    }

    public ItemCreator amount(final Integer itemAmt) {
        create().setAmount(itemAmt);
        return this;
    }

    public ItemCreator name(final String name) {
        meta().setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        create().setItemMeta(meta());
        return this;
    }

    public String name() {
        return meta().getDisplayName();
    }

    public ItemCreator lore(String... lore) {
        List<String> translatedLore = new ArrayList<>();
        for (String line : lore) {
            translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(Lists.newArrayList(translatedLore));
        create().setItemMeta(meta);

        return this;
    }

    public ItemCreator lore(List<String> lore) {
        List<String> translatedLore = new ArrayList<>();
        for (String line : lore) {
            translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        meta.setLore(translatedLore);
        create().setItemMeta(meta);

        return this;
    }

    public ItemCreator addLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemCreator addLore(List<String> lore) {
        meta.setLore(Lists.newArrayList(lore));
        return this;
    }

    public ItemCreator lore(boolean override, String... lore) {
        LinkedList<String> lines = new LinkedList<>();
        for (String targetLore : lore) {
            String s = ChatColor.translateAlternateColorCodes('&', targetLore);
            lines.add(s);
        }

        if (!override) {
            List<String> oldLines = meta().getLore();

            if (oldLines != null && !oldLines.isEmpty()) {
                lines.addAll(0, oldLines);
            }
        }

        java.util.regex.Pattern COLOR_PATTERN = java.util.regex.Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-F]");
        java.util.regex.Pattern START_COLOR_PATTERN = java.util.regex.Pattern.compile("^(?i)" + COLOR_CHAR + "[0-9A-F].*$");

        for (int i = 0; i < lines.size() - 1; i++) {
            String line = lines.get(i);

            if (line == null || line.isEmpty()) {
                continue;
            }

            Matcher nextMatcher = START_COLOR_PATTERN.matcher(lines.get(i + 1));

            if (nextMatcher.find()) {
                continue;
            }

            Matcher currentMatcher = COLOR_PATTERN.matcher(line);

            if (currentMatcher.find()) {
                String lastColor = currentMatcher.group(currentMatcher.groupCount());

                lines.set(i + 1, lastColor + lines.get(i + 1));
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line != null && !line.isEmpty() && !START_COLOR_PATTERN.matcher(line).find()) {
                lines.set(i, ChatColor.GRAY + line);
            }

            if (ChatColor.stripColor(line).isEmpty()) {
                lines.set(i, "");
            }
        }

        meta().setLore(lines);
        create().setItemMeta(meta());
        return this;
    }

    public List<String> lore() {
        return meta().getLore() == null ? Collections.emptyList() : Lists.newArrayList(meta().getLore());
    }

    public short durability() {
        return create().getDurability();
    }

    public ItemCreator durability(final int durability) {
        create().setDurability((short) durability);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemCreator data(final int data) {
        create().setData(new MaterialData(create().getType(), (byte) data));
        return this;
    }

    public ItemCreator patterns(List<Pattern> patterns) {
        if (create().getType() == Material.BANNER) {
            BannerMeta meta = (BannerMeta) meta();
            meta.setPatterns(patterns);
            create().setItemMeta(meta);
        }

        return this;
    }

    public ItemCreator glowing(boolean glowing) {
        if (create().getType().equals(Material.GOLDEN_APPLE)) {
            durability((short) (glowing ? 1 : 0));
            create().setItemMeta(meta());
            return this;
        }

        if (enchantments().isEmpty()) {
            if (glowing) {
                nbt("ench", new NBTTagList());
            } else {
                removeNbt("ench");
            }
        }

        create().setItemMeta(meta());
        return this;
    }

    public ItemCreator clearFlags(ItemFlag... flags) {
        if (flags == null || flags.length == 0) {
            flags = ItemFlag.values();
        }

        meta().removeItemFlags(flags);
        create().setItemMeta(meta());

        return this;
    }

    public ItemCreator flags(ItemFlag... flags) {
        meta().addItemFlags(flags);
        create().setItemMeta(meta());
        return this;
    }

    public Set<ItemFlag> flags() {
        return meta().getItemFlags();
    }

    public ItemCreator persistent(boolean value) {
        create().setItemMeta(meta());
        return this;
    }

    public ItemCreator unbreakable(boolean unbreakable) {
        meta().spigot().setUnbreakable(unbreakable);
        create().setItemMeta(meta());
        return this;
    }

    public ItemCreator metaEnchantment(final Enchantment enchantment, final int level) {
        if (meta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta();

            storageMeta.addStoredEnchant(enchantment, level, true);

            create().setItemMeta(meta());
        }

        return this;
    }

    public ItemCreator enchantment(final Enchantment enchantment, final int level) {
        create().addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemCreator addEnchantment(final Enchantment enchantment, final int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemCreator enchantment(final Enchantment enchantment) {
        create().addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemCreator enchantments(final Enchantment[] enchantments, final int level) {
        create().getEnchantments().clear();
        for (Enchantment enchantment : enchantments) {
            create().addUnsafeEnchantment(enchantment, level);
        }
        return this;
    }

    public ItemCreator enchantments(final Enchantment[] enchantments) {
        create().getEnchantments().clear();
        for (Enchantment enchantment : enchantments) {
            create().addUnsafeEnchantment(enchantment, 1);
        }
        return this;
    }

    public ItemCreator clearEnchantment(final Enchantment enchantment) {
        if (meta().hasEnchant(enchantment)) {
            meta().removeEnchant(enchantment);
        }

        return this;
    }

    public ItemCreator clearEnchantments() {
        create().getEnchantments().clear();
        return this;
    }

    public Map<Enchantment, Integer> enchantments() {
        return create().getEnchantments();
    }

    public ItemCreator clearLore(final String lore) {
        if (meta().getLore().contains(lore)) {
            meta().getLore().remove(lore);
        }
        create().setItemMeta(meta());
        return this;
    }

    public ItemCreator clearLores() {
        if (meta().getLore() != null) {
            meta().getLore().clear();
        }
        create().setItemMeta(meta());
        return this;
    }

    public ItemCreator effect(PotionEffect potionEffect, boolean overwrite) {
        if (meta() instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) meta();
            potionMeta.addCustomEffect(potionEffect, overwrite);

            create().setItemMeta(potionMeta);
        }
        return this;
    }

    public ItemCreator color(Color color) {
        if (create().getType() == Material.LEATHER_HELMET
                || create().getType() == Material.LEATHER_CHESTPLATE
                || create().getType() == Material.LEATHER_LEGGINGS
                || create().getType() == Material.LEATHER_BOOTS) {
            LeatherArmorMeta meta = (LeatherArmorMeta) meta();
            meta.setColor(color);
            create().setItemMeta(meta);
        }
        return this;
    }

    public ItemCreator color(DyeColor color) {

        if (create().getType() == Material.BANNER) {
            BannerMeta meta = (BannerMeta) meta();
            meta.setBaseColor(color);
            create().setItemMeta(meta);
        }

        return this;
    }

    public ItemCreator clearColor() {
        if (create().getType() == Material.LEATHER_HELMET
                || create().getType() == Material.LEATHER_CHESTPLATE
                || create().getType() == Material.LEATHER_LEGGINGS
                || create().getType() == Material.LEATHER_BOOTS) {
            LeatherArmorMeta meta = (LeatherArmorMeta) meta();
            meta.setColor(null);
            create().setItemMeta(meta);
        }

        if (create().getType() == Material.BANNER) {
            BannerMeta meta = (BannerMeta) meta();
            meta.setBaseColor(null);
            create().setItemMeta(meta);
        }

        return this;
    }

    public ItemCreator skullOwner(final String name) {
        if (create().getType() == Material.SKULL_ITEM && create().getDurability() == (byte) 3) {
            SkullMeta skullMeta = (SkullMeta) meta();
            skullMeta.setOwner(name);
            create().setItemMeta(meta());
        }
        return this;
    }

    public ItemCreator skull(Player player) {
        if (create().getType() == Material.SKULL_ITEM && create().getDurability() == (byte) 3) {
            SkullMeta skullMeta = (SkullMeta) meta();
            skullMeta.setOwner("CustomHead");

            try {
                GameProfile playerProfile = ((CraftPlayer) player).getHandle().getProfile();

                GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);

                gameProfile.getProperties().putAll("textures", playerProfile.getProperties().get("textures"));

                Field profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);

                profileField.set(skullMeta, gameProfile);

                create().setItemMeta(skullMeta);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public ItemCreator skull(String value, String signature) {
        if (create().getType() == Material.SKULL_ITEM && create().getDurability() == (byte) 3) {
            SkullMeta skullMeta = (SkullMeta) meta();
            skullMeta.setOwner("CustomHead");

            try {
                GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);

                gameProfile.getProperties().put("textures", new Property("textures", value, signature));

                Field profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);

                profileField.set(skullMeta, gameProfile);

                create().setItemMeta(skullMeta);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public ItemCreator skullUrl(String id) {
        if (create().getType() == Material.SKULL_ITEM && create().getDurability() == (byte) 3) {
            SkullMeta skullMeta = (SkullMeta) meta();
            skullMeta.setOwner("CustomHead");

            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
            byte[] encodedData;

            if (id.startsWith("http://") || id.startsWith("https://")) {
                encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", id).getBytes());
            } else {
                encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/%s\"}}}", id).getBytes());
            }

            gameProfile.getProperties().put("textures", new Property("textures", new String(encodedData), null));
            Field profileField = null;

            try {
                profileField = skullMeta.getClass().getDeclaredField("profile");
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }

            profileField.setAccessible(true);

            try {
                profileField.set(skullMeta, gameProfile);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

            create().setItemMeta(skullMeta);
        }

        return this;
    }

    private void nbt(Consumer<NBTTagCompound> consumer) {
        net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(create());

        NBTTagCompound compound = (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();

        consumer.accept(compound);

        nmsCopy.setTag(compound);

        meta = CraftItemStack.asBukkitCopy(nmsCopy).getItemMeta();
        create().setItemMeta(meta());
    }

    private <T> T nbt(Function<NBTTagCompound, T> function) {
        net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(create());

        NBTTagCompound compound = (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();

        return function.apply(compound);
    }

    public boolean hasNbt(String tag) {
        return nbt(compound -> {
            return compound.hasKey(tag);
        });
    }

    public void removeNbt(String tag) {
        nbt(compound -> {
            compound.remove(tag);
        });
    }

    public ItemCreator nbt(String tag, NBTBase value) {
        nbt(compound -> {
            compound.set(tag, value);
        });

        return this;
    }

    public ItemCreator nbt(String tag, int value) {
        nbt(compound -> {
            compound.setInt(tag, value);
        });

        return this;
    }

    public ItemCreator nbt(String tag, boolean value) {
        nbt(compound -> {
            compound.setBoolean(tag, value);
        });

        return this;
    }

    public ItemCreator nbt(String tag, long value) {
        nbt(compound -> {
            compound.setLong(tag, value);
        });

        return this;
    }

    public ItemCreator nbt(String tag, String value) {
        nbt(compound -> {
            compound.setString(tag, value);
        });

        return this;
    }

    public String nbtString(String tag) {
        return nbt(compound -> {
            return compound.hasKey(tag) ? compound.getString(tag) : null;
        });
    }

    public Integer nbtInt(String tag) {
        return nbt(compound -> {
            return compound.hasKey(tag) ? compound.getInt(tag) : null;
        });
    }

    public Long nbtLong(String tag) {
        return nbt(compound -> {
            return compound.hasKey(tag) ? compound.getLong(tag) : null;
        });
    }

    public Double nbtDouble(String tag) {
        return nbt(compound -> {
            return compound.hasKey(tag) ? compound.getDouble(tag) : null;
        });
    }

    public Boolean nbtBoolean(String tag) {
        return nbt(compound -> {
            return compound.hasKey(tag) ? compound.getBoolean(tag) : null;
        });
    }

    public NBTTagList nbtList(String tag) {
        return nbt(compound -> {
            NBTBase base = compound.get(tag);
            return base instanceof NBTTagList ? (NBTTagList) base : new NBTTagList();
        });
    }

    public NBTTagCompound nbt() {
        net.minecraft.server.v1_8_R3.ItemStack nmsCopy = CraftItemStack.asNMSCopy(create());

        return (nmsCopy.hasTag()) ? nmsCopy.getTag() : new NBTTagCompound();
    }

    public ItemMeta meta() {
        return meta;
    }

    public ItemStack create() {
        return stack;
    }

    @Override
    public ItemCreator clone() {
        return new ItemCreator(this.create().clone());
    }

}
