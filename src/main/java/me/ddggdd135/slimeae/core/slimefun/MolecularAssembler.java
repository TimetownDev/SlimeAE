package me.ddggdd135.slimeae.core.slimefun;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.Map;
import javax.annotation.Nonnull;
import me.ddggdd135.guguslimefunlib.api.abstracts.TickingBlock;
import me.ddggdd135.guguslimefunlib.api.interfaces.InventoryBlock;
import me.ddggdd135.slimeae.api.abstracts.Card;
import me.ddggdd135.slimeae.api.autocraft.AutoCraftingTask;
import me.ddggdd135.slimeae.api.autocraft.CraftType;
import me.ddggdd135.slimeae.api.autocraft.CraftingRecipe;
import me.ddggdd135.slimeae.api.interfaces.ICardHolder;
import me.ddggdd135.slimeae.api.interfaces.IMEVirtualCraftDevice;
import me.ddggdd135.slimeae.core.NetworkInfo;
import me.ddggdd135.slimeae.core.items.MenuItems;
import me.ddggdd135.slimeae.core.items.SlimefunAEItems;
import me.ddggdd135.slimeae.utils.ItemUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MolecularAssembler extends TickingBlock
        // 如果不是TickingBlock的话 玩家不打开一次方块就没法自动合成 奇怪的bug
        implements IMEVirtualCraftDevice, InventoryBlock, ICardHolder {

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    protected void tick(
            @Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull SlimefunBlockData slimefunBlockData) {}

    public MolecularAssembler(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        createPreset(this, item.getDisplayName());
        addItemHandler(onBlockBreak());
    }

    @Override
    public void onNetworkTick(Block block, NetworkInfo networkInfo) {
        SlimefunBlockData slimefunBlockData = StorageCacheUtils.getBlock(block.getLocation());
        BlockMenu menu = slimefunBlockData.getBlockMenu();
        if (menu == null) return;
        tickCards(block, SlimefunItem.getById(slimefunBlockData.getSfId()), slimefunBlockData);
        if (!menu.hasViewer()) return;

        CraftingRecipe recipe = null;
        for (AutoCraftingTask autoCraftingTask : networkInfo.getAutoCraftingSessions()) {
            if (autoCraftingTask.getCraftingSteps().isEmpty()) continue;
            if (autoCraftingTask.getCraftingSteps().get(0).getRecipe().getCraftType() == getCraftingType()) {
                recipe = autoCraftingTask.getCraftingSteps().get(0).getRecipe();
            }
        }

        for (int slot : getCraftingInputSlots()) {
            menu.replaceExistingItem(slot, MenuItems.EMPTY);
        }

        menu.replaceExistingItem(getOutputSlot(), MenuItems.EMPTY);
        if (recipe == null) return;

        ItemStack[] input = recipe.getInput();
        for (int i = 0; i < input.length; i++) {
            ItemStack itemStack = input[i];
            if (itemStack == null || itemStack.getType().isAir()) continue;
            ItemUtils.setSettingItem(menu.getInventory(), getCraftingInputSlots()[i], itemStack);
        }

        ItemStack[] output = recipe.getOutput();
        if (output.length > 0) {
            ItemStack displayItem = output[0];
            ItemUtils.setSettingItem(menu.getInventory(), getOutputSlot(), displayItem);
        }
    }

    @Override
    public void onNetworkUpdate(Block block, NetworkInfo networkInfo) {}

    @Override
    public int[] getInputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public int[] getCardSlots() {
        return new int[] {45, 46, 47};
    }

    public int getProgressSlot() {
        return 23;
    }

    public int[] getCraftingInputSlots() {
        return new int[] {
            11, 12, 13,
            20, 21, 22,
            29, 30, 31
        };
    }

    public int getOutputSlot() {
        return 24;
    }

    public int[] getBorderSlots() {
        return new int[] {
            0,
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8, // 第一行
            9,
            10,
            14,
            15,
            16,
            17, // 第二行边框和空格
            18,
            19,
            23,
            25,
            26, // 第三行边框和空格
            27,
            28,
            32,
            33,
            34,
            35, // 第四行边框和空格
            36,
            37,
            38,
            39,
            40,
            41,
            42,
            43,
            44, // 第五行
            48,
            49,
            50,
            51,
            52,
            53 // 最后一行
        };
    }

    @Override
    public void init(BlockMenuPreset preset) {
        for (int slot : getBorderSlots()) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int slot : getCraftingInputSlots()) {
            preset.addItem(slot, MenuItems.EMPTY, ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(getOutputSlot(), MenuItems.EMPTY, (player, i, itemStack, clickAction) -> false);
    }

    @Override
    public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        initCardSlots(menu);
    }

    @Nonnull
    private BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
                if (blockMenu == null) return;

                dropCards(blockMenu);
            }
        };
    }

    @Override
    public int getSpeed(@Nonnull Block block) {
        Card accelerationCard = (Card) SlimefunItem.getByItem(SlimefunAEItems.ACCELERATION_CARD);
        SlimefunBlockData data = StorageCacheUtils.getBlock(block.getLocation());

        BlockMenu menu = data.getBlockMenu();
        if (menu == null) return 0;

        Map<Card, Integer> amount = cache.get(block.getLocation());
        if (amount == null) {
            ICardHolder.updateCache(block, this, data);
            amount = cache.get(block.getLocation());
        }

        return amount.getOrDefault(accelerationCard, 0) + 1;
    }

    @Override
    public CraftType getCraftingType() {
        return CraftType.CRAFTING_TABLE;
    }
}
