package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.handlers.BlockLeftClickedHandler;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.api.items.ItemRequest;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.ddggdd135.slimeae.utils.SerializeUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MEConversionMonitor extends SlimefunItem implements IMEObject, HologramOwner {
    public static final String ITEM_KEY = "item";
    public static final String LOCKED_KEY = "locked";

    public MEConversionMonitor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        addItemHandler(onRightClick());
        addItemHandler(onLeftClick());
        addItemHandler(onBlockBreak());
        addItemHandler(onBlockPlace());
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {
        updateHologram(block);
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    @Nullable public ItemStack getItem(@Nonnull Block block) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());

        if (data == null) return null;
        Slimefun.getDatabaseManager().getBlockDataController().loadBlockData(data);

        String item = data.getData(ITEM_KEY);
        if (item == null) return null;

        return (ItemStack) SerializeUtils.string2Object(item);
    }

    public void setItem(@Nonnull Block block, @Nullable ItemStack itemStack) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());

        if (data == null) return;
        Slimefun.getDatabaseManager().getBlockDataController().loadBlockData(data);
        if (itemStack == null || itemStack.getType().isAir()) {
            data.removeData(ITEM_KEY);
            return;
        }

        data.setData(ITEM_KEY, SerializeUtils.object2String(itemStack));
    }

    public boolean isLocked(@Nonnull Block block) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());

        if (data == null) return false;
        Slimefun.getDatabaseManager().getBlockDataController().loadBlockData(data);

        String locked = data.getData(LOCKED_KEY);
        if (locked == null) return false;

        return SerializeUtils.stringToBool(locked);
    }

    public void setLocked(@Nonnull Block block, boolean locked) {
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());

        if (data == null) return;
        Slimefun.getDatabaseManager().getBlockDataController().loadBlockData(data);

        data.setData(LOCKED_KEY, SerializeUtils.boolToString(locked));
    }

    private BlockUseHandler onRightClick() {
        return e -> {
            e.cancel();
            Block block = e.getClickedBlock().get();

            if (!(e.getPlayer().hasPermission("slimefun.inventory.bypass")
                    || Slimefun.getProtectionManager()
                            .hasPermission(e.getPlayer(), block.getLocation(), Interaction.INTERACT_BLOCK))) {
                Slimefun.getLocalization().sendMessage(e.getPlayer(), "inventory.no-access", true);
                return;
            }

            ItemStack hand = e.getItem();
            ItemStack item = getItem(e.getClickedBlock().get());
            Inventory playerInventory = e.getPlayer().getInventory();

            NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
            if (networkInfo == null) {
                updateHologram(block);
                return;
            }

            IStorage storage = networkInfo.getStorage();

            if (hand.getType().isAir() && e.getPlayer().isSneaking()) {
                setLocked(block, !isLocked(block));
                updateHologram(block);
                return;
            }

            if (!isLocked(block)) {
                setItem(block, hand);
                updateHologram(block);
                return;
            }

            if (!hand.getType().isAir()
                    && isLocked(block)
                    && !(item == null || item.getType().isAir())) {
                storage.pushItem(hand);

                if (hand.getAmount() == 0) hand.setType(Material.AIR);

                updateHologram(block);
                return;
            }

            if (hand.getType().isAir()
                    && isLocked(block)
                    && (item != null && !item.getType().isAir())) {
                for (int i = 0; i < 36; i++) {
                    ItemStack itemStack = playerInventory.getItem(i);
                    if (itemStack == null || itemStack.getType().isAir()) continue;
                    if (SlimefunUtils.isItemSimilar(itemStack, item, true, false)) storage.pushItem(itemStack);

                    if (itemStack.getAmount() == 0) itemStack.setType(Material.AIR);
                }

                updateHologram(block);
                return;
            }

            updateHologram(block);
        };
    }

    private BlockLeftClickedHandler onLeftClick() {
        return new BlockLeftClickedHandler() {
            @Override
            public void onLeftClick(PlayerInteractEvent e) {
                Block block = e.getClickedBlock();

                if (!(e.getPlayer().hasPermission("slimefun.inventory.bypass")
                        || Slimefun.getProtectionManager()
                        .hasPermission(e.getPlayer(), block.getLocation(), Interaction.INTERACT_BLOCK))) {
                    Slimefun.getLocalization().sendMessage(e.getPlayer(), "inventory.no-access", true);
                    return;
                }

                ItemStack hand = e.getItem();
                ItemStack item = getItem(e.getClickedBlock());
                Inventory playerInventory = e.getPlayer().getInventory();

                if (item == null || item.getType().isAir()) return;
                NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData()
                        .getNetworkInfo(e.getClickedBlock().getLocation());
                if (networkInfo == null) {
                    updateHologram(e.getClickedBlock());
                    return;
                }

                IStorage storage = networkInfo.getStorage();
                ItemKey itemKey = new ItemKey(item);

                int amount = (int) Math.min(
                        e.getPlayer().isSneaking() ? item.getMaxStackSize() : 1,
                        storage.getStorageUnsafe().getOrDefault(itemKey, 0L));
                if (amount == 0) return;

                if (InvUtils.fits(
                        playerInventory,
                        item.asQuantity(amount),
                        IntStream.range(0, 36).toArray())) {
                    ItemStack result =
                            storage.takeItem(new ItemRequest(itemKey, amount)).toItemStacks()[0];
                    playerInventory.addItem(result);
                    updateHologram(e.getClickedBlock());
                }

                updateHologram(e.getClickedBlock());
            }
        };
    }

    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                setItem(b, null);
                setLocked(b, false);

                removeHologram(b);
            }
        };
    }

    private BlockPlaceHandler onBlockPlace() {
        return new BlockPlaceHandler(true) {
            @Override
            public void onPlayerPlace(@NotNull BlockPlaceEvent e) {
                updateHologram(e.getBlockPlaced());
            }
        };
    }

    public void updateHologram(@Nonnull Block block) {
        NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (networkInfo == null) {
            updateHologram(block, CMIChatColor.translate("&c未连接到控制器"));
            return;
        }
        ItemStack item = getItem(block);

        if (item == null || item.getType().isAir()) {
            updateHologram(block, makeLockMessage(block, CMIChatColor.translate("&c未标记物品")));
            return;
        }

        updateHologram(
                block,
                makeLockMessage(
                        block,
                        CMIChatColor.translate("&a" + ItemUtils.getItemName(item) + " &cx&e"
                                + networkInfo.getStorage().getStorageUnsafe().getOrDefault(item, 0L))));
    }

    @Nonnull
    public String makeLockMessage(@Nonnull Block block, @Nonnull String msg) {
        return isLocked(block) ? msg + " &e| &c已锁定" : msg + " &e| &a未锁定";
    }
}
