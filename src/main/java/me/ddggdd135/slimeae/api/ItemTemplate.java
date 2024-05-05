package me.ddggdd135.slimeae.api;

import com.google.common.base.Preconditions;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.papermc.paper.inventory.ItemRarity;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class ItemTemplate
        implements Cloneable, HoverEventSource<HoverEvent.ShowItem>, net.kyori.adventure.translation.Translatable {
    private ItemStack handle;
    private NBTItem nbt_handle;
    private boolean isAir;

    protected ItemTemplate() {
        handle = new ItemStack(Material.STONE, 1);
        isAir = true;
    }

    public ItemTemplate(@NotNull Material type) {
        handle = new ItemStack(type, 1);
        if (type.isAir()) {
            handle.setType(Material.STONE);
            isAir = true;
        }
        if (!isAir) nbt_handle = new NBTItem(handle, true);
    }

    public ItemTemplate(@NotNull ItemStack stack) throws IllegalArgumentException {
        Preconditions.checkArgument(stack != null, "Cannot copy null stack");
        handle = stack.clone();
        if (stack.getType().isAir()) {
            handle.setType(Material.STONE);
            isAir = true;
        }
        if (!isAir) {
            handle.setAmount(1);
            nbt_handle = new NBTItem(handle, true);
        }
    }

    public @NotNull Material getType() {
        return handle.getType();
    }

    public void setType(@NotNull Material type) {
        Preconditions.checkArgument(type != null, "Material cannot be null");
        if (type.isAir()) isAir = true;
        else isAir = false;
        handle.setType(isAir ? Material.STONE : type);
    }

    public int getAmount() {
        return 1;
    }

    public void setAmount(int amount) {}

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
        ItemStack itemStack;
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ItemStack)) {
            if (obj instanceof ItemTemplate template) {
                if (isAir != template.isAir) return false;
                if (isAir && template.isAir) return true;
                itemStack = template.getHandle();
            } else return false;
        } else itemStack = (ItemStack) obj;

        return this.isSimilar(itemStack);
    }

    public boolean isSimilar(@Nullable ItemStack stack) {
        if (stack == null) return false;
        else {
            if (isAir != stack.getType().isAir()) return false;
            if (isAir && stack.getType().isAir()) return true;
            if (nbt_handle == null) nbt_handle = new NBTItem(handle, true);
            return handle.getType() == stack.getType()
                    && stack.getAmount() != 0
                    && new NBTItem(stack, true).equals(nbt_handle);
        }
    }

    @Override
    public @NotNull ItemTemplate clone() {
        return new ItemTemplate(handle);
    }

    public int hashCode() {
        if (nbt_handle == null && !isAir) {
            nbt_handle = new NBTItem(handle, true);
            return nbt_handle.hashCode() + 31 * (isAir ? 32 : 255)
                    ^ handle.getType().hashCode();
        } else {
            return 200 ^ 255;
        }
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

    public boolean editMeta(@NotNull Consumer<? super ItemMeta> consumer) {
        return handle.editMeta(consumer);
    }

    public <M extends ItemMeta> boolean editMeta(@NotNull Class<M> metaClass, @NotNull Consumer<? super M> consumer) {
        return handle.editMeta(metaClass, consumer);
    }

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
        if (!isAir) return handle.clone();
        return new ItemStack(Material.AIR);
    }

    public void setHandle(@NotNull ItemStack handle) {
        this.handle = handle.clone();
        this.handle.setAmount(1);
        nbt_handle = new NBTItem(this.handle, true);
    }
}
