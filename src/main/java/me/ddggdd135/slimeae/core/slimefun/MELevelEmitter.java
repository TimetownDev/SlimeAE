package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.items.ItemKey;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.abstracts.MEBus;
import me.ddggdd135.slimeae.api.blockdata.MELevelEmitterData;
import me.ddggdd135.slimeae.api.blockdata.MELevelEmitterDataAdapter;
import me.ddggdd135.slimeae.api.interfaces.*;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.inventory.ItemStack;

public class MELevelEmitter extends TickingBlock implements IMEObject, InventoryBlock, IDataBlock, ISettingSlotHolder {
    public static final String AMOUNT_KEY = "amount";
    private static long defaultAmount;
    private static final MELevelEmitterDataAdapter adapter = new MELevelEmitterDataAdapter();

    public MELevelEmitter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(
            @Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData slimefunBlockData) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        NetworkInfo networkInfo = SlimeAEPlugin.getNetworkData().getNetworkInfo(block.getLocation());
        if (networkInfo == null) return;
        IStorage storage = networkInfo.getStorage();

        long setting = getAmount(block.getLocation());

        if (!ISettingSlotHolder.cache.containsKey(block.getLocation()))
            ISettingSlotHolder.updateCache(block, this, StorageCacheUtils.getBlock(block.getLocation()));
        List<Pair<ItemKey, Integer>> settings = ISettingSlotHolder.getCache(block.getLocation());

        if (settings.isEmpty() || settings.get(0) == null) {
            updateBlock(block, 0);
            return;
        }
        ItemKey itemKey = settings.get(0).getFirstValue();
        long actual = storage.getStorage().getOrDefault(itemKey, 0L);

        if (actual < setting) {
            updateBlock(block, 0);
            return;
        }

        updateBlock(block, 15);
    }

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public void init(@Nonnull BlockMenuPreset preset) {
        for (int slot : getBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        initSettingSlots(menu);
        menu.addMenuClickHandler(getInfoSlot(), (player, i, itemStack, clickAction) -> {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "请输入你想要设置的数量");
            ChatUtils.awaitInput(player, msg -> {
                try {
                    long amount = Long.parseLong(msg);
                    if (amount <= 0) {
                        player.sendMessage(CMIChatColor.translate("&c&l请输入大于0的数字"));
                        return;
                    }

                    setAmount(block.getLocation(), amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(CMIChatColor.translate("&c&l无效的数字"));
                }

                updateGUI(block);
                menu.open(player);
            });
            return false;
        });

        updateGUI(block);
    }

    public long getAmount(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return defaultAmount;
        try {
            return Long.parseLong(slimefunBlockData.getData(AMOUNT_KEY));
        } catch (Exception ignored) {
            return defaultAmount;
        }
    }

    public void setAmount(@Nonnull Location location, long value) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return;
        slimefunBlockData.setData(AMOUNT_KEY, String.valueOf(value));
    }

    public int getInfoSlot() {
        return 22;
    }

    public void updateGUI(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        ItemStack info = new AdvancedCustomItemStack(
                Material.LIME_STAINED_GLASS_PANE,
                "&c已设置数量" + getAmount(block.getLocation()),
                "",
                "&eAE网络中 设定物品大于这个数量 将发出强度为15的红石信号");
        blockMenu.replaceExistingItem(getInfoSlot(), info);
    }

    public static void reloadConfig() {
        defaultAmount = SlimeAEPlugin.getInstance().getConfig().getLong("level-emitter.default", 64L);
    }

    @Override
    public @Nullable IBlockData getData(@Nonnull Location location) {
        MELevelEmitterData data = new MELevelEmitterData();
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return null;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEBus)) return null;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return null;

        data.setAmount(getAmount(location));
        data.setItemStack(blockMenu.getItemInSlot(getSettingSlot()));

        return data;
    }

    @Override
    public void applyData(@Nonnull Location location, @Nullable IBlockData data) {
        if (!canApplyData(location, data)) return;
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        MELevelEmitterData levelEmitterData = (MELevelEmitterData) data;

        setAmount(blockMenu.getLocation(), levelEmitterData.getAmount());
        blockMenu.replaceExistingItem(getSettingSlot(), levelEmitterData.getItemStack());

        ISettingSlotHolder.cache.remove(location);
        updateGUI(blockMenu.getBlock());
    }

    @Override
    public boolean hasData(@Nonnull Location location) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MEBus)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        return blockMenu != null;
    }

    @Override
    public boolean canApplyData(@Nonnull Location location, @Nullable IBlockData blockData) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(location);
        if (slimefunBlockData == null) return false;
        SlimefunItem slimefunItem = SlimefunItem.getById(slimefunBlockData.getSfId());
        if (!(slimefunItem instanceof MELevelEmitter)) return false;
        BlockMenu blockMenu = slimefunBlockData.getBlockMenu();
        if (blockMenu == null) return false;
        return blockData instanceof MELevelEmitterData;
    }

    @Override
    public @Nonnull IBlockDataAdapter<?> getAdapter() {
        return adapter;
    }

    @Override
    public int[] getSettingSlots() {
        return new int[] {getSettingSlot()};
    }

    public int getSettingSlot() {
        return 13;
    }

    public int[] getBorderSlots() {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26};
    }

    private void updateBlock(@Nonnull Block block, int power) {
        Bukkit.getScheduler().runTask(SlimeAEPlugin.getInstance(), () -> {
            BlockState blockState = PaperLib.getBlockState(block, false).getState();
            AnaloguePowerable analoguePowerable = (AnaloguePowerable) blockState.getBlockData();
            analoguePowerable.setPower(power);
            blockState.setBlockData(analoguePowerable);
            blockState.update(true);

            for (BlockFace blockFace : IMEObject.Valid_Faces) {
                Block target = block.getRelative(blockFace);
                PaperLib.getBlockState(target, false).getState().update(true);
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}
}
