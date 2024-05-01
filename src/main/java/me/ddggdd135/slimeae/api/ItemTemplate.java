package me.ddggdd135.slimeae.api;

import com.google.common.base.Preconditions;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.papermc.paper.inventory.ItemRarity;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.UndefinedNullability;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class ItemTemplate extends ItemStack {
    private ItemStack handle;

    protected ItemTemplate() {
        handle = new ItemStack(Material.AIR, 0);
    }

    public ItemTemplate(@NotNull Material type) {
        this(type, 0);
    }

    public ItemTemplate(@NotNull Material type, int amount) {
        handle = new ItemStack(type, amount);
    }

    public ItemTemplate(@NotNull ItemStack stack) throws IllegalArgumentException {
        Preconditions.checkArgument(stack != null, "Cannot copy null stack");
        this.handle = stack.clone();
        this.handle.setAmount(0);
    }

    public @NotNull Material getType() {
        return handle.getType();
    }

    public void setType(@NotNull Material type) {
        Preconditions.checkArgument(type != null, "Material cannot be null");
        handle.setType(type);
    }

    public int getAmount() {
        return 0;
    }

    public void setAmount(int amount) {}

    /** @deprecated */
    @Deprecated
    public @Nullable MaterialData getData() {
        return handle.getData();
    }

    /** @deprecated */
    @Deprecated
    public void setData(@Nullable MaterialData data) {
        handle.setData(data);
    }

    /** @deprecated */
    @Deprecated
    public void setDurability(short durability) {
        handle.setDurability(durability);
    }

    /** @deprecated */
    @Deprecated
    public short getDurability() {
        return handle.getDurability();
    }

    public int getMaxStackSize() {
        return handle.getMaxStackSize();
    }

    public String toString() {
        StringBuilder toString = (new StringBuilder("ItemStack{"))
                .append(this.getType().name())
                .append(" x ")
                .append(this.getAmount());
        if (this.hasItemMeta()) {
            toString.append(", ").append(this.getItemMeta());
        }

        return toString.append('}').toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ItemStack)) {
            return false;
        } else {
            ItemStack stack = (ItemStack) obj;
            return this.getAmount() == stack.getAmount() && this.isSimilar(stack);
        }
    }

    public boolean isSimilar(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        } else if (stack == this) {
            return true;
        } else {
            return NBTItem.convertItemtoNBT(stack).equals(NBTItem.convertItemtoNBT(handle));
        }
    }

    public @NotNull ItemStack clone() {
        return new ItemTemplate(handle);
    }

    public int hashCode() {
        return new NBTItem(handle, true).hashCode();
    }

    public boolean containsEnchantment(@NotNull Enchantment ench) {
        return handle.containsEnchantment(ench);
    }

    public int getEnchantmentLevel(@NotNull Enchantment ench) {
        return handle.getEnchantmentLevel(ench);
    }

    public @NotNull Map<Enchantment, Integer> getEnchantments() {
        return handle.getEnchantments();
    }

    public void addEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
        handle.addEnchantments(enchantments);
    }

    public void addEnchantment(@NotNull Enchantment ench, int level) {
        handle.addEnchantment(ench, level);
    }

    public void addUnsafeEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
        handle.addUnsafeEnchantments(enchantments);
    }

    public void addUnsafeEnchantment(@NotNull Enchantment ench, int level) {
        handle.addUnsafeEnchantment(ench, level);
    }

    public int removeEnchantment(@NotNull Enchantment ench) {
        return handle.removeEnchantment(ench);
    }

    public @NotNull Map<String, Object> serialize() {
        return handle.serialize();
    }

    public static @NotNull ItemStack deserialize(@NotNull Map<String, Object> args) {
        return ItemStack.deserialize(args);
    }

    public boolean editMeta(@NotNull Consumer<? super ItemMeta> consumer) {
        return handle.editMeta(consumer);
    }

    public <M extends ItemMeta> boolean editMeta(@NotNull Class<M> metaClass, @NotNull Consumer<? super M> consumer) {
        return handle.editMeta(metaClass, consumer);
    }

    @UndefinedNullability
    public ItemMeta getItemMeta() {
        return handle.getItemMeta();
    }

    public boolean hasItemMeta() {
        return true;
    }

    public boolean setItemMeta(@Nullable ItemMeta itemMeta) {
        return handle.setItemMeta(itemMeta);
    }

    public @NotNull ItemStack enchantWithLevels(
            @Range(from = 1L, to = 30L) int levels, boolean allowTreasure, @NotNull Random random) {
        return handle.enchantWithLevels(levels, allowTreasure, random);
    }

    public @NotNull HoverEvent<HoverEvent.ShowItem> asHoverEvent(@NotNull UnaryOperator<HoverEvent.ShowItem> op) {
        return handle.asHoverEvent(op);
    }

    public @NotNull Component displayName() {
        return handle.displayName();
    }

    public @NotNull ItemStack ensureServerConversions() {
        return handle.ensureServerConversions();
    }

    public @NotNull byte[] serializeAsBytes() {
        return handle.serializeAsBytes();
    }

    /** @deprecated */
    @Deprecated
    public @Nullable String getI18NDisplayName() {
        return handle.getI18NDisplayName();
    }

    public int getMaxItemUseDuration() {
        return handle.getMaxItemUseDuration();
    }

    public @NotNull ItemStack asOne() {
        return handle;
    }

    public @NotNull ItemStack asQuantity(int qty) {
        return handle;
    }

    public @NotNull ItemStack add() {
        return handle;
    }

    public @NotNull ItemStack add(int qty) {
        return handle;
    }

    public @NotNull ItemStack subtract() {
        return handle;
    }

    public @NotNull ItemStack subtract(int qty) {
        return handle;
    }

    /** @deprecated */
    @Deprecated
    public @Nullable List<String> getLore() {
        return handle.getLore();
    }

    public @Nullable List<Component> lore() {
        return handle.lore();
    }

    /** @deprecated */
    @Deprecated
    public void setLore(@Nullable List<String> lore) {
        handle.setLore(lore);
    }

    public void lore(@Nullable List<? extends Component> lore) {
        handle.lore(lore);
    }

    public void addItemFlags(ItemFlag... itemFlags) {
        handle.addItemFlags(itemFlags);
    }

    public void removeItemFlags(ItemFlag... itemFlags) {
        handle.removeItemFlags(itemFlags);
    }

    public @NotNull Set<ItemFlag> getItemFlags() {
        return handle.getItemFlags();
    }

    public boolean hasItemFlag(@NotNull ItemFlag flag) {
        return handle.hasItemFlag(flag);
    }

    public @NotNull String translationKey() {
        return handle.translationKey();
    }

    public @NotNull ItemRarity getRarity() {
        return handle.getRarity();
    }

    public boolean isRepairableBy(@NotNull ItemStack repairMaterial) {
        return handle.isRepairableBy(repairMaterial);
    }

    public boolean canRepair(@NotNull ItemStack toBeRepaired) {
        return handle.canRepair(toBeRepaired);
    }

    public @NotNull ItemStack damage(int amount, @NotNull LivingEntity livingEntity) {
        return handle.damage(amount, livingEntity);
    }

    public boolean isEmpty() {
        return handle.getType().isAir();
    }

    public ItemStack getHandle() {
        return handle.clone();
    }

    public void setHandle(ItemStack handle) {
        this.handle = handle.clone();
        this.handle.setAmount(0);
    }
}
