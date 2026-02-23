package me.ddggdd135.slimeae.core.managers;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ProfileDataController;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IManager;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PinnedManager implements IManager {
    private static final int DATA_ITEM_SLOT = 0;
    private static final String BACKPACK_NAME = "AEPinnedItemBackpack";
    private static final @Nullable ProfileDataController controller =
            Slimefun.getDatabaseManager().getProfileDataController();
    private final NamespacedKey PINNED_KEY;

    // === F6: 置顶物品查询缓存 ===
    private final ConcurrentHashMap<UUID, List<ItemStack>> pinnedCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> pinnedCacheTime = new ConcurrentHashMap<>();
    private static final long PINNED_CACHE_TTL = 3000; // 3秒 TTL

    public PinnedManager() {
        this.PINNED_KEY = new NamespacedKey(SlimeAEPlugin.getInstance(), "pinned");
    }

    public void addPinned(@Nonnull Player player, @Nonnull ItemStack itemStack) {
        // F6: 主动使缓存失效
        invalidatePinnedCache(player);
        PlayerBackpack backpack = getOrCreateBookmarkBackpack(player);
        if (backpack == null) {
            return;
        }

        addPinned0(player, backpack, itemStack);
    }

    public void removePinned(@Nonnull Player player, @Nonnull ItemStack itemStack) {
        // F6: 主动使缓存失效
        invalidatePinnedCache(player);
        PlayerBackpack backpack = getOrCreateBookmarkBackpack(player);
        if (backpack == null) {
            return;
        }

        removePinned0(player, backpack, itemStack);
    }

    private void addPinned0(@Nonnull Player player, @Nonnull PlayerBackpack backpack, @Nonnull ItemStack pinned) {
        ItemStack pinsItem = backpack.getInventory().getItem(DATA_ITEM_SLOT);
        if (pinsItem == null || pinsItem.getType().isAir()) {
            pinsItem = new ItemStack(Material.PAPER);
        }

        ItemStack itemStack = markItemAsPinnedDataItem(
                new AdvancedCustomItemStack(pinsItem, itemMeta -> {
                    List<String> lore = itemMeta.getLore();
                    if (lore == null) {
                        lore = new ArrayList<>();
                    }
                    String id = SerializeUtils.getId(pinned.asOne());
                    if (id == null || id.equals("VANILLA_AIR")) return;
                    lore.remove(id);
                    lore.add(id);
                    itemMeta.setLore(lore);
                }),
                player);

        backpack.getInventory().setItem(DATA_ITEM_SLOT, itemStack);
        operateController(controller -> {
            controller.saveBackpackInventory(backpack, DATA_ITEM_SLOT);
        });
    }

    private void removePinned0(@Nonnull Player player, @Nonnull PlayerBackpack backpack, @Nonnull ItemStack pinned) {
        ItemStack pinsItem = backpack.getInventory().getItem(DATA_ITEM_SLOT);
        if (pinsItem == null || pinsItem.getType().isAir()) {
            pinsItem = new ItemStack(Material.PAPER);
        }

        ItemStack itemStack = markItemAsPinnedDataItem(
                new AdvancedCustomItemStack(pinsItem, itemMeta -> {
                    List<String> lore = itemMeta.getLore();
                    if (lore == null) {
                        lore = new ArrayList<>();
                    }
                    String id = SerializeUtils.getId(pinned.asOne());
                    if (id.equals("VANILLA_AIR")) return;
                    lore.remove(id);
                    itemMeta.setLore(lore);
                }),
                player);

        backpack.getInventory().setItem(DATA_ITEM_SLOT, itemStack);
        operateController(controller -> {
            controller.saveBackpackInventory(backpack, DATA_ITEM_SLOT);
        });
    }

    @Nullable public List<ItemStack> getPinnedItems(@Nonnull Player player) {
        // F6: 检查缓存
        UUID uuid = player.getUniqueId();
        Long cachedTime = pinnedCacheTime.get(uuid);
        if (cachedTime != null && (System.currentTimeMillis() - cachedTime) < PINNED_CACHE_TTL) {
            List<ItemStack> cached = pinnedCache.get(uuid);
            if (cached != null) return cached;
        }

        List<ItemStack> result = getPinnedItemsInternal(player);

        // 缓存结果（包括null，用空列表代替）
        pinnedCache.put(uuid, result != null ? result : new ArrayList<>());
        pinnedCacheTime.put(uuid, System.currentTimeMillis());
        return result;
    }

    /**
     * 使指定玩家的置顶缓存失效
     */
    public void invalidatePinnedCache(@Nonnull Player player) {
        UUID uuid = player.getUniqueId();
        pinnedCache.remove(uuid);
        pinnedCacheTime.remove(uuid);
    }

    /**
     * 清空所有置顶缓存
     */
    public void clearPinnedCache() {
        pinnedCache.clear();
        pinnedCacheTime.clear();
    }

    @Nullable private List<ItemStack> getPinnedItemsInternal(@Nonnull Player player) {
        PlayerBackpack backpack = getPinnedBackpack(player);
        if (backpack == null) {
            return null;
        }

        ItemStack pinnedDataItem = backpack.getInventory().getItem(DATA_ITEM_SLOT);
        if (pinnedDataItem == null || pinnedDataItem.getType().isAir()) {
            return null;
        }

        if (!isPinnedDataItem(pinnedDataItem, player)) {
            return null;
        }

        List<ItemStack> pinnedItems = new ArrayList<>();
        ItemMeta itemMeta = pinnedDataItem.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        List<String> lore = itemMeta.getLore();
        if (lore != null) {
            for (String id : lore) {
                ItemStack itemStack = (ItemStack) SerializeUtils.string2Object(id);
                if (itemStack != null) {
                    pinnedItems.add(itemStack);
                }
            }
        }

        return pinnedItems;
    }

    public boolean isPinnedDataItem(@Nonnull ItemStack itemStack, @Nonnull Player player) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        String uuid = itemMeta.getPersistentDataContainer().get(PINNED_KEY, PersistentDataType.STRING);
        if (uuid != null && uuid.equals(player.getUniqueId().toString())) {
            return true;
        }

        return false;
    }

    @Nonnull
    public ItemStack markItemAsPinnedDataItem(@Nonnull ItemStack itemStack, @Nonnull Player player) {
        return new AdvancedCustomItemStack(itemStack, itemMeta -> itemMeta.getPersistentDataContainer()
                        .set(
                                PINNED_KEY,
                                PersistentDataType.STRING,
                                player.getUniqueId().toString()))
                .clone();
    }

    @Nullable public PlayerBackpack getOrCreateBookmarkBackpack(@Nonnull Player player) {
        PlayerBackpack backpack = getPinnedBackpack(player);
        if (backpack == null) {
            backpack = createBackpack(player);
        }

        return backpack;
    }

    @Nullable public PlayerBackpack createBackpack(@Nonnull Player player) {
        PlayerProfile profile = operateController(controller -> {
            return controller.getProfile(player);
        });
        if (profile == null) {
            return null;
        }

        PlayerBackpack backpack = operateController(controller -> {
            return controller.createBackpack(player, BACKPACK_NAME, profile.nextBackpackNum(), 9);
        });
        if (backpack == null) {
            return null;
        }

        backpack.getInventory()
                .setItem(DATA_ITEM_SLOT, markItemAsPinnedDataItem(new ItemStack(Material.PAPER), player));
        operateController(controller -> {
            controller.saveBackpackInventory(backpack, DATA_ITEM_SLOT);
        });
        return backpack;
    }

    @Nullable public PlayerBackpack getPinnedBackpack(@Nonnull Player player) {
        PlayerProfile profile = operateController(controller -> {
            return controller.getProfile(player);
        });
        if (profile == null) {
            return null;
        }

        Set<PlayerBackpack> backpacks = operateController(controller -> {
            return controller.getBackpacks(profile.getUUID().toString());
        });
        if (backpacks == null || backpacks.isEmpty()) {
            return null;
        }

        for (PlayerBackpack backpack : backpacks) {
            if (backpack.getName().equals(BACKPACK_NAME)) {
                Inventory inventory = backpack.getInventory();
                ItemStack[] contents = inventory.getContents();

                ItemStack bookmarksItem = contents[DATA_ITEM_SLOT];
                if (bookmarksItem == null || bookmarksItem.getType() == Material.AIR) {
                    return null;
                }

                if (!isPinnedDataItem(bookmarksItem, player)) {
                    return null;
                }

                for (int i = 0; i < contents.length; i++) {
                    if (i != DATA_ITEM_SLOT && contents[i] != null && contents[i].getType() != Material.AIR) {
                        return null;
                    }
                }

                return backpack;
            }
        }

        return null;
    }

    private void operateController(@Nonnull Consumer<ProfileDataController> consumer) {
        if (controller != null) {
            consumer.accept(controller);
        }
    }

    @Nullable
    private <T, R> R operateController(@Nonnull Function<ProfileDataController, R> function) {
        if (controller != null) {
            return function.apply(controller);
        }
        return null;
    }
}
