package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.ItemHashMap;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.guguslimefunlib.items.AdvancedCustomItemStack;
import me.ddggdd135.guguslimefunlib.libraries.colors.CMIChatColor;
import me.ddggdd135.slimeae.SlimeAEPlugin;
import me.ddggdd135.slimeae.api.interfaces.IMEObject;
import me.ddggdd135.slimeae.api.interfaces.IStorage;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.utils.NetworkUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MECraftingTrigger extends TickingBlock implements IMEObject, InventoryBlock {
    public static final String AMOUNT_KEY = "amount";
    private static long defaultAmount = 64L;
    private static Map<Location, Long> ticks = new HashMap<>();

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem item, @Nonnull SlimefunBlockData data) {
        updateGUI(block);
    }

    public MECraftingTrigger(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this);
        addItemHandler(new SimpleBlockBreakHandler() {
            @Override
            public void onBlockBreak(@NotNull Block block) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                if (blockMenu == null) return;

                blockMenu.dropItems(blockMenu.getLocation(), getSettingSlots());
            }
        });
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {}

    @Override
    public void onNetworkTimeConsumingTick(Block block, NetworkInfo networkInfo) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        long tick = ticks.getOrDefault(block.getLocation(), 0L);
        tick++;
        ticks.put(block.getLocation(), tick);

        long setting = getAmount(block.getLocation());

        IStorage networkStorage = networkInfo.getStorage();
        ItemHashMap<Long> total = networkStorage.getStorageUnsafe();

        int slot = getSettingSlots()[(int) (tick % getSettingSlots().length)];

        ItemStack itemStack = blockMenu.getItemInSlot(slot);
        if (itemStack == null || itemStack.getType().isAir()) return;
        ItemStack template = itemStack.asOne();
        long current = total.getOrDefault(template, 0L);
        if (current >= setting) return;

        long toCraft = setting - current;
        long amount;
        if (toCraft <= 64) amount = toCraft;
        else {
            amount = (long) (5 * toCraft / (Math.log(toCraft) / Math.log(1.15)) + 53.2);
        }
        if (amount > toCraft) amount = toCraft;

        NetworkUtils.doCraft(networkInfo, template, amount);
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
        return 49;
    }

    public void updateGUI(@Nonnull Block block) {
        BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
        if (blockMenu == null) return;

        ItemStack info = new AdvancedCustomItemStack(
                Material.LIME_STAINED_GLASS_PANE,
                "&c已设置数量" + getAmount(block.getLocation()),
                "",
                "&eAE网络中 设定物品不会低于这个数量");
        blockMenu.replaceExistingItem(getInfoSlot(), info);
    }

    public static void reloadConfig() {
        defaultAmount = SlimeAEPlugin.getInstance().getConfig().getLong("me-crafting-trigger.default", 131072L);
    }

    public int[] getBorderSlots() {
        return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
    }

    public int[] getSettingSlots() {
        return new int[] {
            10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42,
            43
        };
    }
}
